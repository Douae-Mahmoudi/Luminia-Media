package com.collection.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppConfig.class)
class AppConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void corsConfigurerBean_ShouldBePresentInContext() {
        WebMvcConfigurer configurer = context.getBean(WebMvcConfigurer.class);

        assertThat(configurer).isNotNull();
    }
}