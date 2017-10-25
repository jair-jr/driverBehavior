package br.com.j2.apm.classifier;

import static br.com.j2.apm.constants.ClassifierEnum.*;
import static br.com.j2.apm.constants.CoordinateEnum.*;
import static br.com.j2.apm.constants.SensorEnum.*;
import static br.com.j2.apm.util.TestUtil.*;
import static org.testng.Assert.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import br.com.j2.apm.constants.ClassEnum;
import br.com.j2.apm.constants.ClassifierEnum;
import br.com.j2.apm.constants.CoordinateEnum;
import br.com.j2.apm.constants.DatasetTypeEnum;
import br.com.j2.apm.constants.SensorEnum;
import br.com.j2.apm.file_loader.FileLoader;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;

public class ClassifierExecutorTest {
	private FileLoader fileLoader;
	
	private Map<ClassifierEnum, ClassifierConfig> classifierConfigs;
	
	@BeforeClass
	public void init(){
		fileLoader = new FileLoader(Paths.get("src/test/resources/instances"), Arrays.asList(4, 5, 6, 7, 8));
		
		classifierConfigs = Stream.of(ClassifierEnum.values()).
		collect(Collectors.toMap(Function.identity(), classifier -> classifier.getClassifierConfigs().get(0)));		
	}
	
