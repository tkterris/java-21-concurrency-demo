package com.redhat.demo.concurrency.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RemoteServiceVirtual extends RemoteServiceThreaded {

	@Override
	public String getDisplayName() {
		return "virtual thread";
	}
	
	@Override
	protected ExecutorService newExecutorService() {
		// New API, added in JDK 21
		return Executors.newVirtualThreadPerTaskExecutor();
	}
}
