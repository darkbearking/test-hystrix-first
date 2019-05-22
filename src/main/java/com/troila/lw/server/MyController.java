package com.troila.lw.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

	@RequestMapping(value = "/normalHello" ,method = RequestMethod.GET)
	public String normalHello() {
		return "hello world";
	}
	
	@RequestMapping(value = "/errorHello" ,method = RequestMethod.GET)
	public String errorhello() throws Exception{
		//用休眠模擬數據庫連接異常
		Thread.sleep(10000);
		return "Error hello world";
	}
}
