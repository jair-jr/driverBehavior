package br.com.j2.apm.classifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import br.com.j2.apm.APMException;
import br.com.j2.apm.Timer;
import br.com.j2.apm.constants.ClassifierEnum;
import br.com.j2.apm.constants.CoordinateEnum;
import br.com.j2.apm.constants.SensorEnum;
import br.com.j2.apm.file_loader.FileLoader;
import libsvm.svm;
import weka.core.Instances;

public class BatchExecutor {

	private Path instancesDir;
	private Path ensembleInstancesFile;
	private Path executionsOutputDir;
	
	private Collection<SensorEnum> sensors = Collections.emptyList();
	
	private Collection<ClassifierEnum> classifiers = Collections.emptyList();
	
	private Collection<Integer> numbersOfFrames = Collections.emptyList();
	
	private int numberOfExecutions = 10;
	private int numberOfFolds = 10;
	
	private int numberOfThreads = Runtime.getRuntime().availableProcessors();
	
	private Long executionTimeInSeconds;
	
	public BatchExecutor() {
		super();
	}
	
	public boolean isSensorDataset(){
		return instancesDir != null;
	}
	
	public Path getInstancesDir() {
		return instancesDir;
	}
	public void setInstancesDir(Path instancesDir) {
		this.instancesDir = instancesDir;
	}

	public Path getEnsembleInstancesFile() {
		return ensembleInstancesFile;
	}
	public void setEnsembleInstancesFile(Path ensembleInstances) {
		this.ensembleInstancesFile = ensembleInstances;
	}

