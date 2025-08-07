package com.zslt_official.zslt_eco.common.result;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 鉴权服务统一返回实体
 * @Author: xunshi
 * @Date: 2025/7/28 17:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String SUCCESS_CODE = "200";

    /**
     * 状态码
     */
    private String code;
    /**
     * 描述
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;
    public static <T> Result<T> success(T data,String message){
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
