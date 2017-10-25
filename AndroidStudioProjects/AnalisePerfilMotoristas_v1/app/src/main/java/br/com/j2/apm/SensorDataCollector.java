package br.com.j2.apm;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import br.com.j2.apm.async.AsyncCallback;
import br.com.j2.apm.async.AsyncCallbackUtil;
import br.com.j2.apm.elapsedTime.ElapsedTime;
import br.com.j2.apm.elapsedTime.ElapsedTimeListener;
import br.com.j2.apm.elapsedTime.ElapsedTimeSampler;
import br.com.j2.apm.event.DataCollectionEvent;
import br.com.j2.apm.function.DataCollectionEventWriterFunction;
import br.com.j2.apm.function.EarthAxesConverterFunction;
import br.com.j2.apm.function.EarthCoordinate;
import br.com.j2.apm.function.Function;
import br.com.j2.apm.function.FunctionUtil;
import br.com.j2.apm.function.SensorManagerEarthCoordinate;

/**
 * Created by pma029 on 03/05/16.
 */
public class SensorDataCollector {

    private static final String LOG_TAG = SensorDataCollector.class.getSimpleName();


    private static final int SENSOR_SAMPLING_PERIOD_NANOS = 0;
    private static final int SENSOR_SAMPLING_PERIOD_MILLIS = SENSOR_SAMPLING_PERIOD_NANOS / 1000;

    private static final String NEXT_TRIP_ID_LABEL = "nextTripId";
    private static final long NEXT_TRIP_ID_INITIAL_VALUE = 1;

    private static final int TIMEOUT_SHUTDOWN_EXECUTOR_SERVICE = 5;

    private static final String APP_HOME_DIR = "AnalisePerfilMotorista";

    private Context context;
    private Clock clock;

    private long nextTripId;
    private File home;

    private SensorManager sensorManager;
    private LocationManager locationManager;
    private BlockingQueue<DataCollectionEvent> blockingQueue;

    private DataCollectionEventConsumer dataCollectionEventConsumer;

    private Trip currentTrip;

    private static final long ELAPSED_TIME_SAMPLE_TIME_MILLIS = 111;
    private ElapsedTime elapsedTimeNanos;
    private ElapsedTimeSampler elapsedTimeSampler;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            elapsedTimeNanos.updateTime(event.timestamp);
            final SensorType st = SensorType.getByAndroidId(event.sensor.getType());
            final DataCollectionEvent dataCollectionEvent = st.createDataCollectionEvent(event, new Date(clock.currentTimeMillis()));
            blockingQueue.add(dataCollectionEvent);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            /*
            * Nota: location.getElapsedRealtimeNanos() não retorna
            * um valor confiável, logo não pode ser usado para
            * atualizar o tempo decorrido. Quando a localização vem
            * da rede (celular ou wifi), este valor não é próximo
            * a System.nanoTime(). Entretanto, quando a localizacação
            * vem do GPS do celular, o valor é próximo a Syste.nanoTime().
             */
            //elapsedTimeNanos.updateTime(location.getElapsedRealtimeNanos());

            final DataCollectionEvent dataCollectionEvent = SensorType.GPS.createDataCollectionEvent(location);
            blockingQueue.add(dataCollectionEvent);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public SensorDataCollector(Context context, Clock clock) {
        this.context = context;
        this.clock = clock;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        home = new File(Environment.getExternalStorageDirectory(), APP_HOME_DIR);
        IOUtil.ensureMkDirs(home);

        nextTripId = getSharedPreferences().getLong(NEXT_TRIP_ID_LABEL, NEXT_TRIP_ID_INITIAL_VALUE);

        blockingQueue = new LinkedBlockingQueue<>();

        elapsedTimeNanos = new ElapsedTime();
    }

    private DataCollectionEventConsumer buildDataCollectionEventConsumer(){
        final DataCollectionEventConsumer consumer = new DataCollectionEventConsumer(blockingQueue);

        final EarthCoordinate earthCoordinate = new SensorManagerEarthCoordinate();

        for(final SensorType st : SensorType.values()){
            consumer.addFunction(st,
                    new DataCollectionEventWriterFunction<>(new File(currentTrip.getHome(), st.getFileName() + ".csv"),
                            "dd/MM/yyyy HH:mm:ss"));

            if(st == SensorType.ROTATION_VECTOR || st == SensorType.GPS){
                continue;
            }

            final EarthAxesConverterFunction earthAxesConverterFunction = new EarthAxesConverterFunction(earthCoordinate, st);
            final DataCollectionEventWriterFunction dataCollectionEventWriterFunction =
                    new DataCollectionEventWriterFunction<>(new File(currentTrip.getHome(), st.getFileName() + "_terra.csv"), "dd/MM/yyyy HH:mm:ss");
            final Function<DataCollectionEvent, Void> earthAxesConverterAndWriter = FunctionUtil.chainWhenNonNull(earthAxesConverterFunction, dataCollectionEventWriterFunction);
            consumer.addFunction(st, earthAxesConverterAndWriter);

            for(SensorType sourceType : EarthAxesConverterFunction.getSourceSensorTypes()){
                consumer.addFunction(sourceType, earthAxesConverterAndWriter);
            }
        }

        return consumer;
    }

