package scrapper;

import scrapper.api.Api;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scrapper implements Runnable {
    private final ExecutorService executor;
    private final CountDownLatch latch;
    private final Integer timeOut;

    private final Api entity;
    private final AtomicBoolean running;


    Scrapper(ExecutorService executor, CountDownLatch latch, Integer timeOut, Api entity, AtomicBoolean running)
    {
        this.executor = executor;
        this.latch = latch;
        this.timeOut = timeOut;
        this.entity = entity;
        this.running = running;
    }

    @Override
    public void run() {
        try {
            entity.fetchData();
        } finally {
            latch.countDown();
            scheduleNextExecution();
        }
    }

    private void scheduleNextExecution() {
        try {
            if (running.get() && latch.getCount() != 0){
                TimeUnit.SECONDS.sleep(timeOut);
                executor.execute(new Scrapper(executor, latch, timeOut, entity, running));
            }
        } catch (InterruptedException |
                RejectedExecutionException e) {
            Thread.currentThread().interrupt();
        }
    }
}
