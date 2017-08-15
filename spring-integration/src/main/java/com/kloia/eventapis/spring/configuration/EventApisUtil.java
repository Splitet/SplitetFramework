package com.kloia.eventapis.spring.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

@Slf4j
@Configuration
@Import(EventApisConfiguration.class)
public class EventApisUtil implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {
    private ApplicationContext applicationContext;
    private ConfigurableListableBeanFactory beanFactory;
    @Autowired
    private EventApisConfiguration eventApisConfiguration;
    private Environment environment;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        log.info("ImportBeanDefinitionRegistrar");
        if (this.beanFactory == null) {
            return;
        }
        EventApisConfiguration bean = this.beanFactory.getBean(EventApisConfiguration.class);

    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    @Override
    public void setEnvironment(Environment environment) {

        this.environment = environment;
    }
}
