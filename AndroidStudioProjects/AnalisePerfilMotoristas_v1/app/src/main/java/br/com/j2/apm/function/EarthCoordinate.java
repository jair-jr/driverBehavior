package br.com.j2.apm.function;

/**
 * Created by pma029 on 11/05/16.
 */
public interface EarthCoordinate {
    boolean getRotationMatrix(float[] R, float[] I, float[] gravity, float[] geomagnetic);
}
