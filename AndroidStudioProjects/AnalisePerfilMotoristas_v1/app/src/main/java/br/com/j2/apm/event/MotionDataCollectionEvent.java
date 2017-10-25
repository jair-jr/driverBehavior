package br.com.j2.apm.event;


import android.hardware.SensorEvent;

import java.text.DateFormat;
import java.util.Date;

import br.com.j2.apm.SensorType;

/**
 * Created by pma029 on 08/04/16.
 */
public class MotionDataCollectionEvent extends DataCollectionEvent {
    private double x;
    private double y;
    private double z;

    public MotionDataCollectionEvent(Date timestamp, long uptimeNanos, SensorType sensorType, double x, double y, double z) {
        super(timestamp, uptimeNanos, sensorType);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MotionDataCollectionEvent(Date timestamp, SensorType sensorType, SensorEvent sensorEvent) {
        super(timestamp, sensorType, sensorEvent);
        this.x = sensorEvent.values[0];
        this.y = sensorEvent.values[1];
        this.z = sensorEvent.values[2];
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String getCSVHeader() {
        return "timestamp,uptimeNanos,x,y,z";
    }

    @Override
    public void toCSV(DateFormat dateFormat, StringBuilder stringBuilder) {
        stringBuilder.append(dateFormat.format(getTimestamp())).append(",")
                .append(getUptimeNanos()).append(",")
                .append(x).append(",")
                .append(y).append(",")
                .append(z);
    }
}
