package com.zslt_official.zslt_eco.common.enums;


/**
 * @Description: JWT返回状态枚举类
 * @Author: xunshi
 * @Date: 2025/7/28 23:11
 */
public enum JwtEnum {
    SUCCESS_GET("200", "获取Token成功"),
    FAILED_GET("400", "获取Token失败"),
    SUCCESS_REFRESH("200", "刷新Token成功"),
    FAILED_REFRESH("400", "刷新Token失败"),
    SUCCESS_REVOKE("200", "撤销Token成功"),
    FAILED_REVOKE("400", "撤销Token失败"),
    SUCCESS_VALIDATE("200", "验证Token成功"),
    INVALID_TOKEN("401", "无效的Token"),
    EXPIRED_TOKEN("401", "Token已过期，请重新获取"),
    REVOKED_TOKEN("401", "Token已撤销，请重新获取"),
    MISSING_TOKEN("400", "缺少Token参数"),
    TOKEN_ALREADY_REVOKED("400", "该Token已经被撤销，请勿重复操作");

    /**
     * 状态码
     */
    private String code;
    /**
     * 描述信息
     */
    private String message;
    JwtEnum(String code, String message){
        this.code = code;
        this.message = message;
    }
    public String getCode(){
        return code;
    }
    public String getMessage(){
        return message;
    }
}
