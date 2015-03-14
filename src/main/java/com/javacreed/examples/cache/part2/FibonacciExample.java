package com.javacreed.examples.cache.part2;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FibonacciExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(FibonacciExample.class);

    public static void main(final String[] args) throws Exception {
        final long index = 12;
        final FibonacciExample example = new FibonacciExample();
        final long fn = example.getNumber(index);
        FibonacciExample.LOGGER.debug("The {}th Fibonacci number is: {}", index, fn);
    }

    private final GenericCacheExample<Long, Long> cache = new GenericCacheExample<>();

    public FibonacciExample() {
        cache.setValueIfAbsent(0L, 1L);
        cache.setValueIfAbsent(1L, 1L);
    }

    public long getNumber(final long index) throws Exception {
        return cache.getValue(index, new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                FibonacciExample.LOGGER.debug("Computing the {} Fibonacci number", index);
                return getNumber(index - 1) + getNumber(index - 2);
            }
        });
    }
}
