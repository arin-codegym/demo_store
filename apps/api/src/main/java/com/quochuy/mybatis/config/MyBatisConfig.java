package com.quochuy.mybatis.config;

import com.quochuy.mybatis.interceptor.MyBatisSqlLogInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {
	@Bean
	public Interceptor myBatisSqlLogInterceptor() {
		return new MyBatisSqlLogInterceptor();
	}
}
