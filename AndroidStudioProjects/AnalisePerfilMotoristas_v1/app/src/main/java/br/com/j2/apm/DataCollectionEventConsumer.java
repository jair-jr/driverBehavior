package br.com.j2.apm;

import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import br.com.j2.apm.async.AsyncCallback;
import br.com.j2.apm.async.AsyncCallbackUtil;
import br.com.j2.apm.event.DataCollectionEvent;
import br.com.j2.apm.function.Function;

/**
 * Created by pma029 on 11/04/16.
 */
public class DataCollectionEventConsumer {
    private static final String LOG_TAG = DataCollectionEventConsumer.class.getSimpleName();

    private final BlockingQueue<DataCollectionEvent> blockingQueue;

    private final Map<SensorType, List<Function<DataCollectionEvent, ?>>> functionsBySensorType;

    private volatile Thread executionThread;

    private ThreadPriority threadPriority;

    /**
     * Constructor used only in tests
     */
    DataCollectionEventConsumer(final BlockingQueue<DataCollectionEvent> blockingQueue, ThreadPriority threadPriority){
        this.blockingQueue = blockingQueue;
        this.threadPriority = threadPriority;
        this.functionsBySensorType = new HashMap<>();
    }

    public DataCollectionEventConsumer(final BlockingQueue<DataCollectionEvent> blockingQueue){
        this(blockingQueue, new ProcessThreadPriority());
    }

    public void addFunction(SensorType sensorType, Function<DataCollectionEvent, ?> function){
        List<Function<DataCollectionEvent, ?>> fs = functionsBySensorType.get(sensorType);
        if(fs == null){
            fs = new ArrayList<>();
            functionsBySensorType.put(sensorType, fs);
        }

        fs.add(function);
    }

    public void start(final AsyncCallback<Void, DataCollectionEventConsumerErrorContext> asyncCallback) {
        Thread thread = executionThread;
        if(thread != null){
            throw new APMException("DataCollectionEventConsumer já está executando");
        }

        final AsyncCallback<Void, DataCollectionEventConsumerErrorContext> quietAsyncCallback = AsyncCallbackUtil.makeQuiet(asyncCallback);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadPriority.set(Process.THREAD_PRIORITY_BACKGROUND);
                try{
                    do {
                        applyFunctions(blockingQueue.take(), quietAsyncCallback);
                    }
                    while(true);
                }
                catch(RuntimeException e){
                    /*
                        applyFunctions já fez o tratamento de exceções.
                        Precisamos apenas sair
                        do laço e não propagar a exceção para a Thread.
                     */
                }
                catch(InterruptedException e){
                    Log.d(LOG_TAG, "Event consumer interrupted (queue = " + blockingQueue.size() + ")");
                    try {
                        processRemainingEvents(quietAsyncCallback);
                        quietAsyncCallback.onSuccess(null);
                    }
                    catch(RuntimeException ex){
                        /*
                            applyFunctions já fez o tratamento de exceções.
                            Nada a fazer a não ser impedir
                            que a exceção seja propagada para a Thread.
                         */
                    }
                }
                finally{
                    cleanFunctions();
                    executionThread = null;
                }
            }
        });

        thread.start();
        executionThread = thread;
    }

    private void applyFunctions(final DataCollectionEvent event, AsyncCallback<Void, DataCollectionEventConsumerErrorContext> asyncCallback){
        Function<DataCollectionEvent, ?> function = null;
        try {
            final List<Function<DataCollectionEvent, ?>> functions = functionsBySensorType.get(event.getSensorType());
            if(functions == null){
                throw new APMException("No function for sensor type: " + event.getSensorType());
            }

            for(final Function<DataCollectionEvent, ?> f : functions){
                function = f;
                f.apply(event);
            }
        }
        catch(RuntimeException e){
            Log.e(LOG_TAG, "Error while executing function: " + function + "; DataCollectionEvent: " + event, e);
            asyncCallback.onError(new DataCollectionEventConsumerErrorContext(function, event, e));
            throw e;
        }
    }

    private void processRemainingEvents(AsyncCallback<Void, DataCollectionEventConsumerErrorContext> asyncCallback){
        final List<DataCollectionEvent> remainingEvents = new ArrayList<>(blockingQueue.size());
        blockingQueue.drainTo(remainingEvents);
        for (final DataCollectionEvent event : remainingEvents) {
            applyFunctions(event, asyncCallback);
        }
        Log.d(LOG_TAG, "Remaining events processed (queue = " + blockingQueue.size() + ")");
    }

    private void cleanFunctions(){
        for(final List<Function<DataCollectionEvent, ?>> functions : functionsBySensorType.values()){
            for(final Function<DataCollectionEvent, ?> f : functions){
                try{
                    f.clean();
                }
                catch(RuntimeException e){
                    Log.e(LOG_TAG, "Error while cleaning function: " + f, e);
                }
            }
        }
        Log.d(LOG_TAG, "Functions cleaned (queue = " + blockingQueue.size() + ")");
    }

    public void stop() throws APMException {
        final Thread thread = executionThread;
        if(thread == null){
            throw new APMException("DataCollectionEventConsumer não está executando");
        }

        thread.interrupt();
    }

}