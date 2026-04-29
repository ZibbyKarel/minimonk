package com.minimonk.user.auth;

import java.util.List;
import java.util.UUID;

public record LoginResponse(String token, UUID userId, String username, List<String> roles) {
}
