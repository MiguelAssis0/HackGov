package com.fiap.hackgov.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.hackgov.entities.User;
import com.fiap.hackgov.infra.exceptions.TokenInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String SECRET_KEY;

    @Value("${api.security.token.refresh-secret}")
    private String REFRESH_SECRET_KEY;

    private static final String ISSUER              = "HackGov";
    private static final int    ACCESS_TOKEN_MINUTES = 15;
    private static final int    REFRESH_TOKEN_DAYS   = 7;

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(ACCESS_TOKEN_MINUTES);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getEmail())
                .withClaim("role", "ROLE_" + user.getRole().name())
                .withClaim("type", "access")
                .withExpiresAt(expiration.toInstant(ZoneOffset.of("-03:00")))
                .sign(algorithm);
    }

    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            DecodedJWT decoded = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "access")
                    .build()
                    .verify(token);
            return decoded.getSubject();
        } catch (JWTVerificationException e) {
            throw new TokenInvalidException("Token invalid or expired");
        }
    }

    public Date getExpiration(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getExpiresAt();
        } catch (JWTVerificationException e) {
            throw new TokenInvalidException("Token invalid or expired");
        }
    }

    public LocalDateTime getExpirationAsLocalDateTime(String token) {
        return getExpiration(token)
                .toInstant()
                .atOffset(ZoneOffset.of("-03:00"))
                .toLocalDateTime();
    }


    public String generateRefreshToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(REFRESH_SECRET_KEY);
        LocalDateTime expiration = LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getEmail())
                .withClaim("type", "refresh")
                .withExpiresAt(expiration.toInstant(ZoneOffset.of("-03:00")))
                .sign(algorithm);
    }

    public String getSubjectFromRefreshToken(String refreshToken) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(REFRESH_SECRET_KEY);
            DecodedJWT decoded = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "refresh")
                    .build()
                    .verify(refreshToken);
            return decoded.getSubject();
        } catch (JWTVerificationException e) {
            throw new TokenInvalidException("Refresh token invalid or expired");
        }
    }

    public String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    public void validateToken(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            throw new TokenInvalidException("Authorization header missing or malformed");
        }
        getSubject(token);
    }
}