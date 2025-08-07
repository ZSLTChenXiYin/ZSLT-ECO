package com.zslt_official.zslt_eco.common.constant;

/**
 * @Description: Redis常量类
 * @Author: xunshi
 * @Date: 2025/7/29 16:29
 */
public interface RedisConstant {
   /**
    * 黑名单缓存key
    */
   String TOKEN_BLACK_LIST = "black_list:user_id:%s:%s_%s";
}
