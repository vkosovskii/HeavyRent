package com.heavyrent.grpc.common;

import io.grpc.Context;

public class UserContextHolder {
    public static final Context.Key<UserContext> KEY = Context.key("user-context");
}
