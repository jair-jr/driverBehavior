package br.com.j2.apm;

public class MainEvaluation {
	private int m_NumClasses;
	private double[][] m_ConfusionMatrix;

	public static void main(String[] args){
		MainEvaluation me = new MainEvaluation();
		me.m_ConfusionMatrix = new double[][]{
			{1, 2, 3, 4},
			{5, 6, 7, 8},
			{9, 10, 11, 12},
			{13, 14, 15, 16},
		};
		me.m_NumClasses = me.m_ConfusionMatrix.length;
		
		System.out.println("True positives: " + me.numTruePositives(1));
		System.out.println("True negatives: " + me.numTrueNegatives(1));
		System.out.println("False positives: " + me.numFalsePositives(1));
		System.out.println("False negatives: " + me.numFalseNegatives(1));
	}

	/**
	   * Calculate the number of true negatives with respect to a particular class.
	   * This is defined as
	   * <p/>
	   * 
	   * <pre>
	   * correctly classified negatives
	   * </pre>
	   * 
	   * @param classIndex the index of the class to consider as "positive"
	   * @return the true positive rate
	   */
	  public double numTrueNegatives(int classIndex) {

	    double correct = 0;
	    for (int i = 0; i < m_NumClasses; i++) {
	      if (i != classIndex) {
	        for (int j = 0; j < m_NumClasses; j++) {
	          if (j != classIndex) {
	        	System.out.printf("[%d,%d]", i, j);
	            correct += m_ConfusionMatrix[i][j];
	          }
	        }
	      }
	    }
	    return correct;
	  }
	  
	  /**
	   * Calculate number of false negatives with respect to a particular class.
	   * This is defined as
	   * <p/>
	   * 
	   * <pre>
	   * incorrectly classified positives
	   * </pre>
	   * 
	   * @param classIndex the index of the class to consider as "positive"
	   * @return the false positive rate
	   */
	  public double numFalseNegatives(int classIndex) {

	    double incorrect = 0;
	    for (int i = 0; i < m_NumClasses; i++) {
	      if (i == classIndex) {
	        for (int j = 0; j < m_NumClasses; j++) {
	          if (j != classIndex) {
	        	System.out.printf("[%d,%d]", i, j);
	            incorrect += m_ConfusionMatrix[i][j];
	          }
	        }
	      }
	    }
	    return incorrect;
	  }

	  /**
	   * Calculate number of false positives with respect to a particular class.
	   * This is defined as
	   * <p/>
	   * 
	   * <pre>
	   * incorrectly classified negatives
	   * </pre>
	   * 
	   * @param classIndex the index of the class to consider as "positive"
	   * @return the false positive rate
	   */
	  public double numFalsePositives(int classIndex) {

	    double incorrect = 0;
	    for (int i = 0; i < m_NumClasses; i++) {
	      if (i != classIndex) {
	        for (int j = 0; j < m_NumClasses; j++) {
	          if (j == classIndex) {
	        	System.out.printf("[%d,%d]", i, j);
	            incorrect += m_ConfusionMatrix[i][j];
	          }
	        }
	      }
	    }
	    return incorrect;
	  }

	  /**
	   * Calculate the number of true positives with respect to a particular class.
	   * This is defined as
	   * <p/>
	   * 
	   * <pre>
	   * correctly classified positives
	   * </pre>
	   * 
	   * @param classIndex the index of the class to consider as "positive"
	   * @return the true positive rate
	   */
	  public double numTruePositives(int classIndex) {

	    double correct = 0;
	    for (int j = 0; j < m_NumClasses; j++) {
	      if (j == classIndex) {
	    	System.out.printf("[%d,%d]", classIndex, j);
	        correct += m_ConfusionMatrix[classIndex][j];
	      }
	    }
	    return correct;
	  }
}
