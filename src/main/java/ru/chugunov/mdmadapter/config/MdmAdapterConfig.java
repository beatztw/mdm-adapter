package ru.chugunov.mdmadapter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.service.strategy.ClientServiceStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class MdmAdapterConfig {

    private final List<ClientServiceStrategy> clientServiceStrategies;

    @Bean
    public Map<MdmMessageOutboxTarget, ClientServiceStrategy> clientServiceByTarget() {
        return clientServiceStrategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ClientServiceStrategy::getTarget,
                        Function.identity()
                ));
    }
}
