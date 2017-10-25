package br.com.j2.apm.elapsedTime;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by pma029 on 05/05/16.
 * from: http://gridgain.blogspot.com.br/2011/06/better-atomiclong-and-atomicinteger-in.html
 */

public class MyAtomicLong extends AtomicLong {

    public MyAtomicLong(long initialValue) {
        super(initialValue);
    }

    public MyAtomicLong() {
        super();
    }

    public boolean greaterAndSet(long check, long update) {
        while (true) {
            long cur = get();

            if (check > cur) {
                if (compareAndSet(cur, update))
                    return true;
            }
            else
                return false;
        }
    }

    public boolean greaterEqualsAndSet(long check, long update) {
        while (true) {
            long cur = get();

            if (check >= cur) {
                if (compareAndSet(cur, update))
                    return true;
            }
            else
                return false;
        }
    }

    public boolean lessAndSet(long check, long update) {
        while (true) {
            long cur = get();

            if (check < cur) {
                if (compareAndSet(cur, update))
                    return true;
            }
            else
                return false;
        }
    }

    public boolean lessEqualsAndSet(long check, long update) {
        while (true) {
            long cur = get();

            if (check <= cur) {
                if (compareAndSet(cur, update))
                    return true;
            }
            else
                return false;
        }
    }
}
