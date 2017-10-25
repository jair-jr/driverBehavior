package br.com.j2.apm.classifier;

import static br.com.j2.apm.constants.ClassifierEnum.BAYES_NET;
import static br.com.j2.apm.constants.ClassifierEnum.MULTILAYER_PERCEPTRON;
import static br.com.j2.apm.constants.ClassifierEnum.RANDOM_FOREST;
import static br.com.j2.apm.constants.ClassifierEnum.SVM;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import br.com.j2.apm.constants.ClassifierEnum;
import br.com.j2.apm.file_loader.FileLoader;
import weka.core.Instances;

public class EnsembleClassifierTest {
	
	@DataProvider
	public Object[][] evaluateAllClassifiersDP(){
		return new Object[][]{
			{"src/test/resources/ensemble/ensembleWithoutNAs.arff"},
			{"src/test/resources/ensemble/ensembleWithNAs.arff"},
		};
	}
	
	@Test(dataProvider = "evaluateAllClassifiersDP")
	public void evaluateAllClassifiers(String instancesPath) throws InterruptedException, ExecutionException{
		final ClassifierEnum[] classifiers = new ClassifierEnum[] {MULTILAYER_PERCEPTRON, RANDOM_FOREST, SVM, BAYES_NET};
		
		final Instances instances = FileLoader.readEnsembleInstances(Paths.get(instancesPath));
		
		for(final ClassifierEnum classifier : classifiers){
			final List<ClassifierConfig> classifierConfigs = classifier.getClassifierConfigs();
			for(final ClassifierConfig classifierConfig : classifierConfigs){
				try{
					final ClassifierExecutor ce = new ClassifierExecutor(classifier, classifierConfig);
					ce.setExecutionSeeds(Collections.singletonList(50));
					ce.setNumberOfCrossValidationFolds(5);
					
					final ExecutionResult result = ce.evaluate(Optional.of(instances));
					Assert.assertNotNull(result, classifier.toString());
				}
				catch(RuntimeException e){
					Assert.fail("classifier: " + classifier + "; classifierConfig: " + classifierConfig, e);
				}
			}
		}
	}

}
