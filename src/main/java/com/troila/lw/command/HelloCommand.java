package com.troila.lw.command;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * 模擬測試正常流程（沒有超時等問題）的情況下
 * 執行遠程調用的效果
 * @author liwei
 *
 */
public class HelloCommand extends HystrixCommand<String> {

	public HelloCommand() {
		//這裡需要設置，作用暫時不知，key名隨意起
		super(HystrixCommandGroupKey.Factory.asKey("TestGroup"));
	}

	/**
	 * 模擬調用正常的接口
	 */
	protected String run() throws Exception {
		String url = "http://localhost:8080/normalHello";
		HttpGet httpget = new HttpGet(url);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpget);
		
		return EntityUtils.toString(response.getEntity());
	}

}
