package com.troila.lw.command;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class RunCommand extends HystrixCommand<String> {
	
	String msg ;
	
	public RunCommand(String msg) {
		//這裡需要設置，作用暫時不知，key名隨意起
		super(HystrixCommandGroupKey.Factory.asKey("TestGroup"));
		this.msg = msg;
	}

	/**
	 * 模擬調用有超時的接口
	 */
	protected String run() throws Exception {
		System.out.println(msg);
		
		return "success";
	}
}
