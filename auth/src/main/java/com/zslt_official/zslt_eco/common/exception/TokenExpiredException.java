package com.zslt_official.zslt_eco.common.exception;

/**
 * @Description: Token过期异常
 * @Author: xunshi
 * @Date: 2025/8/5 0:04
 */
public class TokenExpiredException extends RuntimeException{
    public TokenExpiredException(String message) {
        super(message);
    }
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
