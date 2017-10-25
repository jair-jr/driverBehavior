package br.com.j2.apm.classifier;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.j2.apm.APMException;
import br.com.j2.apm.constants.ClassEnum;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

public class ExecutionResultDetails {

	private int seed;
	private int crossValidationFold;
	private Classifier classifier;
	private Evaluation evaluation;
	private Map<ClassEnum, ClassifierMetrics> detailsByClass;
	
	private double instanceClassificationTimeInNanosAverage;
	private double instanceClassificationTimeInNanosStandardDeviation;
	
	private ClassifierMetrics weightedClassifierMetrics;
	
	private SummaryClassifierMetrics summaryClassifierMetrics;
	
	private Date timestamp;
	
	private Predictions predictions;

	ExecutionResultDetails(final int seed,
			final int crossValidationFold,
			final Classifier classifier, 
			final Evaluation evaluation, 
			final List<ClassEnum> classes) {
		super();
		this.seed = seed;
		this.crossValidationFold = crossValidationFold;
		this.classifier = classifier;
		this.evaluation = evaluation;
		timestamp = new Date();
		weightedClassifierMetrics = new ClassifierMetrics(evaluation);
		summaryClassifierMetrics = new SummaryClassifierMetrics(evaluation);
		
		detailsByClass = new LinkedHashMap<>();
		for(int i = 0; i < classes.size(); i++){
			final ClassEnum clazz = classes.get(i);
			detailsByClass.put(clazz, new ClassifierMetrics(clazz, i, evaluation));
		}
	}
	
	public Predictions getPredictions() {
		return predictions;
	}
	public void setPredictions(Predictions predictions) {
		this.predictions = predictions;
	}

	public SummaryClassifierMetrics getSummaryClassifierMetrics() {
		return summaryClassifierMetrics;
	}
	
	public int getCrossValidationFold() {
		return crossValidationFold;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}
	
	public ClassifierMetrics getWeightedClassifierMetrics() {
		return weightedClassifierMetrics;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public ClassifierMetrics getClassifierMetricsByClass(ClassEnum ce){
		final ClassifierMetrics cm = detailsByClass.get(ce);
		if(cm == null){
			throw new APMException("Class '" + ce + "' is not in instances. ClassifierMetrics are unavailable.");
		}
		return cm;
	}
	
	public Set<ClassEnum> getClasses(){
		return detailsByClass.keySet();
	}
	
	public Collection<ClassifierMetrics> getDetails(){
		return detailsByClass.values();
	}
	
	public int getSeed() {
		return seed;
	}

	public double getInstanceClassificationTimeInNanosAverage() {
		return instanceClassificationTimeInNanosAverage;
	}
	void setInstanceClassificationTimeInNanosAverage(double testTimeNanos) {
		this.instanceClassificationTimeInNanosAverage = testTimeNanos;
	}

	public double getInstanceClassificationTimeInNanosStandardDeviation() {
		return instanceClassificationTimeInNanosStandardDeviation;
	}

	public void setInstanceClassificationTimeInNanosStandardDeviation(
			double instanceClassificationTimeNanosStandardDeviation) {
		this.instanceClassificationTimeInNanosStandardDeviation = instanceClassificationTimeNanosStandardDeviation;
	}
	
	
		
}