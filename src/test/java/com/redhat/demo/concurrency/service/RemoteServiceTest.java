package com.redhat.demo.concurrency.service;

import java.util.ArrayList;
import java.util.List;
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

	private enum ConcurrencyType {
		PLATFORM, NONBLOCKING, VIRTUAL
	}

	@Value("${test.requestCount}")
	private int requestCount;

	@Value("${concurrency.blockTimeMs}")
	private int blockTimeMs;

	@Value("${test.concurrencyTypes:}")
	private List<ConcurrencyType> concurrencyTypes;

	@Autowired
	private RemoteServicePlatform remoteServicePlatform;

	@Autowired
	private RemoteServiceNonblocking remoteServiceNonblocking;

	@Autowired
	private RemoteServiceVirtual remoteServiceVirtual;

	/**
	 * Provides a list of RemoteService, based on the value of test.concurrencyTypes
	 * (if that value is an empty list, all RemoteServices are returned). Referenced
	 * via @ParameterizedTest and @MethodSource on the test method annotations.
	 * 
	 * @return a Stream containing the remote services to run
	 */
	private Stream<Arguments> remoteServices() {
		List<Arguments> arguments = new ArrayList<>();
		if (concurrencyTypes.isEmpty() || concurrencyTypes.contains(ConcurrencyType.PLATFORM)) {
			arguments.add(Arguments.of(remoteServicePlatform));
		}
		if (concurrencyTypes.isEmpty() || concurrencyTypes.contains(ConcurrencyType.NONBLOCKING)) {
			arguments.add(Arguments.of(remoteServiceNonblocking));
		}
		if (concurrencyTypes.isEmpty() || concurrencyTypes.contains(ConcurrencyType.VIRTUAL)) {
			arguments.add(Arguments.of(remoteServiceVirtual));
		}
		return arguments.stream();
	}

	@ParameterizedTest
	@MethodSource("remoteServices")
	public void sendRequest_performance(RemoteService remoteService) throws Exception {
		logger.info("Starting {} performance test with {} requests and a time delay of {}x3 ms",
				remoteService.getDisplayName(), requestCount, blockTimeMs);
		long startTime = System.currentTimeMillis();
		// Submit requestCount requests
		List<Future<String>> results = new ArrayList<>(requestCount);
		for (int i = 0; i < requestCount; i++) {
			results.add(remoteService.sendRequest());
		}
		// Wait for all requests to complete
		for (Future<String> result : results) {
			String resultString = result.get();
			logger.debug("Response: {}", resultString);
		}
		// Return with time taken to complete requests
		logger.info("Completed {} performance test in {} ms", remoteService.getDisplayName(),
				System.currentTimeMillis() - startTime);
	}

	@ParameterizedTest
	@MethodSource("remoteServices")
	public void sendRequestNested_tracing(RemoteService remoteService) throws Exception {
		logger.info("Starting {} tracing test", remoteService.getDisplayName());
		Future<String> result = remoteService.sendRequestNested();
		String resultString = result.get();
		logger.debug("Response: {}", resultString);
		logger.info("Completed {} tracing test", remoteService.getDisplayName());
	}
}
