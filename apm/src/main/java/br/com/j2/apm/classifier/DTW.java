package br.com.j2.apm.classifier;

/**
 * Dynamic Time Warping
 * 
 * Reference: https://en.wikipedia.org/wiki/Dynamic_time_warping
 * 
 * Equivalent to the R call of the dtw library: 
 * 	<code>dtw(x = query, y = template, step.pattern = symmetric1, keep.internals = T)</code>
 * 
 * @author pma029
 */
public class DTW {
	
	private double[] querySequence;
	private double[] templateSequence;
	
	/**
	 * Contains the minimum distances to reach a 
	 * specific point when starting from (0,0).
	 */
	private double[][] cumulativeCostMatrix;
	
	public DTW(final double[] querySequence, final double[] templateSequence) {
		super();
		this.querySequence = querySequence;
		this.templateSequence = templateSequence;
	}
	
	public void calculate(){
		final int n = querySequence.length + 1;
		final int m = templateSequence.length + 1;
		
		cumulativeCostMatrix = new double[n][m];
		
		for(int i = 1; i < n; i++){
			cumulativeCostMatrix[i][0] = Double.POSITIVE_INFINITY;
		}
		
		for(int i = 1; i < m; i++){
			cumulativeCostMatrix[0][i] = Double.POSITIVE_INFINITY;
		}
		
		cumulativeCostMatrix[0][0] = 0;
		
		for(int i = 1; i < n; i++){
			for(int j = 1; j < m; j++){
				final double cost = distance(querySequence[i - 1], templateSequence[j - 1]);
				
				cumulativeCostMatrix[i][j] = cost + Math.min(
						cumulativeCostMatrix[i - 1][j], //insertion 
						Math.min(cumulativeCostMatrix[i][j - 1], //deletion
								cumulativeCostMatrix[i - 1][j - 1])); //match
			}
		}
	}
	
	public double[][] getCumulativeCostMatrix() {
		return cumulativeCostMatrix;
	}
	
	public double getMinimumDistance(){
		return cumulativeCostMatrix[querySequence.length][templateSequence.length];
	}

	/**
	 * Calcula o valor absoluto da diferença entre os valores: |e1 - e2|
	 * @param e1
	 * @param e2
	 * @return o valor absoluto da diferença entre os valores: |e1 - e2|
	 */
	private double distance(double e1, double e2) {
		return Math.abs(e1 - e2);
	}

	public double[] getQuerySequence() {
		return querySequence;
	}

	public double[] getTemplateSequence() {
		return templateSequence;
	}

}