package br.com.j2.apm;

import java.util.concurrent.TimeUnit;

public class Timer {
	private long start;
	private long stop;
	
	public void start(){
		start = System.nanoTime();
	}
	
	public long stop(){
		stop = System.nanoTime();
		return getElapsedTimeNanos();
	}
	
	public long getElapsedTimeNanos(){
		return stop - start;
	}
	
	public long getElapsedTimeSeconds(){
		return TimeUnit.NANOSECONDS.toSeconds(getElapsedTimeNanos());
	}
}
