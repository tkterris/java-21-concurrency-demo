package com.redhat.demo.concurrency.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.redhat.demo.concurrency.Application;

@SpringBootTest
@ContextConfiguration(classes = Application.class)
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class RemoteServiceTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${test.requestCount}")
	private Integer requestCount;

	@Value("${concurrency.blockTimeMs}")
	private Integer blockTimeMs;

	@Autowired
	private RemoteServiceFlat remoteServiceFlat;

	@Autowired
	private RemoteServiceNested remoteServiceNested;
	
	private Stream<Arguments> remoteServices() {
	    return Stream.of(
	    	      Arguments.of(remoteServiceFlat),
	    	      Arguments.of(remoteServiceNested));
	}

	@ParameterizedTest
	@MethodSource("remoteServices")
	public void sendRequestPlatform_performance(RemoteService remoteService) throws Exception {
		logger.info("Starting platform thread performance test with {} requests and a time delay of {} ms", requestCount, blockTimeMs);
		long duration = sendParallelRequests(() -> remoteService.sendRequestPlatform());
		logger.info("Completed platform thread performance test in {} ms", duration);
	}

	@ParameterizedTest
	@MethodSource("remoteServices")
	public void sendRequestNonblocking_performance(RemoteService remoteService) throws Exception {
		logger.info("Starting non blocking performance test with {} requests and a time delay of {} ms", requestCount, blockTimeMs);
		long duration = sendParallelRequests(() -> remoteService.sendRequestNonblocking());
		logger.info("Completed non blocking performance test in {} ms", duration);
	}

	@ParameterizedTest
	@MethodSource("remoteServices")
	public void sendRequestVirtual_performance(RemoteService remoteService) throws Exception {
		logger.info("Starting virtual thread performance test with {} requests and a time delay of {} ms", requestCount, blockTimeMs);
		long duration = sendParallelRequests(() -> remoteService.sendRequestVirtual());
		logger.info("Completed virtual thread performance test in {} ms", duration);
	}
	
	private long sendParallelRequests(Callable<Future<String>> callable) throws Exception {
		long startTime = System.currentTimeMillis();
		// Submit requestCount requests
		List<Future<String>> results = new ArrayList<>(requestCount);
		for (int i = 0; i < requestCount; i++) {
			results.add(callable.call());
		}
		// Wait for all requests to complete
		for (Future<String> result : results) {
			result.get();
		}
		// Return with time taken to complete requests
		return System.currentTimeMillis() - startTime;
	}
}
