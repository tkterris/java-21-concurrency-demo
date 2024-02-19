package com.redhat.demo.concurrency.service;

import java.util.concurrent.Future;

public interface RemoteService {

	Future<String> sendRequestPlatform();

	Future<String> sendRequestVirtual();

	Future<String> sendRequestNonblocking();

}