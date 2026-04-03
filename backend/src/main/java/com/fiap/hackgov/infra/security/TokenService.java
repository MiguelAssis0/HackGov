package com.fiap.hackgov.infra.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fiap.hackgov.entities.User;
import com.fiap.hackgov.infra.exceptions.TokenInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String SECRET_KEY;
    private static final String ISSUER = "HackGov";

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        LocalDateTime fullDate = LocalDateTime.now().plusHours(2);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getEmail())
                .withClaim("role", "ROLE_" + user.getRole().name())
                .withExpiresAt(fullDate.toInstant(ZoneOffset.of("-03:00")))
                .sign(algorithm);
    }

    public String getSubject(String token) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        }catch (JWTVerificationException e){
            throw new TokenInvalidException("Token invalid or expired");
        }
    }

    public String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    public String validateToken(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            throw new TokenInvalidException("Authorization header missing or malformed");
        }
        return getSubject(token);
    }


}
