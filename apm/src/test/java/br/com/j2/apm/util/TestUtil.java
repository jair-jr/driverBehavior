package br.com.j2.apm.util;

import java.util.Collection;

import org.testng.Assert;

public class TestUtil {

	private TestUtil(){
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> void assertContainsInAnyOrder(Collection<?> col, T... values){
		Assert.assertEquals(col.size(), values.length);
		final String colAsString = col.toString();
		for(int i = 0; i < values.length; i++){
			Assert.assertTrue(col.contains(values[i]), "Value '" + values[i] + "' not found in " + colAsString);
		}
	}

}
