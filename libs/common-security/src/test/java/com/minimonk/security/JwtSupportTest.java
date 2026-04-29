package com.minimonk.security;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtSupportTest {
    @Test
    void issuesAndParsesRoles() {
        JwtSupport support = new JwtSupport("dev-secret-dev-secret-dev-secret-123456", "minimonk");
        UUID subject = UUID.randomUUID();

        var claims = support.parse(support.issue(subject, "customer", List.of("CUSTOMER")));

        assertEquals(subject.toString(), claims.getSubject());
        assertEquals("customer", claims.get("username", String.class));
        assertEquals(List.of("CUSTOMER"), claims.get("roles", List.class));
    }

    @Test
    void rejectsShortSecrets() {
        assertThrows(IllegalArgumentException.class, () -> new JwtSupport("too-short", "minimonk"));
    }
}
