package br.com.j2.apm.file_loader;

import static br.com.j2.apm.constants.CoordinateEnum.*;
import static br.com.j2.apm.constants.DatasetTypeEnum.*;
import static br.com.j2.apm.constants.SensorEnum.*;
import static org.testng.Assert.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import br.com.j2.apm.APMException;
import br.com.j2.apm.constants.CoordinateEnum;
import br.com.j2.apm.constants.DatasetTypeEnum;
import br.com.j2.apm.constants.SensorEnum;
import weka.core.Attribute;
import weka.core.Instances;

public class FileLoaderTest {
	
	private FileLoader fileLoader;
	
	@BeforeMethod
	public void startLoader(){
		fileLoader = new FileLoader(Paths.get("src/test/resources/instances"), Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9));
	}

	@DataProvider
	public Object[][] getInstancesDP(){
		return new Object[][]{
			//
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION_EARTH, X, 4, true, 275},
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION_EARTH, Y, 4, true, 275},
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION_EARTH, Z, 4, true, 275},
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION_EARTH, null, 4, true, 275},
			
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, X, 4, true, 275},
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, Y, 4, true, 275},
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, Z, 4, true, 275},
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, null, 4, true, 275},
			
			{ATTRIBUTE_VECTOR, GYROSCOPE, null, 6, false, null},
			
			{ATTRIBUTE_VECTOR, ROTATION_VECTOR, X_COMPONENT, 7, true, 150},
			{ATTRIBUTE_VECTOR, ROTATION_VECTOR, X_COMPONENT, 6, false, null},
			{ATTRIBUTE_VECTOR, ROTATION_VECTOR, Y_COMPONENT, 8, false, null},
			{ATTRIBUTE_VECTOR, ROTATION_VECTOR, null, 9, false, null},
			
			{TIME_SERIES, ACCELEROMETER_EARTH, X, 4, true, 150},
			{TIME_SERIES, ACCELEROMETER_EARTH, Y, 4, true, 150},
			{TIME_SERIES, ACCELEROMETER_EARTH, Z, 4, true, 150},
			{TIME_SERIES, ACCELEROMETER_EARTH, null, 4, true, 150},

			{TIME_SERIES, GYROSCOPE_EARTH, null, 4, true, 50},
			{TIME_SERIES, GYROSCOPE_EARTH, Z, 4, true, 50},

			{TIME_SERIES, ROTATION_VECTOR, Y_COMPONENT, 3, false, null},
			{TIME_SERIES, ROTATION_VECTOR, null, 4, false, null},
			{TIME_SERIES, GRAVITY, null, 5, false, null},
			{TIME_SERIES, GRAVITY, Z, 6, false, null},
		};
	}
	
	@Test(dataProvider = "getInstancesDP")
	public void getInstances(DatasetTypeEnum datasetType, 
			SensorEnum sensor, 
			CoordinateEnum coordinate, 
			int numberOfFrames,
			boolean expectedInstancesPresent, 
			Integer expectedInstancesCount){
		
		final Optional<Instances> instances = fileLoader.getInstances(datasetType, sensor, Optional.ofNullable(coordinate), numberOfFrames);
		assertEquals(instances.isPresent(), expectedInstancesPresent);

		if(expectedInstancesCount != null){
			assertEquals(instances.get().numInstances(), expectedInstancesCount.intValue());
		}

		instances.ifPresent(insts -> {
			assertEquals(insts.classAttribute().name(), FileLoader.SENSOR_CLASS_ATTRIBUTE_NAME);
			assertNotNull(insts.attribute(FileLoader.SENSOR_CLASS_ATTRIBUTE_NAME));
			
			assertNotNull(insts.attribute(FileLoader.INSTANCE_ID_ATTRIBUTE_NAME));

			assertAttributesArePresentForCoordinates(insts, coordinate != null ? Collections.singletonList(coordinate) : sensor.getCoordinates());			
		});
		
	}
	
	private void assertAttributesArePresentForCoordinates(Instances instances, List<CoordinateEnum> coordinates) {
		final Set<String> coordsIds = coordinates.stream()
			.map(CoordinateEnum::getId)
			.collect(Collectors.toSet());
		
		final Set<String> presentCoordsIds = new HashSet<>();
		
		for(int i = 0; i < instances.numAttributes(); i++) {
			final Attribute attribute = instances.attribute(i);
			if(attribute.name().equals(FileLoader.SENSOR_CLASS_ATTRIBUTE_NAME)){
				continue;
			}
			if(attribute.name().equals(FileLoader.INSTANCE_ID_ATTRIBUTE_NAME)){
				continue;
			}
			
			final Optional<String> firstFoundCoordId = coordsIds.stream()
				.filter(coordId -> attribute.name().equals(coordId) || attribute.name().startsWith(coordId + "_"))
				.findFirst();
			
			if(firstFoundCoordId.isPresent()){
				presentCoordsIds.add(firstFoundCoordId.get());
			}
			else {
				fail("Atributo " + attribute.name() + " referencia a(s) coordenada(s): " + coordsIds);
			}			
		}
		
		if(!coordsIds.equals(presentCoordsIds)){
			coordsIds.removeAll(presentCoordsIds);
			fail("Não existem atributos para a(s) coordeanda(s): " + coordsIds);
		}
	}
	
	@DataProvider
	public Object[][] getInstancesFileNameDP(){
		return new Object[][]{
			{TIME_SERIES, LINEAR_ACCELERATION, 3, "st_aceleracaoLinear_nq3.arff"},
			
			{TIME_SERIES, LINEAR_ACCELERATION, 3, "st_aceleracaoLinear_nq3.arff"},

			{TIME_SERIES, ACCELEROMETER, 4, "st_acelerometro_nq4.arff"},
			{TIME_SERIES, MAGNETIC_FIELD, 5, "st_campoMagnetico_nq5.arff"},
			{TIME_SERIES, ROTATION_VECTOR, 6, "st_vetorRotacao_nq6.arff"},
			{TIME_SERIES, ACCELEROMETER, 7, "st_acelerometro_nq7.arff"},
			{TIME_SERIES, ACCELEROMETER, 8, "st_acelerometro_nq8.arff"},
			
			{TIME_SERIES, ACCELEROMETER, 4, "st_acelerometro_nq4.arff"},
			{TIME_SERIES, MAGNETIC_FIELD, 5, "st_campoMagnetico_nq5.arff"},
			{TIME_SERIES, ROTATION_VECTOR, 6, "st_vetorRotacao_nq6.arff"},
			{TIME_SERIES, ACCELEROMETER, 7, "st_acelerometro_nq7.arff"},
			{TIME_SERIES, ACCELEROMETER, 8, "st_acelerometro_nq8.arff"},

			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, 2, "va_aceleracaoLinear_nq2.arff"},
			{ATTRIBUTE_VECTOR, ACCELEROMETER, 3, "va_acelerometro_nq3.arff"},
			{ATTRIBUTE_VECTOR, MAGNETIC_FIELD, 4, "va_campoMagnetico_nq4.arff"},
			{ATTRIBUTE_VECTOR, ROTATION_VECTOR, 5, "va_vetorRotacao_nq5.arff"},
			{ATTRIBUTE_VECTOR, ACCELEROMETER, 6, "va_acelerometro_nq6.arff"}
		};
	}
	
	@Test(dataProvider = "getInstancesFileNameDP")
	public void getInstancesFileName(DatasetTypeEnum datasetType, SensorEnum sensor, int numberOfFrames, String expectedFilename){
		assertEquals(FileLoader.getInstancesFileName(datasetType, sensor, numberOfFrames), expectedFilename);
	}

