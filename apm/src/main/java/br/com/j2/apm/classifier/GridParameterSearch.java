package br.com.j2.apm.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GridParameterSearch {
	
	private String parameterSeparator = " ";
	
	private List<String> parameters = new ArrayList<>();
	private List<String[]> allValues = new ArrayList<>();
	
	public void addParameter(String parameter, String... values){
		Objects.requireNonNull(parameter, "parameter can't be null");
		Objects.requireNonNull(values, "values can't be null");
		if(values.length == 0){
			throw new IllegalArgumentException("values can't be empty");
		}
		parameters.add(parameter);
		allValues.add(values);
	}
	
	public String getParameterSeparator() {
		return parameterSeparator;
	}
	public void setParameterSeparator(String parameterSeparator) {
		this.parameterSeparator = parameterSeparator;
	}
	
	private String[] cartesianProduct(String[] a, String[] b, String separator){
		final String[] c = new String[a.length * b.length];
		int k = 0;
		for(int i = 0; i < a.length; i++){
			for(int j = 0; j < b.length; j++){
				c[k++] = a[i] + separator + b[j];
			}
		}
		return c;
	}

	public List<String> generateParametersCartesianProduct(){
		if(parameters.isEmpty()){
			return Collections.emptyList();
		}
		
		String[] a = cartesianProduct(new String[] {parameters.get(0)}, allValues.get(0), "");
		for(int i = 1; i < parameters.size(); i++){
			final String[] b = cartesianProduct(new String[] {parameters.get(i)}, allValues.get(i), "");
			a = cartesianProduct(a, b, parameterSeparator);
		}
				
		return Arrays.asList(a);
	}
}