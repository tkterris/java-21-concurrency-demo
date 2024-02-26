package com.redhat.demo.concurrency.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;

public abstract class RemoteServiceThreaded implements RemoteService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${concurrency.blockTimeMs}")
	private int blockTimeMs;
	
	private ExecutorService executorService;
	
	protected abstract ExecutorService newExecutorService();
	
	@PostConstruct
	public void setExecutorService() {
		this.executorService = newExecutorService();
	}

	@Override
	public Future<String> sendRequest() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		CompletableFuture<String> result = new CompletableFuture<String>();
		executorService.submit(() -> {
			// CPU stuff
			logger.debug("CPU operations here");
			// Blocking stuff
			logger.debug("Start blocking operation 1");
			Thread.sleep(blockTimeMs);
			logger.debug("Start blocking operation 2");
			Thread.sleep(blockTimeMs);
			logger.debug("Start blocking operation 3");
			Thread.sleep(blockTimeMs);
			// More CPU stuff
			logger.debug("Some more CPU operations");
			result.complete("done");
			return null;
		});
		return result;
	}

	@Override
	public Future<String> sendRequestNested() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		CompletableFuture<String> result = new CompletableFuture<String>();
		executorService.submit(() -> {
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
		Thread.sleep(blockTimeMs);
		threadedSecond(result);
	}
	
	private void threadedSecond(CompletableFuture<String> result) throws InterruptedException {
		logger.debug("Start blocking operation 2");
		Thread.sleep(blockTimeMs);
		threadedThird(result);
	}
	
	private void threadedThird(CompletableFuture<String> result) throws InterruptedException {
		logger.debug("Start blocking operation 3");
		Thread.sleep(blockTimeMs);
		completeResponse(result);
	}

	private void completeResponse(CompletableFuture<String> result) {
		// More CPU stuff
		logger.debug("Some more CPU operations");
		logger.info("Current stack", new RuntimeException("Printing stack"));
		result.complete("done");
	}

}
