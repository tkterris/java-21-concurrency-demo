package com.redhat.demo.concurrency.beans;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorServiceProvider {
	@Value("${concurrency.platformThreads}")
	private Integer platformThreads;
	
	@Bean("platform") 
	public ExecutorService platformExecutorService() {
		return Executors.newFixedThreadPool(platformThreads);
	}
	
	@Bean("virtual") 
	public ExecutorService virtualExecutorService() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}
	
	@Bean("nonblocking") 
	public ScheduledExecutorService nonblockingExecutorService() {
		return Executors.newScheduledThreadPool(1);
	}
}
