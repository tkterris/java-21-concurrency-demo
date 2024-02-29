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
			String intermediateData = "intermediateData";
			// Blocking stuff
			logger.debug("Start blocking operation 1");
			Thread.sleep(blockTimeMs);
			intermediateData = intermediateData + "result1";
			logger.debug("Start blocking operation 2");
			Thread.sleep(blockTimeMs);
			intermediateData = intermediateData + "result2";
			logger.debug("Start blocking operation 3");
			Thread.sleep(blockTimeMs);
			intermediateData = intermediateData + "result3";
			// More CPU stuff
			logger.debug("Some more CPU operations");
			return intermediateData + "resultFinal";
		});
	}

	@Override
	public Future<String> sendRequestNested() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		return executorService.submit(() -> {
			// CPU stuff
			logger.debug("CPU operations here");
			String intermediateData = "intermediateData";
			// Blocking stuff
			return first(intermediateData);
		});
	}

	private String first(String intermediateData) throws InterruptedException {
		logger.debug("Start blocking operation 1");
		Thread.sleep(blockTimeMs);
		return second(intermediateData + "result1");
	}

	private String second(String intermediateData) throws InterruptedException {
		logger.debug("Start blocking operation 2");
		Thread.sleep(blockTimeMs);
		return third(intermediateData + "result2");
	}

	private String third(String intermediateData) throws InterruptedException {
		logger.debug("Start blocking operation 3");
		Thread.sleep(blockTimeMs);
		return completeResponse(intermediateData + "result3");
	}

	private String completeResponse(String intermediateData) {
		// More CPU stuff
		logger.debug("Some more CPU operations");
		logger.info("Printing stack trace", new RuntimeException("Some exception"));
		return intermediateData + "resultFinal";
	}

}
