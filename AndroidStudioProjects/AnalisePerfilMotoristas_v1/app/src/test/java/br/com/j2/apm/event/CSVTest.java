package br.com.j2.apm.event;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;

import static br.com.j2.apm.TestUtil.*;

/**
 * Created by pma029 on 05/05/16.
 */
public class CSVTest {

    @Test
    public void toCSV() throws ParseException {
        final MotionDataCollectionEvent event = createMotionEvent(
                "10/11/2016 10:12:23",
                1000,
                1,
                2,
                3
        );

        final StringBuilder sb = new StringBuilder();
        event.toCSV(DATE_FORMAT, sb);

        final String expectedCSV = "10/11/2016 10:12:23" +
                ",1000" +
                ",1.0,2.0,3.0";

        Assert.assertEquals(expectedCSV, sb.toString());
    }

    @Test
    public void toCSVHeader() throws ParseException {
        final MotionDataCollectionEvent event = createMotionEvent(
                "10/11/2016 10:12:23",
                1000,
                1,
                2,
                3
        );

        final String expectedCSVHeader = "timestamp" +
                ",uptimeNanos" +
                ",x" +
                ",y" +
                ",z";

        Assert.assertEquals(expectedCSVHeader, event.getCSVHeader());
    }

}
