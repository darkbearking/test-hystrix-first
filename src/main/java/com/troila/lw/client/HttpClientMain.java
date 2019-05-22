package com.troila.lw.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics.HealthCounts;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixRequestCache;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategyDefault;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.troila.lw.cb.CacheKey;
import com.troila.lw.cb.CircuitBreakerCommand;
import com.troila.lw.cb.CircuitStrategy;
import com.troila.lw.cb.RequestVolumeThreshold;
import com.troila.lw.command.ErrorCommand;
import com.troila.lw.command.HelloCommand;
import com.troila.lw.command.RunCommand;
import com.troila.lw.config.PropertiesCommand;
import com.troila.lw.config.TimeoutCommand;

import rx.Observable;
import rx.Observer;

/**
 * 是本工程中所有測試類的入口類
 * @author liwei
 *
 */
public class HttpClientMain {

	public static void main(String[] args) throws Exception{
		HttpClientMain.testRequestVolumeThreshold();
	}
	
	//模擬通常情況下的方法調用
	public static void execMethod1() throws Exception {
		String url = "http://localhost:8080/normalHello";
		HttpGet httpget = new HttpGet(url);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpget);
		
		System.out.println( EntityUtils.toString(response.getEntity()));
	}
	
	//調用HytrixCommand，模擬正常情況下的方法調用，調用內容與execMethod1相同
	//與execMethod1相比，當前方法的速度明顯較慢（因為hytrix採用了線程的方式實現）
	public static void execMethod2() {
		HelloCommand command = new HelloCommand();
		String result = command.execute();
		System.out.println(result);
	}
	
	//調用HytrixCommand，模擬異常情況下的方法調用
	public static void execMethod3() {
		ErrorCommand command = new ErrorCommand();
		String result = command.execute();
		System.out.println(result);
	}

	/**
	 * 測試observe和toObserve方法的區別
	 * c1的observe方法會立即執行觀察這個動作
	 * 而c2的toObserve方法僅僅是進行了一個註冊的動作卻沒有立即幹活兒
	 * 若要令註冊的toObserve方法立即干活兒，間c3的所作所為即可
	 * @throws Exception
	 */
	public static void execRunCommand() throws Exception{
		RunCommand c1 = new RunCommand("observe method");
		c1.observe();
		
		RunCommand c2 = new RunCommand("toObserve method");
		c2.toObservable();
		
		RunCommand c3 = new RunCommand("exec toObserve method ,now");
		Observable ob = c3.toObservable();
		ob.subscribe(new Observer<String>() {
			
			public void onCompleted() {
				System.out.println("command completed");
			}

			public void onError(Throwable arg0) {
				
			}

			public void onNext(String arg0) {
				System.out.println(arg0);
			}
		});
		
		Thread.sleep(1000);
	}

	/**
	 * 測試超時時長配置
	 */
	public static void timeoutTest() {
		TimeoutCommand c = new TimeoutCommand();
		c.execute();
	}

	/**
	 * 測試全局性配置
	 * 注意setProperty方法中的那段字符串，不能錯。。。
	 * 否則全局性的超時設置不生效
	 */
	public static void propertiesTest() {
		ConfigurationManager
		.getConfigInstance()
		.setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds"
				, 10000);
		
		PropertiesCommand c = new PropertiesCommand();
		c.execute();
	}

	/**
	 * 斷路器測試
	 */
	public static void CircuitBreakerTest() {
		CircuitBreakerCommand c = new CircuitBreakerCommand();
		c.execute();
	}
	
	/**
	 * 測試斷路器的一些配置方式
	 * 本例測試10秒內有超過10次請求
	 * 同時每次都是失敗的斷路器功能
	 */
	public static void testRequestVolumeThreshold() {
		//全局性配置，限制10秒內，如果有超過20次的請求，就令斷路器斷開（open）
		ConfigurationManager
		.getConfigInstance()
		.setProperty("hystrix.command.default.circuitBreaker.requestVolumeThreshold"
				//20代表20次請求
				, 10);
		
		boolean isTimeout = true;
		
		//測試斷路后不再重打開
		for(int i = 0; i < 30; i++) {
			RequestVolumeThreshold c = new RequestVolumeThreshold(isTimeout);
			c.execute();
			System.out.println("第"+i+"次 "+"斷路器狀態：" + c.isCircuitBreakerOpen());
			
			HealthCounts hc = c.getMetrics().getHealthCounts();
			//通過這倆，可以知道，當斷路器打開后，請求總數就不再進行累加
			System.out.println("健康度狀態： " + c.isCircuitBreakerOpen() + " ，請求數量： " + hc.getTotalRequests());
		}
		
		//因為斷路器在休眠五秒後會處於半開半閉狀態，這時候hystrix會常識性進行連接
		//若可以正常連接，則清空之前的斷路器的計數器和健康度等狀態。否則，繼續休眠五秒等待下一次重試的到來
		
		//斷路器會在休眠五秒後自動執行到這裡
		for(int i = 0; i < 22; i++) {
			RequestVolumeThreshold c = new RequestVolumeThreshold(isTimeout);
			c.execute();
			System.out.println("第"+i+"次 "+"斷路器狀態：" + c.isCircuitBreakerOpen());
			
			HealthCounts hc = c.getMetrics().getHealthCounts();
			//通過這倆，可以知道，當斷路器打開后，請求總數就不再進行累加
			System.out.println("健康度狀態： " + c.isCircuitBreakerOpen() + " ，請求數量： " + hc.getTotalRequests());
			
			/**
			 * 按照我這裡代碼的邏輯，在第一次休眠后，即便再次達到超時次數，也不會在詞休眠了
			 * 這是業務邏輯的問題，不是斷路器本身的問題。
			 * 所以當你看到這裡打印的結果如果有以上疑問的話，不用過度糾結，邏輯問題而已。
			 */
			if(c.isCircuitBreakerOpen()) {
				isTimeout = false;
				System.out.println("=========== 開始休眠? " + (!isTimeout));
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 線程池隔離策略
	 * 當並發數量超過線程池核心配置數量的時候，
	 * hystrix就會自動隔離超過核心配置數的請求，直接觸發回退請求。
	 */
	public static void testCircuitStrategeByThread() {
		//全局性配置，設置線程池數量
		ConfigurationManager
		.getConfigInstance()
		.setProperty("hystrix.threadpool.default.coreSize"
				//3條記錄
				, 3);
		
		for(int i = 0; i < 8; i++) {
			CircuitStrategy c = new CircuitStrategy(i);
			c.queue();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 線程池隔離策略
	 * 當並發數量超過信號量總數的時候，
	 * hystrix就會自動隔離超過這一總數的請求，直接觸發回退請求。
	 */
	public static void testCircuitStrategeBySemaphore() {
		//全局性配置，設置信號量總數
		ConfigurationManager
		.getConfigInstance()
		.setProperty("hystrix.command.default.execution.isolation.strategy"
				, ExecutionIsolationStrategy.SEMAPHORE);
		ConfigurationManager
		.getConfigInstance()
		.setProperty("hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests"
				, 3);
		
		for(int i = 0; i < 8; i++) {
			final int index = i;
			//這裡的寫法要注意，必須要把CircuitStrategy放到線程中執行。
			//否則上面的兩個全局配置不會生效。但為什麼非要放到線程中的原理我不知道。。。
			Thread t = new Thread() {
				public void run() {
					CircuitStrategy c = new CircuitStrategy(index);
					c.execute();
				}
			};
			t.start();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 測試是否來自緩存的響應
	 */
	public static void testCacheKey() {
		//要使用緩存，首先要定義這個上下文
		//重點在於！只能在一次請求中才能使用緩存，多次請求不可以！！
		//Hystrix的緩存實質就是“合並請求”。對相同URL，不同參數的請求合並為一個批處理，犧牲Hystrix的性能換取時間
		HystrixRequestContext ctx = HystrixRequestContext.initializeContext();
		String cacheKey = "cache-key";
		
		CacheKey c1 = new CacheKey(cacheKey);
		CacheKey c2 = new CacheKey(cacheKey);
		CacheKey c3 = new CacheKey(cacheKey);
		c1.execute();
		c2.execute();
		c3.execute();
		
		System.out.println("命令c1,是否讀取緩存： " + c1.isResponseFromCache());
		System.out.println("命令c2,是否讀取緩存： " + c2.isResponseFromCache());
		System.out.println("命令c3,是否讀取緩存： " + c3.isResponseFromCache());
		
		//清空緩存
		HystrixRequestCache cache = HystrixRequestCache.getInstance(
				//注意這裡的key要和類中配置的key相同才可
				HystrixCommandKey.Factory.asKey("CommandKey"), 
				HystrixConcurrencyStrategyDefault.getInstance());
		cache.clear(cacheKey);
		
		CacheKey c4 = new CacheKey(cacheKey);
		c4.execute();
		System.out.println("命令c4,是否讀取緩存： " + c4.isResponseFromCache());
		System.out.println("命令c2,是否讀取緩存： " + c2.isResponseFromCache());
		
		//最後要關閉緩存
		ctx.shutdown();
	}
}
