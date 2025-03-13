package com.beyond.ordersystem.common.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

public class FeignTokenConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return request -> {
          String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
          request.header(HttpHeaders.AUTHORIZATION,token);
        };
    }
}
