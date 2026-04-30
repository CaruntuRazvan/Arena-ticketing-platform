package com.arena.notification.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.arena.notification.service..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 1. Logare INTRARE (Filtrare automată pentru parole)
        String argsString = Arrays.toString(args);
        if (methodName.toLowerCase().contains("login") || methodName.toLowerCase().contains("register")) {
            argsString = "[DATE SENSIBILE PROTEJATE]";
        }

        log.info(">>> [SERVICE START] {} | Argumente: {}", methodName, argsString);

        long start = System.currentTimeMillis();

        try {
            // Execuția propriu-zisă a metodei din Service
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - start;

            // 2. Logare SUCCES (Include timpul de execuție)
            log.info("<<< [SERVICE END] {} | Finalizat în {} ms | Rezultat: {}",
                    methodName, executionTime, (result != null ? "Obiect returnat" : "null"));

            return result;

        } catch (Throwable e) {
            // 3. Logare EROARE (Se va duce automat în errors.log datorită configurației XML)
            log.error("!!! [SERVICE ERROR] {} | Mesaj: {}", methodName, e.getMessage());
            throw e; // Aruncăm eroarea mai departe să o prindă ControllerAdvice-ul
        }
    }
}
