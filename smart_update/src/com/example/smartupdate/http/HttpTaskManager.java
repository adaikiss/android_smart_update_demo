package com.example.smartupdate.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpTaskManager {
	private static final int POOL_SIZE = 3;
	private static HttpTaskManager instance;
	private ExecutorService pool;

	private HttpTaskManager() {
		pool = Executors.newFixedThreadPool(POOL_SIZE);
	}

	public static HttpTaskManager getInstance() {
		if (null == instance) {
			instance = new HttpTaskManager();
		}
		return instance;
	}

	public void post(HttpTask task){
		pool.submit(task);
	}

	public void destroy(){
		if(null != pool){
			pool.shutdown();
			pool = null;
		}
	}
}
