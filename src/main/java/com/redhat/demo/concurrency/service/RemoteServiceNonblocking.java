package com.redhat.demo.concurrency.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class RemoteServiceNonblocking implements RemoteService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${concurrency.blockTimeMs}")
	private int blockTimeMs;

	private Executor delayedExecutor;

	@PostConstruct
	public void setExecutor() {
		this.delayedExecutor = CompletableFuture.delayedExecutor(blockTimeMs, TimeUnit.MILLISECONDS,
				new ScheduledThreadPoolExecutor(1));
	}

	@Override
	public String getDisplayName() {
		return "nonblocking";
	}

	@Override
	public Future<String> sendRequest() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		// CPU stuff
		logger.debug("CPU operations here");
		String intermediateData = "intermediate data";
		// Blocking stuff
		return CompletableFuture.completedFuture(intermediateData).thenCompose(s -> {
			logger.debug("Start blocking operation 1");
			return CompletableFuture.supplyAsync(() -> s + "result1", delayedExecutor);
		}).thenCompose(s -> {
			logger.debug("Start blocking operation 2");
			return CompletableFuture.supplyAsync(() -> s + "result2", delayedExecutor);
		}).thenCompose(s -> {
			logger.debug("Start blocking operation 3");
			return CompletableFuture.supplyAsync(() -> s + "result3", delayedExecutor);
		}).thenApply(s -> {
			logger.debug("Some more CPU operations");
			return s + "resultFinal";
		});
	}

	@Override
	public Future<String> sendRequestNested() {
		logger.debug("Sending request with block time {} ms", blockTimeMs);
		// CPU stuff
		logger.debug("CPU operations here");
		String intermediateData = "intermediateData";
		// Blocking stuff
		return first(intermediateData);
	}

	private CompletableFuture<String> first(String intermediateData) {
		// Blocking stuff
		logger.debug("Start blocking operation 1");
		return CompletableFuture.supplyAsync(() -> intermediateData + "result1", delayedExecutor)
				.thenCompose(s -> second(s));
	}

	private CompletableFuture<String> second(String intermediateData) {
		logger.debug("Start blocking operation 2");
		return CompletableFuture.supplyAsync(() -> intermediateData + "result2", delayedExecutor)
				.thenCompose(s -> third(s));
	}

	private CompletableFuture<String> third(String intermediateData) {
		logger.debug("Start blocking operation 3");
		return CompletableFuture.supplyAsync(() -> intermediateData + "result3", delayedExecutor)
				.thenApply(s -> completeResponse(s));
	}

	private String completeResponse(String intermediateData) {
		// More CPU stuff
		logger.debug("Some more CPU operations");
		logger.info("Printing stack trace", new RuntimeException("Some exception"));
		return intermediateData + "resultFinal";
	}

}
