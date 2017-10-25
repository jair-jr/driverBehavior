package br.com.j2.apm;

import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pma029 on 03/05/16.
 */
public class Trip {
    private static final String ARQUIVO_VIAGEM = "viagem.json";

    private long id;

    private Date start;
    private Date finish;

    private long firstCollectionUptimeNanos;
    private long lastCollectionUptimeNanos;
    private long elapsedTimeNanos;

    private LocationProvider locationProvider;

    private Throwable error;

    private String smartphoneModel;
    private String androidVersion;

    private File home;

    public Trip(long id, File appHome) {
        this.id = id;
        home = new File(appHome, String.valueOf(id));
        IOUtil.ensureMkDirs(home);
    }

    public long getId() {
        return id;
    }

    public File getHome() {
        return home;
    }

    public Date getStart() {
        return start;
    }
    public void setStart(Date start) {
        this.start = start;
    }

    public Date getFinish() {
        return finish;
    }
    public void setFinish(Date finish) {
        this.finish = finish;
    }

    public String getSmartphoneModel() {
        return smartphoneModel;
    }
    public void setSmartphoneModel(String smartphoneModel) {
        this.smartphoneModel = smartphoneModel;
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }
    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }
    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public long getFirstCollectionUptimeNanos() {
        return firstCollectionUptimeNanos;
    }
    public void setFirstCollectionUptimeNanos(long firstCollectionUptimeNanos) {
        this.firstCollectionUptimeNanos = firstCollectionUptimeNanos;
    }

    public long getLastCollectionUptimeNanos() {
        return lastCollectionUptimeNanos;
    }
    public void setLastCollectionUptimeNanos(long lastCollectionUptimeNanos) {
        this.lastCollectionUptimeNanos = lastCollectionUptimeNanos;
    }

    public long getElapsedTimeNanos() {
        return elapsedTimeNanos;
    }
    public void setElapsedTimeNanos(long elapsedTimeNanos) {
        this.elapsedTimeNanos = elapsedTimeNanos;
    }

    public Throwable getError() {
        return error;
    }
    public void setError(Throwable error) {
        this.error = error;
    }

    private String getLocationProviderAccuracyAsString(){
        switch(locationProvider.getAccuracy()){
            case Criteria.ACCURACY_FINE: return "ACCURACY_FINE";
            case Criteria.ACCURACY_COARSE: return "ACCURACY_COARSE";
            default: return String.valueOf(locationProvider.getAccuracy());
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

        String errorStackTrace = null;
        if(error != null){
            final StringWriter sw = new StringWriter();
            try(final PrintWriter pw = new PrintWriter(sw)) {
                error.printStackTrace(pw);
                errorStackTrace = sw.getBuffer().toString();
            }
        }

        final Object jsonLocationProvider;
        if(locationProvider != null){
            final JSONObject locationProviderAsJSON = new JSONObject();
            locationProviderAsJSON.put("name", locationProvider.getName());
            locationProviderAsJSON.put("powerRequirement", locationProvider.getPowerRequirement());
            locationProviderAsJSON.put("accuracy", locationProvider.getAccuracy());
            locationProviderAsJSON.put("requiresCell", locationProvider.requiresCell());
            locationProviderAsJSON.put("requiresNetwork", locationProvider.requiresNetwork());
            locationProviderAsJSON.put("requiresSatellite", locationProvider.requiresSatellite());
            locationProviderAsJSON.put("supportsAltitude", locationProvider.supportsAltitude());
            locationProviderAsJSON.put("supportsBearing", locationProvider.supportsBearing());
            locationProviderAsJSON.put("supportsSpeed", locationProvider.supportsSpeed());
            jsonLocationProvider = locationProviderAsJSON;
        }
        else{
            jsonLocationProvider = "unavailable";
        }

        return new JSONObject()
                .put("start", getStart() != null ? sdf.format(getStart()) : null)
                .put("finish", getFinish() != null ? sdf.format(getFinish()) : null)
                .put("firstCollectionUptimeNanos", firstCollectionUptimeNanos)
                .put("lastCollectionUptimeNanos", lastCollectionUptimeNanos)
                .put("elapsedTimeNanos", elapsedTimeNanos)
                .put("smartphoneModel", getSmartphoneModel())
                .put("androidVersion", getAndroidVersion())
                .put("locationProvider", jsonLocationProvider)
                .put("error", errorStackTrace);
    }

    public void save(){
        try(final Writer w = IOUtil.createWriter(new File(home, ARQUIVO_VIAGEM))){
            w.write(toJSONObject().toString(4));
        }
        catch (IOException | JSONException e) {
            throw new APMException(e);
        }
    }

}
