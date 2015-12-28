Albert Einstein (<a href="http://en.wikipedia.org/wiki/Albert_Einstein" target="_blank">Wiki</a>) defines <em>insanity</em> as follows:


<blockquote cite="http://www.brainyquote.com/quotes/quotes/a/alberteins133991.html">
Insanity: doing the same thing over and over again and expecting different results
</blockquote>


In programming we can avoid <em>insanity</em> by using caching.  Caching, together with avoiding insanity, can boost performance as we do not have to compute complex algorithms again and again.  Instead we simply use the previously computed values.  Caching saves time by using more memory.  Instead of computing something again, the result obtained before is saved in memory and retrieved from memory next time required.  Large cache deposits will require more space, and since memory is a finite resource we cannot cache everything.  We should choose what we cache with care.


In this article we will see how to implement a good, thread-safe caching algorithm that can help boosting the performance.  If you are interested in Spring caching, please refer to another article called: <em>Caching Made Easy with Spring</em> (<a href="http://www.javacreed.com/caching-made-easy-with-spring/">Article</a>).


All code listed below is available at: <a href="https://github.com/javacreed/how-to-cache-results-to-boost-performance" target="_blank">https://github.com/javacreed/how-to-cache-results-to-boost-performance</a>.  Most of the examples will not contain the whole code and may omit fragments which are not relevant to the example being discussed. The readers can download or view all code from the above link.


<h2>Naive Implementation</h2>


A naive caching algorithm will take the following form


<pre>
IF value in cached THEN
  return value from cache
ELSE
  compute value
  save value in cache
  return value
END IF  
</pre>


The above algorithm is the simplest form of caching algorithm.  Let us apply the above algorithm to a simple example.  For this example we will use the Fibonacci number sequence (<a href="http://en.wikipedia.org/wiki/Fibonacci_number" target="_blank">Wiki</a>).  The Fibonacci number sequence is a good candidate for caching as the <em>n</em><sup>th</sup> number is equal to the sum of the previous two numbers in the sequence.  Therefore, if you need the <em>n</em><sup>th</sup> Fibonacci number in the sequence, you have to first compute the (<em>n</em><sup>th</sup> - 1) and (<em>n</em><sup>th</sup> - 2) Fibonacci numbers, and so on and so forth until you reach the base case.


<pre>
package com.javacreed.examples.cache.part1;

import java.util.HashMap;
import java.util.Map;

public class NaiveCacheExample {

  private Map&lt;Long, Long&gt; cache = new HashMap&lt;&gt;();

  public NaiveCacheExample() {
    <span class="comments">// The base case for the Fibonacci Sequence</span>
    cache.put(0L, 1L);
    cache.put(1L, 1L);
  }

  public Long getNumber(long index) {
    <span class="comments">// Check if value is in cache</span>
    if (cache.containsKey(index)) {
      return cache.get(index);
    }

    <span class="comments">// Compute value and save it in cache</span>
    long value = getNumber(index - 1) + getNumber(index - 2);
    cache.put(index, value);
    return value;
  }
}
</pre>


The example shown above, caches the values in a <code>Map</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/Map.html" target="_blank">Java Doc</a>), where the sequence number acts as the map index (or key).  Furthermore, it also caches the base case values rather than adds checks in the <code>getNumber()</code> method.  This approach works well for this problem but has the following shortcomings:


<ol>
<li>
<strong>This approach is not thread-safe</strong>. 

This problem can be mitigated by using a concurrent version of the <code>Map</code>, such as <code>ConcurrentMap</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentMap.html" target="_blank">Java Doc</a>).  Alternatively, you can guard the access to the map by using locks (<a href="http://www.javacreed.com/understanding-threads-monitors-and-locks/" target="_blank">Article</a>) but the other approach is preferred as it scales better.
</li>

<li>
<strong>The same value can be computed more than once</strong>.

