package com.redhat.demo.concurrency.service;

import java.util.concurrent.Future;

public interface RemoteService {

	/**
	 * Get service display name, used in testing logs
	 * 
	 * @return a user-friendly name
	 */
	public String getDisplayName();

	/**
	 * Send an asynchronous request, returning a Future with the response. The
	 * concurrency type is implementation-specific.
	 * 
	 * @return a Future with the response
	 */
	public Future<String> sendRequest();

	/**
	 * Send an asynchronous request, returning a Future with the response. Similar
	 * to sendRequest, but it uses a deeper call stack and prints the stack trace at
	 * the bottom of the stack (after blocking operations are complete).
	 * 
	 * @return a Future with the response
	 */
	public Future<String> sendRequestNested();

}