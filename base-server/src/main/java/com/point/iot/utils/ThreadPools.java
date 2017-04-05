package com.point.iot.utils;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.ThreadFactory;

import java.util.concurrent.ThreadPoolExecutor;

import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class ThreadPools {

	private static AtomicInteger counter = new AtomicInteger(0);

	private final static int nThreads = Runtime.getRuntime()
			.availableProcessors() * 2;

	private final static Logger logger = Logger.getLogger(ThreadPools.class);

	private final static ExecutorService executors = new ThreadPoolExecutor(

	nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1000000), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable runnable) {
					int size = counter.incrementAndGet();
					logger.debug("Thread size" + size);
					return new ThreadPools.WorkThread(runnable, counter);
				}
			}, new ThreadPoolExecutor.DiscardOldestPolicy());

	public static void execute(Runnable runnable) {
		executors.execute(runnable);
	}

	static class WorkThread extends Thread {

		private Runnable target;
		private AtomicInteger counter;

		public WorkThread(Runnable runnable, AtomicInteger counter) {
			this.target = runnable;
			this.counter = counter;
		}

		@Override
		public void run() {
			try {
				target.run();
			} finally {
				counter.getAndDecrement();
			}
		}

	}

}
