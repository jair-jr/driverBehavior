package br.com.j2.apm.elapsedTime;

/**
 * Created by pma029 on 06/05/16.
 */
public class ElapsedTime {
    private static final long START_TIME_INIT_VALUE = Long.MAX_VALUE;
    private static final long FINISH_TIME_INIT_VALUE = 0;

    private MyAtomicLong startTime;
    private MyAtomicLong finishTime;

    public ElapsedTime(){
        startTime = new MyAtomicLong();
        finishTime = new MyAtomicLong();
        init();
    }

    public void init(){
        startTime.set(START_TIME_INIT_VALUE);
        finishTime.set(FINISH_TIME_INIT_VALUE);
    }

    public void updateTime(long time){
        startTime.lessAndSet(time, time);
        finishTime.greaterAndSet(time, time);
    }

    public long getElapsedTime(){
        long diff = finishTime.get() - startTime.get();
        if(diff < 0){
            return 0;
        }

        return diff;
    }

    public long getStartTime() {
        return startTime.get();
    }

    public long getFinishTime() {
        return finishTime.get();
    }
}
