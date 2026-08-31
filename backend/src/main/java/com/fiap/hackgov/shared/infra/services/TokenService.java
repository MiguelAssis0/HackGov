package com.fiap.hackgov.shared.infra.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.shared.infra.exceptions.TokenInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String SECRET_KEY;

    @Value("${api.security.token.refresh-secret}")
    private String REFRESH_SECRET_KEY;

    private static final String ISSUER = "HackGov";

    @org.springframework.beans.factory.annotation.Value("${api.security.token.expiration-minutes:480}")
    private int accessTokenMinutes = 480;

    private static final int REFRESH_TOKEN_DAYS = 7;

    public String generateToken(User user, UUID sessionId) {

        LocalDateTime expiration = LocalDateTime.now().plusMinutes(accessTokenMinutes);

        return JWT.create().withIssuer(ISSUER).withSubject(user.getEmail()).withClaim("role", "ROLE_" + user.getRole().name()).withClaim("type", "access").withClaim("sid", sessionId.toString()).withJWTId(UUID.randomUUID().toString()).withExpiresAt(expiration.toInstant(ZoneOffset.of("-03:00"))).sign(accessAlgorithm());
    }

    public String generateRefreshToken(User user, UUID sessionId) {

        LocalDateTime expiration = LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS);

        return JWT.create().withIssuer(ISSUER).withSubject(user.getEmail()).withClaim("type", "refresh").withClaim("sid", sessionId.toString()).withJWTId(UUID.randomUUID().toString()).withExpiresAt(expiration.toInstant(ZoneOffset.of("-03:00"))).sign(refreshAlgorithm());
    }

    public String getSubject(String token) {

        return verifyAccessToken(token).getSubject();
    }

    public String getSubjectFromRefreshToken(String refreshToken) {

        return verifyRefreshToken(refreshToken).getSubject();
    }

    public UUID getSessionId(String token) {
        return UUID.fromString(verifyAccessToken(token).getClaim("sid").asString());
    }

    public UUID getSessionIdFromRefreshToken(String token) {
        return UUID.fromString(verifyRefreshToken(token).getClaim("sid").asString());
    }

    public Date getExpiration(String token) {

        return verifyAccessToken(token).getExpiresAt();
    }

    public LocalDateTime getExpirationAsLocalDateTime(String token) {

        return getExpiration(token).toInstant().atOffset(ZoneOffset.of("-03:00")).toLocalDateTime();
    }

    public String extractToken(HttpServletRequest request) {

        return extractToken(request.getHeader("Authorization"));
    }

    public String extractToken(String authorization) {

        if (authorization == null || authorization.isBlank()) {

            return null;
        }

        if (!authorization.startsWith("Bearer ")) {

            return null;
        }

        return authorization.replace("Bearer ", "").trim();
    }

    public void validateToken(HttpServletRequest request) {

        String token = extractToken(request);

        if (token == null) {

            throw new TokenInvalidException("Authorization header missing or malformed");
        }

        getSubject(token);
    }

    private Algorithm accessAlgorithm() {

        return Algorithm.HMAC256(SECRET_KEY);
    }

    private Algorithm refreshAlgorithm() {

        return Algorithm.HMAC256(REFRESH_SECRET_KEY);
    }

    private DecodedJWT verifyAccessToken(String token) {

        try {

            return JWT.require(accessAlgorithm()).withIssuer(ISSUER).withClaim("type", "access").build().verify(token);

        } catch (JWTVerificationException e) {

            throw new TokenInvalidException("Token invalid or expired");
        }
    }

    private DecodedJWT verifyRefreshToken(String token) {

        try {

            return JWT.require(refreshAlgorithm()).withIssuer(ISSUER).withClaim("type", "refresh").build().verify(token);

        } catch (JWTVerificationException e) {

            throw new TokenInvalidException("Refresh token invalid or expired");
        }
    }
}
