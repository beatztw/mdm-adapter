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
    public ExecutorService processOutboxEventExecutor() {
        return createElasticExecutor(mdmExecutorsProperty.getProcessOutboxEvent().getThreads(),
                mdmExecutorsProperty.getProcessOutboxEvent().getQueueCapacity());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService userDataIntegrationServiceExecutor() {
        return createElasticExecutor(mdmExecutorsProperty.getUserDataIntegrationService().getThreads(),
                mdmExecutorsProperty.getUserDataIntegrationService().getQueueCapacity());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService scheduledResendMdmMessageExecutor() {
        return createElasticExecutor(mdmExecutorsProperty.getScheduledResendMdmMessage().getThreads(),
                mdmExecutorsProperty.getScheduledResendMdmMessage().getQueueCapacity());
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
