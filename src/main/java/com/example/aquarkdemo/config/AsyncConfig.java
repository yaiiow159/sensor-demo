package com.example.aquarkdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Configuration
public class AsyncConfig {

    private static final int CORE_CPU_SIZE = Runtime.getRuntime().availableProcessors();

    private static final int MAX_POOL_SIZE = CORE_CPU_SIZE * 2;
    @Bean("httpConnectionThreadPoolExecutor")
    public ThreadPoolExecutor threadPoolExecutor() {
        long keepAliveTime = 30L;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(500);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_CPU_SIZE, MAX_POOL_SIZE,
                keepAliveTime, timeUnit, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Bean("batchThreadPoolExecutor")
    public ThreadPoolExecutor batchThreadPoolExecutor() {
        long keepAliveTime = 30L;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(250);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_CPU_SIZE / 2, CORE_CPU_SIZE,
                keepAliveTime, timeUnit, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
