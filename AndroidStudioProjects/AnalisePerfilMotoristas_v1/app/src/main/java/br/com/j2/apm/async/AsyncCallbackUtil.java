package br.com.j2.apm.async;

import android.util.Log;

/**
 * Created by jair on 08/05/16.
 */
public class AsyncCallbackUtil {
    private AsyncCallbackUtil(){

    }

    public static <S,E extends ErrorContext> AsyncCallback<S, E> makeQuiet(AsyncCallback<S, E> asyncCallback){
        if(asyncCallback instanceof QuietAsyncCallback){
            return asyncCallback;
        }

        return new QuietAsyncCallback<>(asyncCallback);
    }

}
