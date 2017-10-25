package br.com.j2.apm;

import static br.com.j2.apm.constants.ClassifierEnum.*;
import static br.com.j2.apm.constants.SensorEnum.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.j2.apm.classifier.BatchExecutor;
import br.com.j2.apm.constants.ClassifierEnum;

public class Main {	
	public static void main(String[] args) {
		if(args.length < 2){
			System.err.println("Missing parameters. Use:\n\tMain (ensemble_arff_dataset or sensor_arff_datasets_dir) (output_dir) <number_of_threads>");
			System.exit(-1);
			return;
		}

		final Path ensembleInstancesFileOrInstancesDir = Paths.get(args[0]);
		final Path executionsOutputDir = Paths.get(args[1]);
		
		final BatchExecutor be = new BatchExecutor();
		if (Files.isDirectory(ensembleInstancesFileOrInstancesDir)) {
			be.setInstancesDir(ensembleInstancesFileOrInstancesDir);
		}
		else {
			be.setEnsembleInstancesFile(ensembleInstancesFileOrInstancesDir);
		}
		
		be.setExecutionsOutputDir(executionsOutputDir);
		be.setNumberOfExecutions(10);
		be.setNumberOfFolds(10);
		
		final List<ClassifierEnum> classifiers = new ArrayList<>(Arrays.asList(MULTILAYER_PERCEPTRON, RANDOM_FOREST, SVM, BAYES_NET, IBK_DTW));

		if(be.isSensorDataset()){
			be.setSensors(Arrays.asList(LINEAR_ACCELERATION_EARTH, ACCELEROMETER_EARTH, GYROSCOPE_EARTH, MAGNETIC_FIELD_EARTH));
			
			be.setNumbersOfFrames(Arrays.asList(4, 5, 6, 7, 8));
		}
		else{
			classifiers.remove(IBK_DTW);
		}
		
		be.setClassifiers(classifiers);
		
		if(args.length > 2){
			be.setNumberOfThreads(Integer.parseInt(args[2]));
		}
		
		be.execute();
		
		System.out.println("Tempo total: " + be.getExecutionTimeInSeconds() + " segundos");		
	}
	
}
