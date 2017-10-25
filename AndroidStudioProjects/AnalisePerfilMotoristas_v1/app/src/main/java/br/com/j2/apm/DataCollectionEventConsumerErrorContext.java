package br.com.j2.apm;

import br.com.j2.apm.async.ErrorContext;
import br.com.j2.apm.event.DataCollectionEvent;
import br.com.j2.apm.function.Function;

/**
 * Created by jair on 08/05/16.
 */
public class DataCollectionEventConsumerErrorContext implements ErrorContext{
    private Function<DataCollectionEvent, ?> function;
    private DataCollectionEvent dataCollectionEvent;
    private Throwable error;

    public DataCollectionEventConsumerErrorContext(Function<DataCollectionEvent, ?> function, DataCollectionEvent dataCollectionEvent, Throwable error) {
        this.function = function;
        this.dataCollectionEvent = dataCollectionEvent;
        this.error = error;
    }

    public Function<DataCollectionEvent, ?> getFunction() {
        return function;
    }

    public DataCollectionEvent getDataCollectionEvent() {
        return dataCollectionEvent;
    }

    public Throwable getError() {
        return error;
    }
}
