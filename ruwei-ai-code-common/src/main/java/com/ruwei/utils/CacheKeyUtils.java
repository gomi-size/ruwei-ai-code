package com.ruwei.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存Key
 */
public class CacheKeyUtils {

    /**
     * 根据对象生成缓存key(JSON+MD5)
     * @param object
     * @return
     */
    public static String getCacheKey(Object object) {
        if (object == null) {
            return DigestUtil.md5Hex("null");
        }

        //先转josn
        String jsonStr = JSONUtil.toJsonStr(object);
        return DigestUtil.md5Hex(jsonStr);


    }
}
