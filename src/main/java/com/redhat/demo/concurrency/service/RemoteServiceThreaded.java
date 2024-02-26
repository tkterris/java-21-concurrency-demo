package com.redhat.demo.concurrency.service;

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
		return executorService.submit(() -> {
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
			return "done";
		});
	}

	@Override
	public Future<String> sendRequestNested() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		return executorService.submit(() -> {
			// CPU stuff
			logger.debug("CPU operations here");
			// Blocking stuff
			return threadedFirst();
		});
	}
	
	private String threadedFirst() throws InterruptedException {
		logger.debug("Start blocking operation 1");
		Thread.sleep(blockTimeMs);
		return threadedSecond();
	}
	
	private String threadedSecond() throws InterruptedException {
		logger.debug("Start blocking operation 2");
		Thread.sleep(blockTimeMs);
		return threadedThird();
	}
	
	private String threadedThird() throws InterruptedException {
		logger.debug("Start blocking operation 3");
		Thread.sleep(blockTimeMs);
		return completeResponse();
	}

	private String completeResponse() {
		// More CPU stuff
		logger.debug("Some more CPU operations");
		logger.info("Current stack", new RuntimeException("Printing stack"));
		return "done";
	}

}
