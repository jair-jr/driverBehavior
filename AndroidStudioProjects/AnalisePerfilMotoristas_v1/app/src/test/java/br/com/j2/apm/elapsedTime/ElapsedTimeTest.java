package br.com.j2.apm.elapsedTime;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import br.com.j2.apm.elapsedTime.ElapsedTime;

/**
 * Created by pma029 on 06/05/16.
 */
@RunWith(Parameterized.class)
public class ElapsedTimeTest {

    private long[] times;
    private long expectedStartTime;
    private long expectedFinishTime;
    private long expectedElapsedTime;

    public ElapsedTimeTest(long[] times,
                           long expectedStartTime,
                           long expectedFinishTime,
                           long expectedElapsedTime) {
        this.times = times;
        this.expectedStartTime = expectedStartTime;
        this.expectedFinishTime = expectedFinishTime;
        this.expectedElapsedTime = expectedElapsedTime;
    }

    @Parameterized.Parameters
    public static Collection data() {
        return Arrays.asList(
                new Object[][]{
                        {new long[]{300, 324, 100, 200, 500, 50}, 50, 500, 450},
                        {new long[]{}, Long.MAX_VALUE, 0, 0},
                        {new long[]{10}, 10, 10, 0}
                }
        );
    }

    @Test
    public void testElapsedTime() throws InterruptedException {
        final ElapsedTime tet = new ElapsedTime();

        final Thread[] threads = new Thread[times.length];
        for(int i = 0; i < times.length; i++){
            final int dummyI = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    tet.updateTime(times[dummyI]);
                }
            });
            threads[i].start();
        }

        for(final Thread thread : threads){
            thread.join();
        }

        Assert.assertEquals(expectedStartTime, tet.getStartTime());
        Assert.assertEquals(expectedFinishTime, tet.getFinishTime());
        Assert.assertEquals(expectedElapsedTime, tet.getElapsedTime());
    }
}
