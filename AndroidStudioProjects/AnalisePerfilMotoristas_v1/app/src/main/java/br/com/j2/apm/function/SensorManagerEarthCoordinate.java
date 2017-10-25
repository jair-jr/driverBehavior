package br.com.j2.apm.function;

import android.hardware.SensorManager;

/**
 * Created by pma029 on 11/05/16.
 */
public class SensorManagerEarthCoordinate implements EarthCoordinate {

    @Override
    public boolean getRotationMatrix(float[] R, float[] I, float[] gravity, float[] geomagnetic) {
        return SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
    }
}
