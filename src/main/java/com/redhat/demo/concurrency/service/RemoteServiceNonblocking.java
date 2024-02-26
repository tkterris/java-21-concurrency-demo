package com.redhat.demo.concurrency.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemoteServiceNonblocking implements RemoteService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${concurrency.blockTimeMs}")
	private int blockTimeMs;

	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	@Override
	public String getDisplayName() {
		return "nonblocking";
	}

	@Override
	public Future<String> sendRequest() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		CompletableFuture<String> result = new CompletableFuture<String>();
		// CPU stuff
		logger.debug("CPU operations here");
		// Blocking stuff
		logger.debug("Start blocking operation 1");
		executorService.schedule(() -> {
			logger.debug("Start blocking operation 2");
			executorService.schedule(() -> {
				logger.debug("Start blocking operation 3");
				executorService.schedule(() -> {
					// More CPU stuff
					logger.debug("Some more CPU operations");
					result.complete("done");
				}, blockTimeMs, TimeUnit.MILLISECONDS);
			}, blockTimeMs, TimeUnit.MILLISECONDS);
		}, blockTimeMs, TimeUnit.MILLISECONDS);
		return result;
	}

	@Override
	public Future<String> sendRequestNested() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		CompletableFuture<String> result = new CompletableFuture<String>();
		// CPU stuff
		logger.debug("CPU operations here");
		// Blocking stuff
		logger.debug("Start blocking operation 1");
		executorService.schedule(() -> nonblockingFirst(result), blockTimeMs, TimeUnit.MILLISECONDS);
		return result;
	}

	private void nonblockingFirst(CompletableFuture<String> result) {
		logger.debug("Start blocking operation 2");
		executorService.schedule(() -> nonblockingSecond(result), blockTimeMs, TimeUnit.MILLISECONDS);
	}

	private void nonblockingSecond(CompletableFuture<String> result) {
		logger.debug("Start blocking operation 2");
		executorService.schedule(() -> completeResponse(result), blockTimeMs, TimeUnit.MILLISECONDS);
	}

	private void completeResponse(CompletableFuture<String> result) {
		// More CPU stuff
		logger.debug("Some more CPU operations");
		logger.info("Current stack", new RuntimeException("Printing stack"));
		result.complete("done");
	}

}
