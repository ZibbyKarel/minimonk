package com.minimonk.observability;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

public final class TraceContext {
    public static final String TRACEPARENT = "traceparent";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TraceContext() {
    }

    public static String ensureTraceparent(String incoming) {
        if (incoming != null && incoming.matches("^[\\da-f]{2}-[\\da-f]{32}-[\\da-f]{16}-[\\da-f]{2}$")) {
            return incoming;
        }
        return "00-" + randomHex(16) + "-" + randomHex(8) + "-01";
    }

    public static String traceId(String traceparent) {
        return Optional.ofNullable(traceparent)
                .filter(value -> value.length() >= 35)
                .map(value -> value.substring(3, 35))
                .orElse("unknown");
    }

    private static String randomHex(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return HexFormat.of().formatHex(value);
    }
}