	public Path getExecutionsOutputDir() {
		return executionsOutputDir;
	}
	public void setExecutionsOutputDir(Path executionsOutputDir) {
		this.executionsOutputDir = executionsOutputDir;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public int getNumberOfExecutions() {
		return numberOfExecutions;
	}
	public void setNumberOfExecutions(int numExecutions) {
		this.numberOfExecutions = numExecutions;
	}

	public int getNumberOfFolds() {
		return numberOfFolds;
	}
	public void setNumberOfFolds(int numFolds) {
		this.numberOfFolds = numFolds;
	}
	
	public Collection<SensorEnum> getSensors() {
		return sensors;
	}
	public void setSensors(Collection<SensorEnum> sensors) {
		this.sensors = sensors;
	}

	public Collection<ClassifierEnum> getClassifiers() {
		return classifiers;
	}
	public void setClassifiers(Collection<ClassifierEnum> classifiers) {
		this.classifiers = classifiers;
	}
	
	public Collection<Integer> getNumbersOfFrames() {
		return numbersOfFrames;
	}
	public void setNumbersOfFrames(Collection<Integer> numbersOfFrames) {
		this.numbersOfFrames = numbersOfFrames;
	}

	private static Path validateDir(Path dir){
		try{
			if(!Files.exists(dir)){
				Files.createDirectories(dir);
			}
			else if(!Files.isDirectory(dir)){
				throw new APMException("'" + dir + "' não é um diretório.");
			}
			return dir;
		}
		catch(IOException e){
			throw new APMException(e); 
		}
	}
	
	public Long getExecutionTimeInSeconds() {
		return executionTimeInSeconds;
	}
	
	private List<Integer> createSeeds() {
		final List<Integer> seeds = Collections.unmodifiableList(IntStream.rangeClosed(1, numberOfExecutions)
				.boxed()
				.map(seed -> seed * 10)
				.collect(Collectors.toList()));
		return seeds;
	}

	private List<ClassifierExecutor> createClassifierExecutorsForSensorDatasets(final Collection<Integer> numbersOfFrames) {

		final List<Integer> seeds = createSeeds();
		
		final List<ClassifierExecutor> classifierExecutors = new ArrayList<>();
		
		for(final ClassifierEnum classifier : classifiers){
			final List<ClassifierConfig> classifierConfigs = classifier.getClassifierConfigs();
			printClassifierConfigs(classifier, classifierConfigs);
			for(final SensorEnum sensor : sensors){
				for(final ClassifierConfig classifierConfig : classifierConfigs){					
					for(final int numberOfFrames : numbersOfFrames){
						classifierExecutors.addAll(createClassifierExecutorsForAllCoordinates(seeds, classifier, sensor,
								classifierConfig, numberOfFrames));
					}
				}
			}
		}
		
		return classifierExecutors;
	}
	
	private List<ClassifierExecutor> createClassifierExecutorsForEnsembleDataset() {
		final List<Integer> seeds = createSeeds();
		
		final List<ClassifierExecutor> classifierExecutors = new ArrayList<>();
	
		for(final ClassifierEnum classifier : classifiers){
			final List<ClassifierConfig> classifierConfigs = classifier.getClassifierConfigs();
			printClassifierConfigs(classifier, classifierConfigs);
			
			for(final ClassifierConfig classifierConfig : classifierConfigs){
				classifierExecutors.add(createClassifierExecutor(null, null, classifier, classifierConfig, null, seeds));
			}
		}
		
		return classifierExecutors;
	}

	private List<ClassifierExecutor> createClassifierExecutorsForAllCoordinates(final List<Integer> seeds,
			final ClassifierEnum classifier,
			final SensorEnum sensor, final ClassifierConfig classifierConfig, final int numberOfFrames) {
		
		final List<ClassifierExecutor> classifierExecutors = new ArrayList<>();
		classifierExecutors.add(createClassifierExecutor(sensor, null, classifier, classifierConfig, numberOfFrames, seeds));

		for(final CoordinateEnum coordinate : sensor.getCoordinates()){
			classifierExecutors.add(createClassifierExecutor(sensor, coordinate, classifier, classifierConfig, numberOfFrames, seeds));
		}
		
		return classifierExecutors;
	}

	private ClassifierExecutor createClassifierExecutor(SensorEnum sensor, 
			CoordinateEnum coordinate, 
			ClassifierEnum classifier,
			ClassifierConfig classifierConfig,
			Integer numberOfFrames,
			List<Integer> seeds){
		final ClassifierExecutor ce = new ClassifierExecutor(sensor, classifier, classifierConfig);
		ce.setCoordinate(coordinate);
		ce.setExecutionSeeds(seeds);
		ce.setNumberOfCrossValidationFolds(numberOfFolds);
		ce.setNumberOfFrames(numberOfFrames);
		
		return ce;
	}
	
	private void printClassifierConfigs(final ClassifierEnum classifierEnum, final List<ClassifierConfig> classifierConfigs){
		System.out.println("====================");
		System.out.println("Classifier configs for: " + classifierEnum);
		for(final ClassifierConfig cc : classifierConfigs){
			System.out.println("\t" + cc);
		}
	}
	
	private void updateExecutionSeeds(ExecutionResultRepository rep, ClassifierExecutor ce){
		final Collection<Integer> executedSeeds = rep.findSeedsByIdentifier(ce.getIdentifier());
		
		if(executedSeeds.isEmpty()){
			return;
		}
		
		final ArrayList<Integer> seeds = new ArrayList<>(ce.getExecutionSeeds());
		seeds.removeAll(executedSeeds);
		ce.setExecutionSeeds(seeds);
	}

	public void execute(){
		executionTimeInSeconds = null;

		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
		    @Override public void print(String s) {} // Disables svm output
		});
		
		final ForkJoinPool forkJoinPool = new ForkJoinPool(numberOfThreads);
		
		final String resultsFile = isSensorDataset() ? "results.csv" : "results_ensemble.csv";

