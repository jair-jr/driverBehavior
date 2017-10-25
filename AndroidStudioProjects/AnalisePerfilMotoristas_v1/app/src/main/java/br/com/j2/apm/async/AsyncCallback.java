package br.com.j2.apm.async;

/**
 * Created by jair on 08/05/16.
 */
public interface AsyncCallback<S,E extends ErrorContext> {
    void onSuccess(S successContext);
    void onError(E errorContext);
}
