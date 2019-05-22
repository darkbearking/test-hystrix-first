package com.troila.lw.config;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class TimeoutCommand extends HystrixCommand<String> {

	public TimeoutCommand() {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(2000)));
	}

	/**
	 * 模擬調用有超時的接口
	 * 這裡設置休眠時長為3秒，要比當前類的構造函數的設置時長更長
	 * 這樣目的是為了造一個異常情況出來，以便看到效果
	 * 如果當前方法的休眠時長小於2秒，則看不到效果
	 */
	protected String run() throws Exception {
		Thread.sleep(3000);
		System.out.println("執行命令");
		return "success";
	}
	
	@Override
	protected String getFallback() {
		System.out.println("執行回退");
		//如果返回值使用的是父類HystrixCommand的getFallback方法，則會產生異常
		//return super.getFallback();
		
		//如果使用其他的字符串作為返回值，則不會產生異常。要看效果可以自行放開注釋即可
		return "fall back";
	}
}
