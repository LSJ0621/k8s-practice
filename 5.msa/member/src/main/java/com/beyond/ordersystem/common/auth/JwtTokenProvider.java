package com.beyond.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
//    @Value는 yml파일의 값을 주입할때 씀.
    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKey}")
    private String secretKey;

    private Key ENCRYPT_SECRET_KEY;

    @Value("${jwt.expirationRT}")
    private int expirationRT;

    @Value("${jwt.secretKeyRT}")
    private String secretKeyRT;
    private Key ENCRYPT_RT_SECRET_KEY;

//    생성자가 호출되고, 스프링빈이 만들어진 직후에 아래 메서드 바로 실행하는 어노테이션
    @PostConstruct
    public void init(){
        ENCRYPT_SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
        ENCRYPT_RT_SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRT), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String email,String role){
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role",role);
        Date now = new Date();
//        claims는 사용자 정보(페이로드 정보)
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expiration*60*1000L)) //30분 세팅
                .signWith(ENCRYPT_SECRET_KEY)
                .compact();
        return token;
    }

    public String createRefreshToken(String email,String role){
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role",role);
        Date now = new Date();
//        claims는 사용자 정보(페이로드 정보)
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expirationRT*60*1000L)) //30분 세팅
                .signWith(ENCRYPT_RT_SECRET_KEY)
                .compact();
        return token;
    }
}
