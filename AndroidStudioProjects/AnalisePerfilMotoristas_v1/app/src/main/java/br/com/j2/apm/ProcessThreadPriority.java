package br.com.j2.apm;

import android.os.Process;

/**
 * Created by jair on 07/05/16.
 */
public class ProcessThreadPriority implements ThreadPriority {

    @Override
    public void set(int priority) {
        Process.setThreadPriority(priority);
    }
}
