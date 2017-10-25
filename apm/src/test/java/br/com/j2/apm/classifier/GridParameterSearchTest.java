package br.com.j2.apm.classifier;

import static br.com.j2.apm.util.TestUtil.*;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GridParameterSearchTest {

	@Test
	public void generateParametersCartesianProductWithoutParameters(){
		final GridParameterSearch gps = new GridParameterSearch();
		
		final List<String> options = gps.generateParametersCartesianProduct();
		Assert.assertEquals(options.size(), 0);
	}
	
	@Test
	public void generateParametersCartesianProductWithOneParameters(){
		final GridParameterSearch gps = new GridParameterSearch();
		gps.addParameter("A=", "1", "2", "3");
		
		final List<String> options = gps.generateParametersCartesianProduct();
		Assert.assertEquals(options.size(), 3);
		assertContainsInAnyOrder(options, 
				"A=1",
				"A=2",
				"A=3");
	}
	
	@Test
	public void generateParametersCartesianProductWithTwoParameters(){
		final GridParameterSearch gps = new GridParameterSearch();
		gps.addParameter("A=", "1", "2", "3");
		gps.addParameter("B=", "C1", "C2", "C3");
		
		final List<String> options = gps.generateParametersCartesianProduct();
		Assert.assertEquals(options.size(), 9);
		assertContainsInAnyOrder(options, 
				"A=1 B=C1",
				"A=1 B=C2",
				"A=1 B=C3",
				"A=2 B=C1",
				"A=2 B=C2",
				"A=2 B=C3",
				"A=3 B=C1",
				"A=3 B=C2",
				"A=3 B=C3");
	}
	
	@Test
	public void generateParametersCartesianProductWithNamelesParameters(){
		final GridParameterSearch gps = new GridParameterSearch();
		gps.addParameter("", "-A", "");
		gps.addParameter("C", "1", "2");
		
		final List<String> options = gps.generateParametersCartesianProduct();
		Assert.assertEquals(options.size(), 4);
		assertContainsInAnyOrder(options, 
				"-A C1",
				" C1",
				"-A C2",
				" C2");
	}

	
	@Test
	public void generateParametersCartesianProductWithThreeParameters(){
		final GridParameterSearch gps = new GridParameterSearch();
		gps.addParameter("A=", "1", "2", "3");
		gps.addParameter("B=", "C1", "C2", "C3");
		gps.addParameter("C=", "D1", "D2");
		gps.setParameterSeparator(";");
		
		final List<String> options = gps.generateParametersCartesianProduct();
		Assert.assertEquals(options.size(), 18);
		assertContainsInAnyOrder(options, 
				"A=1;B=C1;C=D1",
				"A=1;B=C2;C=D1",
				"A=1;B=C3;C=D1",
				"A=2;B=C1;C=D1",
				"A=2;B=C2;C=D1",
				"A=2;B=C3;C=D1",
				"A=3;B=C1;C=D1",
				"A=3;B=C2;C=D1",
				"A=3;B=C3;C=D1",
				"A=1;B=C1;C=D2",
				"A=1;B=C2;C=D2",
				"A=1;B=C3;C=D2",
				"A=2;B=C1;C=D2",
				"A=2;B=C2;C=D2",
				"A=2;B=C3;C=D2",
				"A=3;B=C1;C=D2",
				"A=3;B=C2;C=D2",
				"A=3;B=C3;C=D2");
	}

}