	@DataProvider
	public Object[][] evaluateDP(){
		return new Object[][]{
			{
				ACCELEROMETER_EARTH, //sensor
				null, //coordinate
				IBK_DTW, //classifier
				5, //numFolds
				Arrays.asList(1) //seeds
			},

			{
				ACCELEROMETER_EARTH, //sensor
				Y, //coordinate
				IBK_DTW, //classifier
				5, //numFolds
				Arrays.asList(1) //seeds
			},

			{
				ACCELEROMETER, //sensor
				X, //coordinate
				MULTILAYER_PERCEPTRON, //classifier
				5, //numFolds
				Arrays.asList(1) //seeds
			},
			
			{
				ACCELEROMETER, //sensor
				X, //coordinate
				MULTILAYER_PERCEPTRON, //classifier
				5, //numFolds
				Arrays.asList(1, 10, 15) //seeds
			}, 
			
			{
				ACCELEROMETER, //sensor
				null, //coordinate
				MULTILAYER_PERCEPTRON, //classifier
				5, //numFolds
				Arrays.asList(20) //seeds
			},
			
			{
				MAGNETIC_FIELD, //sensor
				X, //coordinate
				RANDOM_FOREST, //classifier
				5, //numFolds
				Arrays.asList(15) //seeds,
			},

			{
				MAGNETIC_FIELD, //sensor
				X, //coordinate
				RANDOM_FOREST, //classifier
				5, //numFolds
				Arrays.asList(15, 30) //seeds,
			},

			{
				GRAVITY, //sensor
				X, //coordinate
				SVM, //classifier
				8, //numFolds
				Arrays.asList(20, 25, 30) //seeds,
			},

		};
	}
	
	
	@Test(dataProvider = "evaluateDP")
	public void evaluate(SensorEnum sensor, 
			CoordinateEnum coordinate,
			ClassifierEnum classifier,
			int numFolds,
			List<Integer> seeds
			){

		final ClassifierExecutor ce = new ClassifierExecutor(sensor, classifier, classifierConfigs.get(classifier));
		ce.setCoordinate(coordinate);
		ce.setExecutionSeeds(seeds);
		ce.setNumberOfCrossValidationFolds(numFolds);
		ce.setNumberOfFrames(classifier.getDatasetType() == DatasetTypeEnum.TIME_SERIES ? 4 : 7);
		
		final ExecutionResult executionResult = ce.evaluate(fileLoader);
		Assert.assertNotNull(executionResult);
		
		final int numberOfResultDetails = seeds.size() * numFolds;
		final List<ExecutionResultDetails> details = executionResult.getDetails();
		assertEquals(details.size(), numberOfResultDetails);
		
		//check that every ExecutionResultDetails contains all classes
		final Map<ClassEnum, Long> resultClasses = details.stream()
			.flatMap(ds -> ds.getClasses().stream())
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		assertContainsInAnyOrder(resultClasses.keySet(), ClassEnum.values());
		final Set<Long> values = new HashSet<>(resultClasses.values());
		assertEquals(values.size(), 1);
		assertEquals(values.iterator().next().intValue(), numberOfResultDetails);
		
		final Map<Integer, Integer> resultDetailsSumBySeed = new HashMap<>();
		
		for(int i = 0; i < details.size(); i++){
			final ExecutionResultDetails eds = details.get(i);
			
			final int currentDetailsSum = resultDetailsSumBySeed.get(eds.getSeed()) != null ? resultDetailsSumBySeed.get(eds.getSeed()) : 0;
			
			resultDetailsSumBySeed.put(eds.getSeed(), currentDetailsSum + 1);
			
			for(final ClassEnum clazz: ClassEnum.values()){
				final ClassifierMetrics cm = eds.getClassifierMetricsByClass(clazz);
				assertNotNull(cm, clazz.getId());
				assertFalse(Utils.isMissingValue(cm.getAreaUnderROC()), clazz.getId());
				assertNotEquals(cm.getAreaUnderROC(), 0, 1e-3, 
						clazz.getId() + " roc score: " + cm.getAreaUnderROC());
			}
		}
		
		//there should be 'numFolds' ExecutionResultDetails's for every seed
		for(Map.Entry<Integer, Integer> e: resultDetailsSumBySeed.entrySet()){
			assertEquals(e.getValue().intValue(), numFolds, "(seed: " + e.getKey() + ")");
		}
		
		//check predictions
		final Map<Integer, Predictions> predictionsBySeed = executionResult.unionPredictionsBySeed();
		assertEquals(predictionsBySeed.keySet(), new HashSet<>(seeds));
		
		final Instances instances = fileLoader.getInstances(ce.getClassifier().getDatasetType(), ce.getSensor(), Optional.ofNullable(ce.getCoordinate()), ce.getNumberOfFrames()).get();
		final Set<String> expectedInstanceIdValues = getAttributeValuesAsString(instances, FileLoader.INSTANCE_ID_ATTRIBUTE_NAME);
		final Set<String> expectedClasses = getAttributeValuesAsString(instances, FileLoader.SENSOR_CLASS_ATTRIBUTE_NAME);
		
		final String classifierIdentifier = ce.getIdentifier();
		
		for(int seed : predictionsBySeed.keySet()){
			final Predictions predictions = predictionsBySeed.get(seed);
			assertTrue(predictions.size() > 0);
			
			assertEquals(predictions.getClassifierIdentifier(), classifierIdentifier);

			final Set<String> expectedExecutionIds = IntStream.range(0, numFolds)
				.mapToObj(fold -> ce.getIdentifier(Optional.of(seed), Optional.of(fold)))
				.collect(Collectors.toSet());
			
			final Set<String> actualExecutionIds = predictions.getPredictions().stream()
				.map(Prediction::getExecutionId)
				.collect(Collectors.toSet());
			
			assertEquals(actualExecutionIds, expectedExecutionIds);
			
			final Set<String> actualInstanceIdValues = predictions.getPredictions().stream()
					.map(Prediction::getInstanceId)
					.collect(Collectors.toSet());				
			assertEquals(actualInstanceIdValues, expectedInstanceIdValues);
			
			final Set<String> predictedClasses = predictions.getPredictions().stream()
					.map(Prediction::getPredictedClass)
					.collect(Collectors.toSet());				
			assertEquals(predictedClasses, expectedClasses);

			final Set<String> actualClasses = predictions.getPredictions().stream()
					.map(Prediction::getActualClass)
					.collect(Collectors.toSet());				
			assertEquals(actualClasses, expectedClasses);
		}
	}
	
	private Set<String> getAttributeValuesAsString(Instances instances, String attributeName){
		final Attribute instanceIdAttribute = instances.attribute(attributeName);
		assertNotNull(instanceIdAttribute);
		
		final Set<String> values = new HashSet<>();
		for(int i = 0; i < instances.numInstances(); i++){
			final String instanceId = instances.get(i).stringValue(instanceIdAttribute);
			assertNotNull(instanceId);
			
			values.add(instanceId);
		}
		
		return values;
	}
	
