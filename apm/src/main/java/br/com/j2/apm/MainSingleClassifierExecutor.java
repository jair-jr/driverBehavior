package br.com.j2.apm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import br.com.j2.apm.classifier.ClassifierConfig;
import br.com.j2.apm.classifier.ClassifierExecutor;
import br.com.j2.apm.classifier.ExecutionResult;
import br.com.j2.apm.classifier.ExecutionResultRepository;
import br.com.j2.apm.constants.ClassifierEnum;
import br.com.j2.apm.file_loader.FileLoader;

public class MainSingleClassifierExecutor {

	public static void main(String[] args) throws IOException {
		final ClassifierConfig classifierConfig = ClassifierEnum.MULTILAYER_PERCEPTRON.getClassifierConfigs().stream()
				.filter(cc-> cc.getId() == 3)
				.findFirst()
				.get();
		
		final ClassifierExecutor ce = new ClassifierExecutor(ClassifierEnum.MULTILAYER_PERCEPTRON, classifierConfig);
		ce.setExecutionSeeds(Arrays.asList(20));
		ce.setNumberOfCrossValidationFolds(10);
		
		final Path ensembleResults = Paths.get("ensembleResults");
		
		final ExecutionResult executionResult = ce.evaluate(Optional.of(FileLoader.readEnsembleInstances(ensembleResults.resolve("diverseEnsemble2.arff"))));
		
		final ExecutionResultRepository executionResultRepository = new ExecutionResultRepository(ensembleResults.resolve("ensembleResultsMLP2.csv"));
		executionResultRepository.addExecutionResult(executionResult);
		executionResultRepository.close();
	}
}
