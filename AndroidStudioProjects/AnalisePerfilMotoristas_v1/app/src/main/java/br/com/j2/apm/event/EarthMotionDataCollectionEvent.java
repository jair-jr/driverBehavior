package br.com.j2.apm.event;

import android.hardware.SensorEvent;

import java.text.DateFormat;
import java.util.Date;

import br.com.j2.apm.SensorType;

/**
 * Created by pma029 on 05/05/16.
 */
public class EarthMotionDataCollectionEvent extends DataCollectionEvent{

    private double xComponent;
    private double yComponent;
    private double zComponent;
    private double scalarComponent;

    public EarthMotionDataCollectionEvent(Date timestamp, long uptimeNanos, SensorType sensorType,
                                          double xComponent, double yComponent, double zComponent, double scalarComponent) {
        super(timestamp, uptimeNanos, sensorType);

        this.xComponent = xComponent;
        this.yComponent = yComponent;
        this.zComponent = zComponent;
        this.scalarComponent = scalarComponent;
    }

    public EarthMotionDataCollectionEvent(Date timestamp, SensorType sensorType, SensorEvent sensorEvent) {
        super(timestamp, sensorType, sensorEvent);
        this.xComponent = sensorEvent.values[0];
        this.yComponent = sensorEvent.values[1];
        this.zComponent = sensorEvent.values[2];
        this.scalarComponent = sensorEvent.values[3];
    }

    public double getxComponent() {
        return xComponent;
    }

    public double getyComponent() {
        return yComponent;
    }

    public double getzComponent() {
        return zComponent;
    }

    public double getScalarComponent() {
        return scalarComponent;
    }

    @Override
    public String getCSVHeader() {
        return "timestamp,uptimeNanos,componenteX,componenteY,componenteZ,componenteEscalar";
    }

    @Override
    public void toCSV(DateFormat dateFormat, StringBuilder stringBuilder) {
        stringBuilder.append(dateFormat.format(getTimestamp())).append(",")
                .append(getUptimeNanos()).append(",")
                .append(xComponent).append(",")
                .append(yComponent).append(",")
                .append(zComponent).append(",")
                .append(scalarComponent);
    }
}
