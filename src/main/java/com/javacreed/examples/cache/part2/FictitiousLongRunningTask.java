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
