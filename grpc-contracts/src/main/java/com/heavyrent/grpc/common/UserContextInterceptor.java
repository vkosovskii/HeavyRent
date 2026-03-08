package com.heavyrent.grpc.common;

import io.grpc.*;

public class UserContextInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> ID_KEY = Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> ROLE_KEY = Metadata.Key.of("x-user-role", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String id = metadata.get(ID_KEY);
        String role = metadata.get(ROLE_KEY);

        UserContext userContext = (id == null || id.isBlank())
                ? new UserContext("anonymous", null)
                : new UserContext(id, role);
        Context context = Context.current().withValue(UserContextHolder.KEY, userContext);
        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