    public Trip getCurrentTrip() {
        return currentTrip;
    }

    public boolean isTripActive(){
        return currentTrip != null;
    }

    private long nextTripId(){
        long nextId = nextTripId++;

        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(NEXT_TRIP_ID_LABEL, nextTripId);
        editor.commit();

        return nextId;
    }

    private SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences(SensorDataCollector.class.getName(), Context.MODE_PRIVATE);
    }

    private Trip createTrip(){
        final Trip v = new Trip(nextTripId(), home);
        v.setSmartphoneModel(Build.MODEL);
        v.setAndroidVersion(Build.VERSION.RELEASE);
        v.setStart(new Date(clock.currentTimeMillis()));
        v.setLocationProvider(getLocationProvider());

        return v;
    }

    private LocationProvider getLocationProvider(){
        final boolean available = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return available ? locationManager.getProvider(LocationManager.GPS_PROVIDER) : null;
    }

    public Trip startTrip(ElapsedTimeListener elapsedTimeListener, final AsyncCallback<Trip, TripErrorContext> asyncCallback) throws APMException{
        if(isTripActive()){
            throw new APMException("Já existe uma viagem ativa: " + currentTrip.getId());
        }

        if(!IOUtil.isExternalStorageWritable()){
            throw new APMException("Impossível iniciar viagem: área de escrita indisponível");
        }

        final List<Sensor> availableSensors = getAvailableSensors();

        currentTrip = createTrip();
        currentTrip.save();

        elapsedTimeNanos.init();
        elapsedTimeSampler = new ElapsedTimeSampler(ELAPSED_TIME_SAMPLE_TIME_MILLIS, elapsedTimeNanos, elapsedTimeListener);

        final AsyncCallback<Trip, TripErrorContext> quietAsyncCallback = AsyncCallbackUtil.makeQuiet(asyncCallback);

        final Trip cTrip = currentTrip;
        dataCollectionEventConsumer = buildDataCollectionEventConsumer();
        dataCollectionEventConsumer.start(new AsyncCallback<Void, DataCollectionEventConsumerErrorContext>() {
            @Override
            public void onSuccess(Void successContext) {
                quietAsyncCallback.onSuccess(cTrip);
            }

            @Override
            public void onError(DataCollectionEventConsumerErrorContext errorContext) {
                try{
                    finishTrip(errorContext.getError());
                }
                finally{
                    quietAsyncCallback.onError(new TripErrorContext(cTrip, errorContext));
                }
            }
        });

        registerSensorsListeners(availableSensors);

        elapsedTimeSampler.start();

        return currentTrip;
    }

    public void finishTrip() throws APMException {
        finishTrip(null);
    }

    private void finishTrip(Throwable error) throws APMException {
        if(!isTripActive()){
            throw new APMException("Não existe viagem ativa");
        }

        unregisterSensorsListeners();

        elapsedTimeSampler.stopAndWait();
        elapsedTimeSampler = null;

        if(error == null) {
            dataCollectionEventConsumer.stop();
        }
        dataCollectionEventConsumer = null;

        currentTrip.setFinish(new Date(clock.currentTimeMillis()));
        currentTrip.setFirstCollectionUptimeNanos(elapsedTimeNanos.getStartTime());
        currentTrip.setLastCollectionUptimeNanos(elapsedTimeNanos.getFinishTime());
        currentTrip.setElapsedTimeNanos(elapsedTimeNanos.getElapsedTime());
        currentTrip.setError(error);

        currentTrip.save();

        currentTrip = null;
    }

    private List<Sensor> getAvailableSensors() throws UnavailableSensorException{
        final List<Sensor> sensors = new ArrayList<>(SensorType.values().length);
        for(final SensorType ts : SensorType.values()){
            if(ts.getAndroidId() == null){
                continue;
            }

            final Sensor sensor = sensorManager.getDefaultSensor(ts.getAndroidId());
            if(sensor == null){
                throw new UnavailableSensorException(ts);
            }

            sensors.add(sensor);
        }

        return sensors;
    }

    private void registerSensorsListeners(List<Sensor> sensors){
        for(final Sensor sensor : sensors){
            sensorManager.registerListener(sensorEventListener, sensor, SENSOR_SAMPLING_PERIOD_NANOS);
        }

        registerLocationListener();
    }

    private void registerLocationListener() {
        if(currentTrip.getLocationProvider() == null){
            return;
        }

        try {
            locationManager.requestLocationUpdates(currentTrip.getLocationProvider().getName(),
                    SENSOR_SAMPLING_PERIOD_MILLIS,
                    0.01f,
                    locationListener);
        }
        catch(SecurityException e) {
            throw new APMException("Erro ao registrar listener de GPS", e);
        }
    }

    private void unregisterSensorsListeners(){
        unregisterLocationListener();

        sensorManager.unregisterListener(sensorEventListener);
    }

    private void unregisterLocationListener(){
        if(currentTrip.getLocationProvider() == null){
            return;
        }

        try {
            locationManager.removeUpdates(locationListener);
        } catch (SecurityException e) {
            throw new APMException("Erro ao desregistrar listener de GPS", e);
        }
    }


    public void destroy() {
        if(currentTrip != null){
            finishTrip();
        }

        sensorManager = null;
        locationManager = null;
        blockingQueue = null;
    }

}