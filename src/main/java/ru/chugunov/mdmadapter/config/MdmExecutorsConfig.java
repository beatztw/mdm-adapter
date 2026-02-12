package ru.chugunov.mdmadapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class MdmExecutorsConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService outboxElasticExecutor(){
        int threads = 10;
        int queueCapacity = 1000;

        return createElasticExecutor(threads, queueCapacity);
    }

    private ThreadPoolExecutor createElasticExecutor(int threads, int queueCapacity) {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueCapacity);

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            threads, threads,
                60L, TimeUnit.SECONDS,
                queue, new ThreadPoolExecutor.AbortPolicy()
        );

        threadPoolExecutor.allowCoreThreadTimeOut(true);

        return threadPoolExecutor;
    }
}
