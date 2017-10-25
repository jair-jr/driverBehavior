package br.com.j2.apm.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import br.com.j2.apm.APMException;
import br.com.j2.apm.Timer;
import br.com.j2.apm.constants.ClassEnum;
import br.com.j2.apm.constants.ClassifierEnum;
import br.com.j2.apm.constants.CoordinateEnum;
import br.com.j2.apm.constants.SensorEnum;
import br.com.j2.apm.file_loader.FileLoader;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.RemoveByName;

public class ClassifierExecutor {	
	private SensorEnum sensor;
	private CoordinateEnum coordinate;
	private List<Integer> executionSeeds = Collections.emptyList();
	private Integer numberOfFrames;
	private int numberOfCrossValidationFolds = 10;
	
	private ClassifierEnum classifier;
	private ClassifierConfig classifierConfig;
	
	public ClassifierExecutor(SensorEnum sensor,
			ClassifierEnum classifier,
			ClassifierConfig classifierConfig) {
		super();
		this.sensor = sensor;
		this.classifier = Objects.requireNonNull(classifier, "classifier can't be null");
		this.classifierConfig = Objects.requireNonNull(classifierConfig, "classifierConfig can't be null");
	}
	
	public ClassifierExecutor(ClassifierEnum classifier,
			ClassifierConfig classifierConfig) {
		this(null, classifier, classifierConfig);
	}
	
	public SensorEnum getSensor() {
		return sensor;
	}
	
	public void setCoordinate(CoordinateEnum coordinate) {
		this.coordinate = coordinate;
	}
	public CoordinateEnum getCoordinate() {
		return coordinate;
	}

	public ClassifierEnum getClassifier() {
		return classifier;
	}
	
	public ClassifierConfig getClassifierConfig() {
		return classifierConfig;
	}

	public List<Integer> getExecutionSeeds() {
		return executionSeeds;
	}
	public void setExecutionSeeds(List<Integer> executionSeeds) {
		this.executionSeeds = Collections.unmodifiableList(new ArrayList<>(executionSeeds));
	}

	public int getNumberOfCrossValidationFolds() {
		return numberOfCrossValidationFolds;
	}
	public void setNumberOfCrossValidationFolds(int numFolds) {
		this.numberOfCrossValidationFolds = numFolds;
	}
	
	public Integer getNumberOfFrames() {
		return numberOfFrames;
	}
	public void setNumberOfFrames(Integer numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}
	
	public String getIdentifier(){
		return getIdentifier(Optional.empty(), Optional.empty());
	}
	
	/**
	 * Retorna <code>true</code> se esse ClassifierExecutor utiliza um dataset de sensor; <code>false</code> caso contrário.
	 * @return <code>true</code> se esse ClassifierExecutor utiliza um dataset de sensor; <code>false</code> caso contrário.
	 */
	public boolean isSensorDataset(){
		return sensor != null;
	}
	
	public String getIdentifier(Optional<Integer> seed, Optional<Integer> crossValidationFold){
		final List<String> parts = new ArrayList<>(6);
		
		if(isSensorDataset()){
			if(numberOfFrames != null){
				parts.add("f" + numberOfFrames);
			}
			
			parts.add(sensor.getAbbreviation());

			if(coordinate != null){
				parts.add(getCoordinate().getAbbreviation());
			}
		}
		else{
			parts.add("ensemble");
		}
		
		parts.add(classifier.getAbbreviation() + classifierConfig.getId());
		
		if(seed.isPresent()){
			parts.add("s" + seed.get());
		}
		
		if(crossValidationFold.isPresent()){
			parts.add("cvFold" + crossValidationFold.get());
		}
		
		return parts.stream().collect(Collectors.joining("_"));
	}
	
	private static List<ClassEnum> getClasses(Instances instances){
		return Collections.unmodifiableList(IntStream.range(0, instances.numClasses())
			.mapToObj(i -> ClassEnum.getById(instances.classAttribute().value(i)))
			.collect(Collectors.toList()));
	}

	public ExecutionResult evaluate(final FileLoader fileLoader){
		if(!isSensorDataset()){
			throw new APMException("FileLoader is only compatible with a ClassifierExecutor that uses a sensor dataset");
		}
		return evaluate(fileLoader.getInstances(classifier.getDatasetType(), sensor, Optional.ofNullable(coordinate), numberOfFrames));
	}
	
	public ExecutionResult evaluate(final Optional<Instances> instances){		
		if(!instances.isPresent()){
			return null;
		} 
		
		final Instances nonNullInstances = instances.get();
		
		final List<ClassEnum> classes = getClasses(nonNullInstances);

		final List<ExecutionResultDetails> executionResultDetails = Collections.unmodifiableList(
				executionSeeds.parallelStream()
					.flatMap(seed -> trainAndAssessClassifierUsingCrossValidation(nonNullInstances, seed, classes))
					.collect(Collectors.toList()));
		final ExecutionResult result = new ExecutionResult(ClassifierExecutor.this, classes, executionResultDetails);
		return result;
	}

