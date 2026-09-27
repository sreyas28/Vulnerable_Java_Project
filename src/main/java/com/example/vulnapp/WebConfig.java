package com.example.vulnapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final ContentSecurityPolicyInterceptor cspInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, ContentSecurityPolicyInterceptor cspInterceptor) {
        this.authInterceptor = authInterceptor;
        this.cspInterceptor = cspInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/access/**");
        registry.addInterceptor(cspInterceptor).addPathPatterns("/api/xss/**");
    }
}
