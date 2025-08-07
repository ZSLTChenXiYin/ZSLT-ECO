package com.zslt_official.zslt_eco.common.listener;

import com.zslt_official.zslt_eco.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Description: 配置刷新监听器，用于记录Consul配置变更日志
 * @Author: xunshi
 * @Date: 2025/8/5 19:15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigRefreshListener implements ApplicationListener<RefreshScopeRefreshedEvent> {
    private final JwtProperties jwtProperties;
    @Override
    public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
        log.info("========== Consul配置已更新 ==========");
        log.info("触发时间: {}", new java.util.Date());

        // 记录JWT配置的当前值
        log.info("========== 当前JWT配置详情 ==========");
        log.info("Access Token Secret Key: {}", jwtProperties.getAccessToken().getSecretKey());
        log.info("Access Token TTL (毫秒): {}", jwtProperties.getAccessToken().getTtlMillis());
        log.info("Refresh Token Secret Key: {}", jwtProperties.getRefreshToken().getSecretKey());
        log.info("Refresh Token TTL (毫秒): {}", jwtProperties.getRefreshToken().getTtlMillis());
        log.info("Refresh Token Remain Date (天): {}", jwtProperties.getRefreshToken().getRemainDate());
        log.info("=====================================");
    }
}
