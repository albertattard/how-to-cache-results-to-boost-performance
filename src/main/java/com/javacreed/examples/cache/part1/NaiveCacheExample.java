package com.javacreed.examples.cache.part1;

import java.util.HashMap;
import java.util.Map;

public class NaiveCacheExample {

    private final Map<Long, Long> cache = new HashMap<>();

    public NaiveCacheExample() {
        // The base case for the Fibonacci Sequence
        cache.put(0L, 1L);
        cache.put(1L, 1L);
    }

    public Long getNumber(final long index) {
        // Check if value is in cache
        if (cache.containsKey(index)) {
            return cache.get(index);
        }

        // Compute value and save it in cache
        final long value = getNumber(index - 1) + getNumber(index - 2);
        cache.put(index, value);
        return value;
    }

}