		try(final ExecutionResultRepository executionResultRepository = new ExecutionResultRepository(validateDir(executionsOutputDir).resolve(resultsFile))) {
			
			final FileLoader loader;
			final Instances ensembleInstances;
			
			if(isSensorDataset()){
				loader = new FileLoader(instancesDir, numbersOfFrames);
				ensembleInstances = null;

				System.out.printf("Diretório contendo os datasets (instâncias): %s\n", 
						instancesDir.normalize().toAbsolutePath().toString());
			}
			else{
				loader = null;
				ensembleInstances = FileLoader.readEnsembleInstances(ensembleInstancesFile);

				System.out.printf("Dataset de ensemble: %s\n", 
						ensembleInstancesFile.normalize().toAbsolutePath().toString());				
			}
			
			System.out.printf("Número de threads: %d\n\n", numberOfThreads);

			final List<ClassifierExecutor> classifierExecutors = isSensorDataset() ? createClassifierExecutorsForSensorDatasets(numbersOfFrames) : createClassifierExecutorsForEnsembleDataset();
			
			sortClassifierExecutors(classifierExecutors);

			final Timer timer = new Timer();
			timer.start();
			
			final AtomicInteger numberOfRunnedClassifierExecutors = new AtomicInteger(0);
			final AtomicInteger numberOfNotRunnedClassifierExecutors = new AtomicInteger(0);
			final AtomicInteger numberOfPreviouslyRunnedClassifierExecutors = new AtomicInteger(0);
			final int numberOfClassifierExecutors = classifierExecutors.size();
			
			forkJoinPool.submit(() ->
				classifierExecutors.parallelStream()
				.map(ce -> {
					updateExecutionSeeds(executionResultRepository, ce);

					final boolean classifierPreviouslyRunnedForAllSeeds = ce.getExecutionSeeds().isEmpty(); 
					
					final int numberOfPreviouslyRunned = classifierPreviouslyRunnedForAllSeeds ? 
							numberOfPreviouslyRunnedClassifierExecutors.incrementAndGet() 
							: numberOfPreviouslyRunnedClassifierExecutors.get(); 

					final ExecutionResult result = classifierPreviouslyRunnedForAllSeeds ? null : isSensorDataset() ? ce.evaluate(loader) : ce.evaluate(Optional.of(ensembleInstances));
					
					final boolean runned = result != null;
					
					final int numberOfRunned = runned ? numberOfRunnedClassifierExecutors.incrementAndGet() : numberOfRunnedClassifierExecutors.get();
					final int numberOfNotRunned = (classifierPreviouslyRunnedForAllSeeds || runned) ? numberOfNotRunnedClassifierExecutors.get() : numberOfNotRunnedClassifierExecutors.incrementAndGet();
					
					if(!runned){
						final String classifierData = String.format("%s->%s->%s\n\t%s\n\tnumber of frames: %d; cross-validation folds: %d; seeds: %s\n",
								ce.getClassifier(),
								ce.getSensor(),
								ce.getCoordinate() != null ? ce.getCoordinate() : "(todas as coordendas)",
								ce.getClassifierConfig(),
								ce.getNumberOfFrames(),
								ce.getNumberOfCrossValidationFolds(),
								ce.getExecutionSeeds());

						if(classifierPreviouslyRunnedForAllSeeds){
							System.out.print("Previously executed: " + classifierData);
						}
						else{
							System.err.print("Not executed: " + classifierData);
						}
					}
					
					System.out.printf("Completion: %f; (#runned: %d) + (#not runned: %d) + (#previously runned: %d) of %d\n", 
							(numberOfRunned + numberOfNotRunned + numberOfPreviouslyRunned) / (double) numberOfClassifierExecutors,
							numberOfRunned,
							numberOfNotRunned,
							numberOfPreviouslyRunned,
							numberOfClassifierExecutors);
					
					return result;
				})
				.filter(result -> result != null)
				.forEach(result -> {					
					try {
						executionResultRepository.addExecutionResult(result);
					} 
					catch (IOException e) {
						throw new APMException(e);
					}
				})
			).get();
			timer.stop();
			
			executionTimeInSeconds = timer.getElapsedTimeSeconds();
		} 
		catch (InterruptedException | ExecutionException | IOException e) {
			throw new APMException(e);
		}
		finally{
			forkJoinPool.shutdown();			
		}
	}

	private void sortClassifierExecutors(List<ClassifierExecutor> classifierExecutors) {
		Collections.shuffle(classifierExecutors);
	}	
}