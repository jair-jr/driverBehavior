package br.com.j2.apm.classifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opencsv.CSVReader;

import br.com.j2.apm.APMException;
import br.com.j2.apm.constants.ClassEnum;
import br.com.j2.apm.constants.ClassifierEnum;
import br.com.j2.apm.constants.SensorEnum;
import mockit.Expectations;
import mockit.Mocked;
import weka.classifiers.Evaluation;
import weka.classifiers.misc.SerializedClassifier;

public class ExecutionResultRepositoryTest {
	private static final Double NaN = Double.NaN;
	
	private static final int NUMBER_OF_CROSS_VALIDATION_FOLDS = 5;
	
	private final Path file = Paths.get("flatExecutionResultsTest.csv");
	private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private List<ExecutionResultRepository> executionResultRepositories = new ArrayList<>();
	
	@Mocked
	private SerializedClassifier mockClassifier;
	
	@Mocked
	private Evaluation mockEvaluation;

	@AfterMethod
	@BeforeMethod
	private void clean() throws IOException{
		Files.deleteIfExists(file);
		executionResultRepositories.forEach(rep -> {
			try {
				FileUtils.deleteDirectory(rep.getPredictionsHomeDir().toFile());
			} 
			catch (IOException e) {
				throw new APMException("Erro ao apagar diretório de teste: " + rep.getPredictionsHomeDir());
			}
		});
		executionResultRepositories.clear();
	}
	
	private ExecutionResultRepository createExecutionResultRepository() throws IOException{
		final ExecutionResultRepository rep = new ExecutionResultRepository(file);
		executionResultRepositories.add(rep);
		return rep;
	}
	
	@DataProvider
	public Object[][] sensorDatasetDP(){
		return new Object[][]{
			{true},
			{false}
		};
	}
	
	@Test(dataProvider = "sensorDatasetDP")
	public void saveClassifiersPredictions(boolean sensorDataset) throws IOException{
		final List<ClassEnum> classes = Arrays.asList(ClassEnum.EVENTO_NAO_AGRESSIVO, ClassEnum.TROCA_FAIXA_DIREITA_AGRESSIVA);
		
		final int numberOfSeeds = 5;
		
		final ExecutionResult er = createExecutionResult(numberOfSeeds, classes, 20, sensorDataset);
		
		Assert.assertEquals(er.getDetails().size(), numberOfSeeds * NUMBER_OF_CROSS_VALIDATION_FOLDS);
		
		final Set<String> expectedFilePaths;
		if(sensorDataset){
			expectedFilePaths = new HashSet<>(Arrays.asList(
					"acelerometroTerra/f25/BN1/s20/f25_AcelE_BN1_s20",
					"acelerometroTerra/f25/BN1/s21/f25_AcelE_BN1_s21",
					"acelerometroTerra/f25/BN1/s22/f25_AcelE_BN1_s22",
					"acelerometroTerra/f25/BN1/s23/f25_AcelE_BN1_s23",
					"acelerometroTerra/f25/BN1/s24/f25_AcelE_BN1_s24"));
		}
		else{
			expectedFilePaths = new HashSet<>(Arrays.asList(
					"MLP1/s20/ensemble_MLP1_s20",
					"MLP1/s21/ensemble_MLP1_s21",
					"MLP1/s22/ensemble_MLP1_s22",
					"MLP1/s23/ensemble_MLP1_s23",
					"MLP1/s24/ensemble_MLP1_s24"));			
		}
		
		try(final ExecutionResultRepository repository = createExecutionResultRepository()){
			repository.addExecutionResult(er);
			
			final Path classifiersDir = repository.getPredictionsHomeDir();
			Assert.assertTrue(Files.exists(classifiersDir));
						
			final Set<Path> existingFiles = expectedFilePaths.stream().map(classifierId -> classifierId + ".predictions.csv")
				.map(fileString -> classifiersDir.resolve(fileString))
				.filter(path -> Files.exists(path))
				.collect(Collectors.toSet());
			
			Assert.assertEquals(existingFiles.size(), numberOfSeeds, existingFiles.toString());
			
			for(Path existingFile : existingFiles){
				final List<String> predictionsLines = Files.readAllLines(existingFile);
				Assert.assertEquals(predictionsLines.size(), NUMBER_OF_CROSS_VALIDATION_FOLDS + 1); //+ 1 because of the header line
			}
		}
	}
	
