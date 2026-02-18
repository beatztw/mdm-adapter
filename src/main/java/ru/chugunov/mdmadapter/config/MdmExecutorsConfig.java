package ru.chugunov.mdmadapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class MdmExecutorsConfig {

    @Value("${mdm.executors.outbox-elastic.threads}")
    private Integer outboxElasticThreads;
    @Value("${mdm.executors.outbox-elastic.queue-capacity}")
    private Integer outboxElasticQueueCapacity;
    @Value("${mdm.executors.resend-mdm-message-outbox.threads}")
    private Integer resendMdmMessageOutboxThreads;
    @Value("${mdm.executors.resend-mdm-message-outbox.queue-capacity}")
    private Integer resendMdmMessageOutboxQueueCapacity;
    @Value("${mdm.executors.external-service.threads}")
    private Integer externalServiceThreads;
    @Value("${mdm.executors.external-service.queue-capacity}")
    private Integer externalServiceQueueCapacity;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService outboxElasticExecutor() {
        return createElasticExecutor(outboxElasticThreads, outboxElasticQueueCapacity);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService externalServiceExecutor() {
        return createElasticExecutor(externalServiceThreads, externalServiceQueueCapacity);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService resendMdmMessageOutboxExecutor() {
        return createElasticExecutor(resendMdmMessageOutboxThreads, resendMdmMessageOutboxQueueCapacity);
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
