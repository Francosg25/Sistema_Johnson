package com.johnson.practica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PasswordChangeInterceptor passwordChangeInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(PasswordChangeInterceptor passwordChangeInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        this.passwordChangeInterceptor = passwordChangeInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passwordChangeInterceptor);
        registry.addInterceptor(rateLimitInterceptor);
    }
}
