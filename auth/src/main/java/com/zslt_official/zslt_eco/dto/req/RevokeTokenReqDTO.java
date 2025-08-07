package com.zslt_official.zslt_eco.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 撤销token请求参数实体
 * @Author: xunshi
 * @Date: 2025/8/3 23:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokeTokenReqDTO {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 服务名称
     */
    private String serverName;
    /**
     * token类别（at:access_token , rt:refresh_token）
     */
    private String tokenCategory;
}
