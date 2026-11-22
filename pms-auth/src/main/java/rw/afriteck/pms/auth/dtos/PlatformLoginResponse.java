package rw.afriteck.pms.auth.dtos;

public record PlatformLoginResponse(String accessToken, long expiresInSeconds, String role, boolean mustChangePassword) {}

