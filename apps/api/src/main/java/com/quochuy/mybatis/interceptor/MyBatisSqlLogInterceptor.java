package com.quochuy.mybatis.interceptor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Properties;

@Intercepts({
		@Signature(
				type = StatementHandler.class,
				method = "prepare",
				args = {Connection.class, Integer.class}
		)
})
public class MyBatisSqlLogInterceptor implements Interceptor {
	private static final Logger log = LoggerFactory.getLogger(MyBatisSqlLogInterceptor.class);
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler handler = (StatementHandler) invocation.getTarget();
		BoundSql boundSql = handler.getBoundSql();
		
		// SQL sau khi dynamic build (còn ? placeholders)
		String sql = boundSql.getSql();
		if (sql != null) {
			sql = sql.replaceAll("\\s+", " ").trim(); // gọn lại cho dễ đọc
		}
		
		Object paramObj = boundSql.getParameterObject();
		
		log.debug("[MYBATIS] SQL: {}", sql);
		log.debug("[MYBATIS] PARAM_OBJECT: {}", paramObj);
		
		return invocation.proceed();
	}
	
	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}
	
	@Override
	public void setProperties(Properties properties) {}
}
