package br.com.j2.apm.classifier;

import java.util.Objects;

public class Prediction {

	private String executionId;
	private String instanceId;
	private String predictedClass;
	private String actualClass;
	
	public Prediction(String executionId, String instanceId, String predictedClass, String actualClass) {
		super();
		this.executionId = Objects.requireNonNull(executionId, "executionId cannot be null");
		this.instanceId = Objects.requireNonNull(instanceId, "instanceId cannot be null");
		this.predictedClass = Objects.requireNonNull(predictedClass, "predictedClass cannot be null");
		this.actualClass = Objects.requireNonNull(actualClass, "actualClass cannot be null");
	}
	
	public String getExecutionId() {
		return executionId;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public String getPredictedClass() {
		return predictedClass;
	}
	public String getActualClass() {
		return actualClass;
	}
	
}