package br.com.j2.apm.classifier;

import java.util.Objects;

import br.com.j2.apm.constants.ClassEnum;
import weka.classifiers.Evaluation;

public class ClassifierMetrics {
	private Integer wekaClassIndex;
	private ClassEnum clazz;

	private Double areaUnderROC;
	private Double areaUnderPRC;
	private Double matthewsCorrelationCoefficient; // (a.k.a phi coefficient)
	private Double fMeasure; // (2 * recall * precision) / (recall + precision)
	private Double truePositiveRate; // correctly classified positives / total positives (a.k.a recall)
	private Double falsePositiveRate; // incorrectly classified negatives / total negatives
	private Double precision; // correctly classified positives / total predicted as positive
	
	ClassifierMetrics(Evaluation evaluation){
		Objects.requireNonNull(evaluation, "evaluation cannot be null");
		
		areaUnderROC = evaluation.weightedAreaUnderROC();
		areaUnderPRC = evaluation.weightedAreaUnderPRC();
		matthewsCorrelationCoefficient = evaluation.weightedMatthewsCorrelation();
		fMeasure = evaluation.weightedFMeasure();
		truePositiveRate = evaluation.weightedTruePositiveRate();
		falsePositiveRate = evaluation.weightedFalsePositiveRate();
		precision = evaluation.weightedPrecision();			
	}
	ClassifierMetrics(ClassEnum clazz, int wekaClassIndex, Evaluation evaluation){
		Objects.requireNonNull(clazz, "clazz cannot be null");
		Objects.requireNonNull(evaluation, "evaluation cannot be null");
		
		this.wekaClassIndex = wekaClassIndex;
		this.clazz = clazz;
		
		areaUnderROC = evaluation.areaUnderROC(wekaClassIndex);
		areaUnderPRC = evaluation.areaUnderPRC(wekaClassIndex);
		matthewsCorrelationCoefficient = evaluation.matthewsCorrelationCoefficient(wekaClassIndex);
		fMeasure = evaluation.fMeasure(wekaClassIndex);
		truePositiveRate = evaluation.truePositiveRate(wekaClassIndex);
		falsePositiveRate = evaluation.falsePositiveRate(wekaClassIndex);
		precision = evaluation.precision(wekaClassIndex);
	}
	
	ClassifierMetrics(Double areaUnderROC, Double areaUnderPRC, Double matthewsCorrelationCoefficient,
			Double fMeasure, Double truePositiveRate, Double falsePositiveRate, Double precision) {
		super();
		this.areaUnderROC = areaUnderROC;
		this.areaUnderPRC = areaUnderPRC;
		this.matthewsCorrelationCoefficient = matthewsCorrelationCoefficient;
		this.fMeasure = fMeasure;
		this.truePositiveRate = truePositiveRate;
		this.falsePositiveRate = falsePositiveRate;
		this.precision = precision;
	}
	
	public Integer getWekaClassIndex() {
		return wekaClassIndex;
	}
	
	public ClassEnum getClazz() {
		return clazz;
	}
		
	public Double getAreaUnderROC() {
		return areaUnderROC;
	}
	
	public Double getAreaUnderPRC() {
		return areaUnderPRC;
	}
	
	public Double getMatthewsCorrelationCoefficient() {
		return matthewsCorrelationCoefficient;
	}
	
	public Double getFMeasure() {
		return fMeasure;
	}
	
	public Double getTruePositiveRate() {
		return truePositiveRate;
	}
	
	public Double getFalsePositiveRate() {
		return falsePositiveRate;
	}
	
	public Double getPrecision() {
		return precision;
	}
}