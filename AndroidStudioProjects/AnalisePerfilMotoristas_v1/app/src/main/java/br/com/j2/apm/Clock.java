package br.com.j2.apm;

/**
 * Created by pma029 on 12/04/16.
 */
public interface Clock {
    long currentTimeMillis();
    long nanoTime();
}
