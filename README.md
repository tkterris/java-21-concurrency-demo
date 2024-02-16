
```
java -cp target/java-concurrency-demo-1.0-SNAPSHOT.jar -DconcurrencyType=PLATFORM com.redhat.demo.concurrency.App

java -cp target/java-concurrency-demo-1.0-SNAPSHOT.jar -DconcurrencyType=VIRTUAL com.redhat.demo.concurrency.App

watch "cat virtthread.out | grep 'End blocking logic' | wc -l"
```

https://www.alibabacloud.com/blog/how-java-is-used-for-asynchronous-non-blocking-programming_597808
https://www.baeldung.com/java-io-vs-nio
https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-io-2/src/test/java/com/baeldung/blockingnonblocking/NonBlockingClientUnitTest.java
https://javadoc.io/static/com.github.tomakehurst/wiremock-jre8/2.35.1/com/github/tomakehurst/wiremock/client/WireMock.html
https://javadoc.io/static/com.github.tomakehurst/wiremock-jre8/2.35.1/com/github/tomakehurst/wiremock/client/ResponseDefinitionBuilder.html
