package com.arena.auth.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.arena.auth.service..*.*(..))")
    public Object logServiceFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        // Protecție date sensibile
        String argsString = Arrays.toString(args);
        if (methodName.toLowerCase().contains("login") ||
                methodName.toLowerCase().contains("password") ||
                methodName.toLowerCase().contains("api-key")) {
            argsString = "[CONFIDENȚIAL]";
        }

        log.info("==> [{} START] {} | Args: {}", className, methodName, argsString);

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            log.info("<== [{} END] {} | Time: {}ms", className, methodName, executionTime);
            return result;

        } catch (Throwable e) {
            log.error("!!! [{} ERROR] {} | Message: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}