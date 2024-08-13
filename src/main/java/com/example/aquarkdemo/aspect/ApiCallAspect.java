//package com.example.aquarkdemo.aspect;
//
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//
///**
// * 紀錄 api呼叫的總耗時
// */
//@Aspect
//@Component
//@Slf4j
//public class ApiCallAspect {
//
//    @Pointcut("execution(public * com.example.aquarkdemo.service.impl.HttpRequestServiceImpl.callApi())")
//    public void apiCallPointCut() {}
//
//    @Around("apiCallPointCut()")
//    public void apiCallTimeCount(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = Instant.now().toEpochMilli();
//        try {
//            joinPoint.proceed();
//        } finally {
//            long end = Instant.now().toEpochMilli();
//            log.info("呼叫api花費 {} 毫秒", end - start);
//        }
//    }
//
//}