Say that two threads need to retrieve the same Fibonacci number which is not in cache.  Using this approach, both threads will end up computing the same value.  Thus the same value ends up being computed twice.  The following image captures this problem.


<a href="http://www.javacreed.com/wp-content/uploads/2013/10/Compute-the-same-value-twice.png" class="preload" rel="prettyphoto" title="Compute the same value twice" ><img src="http://www.javacreed.com/wp-content/uploads/2013/10/Compute-the-same-value-twice.png" alt="Compute the same value twice" width="574" height="413" class="size-full wp-image-4864" /></a>


This problem not only it defeats the caching purpose, but duplicates the computational power required (or slows the end result as the machine throughput is halved) as shown in the following image.


<a href="http://www.javacreed.com/wp-content/uploads/2013/10/Use-twice-the-required-resources.png" class="preload" rel="prettyphoto" title="Use twice the required resources" ><img src="http://www.javacreed.com/wp-content/uploads/2013/10/Use-twice-the-required-resources.png" alt="Use twice the required resources" width="768" height="359" class="size-full wp-image-4865" /></a>


This problem may seem mild but it can have serious implications to both memory and CPU resources.  Note that the purpose of using caching is to reduce the computations by retrieving the result from cache.
</li>

<li>
<strong>The design cannot be used to cache several different values from other algorithms</strong>.

The caching algorithm is hard-coded to work with a single algorithm, that is, the Fibonacci sequence generator.  The method <code>getNumber()</code> managed the cache and also computes the value.  Using this approach, we cannot have one without the other.  

<a href="http://www.javacreed.com/wp-content/uploads/2013/10/Managing-Cache-and-Computing-Value.png" class="preload" rel="prettyphoto" title="Managing Cache and Computing Value" ><img src="http://www.javacreed.com/wp-content/uploads/2013/10/Managing-Cache-and-Computing-Value.png" alt="Managing Cache and Computing Value" width="848" height="621" class="size-full wp-image-4868" /></a>

Say we need to use the cache for something else, such as determining whether a large number is a prime number (<a href="http://en.wikipedia.org/wiki/Prime_number" target="_blank">Wiki</a>) or not, we need to write both the caching function and the algorithm that computes it.  Therefore, we need to rewrite the caching code in every class/problem we have and need to use caching.
</li>
</ol>


A better version of the caching algorithm can be written using <code>Callable</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Callable.html" target="_blank">Java Doc</a>) and <code>Future</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html" target="_blank">Java Doc</a>).  As already mentioned, the cached values will be stored within an instance of <code>ConcurrentMap</code>.  These three classes can mitigate the shortcomings listed above.


<ol>
<li>The <code>ConcurrentMap</code>'s <code>putIfAbsent()</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentMap.html#putIfAbsent(K, V)" target="_blank">Java Doc</a>) method will only add a value to the map if one does not exists in a thread-safe manner.  This map can be accessed by multiple threads in a safe way.</li>

<li>
The <code>Future</code>'s <code>get()</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html#get()" target="_blank">Java Doc</a>) method is guaranteed to be executed only once even if the second thread requests the value while the first thread is computing it.
</li>

<li>
Finally, the algorithm is moved into an instance of <code>Callable</code>.  Thus we can use this approach to cache anything we need.  All we need to do is to wrap the algorithm within an instance of <code>Callable</code>.  Using this approach we can use the same caching algorithm by different problems.
</li>
</ol>


Let's start building all this. 


<h2>Thread-safe Generic Cache Algorithm</h2>


Three changes are required to convert our naive implementation to a thread-safe, generic cache algorithm.  The caching class needs to be thread-safe and needs to smart enough to prevent the computation of the same value twice even is the first thread has not yet finished yet.  The second thread should still use the value being calculated by the first thread.  We also need to move the Fibonacci related code outside from the caching class.  Since our new approach has to be generic, we will use Generics (<a href="http://docs.oracle.com/javase/tutorial/java/generics/" target="_blank">Tutorial</a>) to support type checking even when used for different scenarios.