	@Test(dataProvider = "sensorDatasetDP")
	public void findSeeds(boolean sensorDataset) throws IOException{
		final List<ClassEnum> classes = Arrays.asList(ClassEnum.EVENTO_NAO_AGRESSIVO, ClassEnum.TROCA_FAIXA_DIREITA_AGRESSIVA);
		
		final ExecutionResult er = createExecutionResult(2, classes, 20, sensorDataset);
				
		final ExecutionResult er2 = createExecutionResult(1, classes, 30, sensorDataset);
		
		try(final ExecutionResultRepository repository = createExecutionResultRepository()){
			repository.addExecutionResult(er);
			repository.addExecutionResult(er2);
		}
		
		try(final ExecutionResultRepository repository = createExecutionResultRepository()){
			Assert.assertEquals(repository.getNumberOfIdentifiers(), 2);
			
			Collection<Integer> seeds = repository.findSeedsByIdentifier(er.getClassifierExecutor().getIdentifier());
			Assert.assertEquals(seeds.size(), 2);
			Assert.assertTrue(seeds.contains(er.getDetails().get(0).getSeed()));
			Assert.assertTrue(seeds.contains(er.getDetails().get(1).getSeed()));
			
			seeds = repository.findSeedsByIdentifier(er2.getClassifierExecutor().getIdentifier());
			Assert.assertEquals(seeds.size(), 1);
			Assert.assertTrue(seeds.contains(er2.getDetails().get(0).getSeed()));
		}
	}
	
	@Test(dataProvider = "sensorDatasetDP")
	public void executionResultToCSVLine(boolean sensorDataset) throws ParseException, IOException{
		final List<ClassEnum> classes = Arrays.asList(ClassEnum.ACELERACAO_AGRESSIVA, ClassEnum.CURVA_ESQUERDA_AGRESSIVA);
		final String expectedClasses = ClassEnum.ACELERACAO_AGRESSIVA + "," + ClassEnum.CURVA_ESQUERDA_AGRESSIVA;
		
		final ExecutionResult er = createExecutionResult(1, classes, 10, sensorDataset);
		try(final ExecutionResultRepository repository = createExecutionResultRepository()){
			repository.addExecutionResult(er);
		}
		
		final String expectedConfusionMatrix = "0.0,1.0,2.0,3.0";
		
		try(final ExecutionResultRepository repository = createExecutionResultRepository();
				final CSVReader reader = repository.createCSVReader()){
			
			Assert.assertEquals(er.getDetails().size(), NUMBER_OF_CROSS_VALIDATION_FOLDS);

			for(int crossValidationFold = 0; crossValidationFold < NUMBER_OF_CROSS_VALIDATION_FOLDS; crossValidationFold++){
				for(final ClassEnum clazz: classes){
					assertLineEqualsExecutionResult(reader.readNext(), crossValidationFold, er, clazz, expectedClasses, expectedConfusionMatrix);	
				}
			}
		}
	}
	
