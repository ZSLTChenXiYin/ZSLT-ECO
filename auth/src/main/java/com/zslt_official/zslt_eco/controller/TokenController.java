package com.zslt_official.zslt_eco.controller;
import com.alibaba.fastjson2.JSON;
import com.zslt_official.zslt_eco.common.enums.JwtEnum;
import com.zslt_official.zslt_eco.common.exception.TokenExpiredException;
import com.zslt_official.zslt_eco.common.exception.TokenInvalidException;
import com.zslt_official.zslt_eco.common.exception.TokenRevokedException;
import com.zslt_official.zslt_eco.common.result.Result;
import com.zslt_official.zslt_eco.dto.req.RevokeTokenReqDTO;
import com.zslt_official.zslt_eco.dto.req.UserLoginReqDTO;
import com.zslt_official.zslt_eco.dto.resp.UserLoginRespDTO;
import com.zslt_official.zslt_eco.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description: Token控制类
 * @Author: xunshi
 * @Date: 2025/7/28 16:40
 */
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {
    private final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private final TokenService tokenService;

    /**
     * @Description  获取Token (access_token、refresh_token)
     * @Author xunshi
     * @Date 2025/7/29 15:07
     * @Param [requestParam]
     * @Return Result<UserLoginRespDTO>
     */
    @PostMapping("/get")
    public Result<UserLoginRespDTO> getToken(@RequestBody UserLoginReqDTO requestParam) {
        logger.info("TokenController getToken start,param: {}", JSON.toJSONString(requestParam));

        try {
            UserLoginRespDTO response = tokenService.getToken(requestParam);
            Result<UserLoginRespDTO> result = Result.<UserLoginRespDTO>builder()
                    .code(JwtEnum.SUCCESS_GET.getCode())
                    .message(JwtEnum.SUCCESS_GET.getMessage())
                    .data(response).build();
            logger.info("TokenController getToken end,response: {}",JSON.toJSONString(result));
            return result;
        }catch (RuntimeException e){
            logger.error("TokenController getToken error: {}",e.getMessage());
            return Result.<UserLoginRespDTO>builder()
                    .code(JwtEnum.FAILED_GET.getCode())
                    .message(JwtEnum.FAILED_GET.getMessage())
                    .build();
        }

    }

    /**
     * @Description  刷新Token (每次刷新access_token,而refresh_token快过期时刷新)
     * @Author xunshi
     * @Date 2025/7/29 15:08
     * @Param [refreshToken]
     * @Return Result<?>
     */
    @PostMapping("/refresh")
    public Result<?> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        logger.info("TokenController refreshToken start,param: {}",refreshToken);

        if(refreshToken == null || refreshToken.isBlank()){
            return handlerMissingToken();
        }

        try {
            Map<String,String> newTokens = tokenService.refreshToken(refreshToken);
            Result<Map<String,String>> result = Result.success(newTokens, "刷新Token成功");

            logger.info("TokenController refreshToken end,response: {}", JSON.toJSONString( result));
            return result;
        }catch (TokenRevokedException e){
            return handleRevokedToken();
        } catch (TokenExpiredException e){
            return handleExpiredToken();
        }catch (TokenInvalidException e){
            return handleInvalidToken();
        }

    }

    /**
     * @Description  撤销Token
     * @Author xunshi
     * @Date 2025/7/29 15:08
     * @Param [requestParam]
     * @Return Result<Void>
     */
    @PostMapping("/revoke")
    public Result<Void> revokeToken(@RequestBody RevokeTokenReqDTO requestParam) {
        logger.info("TokenController revokeToken param: {}",JSON.toJSONString(requestParam));

        try {
            boolean is_revoked = tokenService.revokeToken(requestParam);
            if(Boolean.TRUE.equals(is_revoked)){
                return Result.<Void>builder()
                        .code(JwtEnum.TOKEN_ALREADY_REVOKED.getCode())
                        .message(JwtEnum.TOKEN_ALREADY_REVOKED.getMessage())
                        .build();
            }
            logger.info("TokenController revokeToken response: {}","注销Token成功");
            return Result.success(null,"注销Token成功");
        }catch (RuntimeException e){
            logger.error("TokenController revokeToken error: {}",e.getMessage());
            return Result.<Void>builder().code(JwtEnum.FAILED_REVOKE.getCode()).message(JwtEnum.FAILED_REVOKE.getMessage()).build();
        }

    }

    /**
     * @Description  验证Token
     * @Author xunshi
     * @Date 2025/7/29 15:08
     * @Param [token]
     * @Return Result<Void>
     */
    @GetMapping("/validate")
    public Result<Void> validateToken(@RequestHeader("Authorization") String token) {
        logger.info("TokenController validateToken param: {}",token);

        if (token == null || token.isBlank()) { // 缺少Token
            return handlerMissingToken();
        }

        try {
            tokenService.validateToken(token,"at");
            Result<Void> result = Result.<Void>builder() //验证成功
                    .code(JwtEnum.SUCCESS_VALIDATE.getCode())
                    .message(JwtEnum.SUCCESS_VALIDATE.getMessage())
                    .build();
            logger.info("TokenController validateToken result: {}",JSON.toJSONString(result));
            return result;
        }catch (TokenRevokedException e){
            return handleRevokedToken();
        }catch (TokenExpiredException e){
            return handleExpiredToken();
        }catch (TokenInvalidException e){
            return handleInvalidToken();
        }

    }

    private Result<Void> handlerMissingToken() {
        logger.error("TokenController validateToken error: {}",JwtEnum.MISSING_TOKEN.getMessage());
        return Result.<Void>builder()
                .code(JwtEnum.MISSING_TOKEN.getCode())
                .message(JwtEnum.MISSING_TOKEN.getMessage())
                .build();
    }
    private Result<Void> handleRevokedToken() {
        logger.error("TokenServiceImpl validateToken error: {}",JwtEnum.REVOKED_TOKEN.getMessage());
        return Result.<Void>builder()
                .code(JwtEnum.REVOKED_TOKEN.getCode())
                .message(JwtEnum.REVOKED_TOKEN.getMessage())
                .build();
    }
    private Result<Void> handleExpiredToken() {
        logger.error("TokenServiceImpl validateToken error: {}",JwtEnum.EXPIRED_TOKEN.getMessage());
        return Result.<Void>builder()
                .code(JwtEnum.EXPIRED_TOKEN.getCode())
                .message(JwtEnum.EXPIRED_TOKEN.getMessage())
                .build();
    }
    private Result<Void> handleInvalidToken() {
        logger.error("TokenServiceImpl validateToken error: {}",JwtEnum.INVALID_TOKEN.getMessage());
        return Result.<Void>builder()
                .code(JwtEnum.INVALID_TOKEN.getCode())
                .message(JwtEnum.INVALID_TOKEN.getMessage())
                .build();
    }
}
