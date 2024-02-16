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
public class RemoteServiceNested {

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
			threadedFirst(result);
			return null;
		});
		return result;
	}
	
	private void threadedFirst(CompletableFuture<String> result) throws InterruptedException {
		logger.debug("Start blocking operation 1");
		Thread.sleep(blockTimeMs / 3);
		threadedSecond(result);
	}
	
	private void threadedSecond(CompletableFuture<String> result) throws InterruptedException {
		logger.debug("Start blocking operation 2");
		Thread.sleep(blockTimeMs / 3);
		threadedThird(result);
	}
	
	private void threadedThird(CompletableFuture<String> result) throws InterruptedException {
		logger.debug("Start blocking operation 3");
		Thread.sleep(blockTimeMs / 3);
		threadedComplete(result);
	}

	private void threadedComplete(CompletableFuture<String> result) throws InterruptedException {
		// More CPU stuff
		logger.debug("Some more CPU operations");
		if (logger.isDebugEnabled()) {
			logger.debug("Current stack", new RuntimeException("Printing stack"));
		}
		result.complete("done");
	}

	public Future<String> sendRequestNonblocking() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		CompletableFuture<String> result = new CompletableFuture<String>();
		// CPU stuff
		logger.debug("CPU operations here");
		// Blocking stuff
		logger.debug("Start blocking operation 1");
		nonblockingExecutor.schedule(() -> nonblockingFirst(result), blockTimeMs / 3, TimeUnit.MILLISECONDS);
		return result;
	}

	private void nonblockingFirst(CompletableFuture<String> result) {
		logger.debug("Start blocking operation 2");
		nonblockingExecutor.schedule(() -> nonblockingSecond(result), blockTimeMs / 3, TimeUnit.MILLISECONDS);
	}

	private void nonblockingSecond(CompletableFuture<String> result) {
		logger.debug("Start blocking operation 2");
		nonblockingExecutor.schedule(() -> nonblockingThird(result), blockTimeMs / 3, TimeUnit.MILLISECONDS);
	}

	private void nonblockingThird(CompletableFuture<String> result) {
		// More CPU stuff
		logger.debug("Some more CPU operations");
		if (logger.isDebugEnabled()) {
			logger.debug("Current stack", new RuntimeException("Printing stack"));
		}
		result.complete("done");
	}
}
