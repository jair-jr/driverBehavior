package br.com.j2.apm;

/**
 * Created by pma029 on 12/04/16.
 */
class SystemClock implements Clock{

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
