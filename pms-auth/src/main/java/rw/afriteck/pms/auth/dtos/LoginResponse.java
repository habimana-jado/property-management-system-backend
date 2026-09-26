package rw.afriteck.pms.auth.dtos;

public record LoginResponse(String accessToken, String refreshToken, long expiresInSeconds) {
}
