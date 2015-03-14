package com.javacreed.examples.cache.part2;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class FictitiousLongRunningTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(FictitiousLongRunningTask.class);

    public static void main(final String[] args) throws Exception {
        final FictitiousLongRunningTask task = new FictitiousLongRunningTask();

        final StopWatch stopWatch = new StopWatch("Fictitious Long Running Task");
        stopWatch.start("First Run");
        task.computeLongTask("a");
        stopWatch.stop();

        stopWatch.start("Other Runs");
        for (int i = 0; i < 100; i++) {
            task.computeLongTask("a");
        }
        stopWatch.stop();

        FictitiousLongRunningTask.LOGGER.debug("{}", stopWatch);
    }

    private final GenericCacheExample<String, Long> cache = new GenericCacheExample<>();

    public long computeLongTask(final String key) throws Exception {
        return cache.getValue(key, new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                FictitiousLongRunningTask.LOGGER.debug("Computing Fictitious Long Running Task: {}", key);
                Thread.sleep(10000); // 10 seconds
                return System.currentTimeMillis();
            }
        });
    }

}
