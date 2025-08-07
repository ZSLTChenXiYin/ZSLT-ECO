package com.zslt_official.zslt_eco.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Description: 用户登录请求实体
 * @Author: xunshi
 * @Date: 2025/7/28 22:40
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginReqDTO {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 发起请求的服务名称
     */
    private String serverName;
}
