package ru.devinvader.bank.common.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnClass(RestClient.class)
public class RestClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClient.Builder restClientBuilder(ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        var builder = RestClient.builder();
        var observationRegistry = observationRegistryProvider.getIfAvailable();
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        return builder;
    }
}
