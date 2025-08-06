package com.zslt_official.zslt_eco.common.exception;

/**
 * @Description: Token无效异常
 * @Author: xunshi
 * @Date: 2025/8/5 0:06
 */
public class TokenInvalidException extends RuntimeException{
    public TokenInvalidException(String message){
        super(message);
    }
    public TokenInvalidException(String message, Throwable cause){
        super(message, cause);
    }
}
