package com.zslt_official.zslt_eco.service.impl;

import com.alibaba.fastjson2.JSON;
import com.zslt_official.zslt_eco.common.constant.JwtClaimsConstant;
import com.zslt_official.zslt_eco.common.constant.RedisConstant;
import com.zslt_official.zslt_eco.common.enums.JwtEnum;
import com.zslt_official.zslt_eco.common.exception.TokenExpiredException;
import com.zslt_official.zslt_eco.common.exception.TokenInvalidException;
import com.zslt_official.zslt_eco.common.exception.TokenRevokedException;
import com.zslt_official.zslt_eco.common.util.JwtUtil;
import com.zslt_official.zslt_eco.config.JwtProperties;
import com.zslt_official.zslt_eco.dto.req.RevokeTokenReqDTO;
import com.zslt_official.zslt_eco.dto.req.UserLoginReqDTO;
import com.zslt_official.zslt_eco.dto.resp.UserLoginRespDTO;
import com.zslt_official.zslt_eco.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Token 业务逻辑接口实现类
 * @Author: xunshi
 * @Date: 2025/8/4 15:23
 */
@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private  JwtProperties jwtProperties; //Jwt属性配置,@Autowired注解会加载最新的bean
    @Autowired
    private  StringRedisTemplate redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
    @Override
    public UserLoginRespDTO getToken(UserLoginReqDTO requestParam) {
        logger.info("TokenServiceImpl getToken start, param: {}", JSON.toJSONString(requestParam));

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, requestParam.getUserId());
        claims.put(JwtClaimsConstant.SERVER_NAME, requestParam.getServerName());
        claims.put(JwtClaimsConstant.TOKEN_CATEGORY, "at");

        String accessToken;
        String refreshToken;

        try {
            accessToken = JwtUtil.createJWT(
                    jwtProperties.getAccessToken().getSecretKey(),
                    jwtProperties.getAccessToken().getTtlMillis(),
                    claims);
        }catch (RuntimeException e){
            logger.error("TokenServiceImpl getToken error: 创建访问令牌失败,userId={},serverName={}"
            ,requestParam.getUserId(),requestParam.getServerName());
            throw e;
        }
        try{
            claims.put(JwtClaimsConstant.TOKEN_CATEGORY, "rt");
            refreshToken = JwtUtil.createJWT(
                    jwtProperties.getRefreshToken().getSecretKey(),
                    jwtProperties.getRefreshToken().getTtlMillis(),
                    claims);
        }catch (RuntimeException e){
            logger.error("TokenServiceImpl getToken error: 创建刷新令牌失败,userId={},serverName={}"
            ,requestParam.getUserId(),requestParam.getServerName());
            throw e;
        }

        UserLoginRespDTO response = UserLoginRespDTO.builder()
                .userId(requestParam.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        logger.info("TokenServiceImpl getToken end, result: {}", JSON.toJSONString(response));
        return response;

    }

    @Override
    public Map<String,String> refreshToken(String refreshToken) {
            Claims claims = validateToken(refreshToken, "rt"); //验证refresh_token
            String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
            String serverName = claims.get(JwtClaimsConstant.SERVER_NAME).toString();
            String key = String.format(RedisConstant.TOKEN_BLACK_LIST,userId,serverName,"rt");
            Date expiration = claims.getExpiration(); //得到refresh_token的过期时间
            String newAccessToken;
            String newRefreshToken;

            //创建新的access_token
            try {
                claims.put(JwtClaimsConstant.TOKEN_CATEGORY, "at");
                newAccessToken = JwtUtil.createJWT(
                        jwtProperties.getAccessToken().getSecretKey(),
                        jwtProperties.getAccessToken().getTtlMillis(),
                        claims);
            }catch (RuntimeException e){
                logger.error("TokenServiceImpl refreshToken error: 创建访问令牌失败,userId={},serverName={}"
                ,userId,serverName);
                throw e;
            }

            //创建新的refresh_token实现token轮换
            newRefreshToken = refreshToken;
            //判断refresh_token剩余时间，如果较短(则创建新的refresh_token实现token轮换)
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            if(remainingTime < TimeUnit.DAYS.toMillis(jwtProperties.getRefreshToken().getRemainDate())) {
                logger.info("TokenServiceImpl refreshToken: refresh_token剩余时间小于{}天，创建新的refresh_token", jwtProperties.getRefreshToken().getRemainDate());
                try {
                    claims.put(JwtClaimsConstant.TOKEN_CATEGORY, "rt");
                    newRefreshToken = JwtUtil.createJWT(
                            jwtProperties.getRefreshToken().getSecretKey(),
                            jwtProperties.getRefreshToken().getTtlMillis(),
                            claims);
                }catch (RuntimeException e){
                    logger.error("TokenServiceImpl refreshToken error: 创建刷新令牌失败,userId={},serverName={}"
                    ,userId,serverName);
                    throw e;
                }
                //将旧的refresh_token加入黑名单
                redisTemplate.opsForValue().set(key, "revoked", jwtProperties.getRefreshToken().getTtlMillis(), TimeUnit.MILLISECONDS);
            }

            Map<String,String> tokens = new HashMap<>();
            tokens.put("access_token", newAccessToken);
            tokens.put("refresh_token", newRefreshToken);

            logger.info("TokenServiceImpl refreshToken result: {}", JSON.toJSONString(tokens));
            return tokens;

    }

    @Override
    public boolean revokeToken(@RequestBody RevokeTokenReqDTO requestParam) {
        try {
            // 创建一个用户级别的黑名单标记
            String key = String.format(RedisConstant.TOKEN_BLACK_LIST, requestParam.getUserId(), requestParam.getServerName(), requestParam.getTokenCategory());

            // 检查是否已经撤销，实现幂等性
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                logger.info("TokenController revokeToken: 该token已经被撤销过，请勿重复撤销！ userId={}, serverName={}, tokenCategory={}",
                        requestParam.getUserId(), requestParam.getServerName(), requestParam.getTokenCategory());
                return true; // 已经撤销，直接返回
            }

            long ttlMillis = requestParam.getTokenCategory().equals("rt") ? jwtProperties.getRefreshToken().getTtlMillis() : jwtProperties.getAccessToken().getTtlMillis();
            redisTemplate.opsForValue().set(key, "revoked", ttlMillis, TimeUnit.MILLISECONDS);
            return false;
        }catch (Exception e){
            logger.error("TokenServiceImpl refreshToken error: userId={}, serverName={}, tokenCategory={}, error={}",
                    requestParam.getUserId(), requestParam.getServerName(), requestParam.getTokenCategory(), e.getMessage());
            throw new RuntimeException("注销Token失败: "+e.getMessage(),e);
        }
    }

    @Override
    public Claims validateToken(String token,String tokenCategory) {
        try {
            String secretKey = tokenCategory.equals("rt") ? jwtProperties.getRefreshToken().getSecretKey() : jwtProperties.getAccessToken().getSecretKey();
            Claims claims = JwtUtil.parseJWT(secretKey, token);
            String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
            String serverName = claims.get(JwtClaimsConstant.SERVER_NAME).toString();
            //判断是否存在黑名单中
            String key = String.format(RedisConstant.TOKEN_BLACK_LIST,userId,serverName,tokenCategory);
            if(Boolean.TRUE.equals(redisTemplate.hasKey(key))){ // 存在黑名单中
                throw new TokenRevokedException(JwtEnum.REVOKED_TOKEN.getMessage());
            }
            return claims;
        } catch (ExpiredJwtException ex) { //token已过期
           throw new TokenExpiredException(JwtEnum.EXPIRED_TOKEN.getMessage());
        } catch (JwtException ex){ //token无效
           throw new TokenInvalidException(JwtEnum.INVALID_TOKEN.getMessage());
        }
    }


}
