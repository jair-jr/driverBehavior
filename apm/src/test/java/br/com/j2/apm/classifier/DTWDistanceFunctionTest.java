package br.com.j2.apm.classifier;

import static br.com.j2.apm.constants.CoordinateEnum.*;
import static br.com.j2.apm.constants.SensorEnum.*;
import static org.testng.Assert.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import br.com.j2.apm.constants.CoordinateEnum;
import br.com.j2.apm.constants.DatasetTypeEnum;
import br.com.j2.apm.file_loader.FileLoader;
import weka.core.Instances;

public class DTWDistanceFunctionTest {
	private FileLoader fileLoader;
	
	@BeforeClass
	public void init(){
		fileLoader = new FileLoader(Paths.get("src/test/resources/instances"), Collections.singletonList(4));		
	}

	@DataProvider
	public Object[][] distanceDP(){
		return new Object[][]{
			{0, 0, null, 0D},
			{0, 0, X, 0D},
			{0, 0, Y, 0D},
			{0, 0, Z, 0D},

			{27, 44, null, 17.1541},
			{27, 44, X, 5.161052},
			{27, 44, Y, 11.95769},
			{27, 44, Z, 34.34355},

			{44, 27, null, 17.1541},
			{44, 27, X, 5.161052},
			{44, 27, Y, 11.95769},
			{44, 27, Z, 34.34355},
		};
	}
	
	private String getAttributeIndices(Instances instances, CoordinateEnum coordinate){
		final List<CoordinateEnum> attributeIndexes = new ArrayList<>();
		if(coordinate != null){
			attributeIndexes.add(coordinate);
		}
		else{
			attributeIndexes.addAll(GYROSCOPE_EARTH.getCoordinates());
		}
		
		return attributeIndexes.stream()
			.map(c -> String.valueOf(instances.attribute(c.getId()).index() + 1))
			.collect(Collectors.joining(","));
	}
	
	@Test(dataProvider = "distanceDP")
	public void distance(int instance1Index, int instance2Index, CoordinateEnum coordinate, double expectedDistance){
		final Instances instances = fileLoader.getInstances(DatasetTypeEnum.TIME_SERIES, GYROSCOPE_EARTH, Optional.ofNullable(coordinate), 4).get();
		
		final DTWDistanceFunction dtwDistanceFunction = new DTWDistanceFunction(instances);
		dtwDistanceFunction.setAttributeIndices(getAttributeIndices(instances, coordinate));
		
		assertEquals(dtwDistanceFunction.distance(instances.get(instance1Index), instances.get(instance2Index)), expectedDistance, 1e-5);		
	}
}
