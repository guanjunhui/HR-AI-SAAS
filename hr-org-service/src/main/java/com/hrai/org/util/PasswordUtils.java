package com.hrai.org.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类
 *
 * 使用 BCrypt 算法加密密码
 */
public class PasswordUtils {

    private PasswordUtils() {
        // 工具类禁止实例化
    }

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return true 匹配, false 不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
