package com.example.aquarkdemo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CountTimeAspect {

    @Pointcut("@annotation(com.example.aquarkdemo.aspect.CountTime)")
    public void countTime() {
    }

    @Around("countTime()")
    public Object countTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long endTime = System.currentTimeMillis();
        log.info("方法名: {} 執行時間: {}", proceedingJoinPoint.getSignature().getName() ,endTime - startTime + "ms");
        return result;
    }
}
