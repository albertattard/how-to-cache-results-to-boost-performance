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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class GenericCacheExample<K, V> {

    private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<>();

    private Future<V> createFutureIfAbsent(final K key, final Callable<V> callable) {
        Future<V> future = cache.get(key);
        if (future == null) {
            final FutureTask<V> futureTask = new FutureTask<V>(callable);
            future = cache.putIfAbsent(key, futureTask);
            if (future == null) {
                future = futureTask;
                futureTask.run();
            }
        }
        return future;
    }

    public V getValue(final K key, final Callable<V> callable) throws InterruptedException, ExecutionException {
        try {
            final Future<V> future = createFutureIfAbsent(key, callable);
            return future.get();
        } catch (final InterruptedException e) {
            cache.remove(key);
            throw e;
        } catch (final ExecutionException e) {
            cache.remove(key);
            throw e;
        } catch (final RuntimeException e) {
            cache.remove(key);
            throw e;
        }
    }

    public void setValueIfAbsent(final K key, final V value) {
        createFutureIfAbsent(key, new Callable<V>() {
            @Override
            public V call() throws Exception {
                return value;
            }
        });
    }

}
