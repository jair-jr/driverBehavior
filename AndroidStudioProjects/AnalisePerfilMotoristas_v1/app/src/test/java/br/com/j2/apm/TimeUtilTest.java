package br.com.j2.apm;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by pma029 on 03/05/16.
 */
@RunWith(Parameterized.class)
public class TimeUtilTest {

    private long elapsedTimeMillis;
    private String expectedFormat;

    public TimeUtilTest(long elapsedTimeMillis, String expectedFormat) {
        this.elapsedTimeMillis = elapsedTimeMillis;
        this.expectedFormat = expectedFormat;
    }

    @Parameterized.Parameters
    public static Collection data() {
        return Arrays.asList(
            new Object[][]{
                    {0, "00:00.000"},
                    {1, "00:00.001"},
                    {999, "00:00.999"},
                    {1000, "00:01.000"},
                    {1001, "00:01.001"},
                    {60 * 1000 - 1, "00:59.999"},
                    {60 * 1000, "01:00.000"},
                    {60 * 1000 + 1, "01:00.001"},
                    {24 * 60 * 1000 - 1, "23:59.999"},
                    {24 * 60 * 1000, "24:00.000"},
                    {24 * 60 * 1000 + 1, "24:00.001"},
            }
        );
    }

    @Test
    public void formatMinutesSecondsFromMillis(){
        Assert.assertEquals(expectedFormat, TimeUtil.formatMinutesSecondsFromMillis(elapsedTimeMillis));
    }
}
