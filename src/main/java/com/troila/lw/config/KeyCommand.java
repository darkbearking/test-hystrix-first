package com.troila.lw.config;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

public class KeyCommand extends HystrixCommand<String> {

	public KeyCommand() {
		/**
		 * 組名的作用：
		 * 其實對於每個命令，Hystrix的底層都會分配一個線程池來做對應操作
		 * 當你的命令有多個的時候，Hystrix會用map來維護所有的線程池
		 * 因此，默認情況下，這個組key，就是map的jian(key)值就是線程池對象
		 * 如果你設置了線程池的key，那麼
		 */
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestGroupKey"))
				.andCommandKey(HystrixCommandKey.Factory.asKey("CommandKey"))
				.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("PoolKey")));
	}

	protected String run() throws Exception {
		Thread.sleep(1500);
		System.out.println("執行命令");
		return "success";
	}
	
	@Override
	protected String getFallback() {
		System.out.println("執行回退");

		return "fall back";
	}
}
