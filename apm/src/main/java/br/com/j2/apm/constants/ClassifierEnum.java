package br.com.j2.apm.constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import br.com.j2.apm.APMException;
import br.com.j2.apm.classifier.ClassifierConfig;
import br.com.j2.apm.classifier.GridParameterSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Utils;

public enum ClassifierEnum {
	
	MULTILAYER_PERCEPTRON(MultilayerPerceptron.class, "MLP", 
			"-L 0.3 -M 0.2 -N 500 -V 0 -S %%SEED%% -E 20 %%HIDDEN_LAYERS%%") {
				@Override
				public List<ClassifierConfig> getClassifierConfigs() {
					final GridParameterSearch gps = new GridParameterSearch();
					gps.addParameter("-H ", "a", "40", "30", "20", "10");
					return getTemplateClassifierConfig().createClassifierConfigs("%%HIDDEN_LAYERS%%", gps);
				}
			},
	/*
	BAGGING_REPTREE(Bagging.class, "BagREPTree", 
			"-P 100 -S %%SEED%% -I 10 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S %%SEED%% -L -1") {
				@Override
				public List<ClassifierConfig> getClassifierConfigs() {
					return Collections.singletonList(getTemplateClassifierConfig());
				}
			},
	*/
	RANDOM_FOREST(RandomForest.class, "RF", 
			"-P 100 %%NUMBER_OF_ITERATIONS_AND_FEATURES%% -num-slots 1 -M 1.0 -V 0.001 -S %%SEED%%") {
				@Override
				public List<ClassifierConfig> getClassifierConfigs() {
					final GridParameterSearch gps = new GridParameterSearch();
					gps.addParameter("-I ", "100", "200");
					gps.addParameter("-K ", "0", "10", "15");
					return getTemplateClassifierConfig().createClassifierConfigs("%%NUMBER_OF_ITERATIONS_AND_FEATURES%%", gps);
				}
			},

	//-W \"1 1 1 1 1 1 1\" serve apenas para impedir a exibição da mensagem "Zero Weights processed. Default weights will be used"
	SVM(LibSVM.class, "SVM",
			"-S 0 -W \"1 1 1 1 1 1 1\" %%KERNEL_FUNCTION_C_AND_GAMMA%% -D 3 -R 0.0 -M 40.0 -E 0.001 -P 0.1 -seed %%SEED%%") {
				
				@Override
				public List<ClassifierConfig> getClassifierConfigs() {
					final List<ClassifierConfig> classifierConfigs = new ArrayList<ClassifierConfig>(); 
					
					final GridParameterSearch gps1 = new GridParameterSearch();
					gps1.addParameter("-K ", "2");
					gps1.addParameter("-C ", twoPower(3, 3.5, 5, 5.5, 7, 7.5));
					gps1.addParameter("-G ", twoPower(-5.5, -5, -3.5, -3, -1.5, -1));
					classifierConfigs.addAll(getTemplateClassifierConfig().createClassifierConfigs("%%KERNEL_FUNCTION_C_AND_GAMMA%%", gps1));
					
					final int nextId = classifierConfigs.stream()
						.mapToInt(cc -> cc.getId())
						.max()
						.getAsInt() + 1;
					
					final GridParameterSearch gps2 = new GridParameterSearch();
					gps2.addParameter("-K ", "1");
					gps2.addParameter("-C ", twoPower(5, 5.5, 7, 7.5, 11, 11.5, 13, 13.5));
					gps2.addParameter("-G ", twoPower(-7.5, -7, -5.5, -5, -3.5, -3));
					classifierConfigs.addAll(getTemplateClassifierConfig().createClassifierConfigs("%%KERNEL_FUNCTION_C_AND_GAMMA%%", gps2, nextId));
					
					return classifierConfigs;
				}
				
				private String[] twoPower(double ... expoents){
					return DoubleStream.of(expoents)
						.mapToObj(expoent -> String.valueOf(Math.pow(2, expoent)))
						.toArray(String[]::new);
				}
			},
	
	BAYES_NET(BayesNet.class, "BN", 
			"-D %%SEARCH%% -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5") {
				
				@Override
				public List<ClassifierConfig> getClassifierConfigs() {
					final GridParameterSearch gps = new GridParameterSearch();
					gps.addParameter("-Q ", "weka.classifiers.bayes.net.search.local.K2 -- -P 3 -S BAYES -R", 
							"weka.classifiers.bayes.net.search.local.RepeatedHillClimber -- -U 10 -A %%SEED%% -P 3 -S BAYES",
							"weka.classifiers.bayes.net.search.local.TAN -- -S BAYES",
							"weka.classifiers.bayes.net.search.ci.CISearchAlgorithm -- -S BAYES");
					return getTemplateClassifierConfig().createClassifierConfigs("%%SEARCH%%", gps);
				}
			},
	
	IBK_DTW(IBk.class, "KNN_DTW", 
			"%%NUMBER_OF_NEIGHBOURS%% -W 0 -A \"weka.core.neighboursearch.LinearNNSearch -A \\\"br.com.j2.apm.classifier.DTWDistanceFunction -R first-last\\\"\""){

		@Override
		public List<ClassifierConfig> getClassifierConfigs() {
			final GridParameterSearch gps = new GridParameterSearch();
			gps.addParameter("-K ", "1", "3", "5");
			return getTemplateClassifierConfig().createClassifierConfigs("%%NUMBER_OF_NEIGHBOURS%%", gps);
		}
		
	}
	;
	
	private Class<? extends Classifier> clazz;
	private String abbreviation;
	private ClassifierConfig templateClassifierConfig;

	private ClassifierEnum(Class<? extends Classifier> clazz, String abbreviation, ClassifierConfig templateClassifierConfig) {
		this.clazz = clazz;
		this.abbreviation = abbreviation;
		this.templateClassifierConfig = templateClassifierConfig;		
	}
	
	private ClassifierEnum(Class<? extends Classifier> clazz, String abbreviation, String configTemplate) {
		this(clazz, abbreviation, new ClassifierConfig(-1, configTemplate));
	}

	public Class<? extends Classifier> getClazz() {
		return clazz;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
	
	public ClassifierConfig getTemplateClassifierConfig(){
		return templateClassifierConfig;
	}

	public Classifier createClassifier(int seed, ClassifierConfig config) {
		try {
			return AbstractClassifier.forName(getClazz().getName(), 
					Utils.splitOptions(config.getConfig(seed)));			
		} catch (Exception e) {
			throw new APMException("Error while creating classifier: " + this + "; seed: " + seed, e);
		}
	}
	
	public abstract List<ClassifierConfig> getClassifierConfigs();
	
	public DatasetTypeEnum getDatasetType() {
		if(this == IBK_DTW){
			return DatasetTypeEnum.TIME_SERIES;
		}
		
		return DatasetTypeEnum.ATTRIBUTE_VECTOR;
	}
}