package com.zslt_official.zslt_eco.common.exception;

/**
 * @Description: Token已撤销异常
 * @Author: xunshi
 * @Date: 2025/8/5 0:06
 */
public class TokenRevokedException extends RuntimeException{
    public TokenRevokedException(String message){
        super(message);
    }
    public TokenRevokedException(String message, Throwable cause){
        super(message, cause);
    }
}