	/**
	 * <p>Executa validação cruzada estratificada de <code>numberOfCrossValidationFolds</code> subconjuntos.
	 * <p>Cópias defensivas das instâncias são feitas.</p>
	 * @param instances as amostras para treino e teste.
	 * @param seed a semente aleatória.
	 * @param classes todos valores de rótulo das instâncias. 
	 * @return os <code>numberOfCrossValidationFolds</code> classificadores e {@link Evaluation}'s resultantes 
	 * de treino e teste realizados para a semente aleatória em questão.
	 * @throws APMException se ocorrer um erro no treino/teste do modelo.
	 */
	private Stream<ExecutionResultDetails> trainAndAssessClassifierUsingCrossValidation(
			final Instances instances, 
			final int seed, 
			final List<ClassEnum> classes) throws APMException{
				
		final Random random = new Random(seed);
		
		final Instances instancesCopy = new Instances(instances);
		
		instancesCopy.randomize(random);
		instancesCopy.stratify(numberOfCrossValidationFolds); //necessário quando o atributo classe é nominal (nosso caso)
		
		final Classifier classifierTemplate = classifier.createClassifier(seed, classifierConfig);

		final Instances[] trainsCV = new Instances[numberOfCrossValidationFolds];
		final Instances[] testsCV = new Instances[numberOfCrossValidationFolds];
		for(int i = 0; i < numberOfCrossValidationFolds; i++){
			trainsCV[i] = instancesCopy.trainCV(numberOfCrossValidationFolds, i, random);
			testsCV[i] = instancesCopy.testCV(numberOfCrossValidationFolds, i);
		}
		
		return IntStream.range(0, numberOfCrossValidationFolds)
			.parallel()
			.mapToObj(crossValidationFold -> {
				try{					
					final Instances trainCV = trainsCV[crossValidationFold];
					final Instances testCV = testsCV[crossValidationFold];

					final RemoveByName removeByNameFilter = new RemoveByName();
					removeByNameFilter.setExpression(FileLoader.INSTANCE_ID_ATTRIBUTE_NAME);

					final FilteredClassifier filteredClassifier = new FilteredClassifier();
					filteredClassifier.setFilter(removeByNameFilter);
					filteredClassifier.setClassifier(AbstractClassifier.makeCopy(classifierTemplate));
					filteredClassifier.buildClassifier(trainCV);
					
					/*
					 * Nota: o resultado de passar trainInstancesCopy, trainCV ou testInstances (chamando useNoPriors) 
					 * ao construtor Evaluation() é o mesmo, exceto pelas métricas "relativeAbsoluteError" e "rootMeanSquaredError"
					 * que ficam faltantes (missing) quando usamos useNoPriors
					 */				

					final Evaluation evaluation = new Evaluation(new DataSource(trainCV).getStructure(trainCV.classIndex()));					
					
					final Timer timer = new Timer();
					final DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
					
					final Attribute instanceIdAttribute = testCV.attribute(FileLoader.INSTANCE_ID_ATTRIBUTE_NAME);
					final Attribute classAttribute = testCV.classAttribute();
					
					final Predictions predictions = new Predictions(getIdentifier());
					
					final String executionId = getIdentifier(Optional.of(seed), Optional.of(crossValidationFold));
					
					final int numberOfTestInstances = testCV.numInstances();
					for(int i = 0; i < numberOfTestInstances; i++){
						final Instance testInstance = testCV.get(i);
						timer.start();
						final double prediction = evaluation.evaluateModelOnceAndRecordPrediction(filteredClassifier, testInstance);
						timer.stop();
						descriptiveStatistics.addValue(timer.getElapsedTimeNanos());

						predictions.addPrediction(new Prediction(
								executionId, 
								testInstance.stringValue(instanceIdAttribute), 
								classAttribute.value((int) prediction), 
								testInstance.stringValue(classAttribute)));
					}
					
					final ExecutionResultDetails erd = new ExecutionResultDetails(seed,
							crossValidationFold,
							filteredClassifier, 
							evaluation, 
							classes);
					erd.setInstanceClassificationTimeInNanosAverage(descriptiveStatistics.getMean());
					erd.setInstanceClassificationTimeInNanosStandardDeviation(descriptiveStatistics.getStandardDeviation());
					
					erd.setPredictions(predictions);
					
					return erd;
				}
				catch(Exception e){
					throw new APMException("Error while evaluating classifier. seed: " + seed + "; crossValidationFold: " + crossValidationFold + "; classifier: "+ this, e);
				}
			});
	}
	
	@Override
	public String toString() {
		return "ClassifierExecutor [sensor=" + sensor + ", coordinate=" + coordinate + ", executionSeeds="
				+ executionSeeds + ", numberOfFrames=" + numberOfFrames + ", numberOfFolds=" + numberOfCrossValidationFolds
				+ ", classifier=" + classifier + ", classifierConfig=" + classifierConfig + "]";
	}
}