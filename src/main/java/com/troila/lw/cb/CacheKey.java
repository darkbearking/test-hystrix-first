package com.troila.lw.cb;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

public class CacheKey extends HystrixCommand<String> {

	private String cacheKey;
	
	//這裡需要下面這兩個key
	public CacheKey(String cacheKey) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestGroupKey"))
				.andCommandKey(HystrixCommandKey.Factory.asKey("CommandKey")));
		
		this.cacheKey = cacheKey;
		
	}
	
	protected String run() throws Exception {
		Thread.sleep(800);
		System.out.println("執行命令");
		return "success";
	}

	@Override
	protected String getFallback() {
		System.out.println("執行回退");

		return "fall back";
	}
	
	/**
	 * 這裡需要新增這個key
	 */
	@Override
	protected String getCacheKey() {
		return this.cacheKey;
	}
}
