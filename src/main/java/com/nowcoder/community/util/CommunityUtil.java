package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CommunityUtil {//工具类

    // 生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5加密
    // hello -> abc123def456
    // hello + 3e4a8(随机字符串) -> abc123def456abc
    public static  String md5(String key) {
        if (StringUtils.isBlank(key)) { //判断是否为空串或空格
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
