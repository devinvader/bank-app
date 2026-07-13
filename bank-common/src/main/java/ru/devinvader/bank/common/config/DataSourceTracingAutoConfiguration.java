package ru.devinvader.bank.common.config;

import javax.sql.DataSource;

import io.micrometer.observation.ObservationRegistry;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import net.ttddyy.observation.tracing.DataSourceObservationListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({DataSource.class, ProxyDataSourceBuilder.class})
public class DataSourceTracingAutoConfiguration {

    @Bean
    public static BeanPostProcessor dataSourceObservationBeanPostProcessor(
            ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof DataSource dataSource && !(bean instanceof ProxyDataSource)) {
                    var listener = new DataSourceObservationListener(
                            () -> observationRegistryProvider.getIfAvailable(() -> ObservationRegistry.NOOP));
                    return ProxyDataSourceBuilder.create(dataSource)
                            .name(beanName)
                            .listener(listener)
                            .methodListener(listener)
                            .build();
                }
                return bean;
            }
        };
    }
}
