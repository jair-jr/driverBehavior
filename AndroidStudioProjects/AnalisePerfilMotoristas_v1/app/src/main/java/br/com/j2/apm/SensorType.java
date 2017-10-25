package br.com.j2.apm;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.com.j2.apm.event.DataCollectionEvent;
import br.com.j2.apm.event.EarthMotionDataCollectionEvent;
import br.com.j2.apm.event.LocationEvent;
import br.com.j2.apm.event.MotionDataCollectionEvent;

/**
 * Created by pma029 on 03/05/16.
 */
public enum SensorType {
    ACCELEROMETER(Sensor.TYPE_ACCELEROMETER,
            "Acelerômetro",
            "acelerometro"),

    GYROSCOPE(Sensor.TYPE_GYROSCOPE,
            "Giroscópio",
            "giroscopio"),

    GRAVITY(Sensor.TYPE_GRAVITY,
            "Gravidade",
            "gravidade"),

    LINEAR_ACCELERATION(Sensor.TYPE_LINEAR_ACCELERATION,
            "Aceleração Linear",
            "aceleracaoLinear"),

    MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD,
            "Campo Magnético",
            "campoMagnetico"),

    ROTATION_VECTOR(Sensor.TYPE_ROTATION_VECTOR,
            "Vetor Rotação",
            "vetorRotacao"){

        @Override
        public DataCollectionEvent createDataCollectionEvent(SensorEvent sensorEvent, Date timestamp) {
            return new EarthMotionDataCollectionEvent(timestamp, this, sensorEvent);
        }
    },

    GPS(null,
            "GPS",
            "gps"){

        @Override
        public DataCollectionEvent createDataCollectionEvent(SensorEvent sensorEvent, Date timestamp) {
            throw new APMException("Sensor '" + this + "' não pode criar DataCollectionEvent a partir de sensorEvent");
        }

        @Override
        public DataCollectionEvent createDataCollectionEvent(Location location) {
            return new LocationEvent(location);
        }
    }

    ;

    private static final Map<Integer, SensorType> SENSOR_TYPES_BY_ANDROID_ID = new HashMap<>();
    static{
        for(final SensorType st : values()){
            if(st.getAndroidId() == null) {
                continue;
            }
            SENSOR_TYPES_BY_ANDROID_ID.put(st.getAndroidId(), st);
        }
    }

    private Integer androidId;
    private String name;
    private String fileName;

    SensorType(Integer androidId, String name, String fileName) {
        this.androidId = androidId;
        this.name = name;
        this.fileName = fileName;
    }

    public Integer getAndroidId() {
        return androidId;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public static SensorType getByAndroidId(int androidId){
        return SENSOR_TYPES_BY_ANDROID_ID.get(androidId);
    }

    public DataCollectionEvent createDataCollectionEvent(SensorEvent sensorEvent, Date timestamp) {
        return new MotionDataCollectionEvent(timestamp, this, sensorEvent);
    }

    public DataCollectionEvent createDataCollectionEvent(Location location){
        throw new APMException("Sensor '" + this + "' não pode criar DataCollectionEvent a partir de location");
    }
}
