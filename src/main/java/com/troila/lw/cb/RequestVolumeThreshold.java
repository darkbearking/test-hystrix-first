package com.troila.lw.cb;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class RequestVolumeThreshold extends HystrixCommand<String> {

	private boolean isTimeout;
	
	public RequestVolumeThreshold(boolean isTimeout) {
		super(Setter.withGroupKey(
				HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
				.andCommandPropertiesDefaults(
						HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(500)));
		
		this.isTimeout 	 = isTimeout;
	}

	protected String run() throws Exception {
		if(isTimeout) {
			Thread.sleep(800);
		}else {
			Thread.sleep(200);
		}
		
		System.out.println("執行命令");
		return "success";
	}

	@Override
	protected String getFallback() {
		System.out.println("執行回退");

		return "fall back";
	}
}
