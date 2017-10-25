package br.com.j2.apm.classifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import br.com.j2.apm.constants.ClassEnum;

public class ExecutionResult {
	private ClassifierExecutor classifierExecutor;
	private List<ExecutionResultDetails> details;
	
	private List<ClassEnum> classes;
	
	ExecutionResult(final ClassifierExecutor classifierExecutor, 
			final List<ClassEnum> classes,
			final List<ExecutionResultDetails> details
			) {
		super();
		Objects.requireNonNull(classifierExecutor, "classifierExecutor cannot be null");
		Objects.requireNonNull(classes, "classes cannot be null");
		Objects.requireNonNull(details, "details cannot be null");
		if(details.isEmpty()){
			throw new IllegalArgumentException("'details' cannot be empty");
		}
		
		this.classifierExecutor = classifierExecutor;
		this.classes = classes;
		this.details = details;
	}
		
	public List<ClassEnum> getClasses() {
		return classes;
	}

	public ClassifierExecutor getClassifierExecutor() {
		return classifierExecutor;
	}

	public List<ExecutionResultDetails> getDetails() {
		return details;
	}
	
	public Map<Integer, Predictions> unionPredictionsBySeed(){
		return details.stream()
			.collect(
					Collectors.groupingBy(ExecutionResultDetails::getSeed, 
						Collector.of(Predictions::new, 
							(preds, erd) -> {
								if(erd.getPredictions() == null){
									return;
								}
								
								preds.addAll(erd.getPredictions());
							}, 
							Predictions::addAll
						)
					)
			);
	}
}