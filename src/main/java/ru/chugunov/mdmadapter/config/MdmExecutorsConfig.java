package ru.chugunov.mdmadapter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.chugunov.mdmadapter.property.MdmExecutorsProperty;

import java.util.concurrent.*;

@Configuration
@RequiredArgsConstructor
public class MdmExecutorsConfig {

    private final MdmExecutorsProperty mdmExecutorsProperty;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService outboxElasticExecutor() {
        return createElasticExecutor(mdmExecutorsProperty.getOutboxElastic().getThreads(),
                mdmExecutorsProperty.getOutboxElastic().getQueueCapacity());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService externalServiceExecutor() {
        return createElasticExecutor(mdmExecutorsProperty.getExternalService().getThreads(),
                mdmExecutorsProperty.getExternalService().getQueueCapacity());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService resendMdmMessageOutboxExecutor() {
        return createElasticExecutor(mdmExecutorsProperty.getResendMdmMessageOutbox().getThreads(),
                mdmExecutorsProperty.getResendMdmMessageOutbox().getQueueCapacity());
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
