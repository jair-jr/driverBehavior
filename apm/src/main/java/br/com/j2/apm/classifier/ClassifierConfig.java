package br.com.j2.apm.classifier;

import java.util.List;
import java.util.stream.Collectors;

public class ClassifierConfig {
	private int id;
	private String rawConfig;
	
	public ClassifierConfig(int id, String rawConfig) {
		super();
		this.id = id;
		this.rawConfig = rawConfig;
	}

	public int getId() {
		return id;
	}
	
	public List<ClassifierConfig> createClassifierConfigs(String variable, GridParameterSearch gridParameterSearch){
		return createClassifierConfigs(variable, gridParameterSearch, 1);
	}
	
	public List<ClassifierConfig> createClassifierConfigs(String variable, GridParameterSearch gridParameterSearch, int firstConfigId){
		final int[] i = new int[1];
		i[0] = firstConfigId - 1;
		return gridParameterSearch.generateParametersCartesianProduct().stream()
			.map(par -> new ClassifierConfig(++i[0], getConfig(variable, par)))
			.collect(Collectors.toList());
	}
	
	public String getConfig(String variable, String value){
		return rawConfig.replace(variable, value);
	}
	
	public String getConfig(int seed) {
		return getConfig("%%SEED%%", String.valueOf(seed));
	}
	
	public String getRawConfig() {
		return rawConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((rawConfig == null) ? 0 : rawConfig.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassifierConfig other = (ClassifierConfig) obj;
		if (id != other.id)
			return false;
		if (rawConfig == null) {
			if (other.rawConfig != null)
				return false;
		} else if (!rawConfig.equals(other.rawConfig))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", config=" + rawConfig + "]";
	}
	
}