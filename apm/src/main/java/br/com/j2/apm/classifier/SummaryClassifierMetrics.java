package br.com.j2.apm.classifier;

import br.com.j2.apm.APMException;
import weka.classifiers.Evaluation;

public class SummaryClassifierMetrics {
	private Double numberOfInstances;
	private Double correctInstancesRate;
	private Double incorrectInstancesRate;
	private Double unclassifiedInstancesRate;
	private Double kappaStatistic;
	private Double meanAbsoluteError;
	private Double rootMeanSquaredError;
	private Double relativeAbsoluteError;
	private Double rootRelativeSquaredError;
		
	private double[][] confusionMatrix;
	
	SummaryClassifierMetrics(Double numberOfInstances, Double correctInstancesRate,
			Double incorrectInstancesRate, Double unclassifiedInstancesRate, Double kappaStatistic,
			Double meanAbsoluteError, Double rootMeanSquaredError, Double relativeAbsoluteError,
			Double rootRelativeSquaredError, double[][] confusionMatrix) {
		super();
		this.numberOfInstances = numberOfInstances;
		this.correctInstancesRate = correctInstancesRate;
		this.incorrectInstancesRate = incorrectInstancesRate;
		this.unclassifiedInstancesRate = unclassifiedInstancesRate;
		this.kappaStatistic = kappaStatistic;
		this.meanAbsoluteError = meanAbsoluteError;
		this.rootMeanSquaredError = rootMeanSquaredError;
		this.relativeAbsoluteError = relativeAbsoluteError;
		this.rootRelativeSquaredError = rootRelativeSquaredError;
		this.confusionMatrix = confusionMatrix;
	}

	SummaryClassifierMetrics(Evaluation evaluation){
		numberOfInstances = evaluation.numInstances();
		correctInstancesRate = evaluation.pctCorrect() / 100;
		incorrectInstancesRate = evaluation.pctIncorrect() / 100;
		unclassifiedInstancesRate = evaluation.pctUnclassified() / 100;
		kappaStatistic = evaluation.kappa();
		meanAbsoluteError = evaluation.meanAbsoluteError();
		rootMeanSquaredError = evaluation.rootMeanSquaredError();
		try {
			relativeAbsoluteError = evaluation.relativeAbsoluteError();
		} 
		catch (Exception e) {
			throw new APMException("Exception calculating relativeAbsoluteError", e);
		}
		rootRelativeSquaredError = evaluation.rootRelativeSquaredError();
		confusionMatrix = evaluation.confusionMatrix();
	}
	
	public double[][] getConfusionMatrix() {
		return confusionMatrix;
	}

	public Double getNumberOfInstances() {
		return numberOfInstances;
	}
	
	public Double getCorrectInstancesRate() {
		return correctInstancesRate;
	}

	public Double getIncorrectInstancesRate() {
		return incorrectInstancesRate;
	}
	
	public Double getUnclassifiedInstancesRate() {
		return unclassifiedInstancesRate;
	}

	public Double getKappaStatistic() {
		return kappaStatistic;
	}

	public Double getMeanAbsoluteError() {
		return meanAbsoluteError;
	}

	public Double getRootMeanSquaredError() {
		return rootMeanSquaredError;
	}

	public Double getRelativeAbsoluteError() {
		return relativeAbsoluteError;
	}

	public Double getRootRelativeSquaredError() {
		return rootRelativeSquaredError;
	}
	
	
//Total: evaluation.numInstances
	
	/*
	 * === Summary ===

Correctly Classified Instances         435               91.0042 %
Incorrectly Classified Instances        43                8.9958 %
Kappa statistic                          0.8927
Mean absolute error                      0.0745
Root mean squared error                  0.1576
Relative absolute error                 31.0579 %
Root relative squared error             45.4849 %
Total Number of Instances              478     

=== Detailed Accuracy By Class ===

                 TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0,935    0,052    0,813      0,935    0,870      0,839    0,988     0,949     evento_nao_agressivo
                 0,896    0,007    0,958      0,896    0,926      0,913    0,997     0,984     curva_direita_agressiva
                 0,870    0,010    0,944      0,870    0,905      0,889    0,991     0,965     curva_esquerda_agressiva
                 0,943    0,002    0,971      0,943    0,957      0,953    0,999     0,991     troca_faixa_direita_agressiva
                 0,917    0,005    0,975      0,917    0,945      0,934    0,994     0,982     freada_agressiva
                 0,929    0,028    0,876      0,929    0,902      0,881    0,987     0,961     aceleracao_agressiva
                 0,857    0,004    0,923      0,857    0,889      0,883    0,996     0,942     troca_faixa_esquerda_agressiva
Weighted Avg.    0,910    0,019    0,915      0,910    0,911      0,894    0,992     0,968     

=== Confusion Matrix ===

  a  b  c  d  e  f  g   <-- classified as
 87  1  0  0  0  5  0 |  a = evento_nao_agressivo
  5 69  1  1  0  1  0 |  b = curva_direita_agressiva
  7  0 67  0  0  1  2 |  c = curva_esquerda_agressiva
  0  1  1 33  0  0  0 |  d = troca_faixa_direita_agressiva
  3  0  0  0 77  4  0 |  e = freada_agressiva
  4  0  0  0  2 78  0 |  f = aceleracao_agressiva
  1  1  2  0  0  0 24 |  g = troca_faixa_esquerda_agressiva
	 */
}