	private void assertLineEqualsExecutionResult(final String[] line, final int crossValidationFold, final ExecutionResult er, final ClassEnum currentClass, 
			final String expectedClasses, final String expectedConfusionMatrix) {		
		final ClassifierExecutor ce = er.getClassifierExecutor();
		final ExecutionResultDetails details = er.getDetails().get(crossValidationFold);
		
		final ClassifierMetrics weightedClassifierMetrics = details.getWeightedClassifierMetrics();
		final ClassifierMetrics classClassifierMetrics = details.getClassifierMetricsByClass(currentClass);
		final SummaryClassifierMetrics summaryClassifierMetrics = details.getSummaryClassifierMetrics();
		
		int i = 0;
		
		final String assertFailMessage = "(crossValidationFold: " + crossValidationFold + ")";
		
		Assert.assertEquals(line[i++], nullSafeToString(ce.getIdentifier(Optional.of(details.getSeed()), Optional.of(details.getCrossValidationFold()))), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(ce.getIdentifier()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(details.getSeed()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(details.getCrossValidationFold()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeDateToString(details.getTimestamp()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(ce.getSensor()), assertFailMessage);
		Assert.assertEquals(line[i++], ce.getSensor() != null ? String.valueOf(ce.getSensor().isEarth()) : "", assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(ce.getCoordinate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(ce.getClassifier()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(ce.getClassifierConfig().getId()), assertFailMessage);
		Assert.assertEquals(line[i++], ce.getClassifier().getClazz().getName() + " " + ce.getClassifierConfig().getConfig(details.getSeed()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(ce.getNumberOfCrossValidationFolds()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(ce.getNumberOfFrames()), assertFailMessage);

		Assert.assertEquals(line[i++], expectedClasses, assertFailMessage);
		
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getNumberOfInstances()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getCorrectInstancesRate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getIncorrectInstancesRate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getUnclassifiedInstancesRate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getKappaStatistic()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getMeanAbsoluteError()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getRootMeanSquaredError()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getRelativeAbsoluteError()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(summaryClassifierMetrics.getRootRelativeSquaredError()), assertFailMessage);
		Assert.assertEquals(line[i++], expectedConfusionMatrix, assertFailMessage);

		Assert.assertEquals(line[i++], nullSafeToString(weightedClassifierMetrics.getAreaUnderROC()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(weightedClassifierMetrics.getAreaUnderPRC()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(weightedClassifierMetrics.getMatthewsCorrelationCoefficient()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(weightedClassifierMetrics.getFMeasure()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(weightedClassifierMetrics.getTruePositiveRate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(weightedClassifierMetrics.getFalsePositiveRate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(weightedClassifierMetrics.getPrecision()), assertFailMessage);
		
		Assert.assertEquals(line[i++], nullSafeToString(currentClass), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(details.getInstanceClassificationTimeInNanosAverage()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(details.getInstanceClassificationTimeInNanosStandardDeviation()), assertFailMessage);
		
		Assert.assertEquals(line[i++], nullSafeToString(classClassifierMetrics.getAreaUnderROC()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(classClassifierMetrics.getAreaUnderPRC()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(classClassifierMetrics.getMatthewsCorrelationCoefficient()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(classClassifierMetrics.getFMeasure()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(classClassifierMetrics.getTruePositiveRate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(classClassifierMetrics.getFalsePositiveRate()), assertFailMessage);
		Assert.assertEquals(line[i++], nullSafeToString(classClassifierMetrics.getPrecision()), assertFailMessage);
		
	}

	private String nullSafeToString(Object o){
		if(o == null){
			return "";
		}
		if(NaN.equals(o)){
			return "";
		}
		return o.toString();
	}
	
	private String nullSafeDateToString(Date date){
		return date != null ? dateFormat.format(date) : "";
	}
	
	private ClassifierExecutor createClassifierExecutor(int sequence, boolean sensorDataset){
		final SensorEnum sensor;
		final Integer numberOfFrames;
		final ClassifierEnum classifier;
		
		if(sensorDataset){
			sensor = SensorEnum.ACCELEROMETER_EARTH;
			numberOfFrames = sequence;
			classifier = ClassifierEnum.BAYES_NET;
		}
		else {
			sensor = null;
			numberOfFrames = null;
			
			/* não é possível fazer numberOfFrames = sequence (numberOfFrames é nulo quando ClassifierExecutor é ensemble).
			 * Portanto, é preciso que o classificador seja alterado em função de sequence. Assim teremos identificadores diferentes
			 * em função de sequence.
			 */
			classifier = ClassifierEnum.values()[sequence % ClassifierEnum.values().length];
		}
		
		final ClassifierExecutor ce = new ClassifierExecutor(sensor, classifier, classifier.getClassifierConfigs().get(0));
		ce.setNumberOfFrames(numberOfFrames);
		ce.setNumberOfCrossValidationFolds(NUMBER_OF_CROSS_VALIDATION_FOLDS);
		return ce;
	}
	
	private void addClassifierMetricsExpectation(final Evaluation evaluation, Integer classIndex, int sequence){
		final double delta = 0.1;
		final double initValue = sequence / 10D; 
		if(classIndex != null){
			new Expectations(){{
				double value = initValue;
				
				evaluation.areaUnderROC(classIndex); result = (value += delta);
				evaluation.areaUnderPRC(classIndex); result = (value += delta);
				evaluation.matthewsCorrelationCoefficient(classIndex); result = NaN;
				evaluation.fMeasure(classIndex); result = (value += delta);
				evaluation.truePositiveRate(classIndex); result = (value += delta);
				evaluation.falsePositiveRate(classIndex); result = (value += delta);
				evaluation.precision(classIndex); result = (value += delta);
			}};
		}
		else{
			new Expectations(){{
				double value = initValue;
		
				evaluation.weightedAreaUnderROC(); result = (value += delta);
				evaluation.weightedAreaUnderPRC(); result = (value += delta);
				evaluation.weightedMatthewsCorrelation(); result = NaN;
				evaluation.weightedFMeasure(); result = (value += delta);
				evaluation.weightedTruePositiveRate(); result = (value += delta);
				evaluation.weightedFalsePositiveRate(); result = (value += delta);
				evaluation.weightedPrecision(); result = (value += delta);				
			}};
		}
	}
	
	private void addSummaryClassifierMetricsExpectations(final Evaluation evaluation, int sequence){
		final double delta = 0.2;
		
		final int numberOfClasses = 2;
		int k = 0;
		final double[][] confusionMatrix = new double[numberOfClasses][numberOfClasses];
		for(int i = 0; i < numberOfClasses; i++){
			for(int j = 0; j < numberOfClasses; j++){
				confusionMatrix[i][j] = k++;
			}
		}
		
		final double initValue = sequence / 10D;

		try{
			new Expectations(){{
				double value = initValue;
				
				evaluation.numInstances(); result = Math.round(delta * 3);
				evaluation.pctCorrect(); result = (value += delta);
				evaluation.pctIncorrect(); result = (value += delta);
				evaluation.pctUnclassified(); result = (value += delta);
				evaluation.kappa(); result = (value += delta);
				evaluation.meanAbsoluteError(); result = (value += delta);
				evaluation.rootMeanSquaredError(); result = NaN;
				evaluation.relativeAbsoluteError(); result = (value += delta);
				evaluation.rootRelativeSquaredError(); result = (value += delta);				
				evaluation.confusionMatrix(); result = confusionMatrix;
			}};
		}
		catch(Exception e){
			throw new APMException(e);
		}
	}
	
	private ExecutionResult createExecutionResult(final int numberOfSeeds, 
			final List<ClassEnum> classes, 
			int sequence, boolean sensorDataset) {
		final Evaluation ev;
		try {
			ev = new Evaluation(null);
		}
		catch (Exception e) {
			throw new APMException(e);
		}
		
		final List<ExecutionResultDetails> allDetails = new ArrayList<>();
		
		for(int seedIndex = 0; seedIndex < numberOfSeeds; seedIndex++){
			for(int crossValidationFoldIndex = 0; crossValidationFoldIndex < NUMBER_OF_CROSS_VALIDATION_FOLDS; crossValidationFoldIndex++){
				addClassifierMetricsExpectation(ev, null, sequence + 5 + seedIndex);
				
				addSummaryClassifierMetricsExpectations(ev, sequence + 7 + seedIndex);
				
				for(int classIndex = 0; classIndex < classes.size(); classIndex++){
					addClassifierMetricsExpectation(ev, classIndex, sequence + 6 + seedIndex);
				}
				
				final Predictions predictions = new Predictions("classifierId" + seedIndex);
				predictions.addPrediction(new Prediction("seed" + seedIndex, "seq" + sequence, "classe1", "classe2"));
				
				final ExecutionResultDetails executionResultDetails = new ExecutionResultDetails(sequence + seedIndex, crossValidationFoldIndex, mockClassifier, ev, classes);
				executionResultDetails.setPredictions(predictions);
				allDetails.add(executionResultDetails);
			}
		}
		
		final ExecutionResult er = new ExecutionResult(createClassifierExecutor(sequence + numberOfSeeds, sensorDataset), 
				classes, 
				allDetails);
		
		return er;
	}
	
}
