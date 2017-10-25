package br.com.j2.apm.event;

import android.hardware.SensorEvent;

import java.text.DateFormat;
import java.util.Date;

import br.com.j2.apm.SensorType;

/**
 * Created by pma029 on 08/04/16.
 */
public abstract class DataCollectionEvent {
    private Date timestamp;
    private long uptimeNanos;
    private SensorType sensorType;

    public DataCollectionEvent(Date timestamp, long uptimeNanos, SensorType sensorType) {
        this.timestamp = timestamp;
        this.uptimeNanos = uptimeNanos;
        this.sensorType = sensorType;
    }

    public DataCollectionEvent(Date timestamp, SensorType sensorType, SensorEvent sensorEvent){
        this(timestamp, sensorEvent.timestamp, sensorType);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public long getUptimeNanos() {
        return uptimeNanos;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public abstract String getCSVHeader();
    public abstract void toCSV(DateFormat dateFormat, StringBuilder stringBuilder);
}
