package br.com.j2.apm.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

import java.util.Date;

import br.com.j2.apm.SensorType;
import br.com.j2.apm.event.MotionDataCollectionEvent;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EarthAxesConverterFunctionTest {
    private static final double DELTA = .0001;

    private static final float[] IDENTITY_MATRIX = new float[]{
        1, 0, 0,
        0, 1, 0,
        0, 0, 1
    };

    private long time;

    @Mock
    private EarthCoordinate earthCoordinate;

    public void configWhenForGetRotationMatrix(){
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final float[] matrix = (float[]) invocation.getArguments()[0];
                System.arraycopy(IDENTITY_MATRIX, 0, matrix, 0, IDENTITY_MATRIX.length);
                return true;
            }
        }).when(earthCoordinate).getRotationMatrix(notNull(float[].class),
                isNull(float[].class),
                notNull(float[].class),
                notNull(float[].class)
        );
    }

    public void configVerifyForGetRotationMatrix(MotionDataCollectionEvent accelerometerEvent, MotionDataCollectionEvent magneticFieldEvent){
        verify(earthCoordinate).getRotationMatrix(notNull(float[].class),
                isNull(float[].class),
                eq(new float[]{ (float) accelerometerEvent.getX(), (float) accelerometerEvent.getY(), (float) accelerometerEvent.getZ()}),
                eq(new float[]{ (float) magneticFieldEvent.getX(), (float) magneticFieldEvent.getY(), (float) magneticFieldEvent.getZ()})
        );
    }

    public void configGetRotationMatrixNeverCalled(){
        verify(earthCoordinate, never()).getRotationMatrix(any(float[].class),
                any(float[].class),
                any(float[].class),
                any(float[].class)
        );
    }

    @Test
    public void applyPreviousEventIsTargetEvent() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.GRAVITY);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.GRAVITY, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);
        configWhenForGetRotationMatrix();

        assertNull(earthAxesConverterFunction.apply(e1));
        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e2);
        assertNotNull(converted);

        configVerifyForGetRotationMatrix(e1, e2);
        assertEventsMatch(converted, e1);
    }

    @Test
    public void applyPreviousEventIsTargetEventWithDifferentSensorType() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.GYROSCOPE);
        configWhenForGetRotationMatrix();
        final MotionDataCollectionEvent e1 = createEvent(SensorType.GYROSCOPE, 0.8, -0.7, 1.2); //targetEvent
        final MotionDataCollectionEvent e2 = createEvent(SensorType.GRAVITY, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e3 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNull(earthAxesConverterFunction.apply(e2));
        final MotionDataCollectionEvent applied = earthAxesConverterFunction.apply(e3);
        assertNotNull(applied);

        configVerifyForGetRotationMatrix(e2, e3);
        assertEventsMatch(applied, e1);
    }

    @Test
    public void applyCurrentEventIsTargetEvent() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.MAGNETIC_FIELD);
        configWhenForGetRotationMatrix();
        final MotionDataCollectionEvent e1 = createEvent(SensorType.GRAVITY, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e2);
        assertNotNull(converted);

        configVerifyForGetRotationMatrix(e1, e2);
        assertEventsMatch(converted, e2);
    }

    @Test
    public void applyCurrentEventIsTargetEventWithDifferentSensorType() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.GYROSCOPE);
        configWhenForGetRotationMatrix();
        final MotionDataCollectionEvent e1 = createEvent(SensorType.GRAVITY, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);
        final MotionDataCollectionEvent e3 = createEvent(SensorType.GYROSCOPE, 0.8, -0.7, 1.2);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNull(earthAxesConverterFunction.apply(e2));
        final MotionDataCollectionEvent applied = earthAxesConverterFunction.apply(e3);
        assertNotNull(applied);

        configVerifyForGetRotationMatrix(e1, e2);
        assertEventsMatch(applied, e3);
    }

    @Test
    public void applyMissingEvent() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.GRAVITY);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.GRAVITY, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.GRAVITY, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNull(earthAxesConverterFunction.apply(e2));

        configGetRotationMatrixNeverCalled();
    }

    @Test
    public void applyFirstEventIsMissed() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.MAGNETIC_FIELD);
        configWhenForGetRotationMatrix();
        final MotionDataCollectionEvent e1 = createEvent(SensorType.MAGNETIC_FIELD, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);
        final MotionDataCollectionEvent e3 = createEvent(SensorType.GRAVITY, 1.2, -0.9, 5.0);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNull(earthAxesConverterFunction.apply(e2));
        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e3);

        assertNotNull(converted);
        assertEventsMatch(converted, e2);
        configVerifyForGetRotationMatrix(e3, e2);
    }

    @Test
    public void applyFirstEventIsMissedWithDifferentSensorType() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.LINEAR_ACCELERATION);
        configWhenForGetRotationMatrix();
        final MotionDataCollectionEvent e0 = createEvent(SensorType.LINEAR_ACCELERATION, 0.8, -1.5, 2.2);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.MAGNETIC_FIELD, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -1.3);
        final MotionDataCollectionEvent e3 = createEvent(SensorType.GRAVITY, 1.2, -0.9, 5.0);

        assertNull(earthAxesConverterFunction.apply(e0));
        assertNull(earthAxesConverterFunction.apply(e1));
        assertNull(earthAxesConverterFunction.apply(e2));

        final MotionDataCollectionEvent applied = earthAxesConverterFunction.apply(e3);
        assertNotNull(applied);

        assertEventsMatch(applied, e0);
        configVerifyForGetRotationMatrix(e3, e2);
    }

    @Test
    public void applyReturnsNotNullTwice() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.MAGNETIC_FIELD);
        configWhenForGetRotationMatrix();

        final MotionDataCollectionEvent e1 = createEvent(SensorType.MAGNETIC_FIELD, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.GRAVITY, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e1));
        assertNotNull(earthAxesConverterFunction.apply(e2));
        configVerifyForGetRotationMatrix(e2, e1);

        final MotionDataCollectionEvent e3 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -0.7);
        final MotionDataCollectionEvent e4 = createEvent(SensorType.MAGNETIC_FIELD, 0.89, -0.91, -0.456);
        final MotionDataCollectionEvent e5 = createEvent(SensorType.GRAVITY, 2.2, -3.4, -2.1);

        assertNull(earthAxesConverterFunction.apply(e3));
        assertNull(earthAxesConverterFunction.apply(e4));

        final MotionDataCollectionEvent converted = earthAxesConverterFunction.apply(e5);
        assertNotNull(converted);
        assertEventsMatch(converted, e4);
        configVerifyForGetRotationMatrix(e5, e4);
    }

    @Test
    public void applyReturnsNotNullTwiceWithDifferentSensorType() throws Exception {
        final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, SensorType.GYROSCOPE);
        configWhenForGetRotationMatrix();

        final MotionDataCollectionEvent e0 = createEvent(SensorType.GYROSCOPE, 0.23, 0.32, 0.76);
        final MotionDataCollectionEvent e1 = createEvent(SensorType.MAGNETIC_FIELD, 0.2, 0.1, 0.9);
        final MotionDataCollectionEvent e2 = createEvent(SensorType.GRAVITY, 0.5, -0.2, -1.3);

        assertNull(earthAxesConverterFunction.apply(e0));
        assertNull(earthAxesConverterFunction.apply(e1));
        assertNotNull(earthAxesConverterFunction.apply(e2));
        configVerifyForGetRotationMatrix(e2, e1);

        final MotionDataCollectionEvent e3 = createEvent(SensorType.GYROSCOPE, 0.76, 1.23, -8.2);
        final MotionDataCollectionEvent e4 = createEvent(SensorType.MAGNETIC_FIELD, 0.5, -0.2, -0.7);
        final MotionDataCollectionEvent e5 = createEvent(SensorType.MAGNETIC_FIELD, 0.89, -0.91, -0.456);
        final MotionDataCollectionEvent e6 = createEvent(SensorType.GRAVITY, 2.2, -3.4, -2.1);

        assertNull(earthAxesConverterFunction.apply(e3));
        assertNull(earthAxesConverterFunction.apply(e4));
        assertNull(earthAxesConverterFunction.apply(e5));

        final MotionDataCollectionEvent applied = earthAxesConverterFunction.apply(e6);
        assertNotNull(applied);
        assertEventsMatch(applied, e3);
        configVerifyForGetRotationMatrix(e6, e5);
    }

    private void assertEventsMatch(MotionDataCollectionEvent actual, MotionDataCollectionEvent expected){
        assertEquals(expected.getSensorType(), actual.getSensorType());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getUptimeNanos(), actual.getUptimeNanos());
        assertEquals(expected.getX(), actual.getX(), DELTA);
        assertEquals(expected.getY(), actual.getY(), DELTA);
        assertEquals(expected.getZ(), actual.getZ(), DELTA);
    }

    private MotionDataCollectionEvent createEvent(SensorType st, double x, double y, double z){
        return new MotionDataCollectionEvent(
                new Date(time += 200),
                time += 300,
                st,
                x,
                y,
                z
        );
    }
}