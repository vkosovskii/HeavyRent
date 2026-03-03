package com.heavyrent.user.config;

import com.heavyrent.grpc.common.UserContextInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Bean
    @GrpcGlobalServerInterceptor
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }
}
