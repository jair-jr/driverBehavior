package br.com.j2.apm.file_loader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import br.com.j2.apm.APMException;
import br.com.j2.apm.constants.CoordinateEnum;
import br.com.j2.apm.constants.DatasetTypeEnum;
import br.com.j2.apm.constants.SensorEnum;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class FileLoader {
	
	public static final String ENSEMBLE_CLASS_ATTRIBUTE_NAME = "actualClass";
	
	public static final String SENSOR_CLASS_ATTRIBUTE_NAME = "evento";
	
	public static final String INSTANCE_ID_ATTRIBUTE_NAME = "quadro0";
		
	private static final String INSTANCES_FILE_EXTENSION = "arff";
	
	private Path instancesDir;
	private List<SensorEnum> sensors;
	private Set<Integer> numbersOfFrames;
	
	private Map<String, Instances> instancesByFilename = new ConcurrentHashMap<>();
	
	public FileLoader(Path instancesDir, Collection<Integer> numbersOfFrames){
		this(instancesDir, SensorEnum.values(), numbersOfFrames);
	}
	
	public FileLoader(Path instancesDir, SensorEnum[] sensors, Collection<Integer> numbersOfFrames){
		if(!Files.isDirectory(instancesDir) || !Files.exists(instancesDir)){
			throw new APMException("'" + instancesDir.toAbsolutePath().normalize() + "' não existe ou não é um diretório");
		}
		Objects.requireNonNull(numbersOfFrames, "numbersOfFrames is required");
		
		this.instancesDir = instancesDir;
		this.sensors = Collections.unmodifiableList(Arrays.asList(sensors));
		this.numbersOfFrames = Collections.unmodifiableSet(new HashSet<>(numbersOfFrames));		
	}
	
	private static Instances readSensorInstances(Path p){
		return readInstances(p, SENSOR_CLASS_ATTRIBUTE_NAME);
	}
	
	public static Instances readEnsembleInstances(Path p){
		return readInstances(p, ENSEMBLE_CLASS_ATTRIBUTE_NAME);
	}
	
	private static Instances readInstances(Path p, String classAttributeName){
		try{
			final Instances instances = ConverterUtils.DataSource.read(p.toString());
			if(classAttributeName != null){
				instances.setClass(instances.attribute(classAttributeName));
			}
			return instances;
		}
		catch(Exception e){
			throw new APMException(e);
		}
	}
		
	public Path getInstancesDir() {
		return instancesDir;
	}

	public List<SensorEnum> getSensors() {
		return sensors;
	}

	public Collection<Integer> getNumbersOfFrames() {
		return numbersOfFrames;
	}
		
	/*
	 * package level just to make testing easier
	 */
	static String getInstancesFileName(DatasetTypeEnum datasetType, SensorEnum sensor, 
			int numberOfFrames){
		
		return String.format("%s_%s_nq%s.%s",
				datasetType.getId(),
				sensor.getId(), 
				numberOfFrames,
				INSTANCES_FILE_EXTENSION);		
	}
	
	public Optional<Instances> getInstances(DatasetTypeEnum datasetType, SensorEnum sensor, 
			Optional<CoordinateEnum> coordinate, int numberOfFrames){
		
		Objects.requireNonNull(datasetType, "datasetType cannot be null");
		Objects.requireNonNull(sensor, "sensor cannot be null");
		
		if(coordinate.isPresent() && !sensor.getCoordinates().contains(coordinate.get())){
			throw new IllegalArgumentException(sensor + " does not support coordinate: " + coordinate);
		}

		if(!numbersOfFrames.contains(numberOfFrames)){
			throw new APMException("numberOfFrames '" + numberOfFrames + "' is not in " + numbersOfFrames);
		}
		
		final Instances instances = getOrLoadInstancesByFileName(getInstancesFileName(datasetType, sensor, numberOfFrames));
		if(instances == null){
			return Optional.empty();
		}
		
		final Instances instancesCopy = new Instances(instances);
		deleteAttributesNotMatchingCoordinateFromInstances(coordinate, instancesCopy);
		return Optional.of(instancesCopy);
	}
	
	private Instances getOrLoadInstancesByFileName(final String fileName){
		return instancesByFilename.computeIfAbsent(fileName, fileNameKey -> {
			final Path path = instancesDir.resolve(fileNameKey);
			if(!Files.exists(path)){
				return null;
			}
			return readSensorInstances(path);
		});
	}

	private void deleteAttributesNotMatchingCoordinateFromInstances(Optional<CoordinateEnum> coordinate, final Instances instances) {
		if(!coordinate.isPresent()) {
			//não há atributos a remover porque todas as coordenadas devem ser utilizadas
			return;
		}
		
		final String className = instances.classAttribute().name();
		final CoordinateEnum coord = coordinate.get();
		final String coordPrefix = coord.getId() + "_";
		
		for(int i = 0; i < instances.numAttributes(); i++){
			final Attribute att = instances.attribute(i);
			if(att.name().equals(className)){
				continue;
			}
			
			if(att.name().equals(INSTANCE_ID_ATTRIBUTE_NAME)){
				continue;
			}
						
			if(coord.getId().equals(att.name()) || att.name().startsWith(coordPrefix)){
				continue;
			}
			
			instances.deleteAttributeAt(i);
			i--;
		}
	}
		
}