	@Test
	public void evaluateAllClassifiers(){
		for(final ClassifierEnum classifier : ClassifierEnum.values()){
			final List<ClassifierConfig> classifierConfigs = classifier.getClassifierConfigs();
			for(final ClassifierConfig classifierConfig : classifierConfigs){
				final SensorEnum sensor = classifier.getDatasetType() == DatasetTypeEnum.TIME_SERIES ? ACCELEROMETER_EARTH : ACCELEROMETER;
				try{
					final ClassifierExecutor ce = new ClassifierExecutor(sensor, classifier, classifierConfig);
					ce.setCoordinate(null);
					ce.setExecutionSeeds(Collections.singletonList(50));
					ce.setNumberOfCrossValidationFolds(5);
					ce.setNumberOfFrames(4);
					
					final ExecutionResult result = ce.evaluate(fileLoader);
					Assert.assertNotNull(result, classifier.toString());
				}
				catch(RuntimeException e){
					Assert.fail("classifier: " + classifier + "; classifierConfig: " + classifierConfig, e);
				}
			}
		}
	}
	
	@DataProvider
	public Object[][] getIdentifierDP(){
		final ClassifierExecutor ce1 = new ClassifierExecutor(ACCELEROMETER_EARTH, MULTILAYER_PERCEPTRON, MULTILAYER_PERCEPTRON.getClassifierConfigs().get(0));
		ce1.setCoordinate(Y);
		ce1.setNumberOfFrames(5);

		final ClassifierExecutor ce2 = new ClassifierExecutor(GRAVITY, RANDOM_FOREST, RANDOM_FOREST.getClassifierConfigs().get(1));
		ce2.setNumberOfFrames(9);

		final ClassifierExecutor ce3 = new ClassifierExecutor(ACCELEROMETER_EARTH, IBK_DTW, IBK_DTW.getClassifierConfigs().get(0));
		ce3.setNumberOfFrames(5);
		
		final ClassifierExecutor ce4 = new ClassifierExecutor(GYROSCOPE_EARTH, IBK_DTW, IBK_DTW.getClassifierConfigs().get(1));
		ce4.setNumberOfFrames(6);
		ce4.setCoordinate(Z);

		final ClassifierExecutor ce5 = new ClassifierExecutor(IBK_DTW, IBK_DTW.getClassifierConfigs().get(0));
		
		final ClassifierExecutor ce6 = new ClassifierExecutor(MULTILAYER_PERCEPTRON, MULTILAYER_PERCEPTRON.getClassifierConfigs().get(2));
		ce6.setCoordinate(X);

		return new Object[][]{
			{ce1, 11, null, "f5_AcelE_y_MLP1_s11"},
			{ce1, 11, 4, "f5_AcelE_y_MLP1_s11_cvFold4"},
			{ce1, null, null, "f5_AcelE_y_MLP1"},
			{ce1, null, 2, "f5_AcelE_y_MLP1_cvFold2"},
			{ce2, 20, null, "f9_Grav_RF2_s20"},
			{ce2, 20, 6, "f9_Grav_RF2_s20_cvFold6"},
			{ce2, null, null, "f9_Grav_RF2"},
			{ce2, null, 8, "f9_Grav_RF2_cvFold8"},
			
			{ce3, null, null, "f5_AcelE_KNN_DTW1"},
			{ce3, null, 12, "f5_AcelE_KNN_DTW1_cvFold12"},
			{ce3, 12, null, "f5_AcelE_KNN_DTW1_s12"},
			{ce3, 12, 3, "f5_AcelE_KNN_DTW1_s12_cvFold3"},

			{ce4, null, null, "f6_GirE_z_KNN_DTW2"},
			{ce4, null, 12, "f6_GirE_z_KNN_DTW2_cvFold12"},
			{ce4, 12, null, "f6_GirE_z_KNN_DTW2_s12"},
			{ce4, 12, 3, "f6_GirE_z_KNN_DTW2_s12_cvFold3"},
			
			{ce5, null, null, "ensemble_KNN_DTW1"},
			{ce5, null, 12, "ensemble_KNN_DTW1_cvFold12"},
			{ce5, 12, null, "ensemble_KNN_DTW1_s12"},
			{ce5, 12, 3, "ensemble_KNN_DTW1_s12_cvFold3"},

			{ce6, null, null, "ensemble_MLP3"},
			{ce6, null, 12, "ensemble_MLP3_cvFold12"},
			{ce6, 12, null, "ensemble_MLP3_s12"},
			{ce6, 12, 3, "ensemble_MLP3_s12_cvFold3"},
		};
	}
	
	@Test(dataProvider = "getIdentifierDP")
	public void getIdentifier(ClassifierExecutor ce, Integer seed, Integer crossValidationFold, String expectedIdentifier){
		Assert.assertEquals(ce.getIdentifier(Optional.ofNullable(seed), Optional.ofNullable(crossValidationFold)), expectedIdentifier);
	}
	
}