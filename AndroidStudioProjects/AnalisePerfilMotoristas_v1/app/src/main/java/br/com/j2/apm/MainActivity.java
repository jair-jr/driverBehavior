package br.com.j2.apm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import br.com.j2.apm.async.AsyncCallback;
import br.com.j2.apm.elapsedTime.ElapsedTimeListener;

public class MainActivity extends AppCompatActivity {
    private Button startFinishTripButton;
    private TextView timerTextView;
    private TextView tripStatusTextView;
    private TextView warningTextView;

    private SensorDataCollector sensorDataCollector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startFinishTripButton = (Button) findViewById(R.id.startFinishTripButton);
        timerTextView = (TextView) findViewById(R.id.timerTextView);
        tripStatusTextView = (TextView) findViewById(R.id.tripStatusTextView);
        warningTextView = (TextView) findViewById(R.id.warningTextView);

        startFinishTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sensorDataCollector.isTripActive()){
                    finishTrip();
                }
                else{
                    startTrip();
                }
            }
        });

        showVersion();
    }

    private void updateTripStatus() {
        if(sensorDataCollector == null){
            return;
        }

        final Trip currentTrip = sensorDataCollector.getCurrentTrip();

        if(currentTrip != null){
            startFinishTripButton.setText(getString(R.string.finishTrip));
            tripStatusTextView.setText(getString(R.string.existingTripStatus, sensorDataCollector.getCurrentTrip().getId()));
            final String gpsWarning = currentTrip.getLocationProvider() == null ? getString(R.string.unavailableGPSWarning) : "";
            warningTextView.setText(gpsWarning);
        }
        else{
            startFinishTripButton.setText(getText(R.string.startTrip));
            tripStatusTextView.setText(getString(R.string.nonexistentTripStatus));
            warningTextView.setText("");
            updateTimerTextView(0);
        }
    }

    private void updateTimerTextView(long elapsedTimeMillis){
        timerTextView.setText(TimeUtil.formatMinutesSecondsFromMillis(elapsedTimeMillis));
    }

    private void finishTrip() {
        sensorDataCollector.finishTrip();
    }

    private void startTrip() {
        try {
            final ElapsedTimeListener elapsedTimeListener = new ElapsedTimeListener() {
                @Override
                public void elapsedTimeUpdated(final long elapsedTimeNanos) {
                    final long elapsedMillis = TimeUtil.nanosToMillis(elapsedTimeNanos);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTimerTextView(elapsedMillis);
                        }
                    });
                }
            };

            final AsyncCallback<Trip, TripErrorContext> asyncCallback = new AsyncCallback<Trip, TripErrorContext>() {
                @Override
                public void onSuccess(final Trip trip) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getString(R.string.tripFinishedOk, trip.getId()), Toast.LENGTH_LONG).show();
                            updateTripStatus();
                        }
                    });
                }

                @Override
                public void onError(final TripErrorContext errorContext) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTripErrorDialog(errorContext);

                            updateTripStatus();
                        }
                    });
                }
            };

            final Trip trip = sensorDataCollector.startTrip(elapsedTimeListener, asyncCallback);
            updateTripStatus();
            Toast.makeText(this, getString(R.string.tripStarted, trip.getId()),Toast.LENGTH_LONG).show();
        }
        catch(UnavailableSensorException e){
            showUnavailableSensorDialog(e.getSensorType());
        }
    }

    private void showTripErrorDialog(TripErrorContext errorContext){
        final DataCollectionEventConsumerErrorContext consumerErrorContext = errorContext.getDataCollectionEventConsumerErrorContext();
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.unavailableSensorDialogTitle))
                .setMessage(getString(R.string.tripFinishedError,
                        errorContext.getTrip().getId(),
                        errorContext.getError().getMessage(),
                        consumerErrorContext.getFunction() != null ? consumerErrorContext.getFunction().getClass().getName() : null,
                        consumerErrorContext.getDataCollectionEvent()))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .create()
                .show();

    }
    private void showUnavailableSensorDialog(SensorType sensorType) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.unavailableSensorDialogTitle))
                .setMessage(getString(R.string.unavailableSensorDialogMessage,
                        sensorType.getName(),
                        getString(R.string.unavailableSensorDialogPositiveButton)))
                .setPositiveButton(getString(R.string.unavailableSensorDialogPositiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startTrip();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .create()
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(sensorDataCollector.isTripActive()) {
            finishTrip();
        }
        sensorDataCollector.destroy();
        sensorDataCollector = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorDataCollector = new SensorDataCollector(getApplicationContext(), new SystemClock());

        updateTripStatus();
    }

    private void showVersion() {
        try {
            final PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            setTitle(getTitle() + " v" + pi.versionName);
        }
        catch(PackageManager.NameNotFoundException e){
            throw new APMException(e);
        }
    }

}
