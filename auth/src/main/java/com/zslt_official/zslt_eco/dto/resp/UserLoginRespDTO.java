package com.zslt_official.zslt_eco.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @Description: 用户登录返回实体
 * @Author: xunshi
 * @Date: 2025/7/28 22:47
 */
@Data
@Builder
public class UserLoginRespDTO {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * access_token
     */
    private String accessToken;
    /**
     * refresh_token
     */
    private String refreshToken;
}
