package com.heavyrent.grpc.common;

import io.grpc.*;

public class UserContextClientInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> ID_KEY = Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> ROLE_KEY = Metadata.Key.of("x-user-role", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        UserContext context = UserContextHolder.KEY.get();
        String id = context.keycloakId();
        String role = context.role();

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(ID_KEY, id);
                headers.put(ROLE_KEY, role);
                super.start(responseListener, headers);
            }
        };
    }
}
