package com.troila.lw.config;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class PropertiesCommand extends HystrixCommand<String> {
	
	public PropertiesCommand() {
		//當我在HttpClientMain這個入口類設置了全局超時設置之後，這裡如果再配置超時時間，
		//那麼這裡的會覆蓋掉入口類中的那個全局配置，因此需要注釋掉下面的寫法
		/*super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(500)));*/
		
		//注釋掉上面的寫法之後，換用下面的寫法即可
		super(HystrixCommandGroupKey.Factory.asKey("TestGroup"));
	}

	protected String run() throws Exception {
		//當這裡取值為1500的時候，小於PropertyMain中的閾值2000毫秒，則會成功，否則取值大於2000則會回退
		Thread.sleep(3500);
		System.out.println("執行命令");
		return "success";
	}
	
	@Override
	protected String getFallback() {
		System.out.println("執行回退");

		return "fall back";
	}
}
