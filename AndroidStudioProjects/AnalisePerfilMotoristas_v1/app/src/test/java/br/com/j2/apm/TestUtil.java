package br.com.j2.apm;

import junit.framework.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.j2.apm.event.DataCollectionEvent;
import br.com.j2.apm.event.MotionDataCollectionEvent;

/**
 * Created by pma029 on 12/04/16.
 */
public class TestUtil {
    public static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

    private TestUtil(){

    }

    public static MotionDataCollectionEvent createMotionEvent(String timestamp, long uptimeNanos,
                                                              double x, double y, double z) throws ParseException {
        return createMotionEvent(stringToDate(timestamp), uptimeNanos, x, y, z);
    }

    public static MotionDataCollectionEvent createMotionEvent(Date timestamp, long uptimeNanos,
                                                       double x, double y, double z) {
        final MotionDataCollectionEvent e = new MotionDataCollectionEvent(
                timestamp,
                uptimeNanos,
                SensorType.ACCELEROMETER,
                x,
                y,
                z
        );

        return e;
    }

    public static Date stringToDate(String dateStr) throws ParseException {
        return DATE_FORMAT.parse(dateStr);
    }

}