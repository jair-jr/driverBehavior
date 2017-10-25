package br.com.j2.apm;

/**
 * Created by pma029 on 04/05/16.
 */
public class UnavailableSensorException extends APMException{
    private SensorType sensorType;

    public UnavailableSensorException(SensorType sensorType) {
        this("Sensor indisponível: " + sensorType.getName());
        this.sensorType = sensorType;
    }

    public UnavailableSensorException(String detailMessage) {
        super(detailMessage);
    }

    public UnavailableSensorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnavailableSensorException(Throwable throwable) {
        super(throwable);
    }

    public SensorType getSensorType() {
        return sensorType;
    }
}
