package com.heavyrent.equipment.config;

import com.heavyrent.grpc.common.UserContextInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GrpcConfig {

    @Bean
    @GrpcGlobalServerInterceptor
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }
}
