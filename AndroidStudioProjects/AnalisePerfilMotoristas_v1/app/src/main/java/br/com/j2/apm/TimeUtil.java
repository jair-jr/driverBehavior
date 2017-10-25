package br.com.j2.apm;

import java.util.concurrent.TimeUnit;

/**
 * Created by pma029 on 03/05/16.
 */
public class TimeUtil {
    private static final long ONE_MINUTE_IN_SECONDS = TimeUnit.MINUTES.toSeconds(1);
    private static final long ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);

    private TimeUtil(){

    }

    public static String formatMinutesSecondsFromMillis(long tempoDecorridoMillis){
        return String.format("%02d:%02d.%03d", TimeUnit.MILLISECONDS.toMinutes(tempoDecorridoMillis),
                TimeUnit.MILLISECONDS.toSeconds(tempoDecorridoMillis) % ONE_MINUTE_IN_SECONDS,
                tempoDecorridoMillis % ONE_SECOND_IN_MILLIS);
    }

    public static long nanosToMillis(long nanos){
        return nanos / 1000000;
    }
}
