# Java 21 Virtual Threads Demo

This repository demonstrates the benefits of Java 21 virtual threads, in terms 
of performance (in comparison to platform threads) and tracing/debugging (in 
comparison to non blocking with callbacks).

## Design

### Application

In this repository, the `RemoteService` Interface contains the method 
signatures that will be tested. `sendRequest()` is a simple request in a single
method, and `sendRequestNested()` has a deeper call stack and prints the stack
trace.

`RemoteServicePlatform`, `RemoteServiceVirtual` (both extending 
`RemoteServiceThreaded`), and `RemoteServiceNonblocking` each implement
`RemoteService`, and use different concurrency strategies to handle requests.

The blocking operation being demonstrated is a simple `Thread.sleep()`/
`ScheduledExecutorService.schedule()`, but this is applicable for any blocking
operation in an application.

### Tests

`RemoteServiceTest` contains two tests. `sendRequest_performance` calls 
`sendRequest()` multiple times (defined with the Spring property 
`test.requestCount`), and logs the amount of time taken to complete the 
requests. `sendRequestNested_tracing` sends a single request to 
`sendRequestNested()`, causing a stack trace to be printed.

By default, the tests are executed using all three implementations of 
`RemoteService`, but a subset can be executed by setting 
`test.concurrencyTypes`. 

## Executing

Note that Maven and Java 21 must be installed, with the Java 21 JDK set via 
the `JAVA_HOME` environment variable.

Building the project and running all tests can be performed by running:

```
mvn clean install
```

Custom test parameters can be set via `-D`. For example, to set the 
concurrency types to just non blocking and virtual threads, and increasing 
the request count to 100,000 (from the default of 10,000), use:

```
mvn clean install -Dtest.concurrencyTypes=NONBLOCKING,VIRTUAL -Dtest.requestCount=100000
```

## Interpreting Results

### Performance Test Results

When running the performance tests with the default parameters, we see the 
following output:

```
2024-02-26T13:49:59.263-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Starting platform thread performance test with 10000 requests and a time delay of 100x3 ms
2024-02-26T13:50:11.297-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Completed platform thread performance test in 12033 ms
2024-02-26T13:50:11.301-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Starting nonblocking performance test with 10000 requests and a time delay of 100x3 ms
2024-02-26T13:50:11.634-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Completed nonblocking performance test in 333 ms
2024-02-26T13:50:11.638-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Starting virtual thread performance test with 10000 requests and a time delay of 100x3 ms
2024-02-26T13:50:12.077-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Completed virtual thread performance test in 439 ms
```

We can see that executing via platform threads takes approximately 12 seconds. 
Since there are 10,000 requests, each with 3 blocking operations that take 
100ms each, that's 3,000 seconds of serial execution. Parallelizing over 250 
platform threads, we get an expected parallel execution time of 12 seconds.

Using non blocking execution or virtual threads, the time is significantly 
lower, 333ms and 439ms respectively. This is only a bit longer than the 
execution time of a single request. Without the need to schedule requests on a 
limited number of platform threads, these methods are able to better utilize 
the CPU. 

If the processes are examined using a Java debugging tool, such as JConsole, 
it can be observed that thread usage is significantly lower when using non 
blocking or virtual threads.

### Tracing Results

When running the tracing tests with the default parameters, we see the 
following output:

```
2024-02-26T13:49:58.323-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Starting platform thread tracing test
2024-02-26T13:49:58.625-05:00  INFO 29063 --- [pool-4-thread-1] .s.RemoteServicePlatform$$SpringCGLIB$$0 : Current stack

java.lang.RuntimeException: Printing stack
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.completeResponse(RemoteServiceThreaded.java:79) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.threadedThird(RemoteServiceThreaded.java:73) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.threadedSecond(RemoteServiceThreaded.java:67) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.threadedFirst(RemoteServiceThreaded.java:61) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.lambda$sendRequestNested$1(RemoteServiceThreaded.java:54) ~[classes/:na]
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2024-02-26T13:49:58.627-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Completed platform thread tracing test
2024-02-26T13:49:58.636-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Starting nonblocking tracing test
2024-02-26T13:49:58.938-05:00  INFO 29063 --- [pool-3-thread-1] RemoteServiceNonblocking$$SpringCGLIB$$0 : Current stack

java.lang.RuntimeException: Printing stack
	at com.redhat.demo.concurrency.service.RemoteServiceNonblocking.completeResponse(RemoteServiceNonblocking.java:86) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceNonblocking.lambda$nonblockingThird$5(RemoteServiceNonblocking.java:80) ~[classes/:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2024-02-26T13:49:58.939-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Completed nonblocking tracing test
2024-02-26T13:49:58.946-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Starting virtual thread tracing test
2024-02-26T13:49:59.250-05:00  INFO 29063 --- [               ] c.s.RemoteServiceVirtual$$SpringCGLIB$$0 : Current stack

java.lang.RuntimeException: Printing stack
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.completeResponse(RemoteServiceThreaded.java:79) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.threadedThird(RemoteServiceThreaded.java:73) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.threadedSecond(RemoteServiceThreaded.java:67) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.threadedFirst(RemoteServiceThreaded.java:61) ~[classes/:na]
	at com.redhat.demo.concurrency.service.RemoteServiceThreaded.lambda$sendRequestNested$1(RemoteServiceThreaded.java:54) ~[classes/:na]
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317) ~[na:na]
	at java.base/java.lang.VirtualThread.run(VirtualThread.java:309) ~[na:na]

2024-02-26T13:49:59.251-05:00  INFO 29063 --- [           main] c.r.d.c.service.RemoteServiceTest        : Completed virtual thread tracing test
```

In this output, we see stack traces printed with both Platform and Virtual 
threads include the full stack trace, allowing us to trace the execution back 
to the original call in `RemoteService.sendRequestNested()`. However, the 
trace in the non blocking execution tops out at 
`RemoteServiceNonblocking.nonBlockingThird()`, near the bottom of the stack. 
This can complicate debugging why an exception was thrown.

### Conclusions

From these results, we can see that Virtual Threads provide the same level of 
traceability as platform threads with minimal API changes, while nearly 
matching the performance of non blocking execution. There are a number of 
caveats to the performance improvements, though, particularly around object 
pinning and cases where IO operations are not a bottleneck. See 
[the Virtual Threads JEP](https://openjdk.org/jeps/444) for more details.

## Further Reading

- [JEP 444](https://openjdk.org/jeps/444)
- [Virtual Threads documentation](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)
- [Discussion of async and non blocking design](https://www.alibabacloud.com/blog/how-java-is-used-for-asynchronous-non-blocking-programming_597808)
