package rw.afriteck.pms.dtos;

public record LoginResponse(String accessToken, String refreshToken, long expiresInSeconds) {
}
