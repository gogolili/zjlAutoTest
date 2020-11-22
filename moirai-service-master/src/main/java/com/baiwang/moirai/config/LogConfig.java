package com.baiwang.moirai.config;

import com.baiwang.moirai.filter.LogIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
@Configuration
public class LogConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private LogIntercepter logIntercepter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logIntercepter).addPathPatterns("/**");
    }


}
