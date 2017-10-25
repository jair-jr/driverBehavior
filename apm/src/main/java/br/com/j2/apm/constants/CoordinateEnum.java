package br.com.j2.apm.constants;

public enum CoordinateEnum {
	
	X("x"),
	Y("y"),
	Z("z"),
	X_COMPONENT("componenteX", "CX"),
	Y_COMPONENT("componenteY", "CY"),
	Z_COMPONENT("componenteZ", "CZ"),
	SCALAR_COMPONENT("componenteEscalar", "CEsc");
	
	private String id;
	private String abbreviation;
	
	private CoordinateEnum(String id){
		this(id, id);
	}
	
	private CoordinateEnum(String id, String abbreviation) {
		this.id = id;
		this.abbreviation = abbreviation;
	}

	public String getId(){
		return id;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
}