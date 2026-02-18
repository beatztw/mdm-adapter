package ru.chugunov.mdmadapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class MdmExecutorsConfig {

    @Value("${mdm.executors.outbox-elastic.threads}")
    private Integer OutboxElasticThreads;
    @Value("${mdm.executors.outbox-elastic.queue-capacity}")
    private Integer OutboxElasticQueueCapacity;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService outboxElasticExecutor(){
        int threads = OutboxElasticThreads;
        int queueCapacity = OutboxElasticQueueCapacity;

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
