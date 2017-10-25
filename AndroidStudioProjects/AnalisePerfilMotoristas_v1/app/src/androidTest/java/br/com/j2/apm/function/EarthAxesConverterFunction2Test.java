package br.com.j2.apm.function;

import android.content.Context;
import android.hardware.SensorManager;
import android.test.AndroidTestCase;

import java.util.Date;

import br.com.j2.apm.SensorType;
import br.com.j2.apm.event.MotionDataCollectionEvent;
import br.com.j2.apm.function.EarthAxesConverterFunction;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 * Intrumentation test cases that run on the device (not on the computer JVM).
 * These are "JUnit3 style" test cases
 */
public class EarthAxesConverterFunction2Test extends AndroidTestCase {

    /*
    private long time;

    private EarthAxesConverterFunction earthAxesConverterFunction;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        earthAxesConverterFunction = new EarthAxesConverterFunction((SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        earthAxesConverterFunction = null;
    }

    public void testConvertPreviousEventIsBaseEvent() throws Exception {
        earthAxesConverterFunction.setTargetSensorType(SensorType.ACCELEROMETER);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.ACCELEROMETER, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e2);
        assertNotNull(converted);

        assertEventsMatch(converted, e1);
    }

    public void testConvertCurrentEventIsBaseEvent() throws Exception {
        earthAxesConverterFunction.setTargetSensorType(SensorType.MAGNETIC_FIELD);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.ACCELEROMETER, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e2);
        assertNotNull(converted);

        assertEventsMatch(converted, e2);
    }

    public void testConvertMissingEvent() throws Exception {
        earthAxesConverterFunction.setTargetSensorType(SensorType.ACCELEROMETER);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.ACCELEROMETER, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.ACCELEROMETER, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNull(earthAxesConverterFunction.apply(e2));
    }

    public void testConvertFirstEventIsMissed() throws Exception {
        earthAxesConverterFunction.setTargetSensorType(SensorType.MAGNETIC_FIELD);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.MAGNETIC_FIELD, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);
        final MotionDataCollectionEvent e3 = createEvent(SensorType.ACCELEROMETER, 1.2, -0.9, 5.0);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNull(earthAxesConverterFunction.apply(e2));
        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e3);

        assertNotNull(converted);
        assertEventsMatch(converted, e2);
    }

    public void testConvertAssertEventsBeforeSuccessfulConvertionAreCleaned() throws Exception {
        earthAxesConverterFunction.setTargetSensorType(SensorType.MAGNETIC_FIELD);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.MAGNETIC_FIELD, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.ACCELEROMETER, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNotNull(earthAxesConverterFunction.apply(e2));

        final MotionDataCollectionEvent e3 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -0.7);
        final MotionDataCollectionEvent e4 = createEvent(SensorType.MAGNETIC_FIELD, 0.89, -0.91, -0.456);
        final MotionDataCollectionEvent e5 = createEvent(SensorType.ACCELEROMETER, 2.2, -3.4, -2.1);

        assertNull(earthAxesConverterFunction.apply(e3));
        assertNull(earthAxesConverterFunction.apply(e4));
        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e5);

        assertNotNull(converted);
        assertEventsMatch(converted, e4);
    }

    private void assertEventsMatch(MotionDataCollectionEvent actual, MotionDataCollectionEvent expected){
        assertEquals(actual.getSensorType(), expected.getSensorType());
        assertEquals(actual.getTimestamp(), expected.getTimestamp());
        assertEquals(actual.getUptimeNanos(), expected.getUptimeNanos());
    }

    private MotionDataCollectionEvent createEvent(SensorType st, double x, double y, double z){
        final MotionDataCollectionEvent e = new MotionDataCollectionEvent(
                new Date(time += 200),
                time += 300,
                st,
                x,
                y,
                z
        );

        return e;
    }
    */
}