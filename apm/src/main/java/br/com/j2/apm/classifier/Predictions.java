package br.com.j2.apm.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;

import br.com.j2.apm.APMException;
import br.com.j2.apm.file_loader.FileLoader;

public class Predictions {
	
	private List<Prediction> predictions = new ArrayList<>();
	private String classifierIdentifier;
	
	public Predictions(){
		
	}
	
	public Predictions(String classifierId) {
		super();
		this.classifierIdentifier = classifierId;
	}

	public void addPrediction(Prediction prediction){
		Objects.requireNonNull(prediction, "prediction cannot be null");
		
		predictions.add(prediction);
	}
	
	public String getClassifierIdentifier() {
		return classifierIdentifier;
	}
	
	public List<Prediction> getPredictions() {
		return Collections.unmodifiableList(predictions);
	}
	
	public int size(){
		return predictions.size();
	}
	
	public void writeTo(CSVWriter csvWriter){
		final String[] csvHeader = new String[] {"executionId", FileLoader.INSTANCE_ID_ATTRIBUTE_NAME, classifierIdentifier, FileLoader.ENSEMBLE_CLASS_ATTRIBUTE_NAME};
		
		csvWriter.writeNext(csvHeader);
		csvWriter.writeAll(predictions.stream()
				.map(p -> new String[]{
						p.getExecutionId(), 
						p.getInstanceId(),
						p.getPredictedClass(),
						p.getActualClass()
				})
				.collect(Collectors.toList())
		);
	}
	
	/**
	 * Adiciona todas as predições de <code>another</code>
	 * a essa Predictions.
	 * @param another
	 * @return essa Predictions com todas as predições
	 * de <code>another</code>.
	 * @throws APMException se {@link #getClassifierIdentifier()} deste objeto for diferente do de <code>another</code>.
	 */
	public Predictions addAll(Predictions another) throws APMException{
		if(classifierIdentifier == null){
			classifierIdentifier = another.classifierIdentifier;
		}
		
		if(!nullSafeEquals(classifierIdentifier, another.classifierIdentifier)){
			throw new APMException("Different classifier identifiers: " + classifierIdentifier + " != " + another.classifierIdentifier);
		}
		
		if(another.predictions.isEmpty()){
			return this;
		}
		
		predictions.addAll(another.predictions);
		return this;
	}
	
	private boolean nullSafeEquals(String s1, String s2){
		return (s1 == null ? s2 == null : s1.equals(s2));
	}
}
