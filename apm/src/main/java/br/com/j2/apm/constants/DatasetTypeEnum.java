package br.com.j2.apm.constants;

public enum DatasetTypeEnum {
	ATTRIBUTE_VECTOR("va"), TIME_SERIES("st");
	
	private String id;

	private DatasetTypeEnum(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
