package com.redhat.demo.concurrency.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemoteServiceFlat {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${concurrency.blockTimeMs}")
	private int blockTimeMs;

	@Autowired
	@Qualifier("platform")
	private ExecutorService platformExecutor;

	@Autowired
	@Qualifier("virtual")
	private ExecutorService virtualExecutor;

	@Autowired
	@Qualifier("nonblocking")
	private ScheduledExecutorService nonblockingExecutor;

	public Future<String> sendRequestPlatform() {
		return sendRequestThreaded(platformExecutor);
	}

	public Future<String> sendRequestVirtual() {
		return sendRequestThreaded(virtualExecutor);
	}

	private Future<String> sendRequestThreaded(ExecutorService executor) {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		CompletableFuture<String> result = new CompletableFuture<String>();
		executor.submit(() -> {
			// CPU stuff
			logger.debug("CPU operations here");
			// Blocking stuff
			logger.debug("Start blocking operation 1");
			Thread.sleep(blockTimeMs / 3);
			logger.debug("Start blocking operation 2");
			Thread.sleep(blockTimeMs / 3);
			logger.debug("Start blocking operation 3");
			Thread.sleep(blockTimeMs / 3);
			// More CPU stuff
			logger.debug("Some more CPU operations");
			result.complete("done");
			return null;
		});
		return result;
	}

	public Future<String> sendRequestNonblocking() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		CompletableFuture<String> result = new CompletableFuture<String>();
		// CPU stuff
		logger.debug("CPU operations here");
		// Blocking stuff
		logger.debug("Start blocking operation 1");
		nonblockingExecutor.schedule(() -> {
			logger.debug("Start blocking operation 2");
			nonblockingExecutor.schedule(() -> {
				logger.debug("Start blocking operation 3");
				nonblockingExecutor.schedule(() -> {
					// More CPU stuff
					logger.debug("Some more CPU operations");
					result.complete("done");
				}, blockTimeMs / 3, TimeUnit.MILLISECONDS);
			}, blockTimeMs / 3, TimeUnit.MILLISECONDS);
		}, blockTimeMs / 3, TimeUnit.MILLISECONDS);
		return result;
	}
}