//	@DataProvider
//	public Object[][] getTimeSeriesInstancesDP(){
//		return new Object[][]{
//			{ACCELEROMETER_EARTH, X, true, 69},
//			{ACCELEROMETER_EARTH, Y, true, 69},
//			{GYROSCOPE_EARTH, null, true, 69},
//			{GYROSCOPE_EARTH, Z, true, 69},
//			{ROTATION_VECTOR, Y_COMPONENT, false, null},
//			{ROTATION_VECTOR, null, false, null},
//			{GRAVITY, null, false, null},
//			{GRAVITY, Z, false, null},
//		};
//	}
//	
//	@Test(dataProvider = "getTimeSeriesInstancesDP")
//	public void getTimeSeriesInstances(SensorEnum sensor, 
//			CoordinateEnum coordinate, 
//			boolean expectedInstancesPresent, 
//			Integer expectedInstancesCount){
//		final Optional<Instances> instances = fileLoader.getTimeSeriesInstances(sensor, Optional.ofNullable(coordinate));
//		assertEquals(instances.isPresent(), expectedInstancesPresent);
//		
//		final Instances insts = instances.orElse(null); 
//		
//		if(expectedInstancesCount != null){
//			assertEquals(insts.numInstances(), expectedInstancesCount.intValue());				
//		}
//
//		if(insts != null){
//			assertEquals(insts.classAttribute().name(), FileLoader.CLASS_ATTRIBUTE_NAME);
//			
//			final List<CoordinateEnum> coords = coordinate != null ? Collections.singletonList(coordinate) : sensor.getCoordinates();
//			for(CoordinateEnum coord : coords){
//				assertNotNull(insts.attribute(coord.getId()));
//			}
//			assertNotNull(insts.attribute("evento"));
//			assertEquals(insts.numAttributes(), 1 + coords.size());
//		}
//	}

	
	@DataProvider
	public Object[][] getInstancesWithInvalidNumberOfFramesDP(){
		return new Object[][]{
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, X},
			{TIME_SERIES, GYROSCOPE, null},
			{TIME_SERIES, GYROSCOPE, null},
			{TIME_SERIES, GYROSCOPE, Y},
		};
	}
	
	@Test(dataProvider = "getInstancesWithInvalidNumberOfFramesDP", expectedExceptions = APMException.class)
	public void getInstancesWithInvalidNumberOfFrames(DatasetTypeEnum datasetType, SensorEnum sensor, CoordinateEnum coordinate){
		fileLoader.getInstances(datasetType, sensor, Optional.ofNullable(coordinate), 1);
	}
	
	@DataProvider
	public Object[][] getInstancesWithUnsupportedCoordinateDP(){
		return new Object[][]{
			{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, X_COMPONENT},
			{ATTRIBUTE_VECTOR, ROTATION_VECTOR, Z},

			{TIME_SERIES, LINEAR_ACCELERATION, X_COMPONENT},
			{TIME_SERIES, ROTATION_VECTOR, Z},
		};
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "getInstancesWithUnsupportedCoordinateDP")
	public void getInstancesWithUnsupportedCoordinate(DatasetTypeEnum datasetType, SensorEnum sensor, CoordinateEnum coordinate){
		fileLoader.getInstances(datasetType, sensor, Optional.of(coordinate), 3);
	}
	
	@DataProvider
	public Object[][] checkReturnedInstancesIsADefensiveCopyDP(){
		return new Object[][]{
				{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, X, 4},
				{TIME_SERIES, ACCELEROMETER_EARTH, X, 4},

				{ATTRIBUTE_VECTOR, LINEAR_ACCELERATION, null, 4},
				{TIME_SERIES, ACCELEROMETER_EARTH, null, 4},
		};
	}
	
	@Test(dataProvider = "checkReturnedInstancesIsADefensiveCopyDP")
	public void checkReturnedInstancesIsADefensiveCopy(DatasetTypeEnum datasetType, SensorEnum sensor, CoordinateEnum coordinate, int numberOfFrames){
		final Optional<Instances> instancesOpt1 = fileLoader.getInstances(datasetType, sensor, Optional.ofNullable(coordinate), numberOfFrames);
		Assert.assertTrue(instancesOpt1.isPresent());

		final Instances instances1 = instancesOpt1.get();
		instances1.remove(instances1.size() - 1);
		instances1.deleteAttributeAt(0);
		
		final Optional<Instances> instancesOpt2 = fileLoader.getInstances(datasetType, sensor, Optional.ofNullable(coordinate), numberOfFrames);
		Assert.assertTrue(instancesOpt2.isPresent());
		final Instances instances2 = instancesOpt2.get();
		
		Assert.assertEquals(instances1.size(), instances2.size() - 1);
		Assert.assertEquals(instances1.classIndex(), instances2.classIndex() - 1);
	}

}