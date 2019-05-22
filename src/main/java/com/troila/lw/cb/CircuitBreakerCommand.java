package com.troila.lw.cb;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * 斷路器基礎測試類
 * 測試斷路器默認強制打開的效果
 * @author liwei
 *
 */
public class CircuitBreakerCommand extends HystrixCommand<String> {

	public CircuitBreakerCommand() {

		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CircuitBreakerCommand"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						//這裡是令斷路器強制打開。也就是，無論你的程度是否有問題，總會執行回退方法
						//甚至我們都不需要設置是否超時
						.withCircuitBreakerForceOpen(true)));
	}

	protected String run() throws Exception {
		System.out.println("執行命令");
		return "success";
	}
	
	@Override
	protected String getFallback() {
		System.out.println("執行回退");

		return "fall back";
	}
}
