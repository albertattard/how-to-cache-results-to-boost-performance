/*
 * #%L
 * How to Cache Results to Boost Performance
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2015 Java Creed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
