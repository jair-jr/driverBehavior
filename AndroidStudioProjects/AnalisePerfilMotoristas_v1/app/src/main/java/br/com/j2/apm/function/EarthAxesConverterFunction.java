package br.com.j2.apm.function;

import android.support.annotation.NonNull;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import br.com.j2.apm.SensorType;
import br.com.j2.apm.event.MotionDataCollectionEvent;


/**
 * Function that converts an event from the device's to Earth's coordinate system.
 * Note: this function holds state between calls to {@link #apply(MotionDataCollectionEvent)}
 */
public class EarthAxesConverterFunction implements Function<MotionDataCollectionEvent, MotionDataCollectionEvent> {
    private static final Set<SensorType> SOURCE_SENSOR_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(SensorType.GRAVITY, SensorType.MAGNETIC_FIELD)));

    private SensorType targetSensorType;

    private MotionDataCollectionEvent magneticFieldEvent;
    private MotionDataCollectionEvent gravity;
    private MotionDataCollectionEvent targetEvent;

    private EarthCoordinate earthCoordinate;

    public EarthAxesConverterFunction(@NonNull EarthCoordinate earthCoordinate, @NonNull SensorType targetSensorType) {
        this.earthCoordinate = earthCoordinate;
        this.targetSensorType = targetSensorType;
    }

    public static Set<SensorType> getSourceSensorTypes() {
        return SOURCE_SENSOR_TYPES;
    }

    public SensorType getTargetSensorType() {
        return targetSensorType;
    }

    public EarthCoordinate getEarthCoordinate() {
        return earthCoordinate;
    }

    private MotionDataCollectionEvent createEvent(){
        final float[] rotationMatrix = new float[9];
        final float[] gravityMatrix = new float[]{(float) gravity.getX(), (float) gravity.getY(), (float) gravity.getZ()};
        final float[] geomagneticMatrix = new float[]{(float) magneticFieldEvent.getX(), (float) magneticFieldEvent.getY(), (float) magneticFieldEvent.getZ()};

        if(!earthCoordinate.getRotationMatrix(rotationMatrix, null, gravityMatrix, geomagneticMatrix)){
            return null;
        }

        final RealMatrix targetEventRealMatrix3_1 = MatrixUtils.createRealMatrix(new double[][] {
                {targetEvent.getX()},
                {targetEvent.getY()},
                {targetEvent.getZ()}
        });

        final RealMatrix rotationRealMatrix3_3 = MatrixUtils.createRealMatrix(new double[][]{
                {rotationMatrix[0], rotationMatrix[1], rotationMatrix[2]},
                {rotationMatrix[3], rotationMatrix[4], rotationMatrix[5]},
                {rotationMatrix[6], rotationMatrix[7], rotationMatrix[8]},
        });

        final RealMatrix targetEventEarthCoordinateRealMatrix3_1 = rotationRealMatrix3_3.multiply(targetEventRealMatrix3_1);

        final MotionDataCollectionEvent e = new MotionDataCollectionEvent(
                targetEvent.getTimestamp(),
                targetEvent.getUptimeNanos(),
                targetSensorType,
                targetEventEarthCoordinateRealMatrix3_1.getData()[0][0],
                targetEventEarthCoordinateRealMatrix3_1.getData()[1][0],
                targetEventEarthCoordinateRealMatrix3_1.getData()[2][0]
        );
        return e;
    }

    @Override
    public MotionDataCollectionEvent apply(MotionDataCollectionEvent event) {
        if(event.getSensorType() == SensorType.GRAVITY){
            gravity = event;
        }
        else if(event.getSensorType() == SensorType.MAGNETIC_FIELD){
            magneticFieldEvent = event;
        }

        if(event.getSensorType() == targetSensorType){
            targetEvent = event;
        }

        if(magneticFieldEvent != null
                && gravity != null
                && targetEvent != null){
            final MotionDataCollectionEvent e = createEvent();
            if(e == null){
                return null;
            }

            magneticFieldEvent = null;
            gravity = null;
            targetEvent = null;

            return e;
        }

        return null;
    }

    @Override
    public void clean() {
    }
}
