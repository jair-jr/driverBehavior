package br.com.j2.apm;

import br.com.j2.apm.async.ErrorContext;

/**
 * Created by jair on 08/05/16.
 */
public class TripErrorContext implements ErrorContext{
    private Trip trip;
    private DataCollectionEventConsumerErrorContext dataCollectionEventConsumerErrorContext;

    public TripErrorContext(Trip trip, DataCollectionEventConsumerErrorContext dataCollectionEventConsumerErrorContext) {
        this.trip = trip;
        this.dataCollectionEventConsumerErrorContext = dataCollectionEventConsumerErrorContext;
    }

    public Trip getTrip() {
        return trip;
    }

    @Override
    public Throwable getError() {
        return dataCollectionEventConsumerErrorContext.getError();
    }

    public DataCollectionEventConsumerErrorContext getDataCollectionEventConsumerErrorContext() {
        return dataCollectionEventConsumerErrorContext;
    }
}
