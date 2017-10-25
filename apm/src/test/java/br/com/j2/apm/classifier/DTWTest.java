package br.com.j2.apm.classifier;

import static org.testng.Assert.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DTWTest {

	@DataProvider
	public Object[][] getDistanceDP(){
		return new Object[][]{
			{
				new double[]{1.1, 1.2, 1.3, 1.4}, //querySequence
				
				new double[]{1.1, 1.2, 1.3, 1.4}, //templateSequence
				
				0D
			},
			
			{
				new double[]{
					-2.66423748806119, 2.12020452599972, 7.84856855403632, 
					-7.25875231903046, 9.08444354776293, -1.09156557824463, 
					2.18499881681055, -9.19510917272419, 4.44269376341254, 
					9.30907371919602, 2.06689388491213, -3.58112975489348, 
					0.589294219389558, -5.39092163089663, -8.07750663720071
				},
				
				new double[]{
					-7.58286838885397, 3.26778520829976, 7.68958908040076, 
					-3.08737812563777, -5.28552907984704, 3.53204651735723, 
					-8.28275555744767, -7.04466476570815, 2.29280990082771, 
					-4.54883823636919, -8.00383547786623, 8.11651484109461, 
					6.50119502097368, 8.14570927992463, 4.3908927962184, 
					-5.62803287059069, 3.50479098502547, 9.0818087104708, 
					-7.61949024163187, 9.90649521350861, -3.94085760228336, 
					3.34657128434628, 3.47697752527893
				},
				
				95.250712
			},
			
			{
				new double[]{
						0.759018341312185, 0.145224806386977, 0.424091615248471, 
						0.603180781239644, 0.198242876213044, 0.380709723569453, 
						0.316893171984702, 0.00634256168268621, 0.0878162905573845, 
						0.534683284116909
				},
				
				new double[]{
						0.693643479607999, 0.701960485428572, 0.788476596819237, 
						0.688960057683289, 0.417373842792585, 0.507100738352165, 
						0.840327968122438, 0.403762231348082, 0.312896979507059, 
						0.463358752662316
				},
				
				1.649782
			}
		};
	}
	
	@Test(dataProvider = "getDistanceDP")
	public void getDistance(double[] querySequence, double[] templateSequence, double expectedDistance){
		DTW dtw = new DTW(querySequence, templateSequence);
		dtw.calculate();
		
		assertEquals(dtw.getMinimumDistance(), expectedDistance, 1e-6);
		
		dtw = null;
		
		final DTW inverseDTW = new DTW(templateSequence, querySequence);
		inverseDTW.calculate();
		
		assertEquals(inverseDTW.getMinimumDistance(), expectedDistance, 1e-6);
	}
}
