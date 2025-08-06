package com.zslt_official.zslt_eco.service;

import com.zslt_official.zslt_eco.common.result.Result;
import com.zslt_official.zslt_eco.dto.req.RevokeTokenReqDTO;
import com.zslt_official.zslt_eco.dto.req.UserLoginReqDTO;
import com.zslt_official.zslt_eco.dto.resp.UserLoginRespDTO;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Description: Token 业务逻辑接口
 * @Author: xunshi
 * @Date: 2025/8/4 15:23
 */
public interface TokenService {
    /**
     * @Description  获取Token
     * @Author xunshi
     * @Date 2025/8/4 15:49
     * @Param [requestParam]
     * @Return UserLoginRespDTO
     */
    UserLoginRespDTO getToken(UserLoginReqDTO requestParam);
    /**
     * @Description  刷新Token
     * @Author xunshi
     * @Date 2025/8/4 15:49
     * @Param [refreshToken]
     * @Return Map<String,String>
     */
    Map<String,String> refreshToken(String refreshToken);
    /**
     * @Description  撤销Token
     * @Author xunshi
     * @Date 2025/8/4 15:49
     * @Param [requestParam]
     * @Return boolean
     */
    boolean revokeToken(@RequestBody RevokeTokenReqDTO requestParam);
    /**
     * @Description  验证Token
     * @Author xunshi
     * @Date 2025/8/4 15:49
     * @Param [token,tokenCategory]
     * @Return Claims
     */
    Claims validateToken(String token,String tokenCategory);
}
