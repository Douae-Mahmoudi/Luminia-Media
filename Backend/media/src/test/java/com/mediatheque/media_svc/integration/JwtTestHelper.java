package com.mediatheque.media_svc.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtTestHelper {

    private static final String SECRET_BASE64 = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private static SecretKey key() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String userToken() {
        return Jwts.builder()
                .subject("user-test@mediatheque.com")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key())
                .compact();
    }

    public static String adminToken() {
        return Jwts.builder()
                .subject("admin-test@mediatheque.com")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key())
                .compact();
    }

    public static String userId()  { return "1"; }
    public static String adminId() { return "2"; }
}