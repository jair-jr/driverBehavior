package br.com.j2.apm.async;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by jair on 08/05/16.
 */
public class AsyncCallbackUtilTest {

    private AsyncCallback<Void, ErrorContext> asyncCallback;

    @Before
    public void setup(){
        asyncCallback = new AsyncCallback<Void, ErrorContext>() {
            @Override
            public void onSuccess(Void successContext) {
                throw new RuntimeException("onSuccess");
            }

            @Override
            public void onError(ErrorContext errorContext) {
                throw new RuntimeException("onError");
            }
        };
    }

    @Test
    public void makeQuiet(){
        final AsyncCallback<Void, ErrorContext> quiet = AsyncCallbackUtil.makeQuiet(asyncCallback);
        quiet.onSuccess(null);
        quiet.onError(null);
    }

    @Test
    public void makeQuietOnAlreadyQuietAsyncCallback(){
        final AsyncCallback<Void, ErrorContext> quiet = AsyncCallbackUtil.makeQuiet(asyncCallback);
        final AsyncCallback<Void, ErrorContext> quiet2 = AsyncCallbackUtil.makeQuiet(quiet);

        Assert.assertSame(quiet, quiet2);
    }

}