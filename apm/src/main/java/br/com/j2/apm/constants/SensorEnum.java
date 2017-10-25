package br.com.j2.apm.constants;

import static br.com.j2.apm.constants.CoordinateEnum.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SensorEnum {
	
	LINEAR_ACCELERATION("aceleracaoLinear", "AclLin", false, X, Y, Z),
	LINEAR_ACCELERATION_EARTH("aceleracaoLinearTerra", "AclLinE", true, X, Y, Z),
	
	ACCELEROMETER("acelerometro", "Acel", false, X, Y, Z),
	ACCELEROMETER_EARTH("acelerometroTerra", "AcelE", true, X, Y, Z),
	
	MAGNETIC_FIELD("campoMagnetico", "Mag", false, X, Y, Z),
	MAGNETIC_FIELD_EARTH("campoMagneticoTerra", "MagE", true, Y, Z), //X axis values are always close to 0
	
	GYROSCOPE("giroscopio", "Gir", false, X, Y, Z),
	GYROSCOPE_EARTH("giroscopioTerra", "GirE", true, X, Y, Z),
	
	GRAVITY("gravidade", "Grav", false, X, Y, Z),
	//gravity in Earth's coordinates is always close to X = 0, Y = 0, Z = 9.8, 
	//so we can't use gravity in Earth's coordinates.
	
	ROTATION_VECTOR("vetorRotacao", "VRot", false, X_COMPONENT, Y_COMPONENT, Z_COMPONENT, SCALAR_COMPONENT);
	
	private String id;
	private String abbreviation;
	private boolean earth;
	private List<CoordinateEnum> coordinates;
	
	private SensorEnum(String id, String abbreviation, boolean earth, CoordinateEnum ... coordinates){
		this.id = id;
		this.abbreviation = abbreviation;
		this.earth = earth;
		this.coordinates = Collections.unmodifiableList(Arrays.asList(coordinates));
	}
	
	public String getId(){
		return id;
	}
	
	public boolean isEarth() {
		return earth;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public List<CoordinateEnum> getCoordinates() {
		return coordinates;
	}
	
}