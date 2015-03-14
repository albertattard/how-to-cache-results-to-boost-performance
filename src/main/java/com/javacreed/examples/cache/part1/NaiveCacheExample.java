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
