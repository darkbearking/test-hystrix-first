package com.troila.lw.cb;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * 測試不同的斷路策略的效果之線程策略
 * @author liwei
 *
 */
public class CircuitStrategy extends HystrixCommand<String> {

	private int index;
	
	public CircuitStrategy(int index) {
		super(Setter.withGroupKey(
				HystrixCommandGroupKey.Factory.asKey("ExampleGroup")));
		this.index = index;
		
	}
	
	protected String run() throws Exception {
		Thread.sleep(800);
		System.out.println("執行命令，當前索引 " + index);
		return "success";
	}

	@Override
	protected String getFallback() {
		System.out.println("執行回退，當前索引 " + index);

		return "fall back";
	}
}
