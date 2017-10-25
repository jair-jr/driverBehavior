package br.com.j2.apm.async;

import android.util.Log;

/**
 * Created by jair on 08/05/16.
 */
final class QuietAsyncCallback<S, E extends ErrorContext> implements AsyncCallback<S, E> {

    private AsyncCallback<S, E> originalAsyncCallback;

    QuietAsyncCallback(AsyncCallback<S, E> originalAsyncCallback) {
        this.originalAsyncCallback = originalAsyncCallback;
    }

    @Override
    public void onSuccess(S successContext) {
        try{
            originalAsyncCallback.onSuccess(successContext);
        }
        catch(Throwable e){
            Log.e(originalAsyncCallback.getClass().getSimpleName(), "Error in AsyncCallback onSuccess implementation: " + originalAsyncCallback, e);
        }
    }

    @Override
    public void onError(E errorContext) {
        try{
            originalAsyncCallback.onError(errorContext);
        }
        catch(Throwable e){
            Log.e(originalAsyncCallback.getClass().getSimpleName(), "Error in AsyncCallback onError implementation: " + originalAsyncCallback, e);
        }
    }

}
