package br.com.j2.apm.event;

import android.hardware.SensorEvent;
import android.location.Location;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

import br.com.j2.apm.SensorType;

/**
 * Created by pma029 on 19/08/16.
 */
public class LocationEvent extends DataCollectionEvent{

    private double latitude;
    private double longitude;
    private Float accuracy;

    private Float bearing;
    private Double altitude;
    private Float speed;

    private String provider;

    public LocationEvent(Location location) {
        super(new Date(location.getTime()), location.getElapsedRealtimeNanos(), SensorType.GPS);

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        accuracy = location.getAccuracy();

        provider = location.getProvider();

        if(location.hasBearing()){
            bearing = location.getBearing();
        }
        if(location.hasAltitude()){
            altitude = location.getAltitude();
        }
        if(location.hasSpeed()){
            speed = location.getSpeed();
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public Float getBearing() {
        return bearing;
    }

    public Double getAltitude() {
        return altitude;
    }

    public Float getSpeed() {
        return speed;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public String getCSVHeader() {
        return "timestamp,uptimeNanos,latitude,longitude,accuracy,bearing,altitude,speed,provider";
    }

    @Override
    public void toCSV(DateFormat dateFormat, StringBuilder stringBuilder) {
        stringBuilder.append(dateFormat.format(getTimestamp())).append(",")
                .append(getUptimeNanos()).append(",")
                .append(latitude).append(",")
                .append(longitude).append(",")
                .append(Objects.toString(accuracy, "")).append(",")
                .append(Objects.toString(bearing, "")).append(",")
                .append(Objects.toString(altitude, "")).append(",")
                .append(Objects.toString(speed, "")).append(",")
                .append(Objects.toString(provider, ""));
    }

}