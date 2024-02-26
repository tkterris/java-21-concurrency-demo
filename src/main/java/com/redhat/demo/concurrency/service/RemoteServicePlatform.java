package com.redhat.demo.concurrency.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemoteServicePlatform extends RemoteServiceThreaded {

	@Value("${concurrency.platformThreads}")
	private Integer platformThreads;

	@Override
	public String getDisplayName() {
		return "platform thread";
	}
	
	@Override
	protected ExecutorService newExecutorService() {
		return Executors.newFixedThreadPool(platformThreads);
	}
}
