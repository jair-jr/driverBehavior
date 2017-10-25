package br.com.j2.apm;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.j2.apm.async.AsyncCallback;
import br.com.j2.apm.event.DataCollectionEvent;
import br.com.j2.apm.function.Function;

import static org.mockito.Mockito.*;

/**
 * Created by jair on 07/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionEventConsumerTest {
    private BlockingQueue<DataCollectionEvent> blockingQueue;
    private DataCollectionEventConsumer dataCollectionEventConsumer;

    @Before
    public void setup(){
        blockingQueue = new LinkedBlockingQueue<>();
        dataCollectionEventConsumer = new DataCollectionEventConsumer(blockingQueue, mock(ThreadPriority.class));
    }

    @Test
    public void success() throws InterruptedException {
        final SensorType sensorType = SensorType.ACCELEROMETER;

        final Function<DataCollectionEvent, Void> f1 = mock(Function.class);

        final DataCollectionEvent[] events = new DataCollectionEvent[3];
        for(int i = 0; i < events.length; i++){
            events[i] = mock(DataCollectionEvent.class);
            when(events[i].getSensorType()).thenReturn(sensorType);
            when(f1.apply(events[i])).thenReturn(null);
        }

        blockingQueue.addAll(Arrays.asList(events));
        dataCollectionEventConsumer.addFunction(sensorType, f1);

        final AsyncCallback<Void, DataCollectionEventConsumerErrorContext> callback = mock(AsyncCallback.class);
        dataCollectionEventConsumer.start(callback);

        Thread.sleep(300); //wait for thread to start and process the queue
        dataCollectionEventConsumer.stop();
        Thread.sleep(100); //wait for thread to stop and notify the callback

        for(DataCollectionEvent event : events){
            verify(f1).apply(event);
        }

        verify(callback).onSuccess(any(Void.class));
        verify(callback, never()).onError(any(DataCollectionEventConsumerErrorContext.class));
        verify(f1).clean();
    }

    @Test
    public void errorOnCleanFunction() throws InterruptedException {
        final SensorType sensorType = SensorType.ACCELEROMETER;

        final Function<DataCollectionEvent, Void> f1 = mock(Function.class);
        final Function<DataCollectionEvent, Void> f2 = mock(Function.class);

        final RuntimeException cleanException = new RuntimeException("clean test");
        doThrow(cleanException).when(f1).clean();

        final DataCollectionEvent[] events = new DataCollectionEvent[3];
        for(int i = 0; i < events.length; i++){
            events[i] = mock(DataCollectionEvent.class);
            when(events[i].getSensorType()).thenReturn(sensorType);
            when(f1.apply(events[i])).thenReturn(null);
            when(f2.apply(events[i])).thenReturn(null);
        }

        blockingQueue.addAll(Arrays.asList(events));
        dataCollectionEventConsumer.addFunction(sensorType, f1);
        dataCollectionEventConsumer.addFunction(sensorType, f2);

        final AsyncCallback<Void, DataCollectionEventConsumerErrorContext> callback = mock(AsyncCallback.class);
        dataCollectionEventConsumer.start(callback);

        Thread.sleep(300); //wait for thread to start and process the queue
        dataCollectionEventConsumer.stop();
        Thread.sleep(100); //wait for thread to stop and notify the callback

        for(DataCollectionEvent event : events){
            verify(f1).apply(event);
            verify(f2).apply(event);
        }

        verify(f1).clean();
        verify(f2).clean();

        verify(callback).onSuccess(any(Void.class));
        verify(callback, never()).onError(any(DataCollectionEventConsumerErrorContext.class));
    }


    @Test
    public void functionApplyThrowsException() throws InterruptedException {
        final SensorType sensorType = SensorType.ACCELEROMETER;

        final DataCollectionEvent[] events = new DataCollectionEvent[3];
        for(int i = 0; i < events.length; i++){
            events[i] = mock(DataCollectionEvent.class);
            when(events[i].getSensorType()).thenReturn(sensorType);
        }

        final RuntimeException ex = new RuntimeException("test");

        final Function<DataCollectionEvent, Void> f1 = mock(Function.class);
        when(f1.apply(events[0])).thenReturn(null);
        when(f1.apply(events[1])).thenThrow(ex); //event at index 1 triggers the error

        blockingQueue.addAll(Arrays.asList(events));
        dataCollectionEventConsumer.addFunction(sensorType, f1);

        final AsyncCallback<Void, DataCollectionEventConsumerErrorContext> callback = mock(AsyncCallback.class);
        dataCollectionEventConsumer.start(callback);

        Thread.sleep(300); //wait for thread to start and process the queue

        verify(f1).apply(events[0]);
        verify(f1).apply(events[1]);
        verify(f1, never()).apply(events[2]);


        final ArgumentCaptor<DataCollectionEventConsumerErrorContext> arg = ArgumentCaptor.forClass(DataCollectionEventConsumerErrorContext.class);
        verify(callback, never()).onSuccess(any(Void.class));
        verify(callback).onError(arg.capture());
        Assert.assertSame(f1, arg.getValue().getFunction());
        Assert.assertSame(events[1], arg.getValue().getDataCollectionEvent());
        Assert.assertSame(ex, arg.getValue().getError());

        verify(f1).clean();

        final List<DataCollectionEvent> remainingEvents = new ArrayList<>(blockingQueue);
        Assert.assertEquals(1, remainingEvents.size());
        Assert.assertSame(events[2], remainingEvents.get(0));
    }

    @Test
    public void errorWhileProcessingRemainingEventsAfterStop() throws InterruptedException {
        final SensorType sensorType = SensorType.ACCELEROMETER;

        final int sleepEventIndex = 1;
        final int errorEventIndex = 3;
        final DataCollectionEvent[] events = new DataCollectionEvent[6];
        for(int i = 0; i < events.length; i++){
            events[i] = mock(DataCollectionEvent.class);
            when(events[i].getSensorType()).thenReturn(sensorType);
        }

        final AtomicBoolean wait = new AtomicBoolean(true);
        final AtomicBoolean cleaned = new AtomicBoolean(false);

        final RuntimeException ex = new RuntimeException("test");
        final List<DataCollectionEvent> processedEvents = Collections.synchronizedList(new ArrayList<DataCollectionEvent>());
        final Function<DataCollectionEvent, Void> f1 = new Function<DataCollectionEvent, Void>() {
            @Override
            public Void apply(DataCollectionEvent event) {
                if(event == events[sleepEventIndex]){
                    while(wait.get()){}
                }
                else if(event == events[errorEventIndex]){
                    processedEvents.add(event);
                    throw ex;
                }

                processedEvents.add(event);
                return null;
            }

            @Override
            public void clean() {
                cleaned.set(true);
            }
        };

        blockingQueue.addAll(Arrays.asList(events));
        dataCollectionEventConsumer.addFunction(sensorType, f1);

        final AsyncCallback<Void, DataCollectionEventConsumerErrorContext> callback = mock(AsyncCallback.class);
        dataCollectionEventConsumer.start(callback);

        Thread.sleep(300); //wait for thread to start and process the queue

        dataCollectionEventConsumer.stop();
        wait.set(false);

        Thread.sleep(300); //wait for thread to process remaining events

        final ArgumentCaptor<DataCollectionEventConsumerErrorContext> arg = ArgumentCaptor.forClass(DataCollectionEventConsumerErrorContext.class);
        verify(callback, never()).onSuccess(any(Void.class));
        verify(callback).onError(arg.capture());
        Assert.assertSame(f1, arg.getValue().getFunction());
        Assert.assertSame(events[errorEventIndex], arg.getValue().getDataCollectionEvent());
        Assert.assertSame(ex, arg.getValue().getError());

        Assert.assertTrue(cleaned.get());

        final List<DataCollectionEvent> expectedProcessedEvents = new ArrayList<>(Arrays.asList(
                Arrays.copyOfRange(events, 0, errorEventIndex + 1)));
        Assert.assertEquals(expectedProcessedEvents, processedEvents);

        //the blockingQueue should be drained in processRemainingEvents
        Assert.assertEquals(0, blockingQueue.size());
    }

    @Test
    public void noFunctionForSensorTypeError() throws InterruptedException {
        final SensorType sensorType = SensorType.ACCELEROMETER;

        final Function<DataCollectionEvent, Void> f1 = mock(Function.class);
        final DataCollectionEvent[] events = new DataCollectionEvent[3];
        final int wrongSensorTypeEventIndex = 1;
        for(int i = 0; i < events.length; i++){
            events[i] = mock(DataCollectionEvent.class);

            if(i != wrongSensorTypeEventIndex) {
                when(events[i].getSensorType()).thenReturn(sensorType); //expected sensor type
            }
            else{
                when(events[i].getSensorType()).thenReturn(SensorType.MAGNETIC_FIELD); //unexpected sensor type
            }

            when(f1.apply(events[i])).thenReturn(null);
        }

        blockingQueue.addAll(Arrays.asList(events));
        dataCollectionEventConsumer.addFunction(sensorType, f1);

        final AsyncCallback<Void, DataCollectionEventConsumerErrorContext> callback = mock(AsyncCallback.class);
        dataCollectionEventConsumer.start(callback);

        Thread.sleep(300); //wait for thread to start and process the queue

        final List<DataCollectionEvent> remainingEvents = new ArrayList<>(blockingQueue);

        Assert.assertEquals(1, remainingEvents.size());
        Assert.assertSame(events[2], remainingEvents.get(0));

        for(int i = 0; i < events.length; i++) {
            if(i < wrongSensorTypeEventIndex){
                verify(f1).apply(events[i]);
            }
            else{
                verify(f1, never()).apply(events[i]);
            }
        }


        final ArgumentCaptor<DataCollectionEventConsumerErrorContext> arg = ArgumentCaptor.forClass(DataCollectionEventConsumerErrorContext.class);
        verify(callback, never()).onSuccess(any(Void.class));
        verify(callback).onError(arg.capture());
        Assert.assertNull(arg.getValue().getFunction());
        Assert.assertSame(events[wrongSensorTypeEventIndex], arg.getValue().getDataCollectionEvent());
        Assert.assertTrue(arg.getValue().getError() instanceof APMException);

        verify(f1).clean();
    }

}