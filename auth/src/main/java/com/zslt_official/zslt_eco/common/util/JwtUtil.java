package com.zslt_official.zslt_eco.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;


/**
 * @Description: JWT工具类
 * @Author: xunshi
 * @Date: 2025/7/28 17:00
 */
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    /**
     * @Description  创建JWT
     * @Author xunshi
     * @Date 2025/7/28 17:16
     * @Param [secretKey, ttlMillis, claims]
     * @Return String
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String,Object> claims){
        try {
            // 签名算法
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

            // 过期时间
            long expMillis = System.currentTimeMillis() + ttlMillis;
            Date exp = new Date(expMillis);
            //移除旧的exp，复用claims
            claims.remove("exp");
            // 载荷payload
            JwtBuilder builder = Jwts.builder()
                    .setClaims(claims)
                    .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                    .setExpiration(exp);
            return builder.compact();
        }catch (Exception e){
            logger.error("JwtUtil createJWT error: secretKey={} , ttlMillis={} , claims={} ,ex=:{}",secretKey,ttlMillis,claims,e.getMessage());
            throw new RuntimeException("JWT创建失败："+e.getMessage());
        }
    }
    /**
     * @Description  解析JWT
     * @Author xunshi
     * @Date 2025/7/28 18:20
     * @Param [secretKey, token]
     * @Return void
     */
    public static Claims parseJWT(String secretKey,String token){
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }
}
