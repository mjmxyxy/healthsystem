package com.healthy.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.healthy.entity.User;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "change_this_secret_please");
    private static final long EXPIRES_MS = 1000L * 60 * 60 * 24; // 24h

    public static String generateToken(User user) {
        Algorithm alg = Algorithm.HMAC256(SECRET);
        return JWT.create()
                .withSubject(user.getAccount())
                .withClaim("userId", user.getId())
                .withClaim("role", user.getRole())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRES_MS))
                .sign(alg);
    }

    public static DecodedJWT verify(String token) throws JWTVerificationException {
        Algorithm alg = Algorithm.HMAC256(SECRET);
        JWTVerifier verifier = JWT.require(alg).build();
        return verifier.verify(token);
    }
}
