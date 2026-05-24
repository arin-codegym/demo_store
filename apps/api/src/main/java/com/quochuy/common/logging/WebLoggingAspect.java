package com.quochuy.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * ASPECT LOGGING TOÀN CỤC Cơ chế này tự động "nghe" mọi phương thức bên trong các lớp có đánh dấu
 *
 * @RestController. Giúp bạn biết chính xác: Controller nào chạy, tham số truyền vào là gì, và mất
 * bao lâu để xử lý.
 */
@Aspect
@Component
public class WebLoggingAspect {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * @Around: Bao quanh phương thức. Chạy cả trước và sau khi Controller thực thi.
	 * "within(@org.springframework.web.bind.annotation.RestController *)" -> Áp dụng cho tất cả các
	 * class có annotation @RestController.
	 */
	@Around("within(@org.springframework.web.bind.annotation.RestController *)")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		// 1. Lấy thông tin về Request hiện tại từ Context
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		// 2. Lấy thông tin về phương thức đang được gọi
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = signature.getName();
		String[] parameterNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();
		long start = System.currentTimeMillis();
		// LOG TRƯỚC KHI CHẠY (Before)
		logger.info("--------------------------------------------------");
		logger.info(">>> INCOMING REQUEST: [{}] {}", request.getMethod(), request.getRequestURI());
		logger.info(">>> CONTROLLER: {}.{}", className, methodName);
		if (args.length > 0) {
			logger.info(">>> PARAMS: {}", Arrays.toString(args));
		}
		// 3. Thực thi logic thực tế của Controller
		Object result;
		try {
			result = joinPoint.proceed();
		} catch (Throwable throwable) {
			// Nếu có lỗi, ta log lỗi ở đây (hoặc để GlobalExceptionHandler lo)
			logger.error("!!! EXECUTION FAILED: {}.{}", className, methodName);
			throw throwable;
		}
		// 4. Tính toán thời gian thực hiện
		long executionTime = System.currentTimeMillis() - start;
		// LOG SAU KHI CHẠY XONG (After)
		logger.info("<<< COMPLETED: {}.{} in {}ms", className, methodName, executionTime);
		logger.info("--------------------------------------------------");
		return result;
	}
}
