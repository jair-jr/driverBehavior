package br.com.j2.apm.elapsedTime;

import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

import br.com.j2.apm.APMException;

/**
 * Created by pma029 on 06/05/16.
 */
public class ElapsedTimeSampler {
    private static final String LOG_TAG = ElapsedTimeSampler.class.getSimpleName();

    private long sampleTimeMillis;
    private ElapsedTime elapsedTime;
    private ElapsedTimeListener listener;

    private volatile Thread samplerThread;

    public ElapsedTimeSampler(long sampleTimeMillis, ElapsedTime elapsedTime, ElapsedTimeListener listener) {
        this.sampleTimeMillis = sampleTimeMillis;
        this.elapsedTime = elapsedTime;
        this.listener = listener;
    }

    public void start(){
        Thread thread = samplerThread;
        if(thread != null){
            throw new APMException("ElapsedTimeSampler already started");
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        try {
                            listener.elapsedTimeUpdated(elapsedTime.getElapsedTime());
                        }
                        catch(RuntimeException e){
                            Log.d(LOG_TAG, "Exception on listener elapsedTimeUpdated. ElapsedTimeSampler will keep on running", e);
                        }
                        Thread.sleep(sampleTimeMillis);
                    }
                }
                catch(InterruptedException e){
                    Log.d(LOG_TAG, "samplerThread interrupted");
                }
                finally{
                    samplerThread = null;
                }
            }
        });

        thread.setDaemon(true);
        thread.start();

        samplerThread = thread;
    }

    public void stopAndWait(){
        final Thread thread = samplerThread;
        if(thread == null){
            throw new APMException("ElapsedTimeSampler was not started");
        }

        thread.interrupt();

        try {
            thread.join();
        }
        catch(InterruptedException e){
            throw new APMException("Thread inesperadamente interrompida ao esperar parada de ElapsedTimeSampler");
        }
    }
}