Following is the new caching algorithm.


<pre>
package com.javacreed.examples.cache.part2;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class GenericCacheExample&lt;K, V&gt; {

  private final ConcurrentMap&lt;K, Future&lt;V&gt;&gt; cache = new ConcurrentHashMap&lt;&gt;();

  private Future&lt;V&gt; createFutureIfAbsent(final K key, final Callable&lt;V&gt; callable) {
    Future&lt;V&gt; future = cache.get(key);
    if (future == null) {
      final FutureTask&lt;V&gt; futureTask = new FutureTask&lt;V&gt;(callable);
      future = cache.putIfAbsent(key, futureTask);
      if (future == null) {
        future = futureTask;
        futureTask.run();
      }
    }
    return future;
  }

  public V getValue(final K key, final Callable&lt;V&gt; callable) throws InterruptedException, ExecutionException {
    try {
      final Future&lt;V&gt; future = createFutureIfAbsent(key, callable);
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
    createFutureIfAbsent(key, new Callable&lt;V&gt;() {
      @Override
      public V call() throws Exception {
        return value;
      }
    });
  }

}
</pre>


This class is less than 55 lines of code and meets all our requirements as explained next.  Like any other problem, let's break this one into smaller pieces and understand each piece first.  Then we will put it all together.


<ol>
<li>
The class makes use of generics.  <code>K</code> is the type representing the key, <code>Long</code> in the Fibonacci example, where <code>V</code> represents type of the value that will be returned.  This is too <code>Long</code> in the Fibonacci example.
<pre>
public class GenericCacheExample<span class="highlight">&lt;K, V&gt;</span> {
</pre>
</li>

<li>
The cache is now saved in a <code>ConcurrentMap</code> instead of a <code>Map</code>.  Furthermore, the field <code>cache</code> does not hold the value but an instance of <code>Future</code>.  This is a very important change as this prevents the same value from being computed twice (even when the first value is still being computed).

<pre>
  private ConcurrentMap&lt;K, Future&lt;V&gt;&gt; cache = new ConcurrentHashMap&lt;&gt;();
</pre>

The <code>Future</code> represents the code that will compute and return the value.  There will be only one future for any given key (as we will see later on).  When thread 1 adds the <code>Future</code> instance to the cache, thread 2 will obtain the same <code>Future</code> instance and will both wait for this to finish.

<a href="http://www.javacreed.com/wp-content/uploads/2013/10/Using-Future.png" class="preload" rel="prettyphoto" title="Using Future" ><img src="http://www.javacreed.com/wp-content/uploads/2013/10/Using-Future.png" alt="Using Future" width="754" height="452" class="size-full wp-image-4870" /></a>

This solves the second problem but we still need to make sure that only one instance of <code>Future</code> is added to the <code>cache</code> field.
</li>

<li>
We need to make sure that only one <code>Future</code> is added to the <code>cache</code> irrespective of the number of threads using it.  The following code fragment is responsible from this.

<pre>
  private Future&lt;V&gt; createFutureIfAbsent(final K key, final Callable&lt;V&gt; callable) {
    Future&lt;V&gt; future = cache.get(key);
    if (future == null) {
      final FutureTask&lt;V&gt; futureTask = new FutureTask&lt;V&gt;(callable);
      future = cache.putIfAbsent(key, futureTask);
      if (future == null) {
        future = futureTask;
        futureTask.run();
      }
    }
    return future;
  }

</pre>

This code may not be as straightforward as one would hope, so we will break this further.

<ol type="a">
<li>
First we try to obtain the <code>Future</code> from the <code>cache</code>.  
<pre>
    Future&lt;V&gt; future = cache.get(key);
</pre>
</li>
<li>
If this is <code>null</code>, then we need to create a new instance of <code>Future</code> and add it to the cache.
<pre>
      final FutureTask&lt;V&gt; futureTask = new FutureTask&lt;V&gt;(callable);
      future = cache.putIfAbsent(key, futureTask);
</pre>

Here we are using the <code>putIfAbsent()</code> method of the <code>ConcurrentMap</code> which guarantees to add the given object if and only if no other entry exists for the given key.
</li>

<li>
The <code>putIfAbsent()</code> method will return the previous value attached with the given key.  If this is <code>null</code> it means that no value was attached with this key and that our <code>Future</code> instance was added to the map.
<pre>
      if (future == null) {
        future = futureTask;
        futureTask.run();
      }
</pre>

In this case we have to start the task as otherwise it would never kick off and we will stuck waiting for it forever.  
</li>
</ol>

It is imperative to only execute the <code>FutureTask</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/FutureTask.html" target="_blank">Java Doc</a>) <code>run()</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/FutureTask.html#run()" target="_blank">Java Doc</a>) method once as otherwise we will have the same value computed several times.  This is the key part in preventing the same value being computed several times.
</li>

<li>
Finally we need to retrieve the value and return it to the caller.  This is achieved through the <code>Future</code>' <code>get()</code> method.

<pre>
  public V getValue(final K key, final Callable&lt;V&gt; callable) throws InterruptedException, ExecutionException {
    try {
      final Future&lt;V&gt; future = createFutureIfAbsent(key, callable);
      <span class="highlight">return future.get();</span>
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
</pre>

The <code>get()</code> may throw an exception should an error occurs while computing the value.  In this case we need to remove this value from the <code>cache</code> and rethrow the exception.
</li>

<li>
The <code>setValueIfAbsent()</code> method allows the user to add values to the cache if these are not yet added.  This is ideal for cases such as the Fibonacci base case where the values for indices 0 and 1 are both 1.
<pre>
  public void setValueIfAbsent(final K key, final V value) {
    createFutureIfAbsent(key, new Callable&lt;V&gt;() {
      @Override
      public V call() throws Exception {
        return value;
      }
    });
  }
</pre>

Note that while this method takes a value, it must wrap it in a <code>Callable</code> and then add it to the <code>cache</code> through an instance of <code>Future</code>.
</li>
</ol>


With the algorithm explained, let's now put this into action.  In the following section we will see some examples of how to use the new generic, thread-safe, cache algorithm.


<h2>Using the new cache algorithm</h2>


Now that we understand how the caching algorithm works, it is time to turn our attention to explore how we can make good use of this algorithm.  We will analyse two different examples.  We will start with the Fibonacci implementation and see how we can easily use the caching algorithm to save us time from recomputing values that were already computed.  After that we will use the same algorithm for a fictitious task that will take a considerable amount of time to compute (say a couple of seconds).


<h3>Fibonacci Sequence Numbers</h3>


The caching algorithm requires two inputs.  The key that uniquely identifies the value in the case and a <code>Callable</code> that is able to compute the value should this be found missing in the case.  In order to use the caching algorithm with our Fibonacci problem, we need to convert our example to make use of the new caching algorithm as shown next


<pre>
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

  private final GenericCacheExample&lt;Long, Long&gt; cache = new GenericCacheExample&lt;&gt;();

  public FibonacciExample() {
    cache.setValueIfAbsent(0L, 1L);
    cache.setValueIfAbsent(1L, 1L);
  }

  public long getNumber(final long index) throws Exception {
    return cache.getValue(index, new Callable&lt;Long&gt;() {
      @Override
      public Long call() throws Exception {
        FibonacciExample.LOGGER.debug("Computing the {} Fibonacci number", index);
        return getNumber(index - 1) + getNumber(index - 2);
      }
    });
  }
}
</pre>


As you can see in the above example, the modifications required were minimal.  All caching code is encapsulated within the caching algorithm and our code simple interacts with it.  The caching algorithm is thread-safe and since all the state is saved by the caching algorithm, our class is inherently thread-safe.  Using this new approach, we can have this class (<code>FibonacciExample</code>) focusing on its business logic, that is, computing the Fibonacci sequence.


This example included a <code>main()</code> method to simplify the demo.  The above class will produce something similar to the follow.


<pre>
00:18:21.913 [main] DEBUG FibonacciExample.java:30 - Computing the 12 Fibonacci number
00:18:21.915 [main] DEBUG FibonacciExample.java:30 - Computing the 11 Fibonacci number
00:18:21.916 [main] DEBUG FibonacciExample.java:30 - Computing the 10 Fibonacci number
00:18:21.917 [main] DEBUG FibonacciExample.java:30 - Computing the 9 Fibonacci number
00:18:21.917 [main] DEBUG FibonacciExample.java:30 - Computing the 8 Fibonacci number
00:18:21.917 [main] DEBUG FibonacciExample.java:30 - Computing the 7 Fibonacci number
00:18:21.917 [main] DEBUG FibonacciExample.java:30 - Computing the 6 Fibonacci number
00:18:21.918 [main] DEBUG FibonacciExample.java:30 - Computing the 5 Fibonacci number
00:18:21.918 [main] DEBUG FibonacciExample.java:30 - Computing the 4 Fibonacci number
00:18:21.918 [main] DEBUG FibonacciExample.java:30 - Computing the 3 Fibonacci number
00:18:21.918 [main] DEBUG FibonacciExample.java:30 - Computing the 2 Fibonacci number
00:18:21.919 [main] DEBUG FibonacciExample.java:16 - The 12th Fibonacci number is: 233
</pre>


As shown in the output, each Fibonacci number is evaluated only once.  All the other times, this was retrieved from the cache.  In the following example we will see how to use the same cache algorithm in another context.


<h3>Fictitious Long Running Task</h3>


The caching algorithm is not bound with the Fibonacci sequence or any other class.  On the contrary it can be used in other cases too.  Here we have a fictitious long running task, which we would like to cache.  As shown in the following, example, our caching algorithm can do the trick.


<pre>
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

  private final GenericCacheExample&lt;String, Long&gt; cache = new GenericCacheExample&lt;&gt;();

  public long computeLongTask(final String key) throws Exception {
    return cache.getValue(key, new Callable&lt;Long&gt;() {
      @Override
      public Long call() throws Exception {
        FictitiousLongRunningTask.LOGGER.debug("Computing Fictitious Long Running Task: {}", key);
        Thread.sleep(10000); // 10 seconds
        return System.currentTimeMillis();
      }
    });
  }
}
</pre>


In this example we made use of the <code>StopWatch</code> (<a target="_blank" href="http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/util/StopWatch.html">Java Doc</a>) utility class to measure the time it takes to retrieve the items from cache once the first item is computed.


No changes were required to the caching algorithm and implementing is was quite easy.  The above code will produce something similar to the following.


<pre>
00:24:16.086 [main] DEBUG FictitiousLongRunningTask.java:36 - Computing Fictitious Long Running Task: a
00:24:26.089 [main] DEBUG FictitiousLongRunningTask.java:27 - StopWatch 'Fictitious Long Running Task': running time (millis) = 10006; [First Run] took 10005 = 100%; [Other Runs] took 1 = 0%
</pre>


As shown on the output above, once the first value is computed and saved in cache, all other retrievals happened instantly without introducing any noticeable delays.


<h2>Conclusion</h2>


Caching is very handy as it can boost the performance of an application.  But when done incorrectly can cripple an application as described in this article.  Spring provides caching as described in the article titled: <em>Caching Made Easy with Spring</em> (<a href="http://www.javacreed.com/caching-made-easy-with-spring/">Article</a>).  This simplifies caching further as you do not have to worry about the caching algorithm.  With that said, such approach has its limitations as it does not work well with recursion algorithms.
