package com.tbit.tbitblesdk.util;

/**
 * Created by Kenny on 2016/3/28 9:02.
 * Desc：加密解密算法
 */
public class EncryptUtil {
    private static final int MAX_ENCRYPT_COUNT = 95;
    public static char[] szKey = {0x4c, 0x23, 0x36, 0x4d, 0x79, 0x4e, 0x5e, 0x4f, 0x50, 0x75,
            0x51, 0x74, 0x52, 0x3d, 0x53, 0x68, 0x54, 0x32, 0x55, 0x56,
            0x20, 0x62, 0x22, 0x64, 0x66, 0x24, 0x67, 0x69, 0x2d, 0x2c,
            0x6b, 0x2b, 0x6c, 0x2f, 0x2e, 0x34, 0x3e, 0x40, 0x37, 0x3c,
            0x42, 0x35, 0x45, 0x46, 0x48, 0x39, 0x49, 0x7b, 0x31, 0x5c,
            0x5f, 0x61, 0x77, 0x4b, 0x6f, 0x59, 0x5b, 0x71, 0x65, 0x26,
            0x2a, 0x76, 0x29, 0x28, 0x73, 0x27, 0x60, 0x30, 0x3b, 0x44,
            0x33, 0x78, 0x7a, 0x7c, 0x21, 0x7d, 0x47, 0x7e, 0x63, 0x25,
            0x43, 0x72, 0x3a, 0x70, 0x6a, 0x3f, 0x5d, 0x6e, 0x6d, 0x58,
            0x41, 0x4a, 0x38, 0x57, 0x5a};

    public static String encryptStr(String in_str) {
        int count = 0;

        StringBuilder builder = new StringBuilder();
        if (in_str == null || in_str.length() == 0) {
            return null;
        }

        count = in_str.length();
        if (count > MAX_ENCRYPT_COUNT) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            builder.append(szKey[in_str.charAt(i) - 0x21]);
        }
        return builder.toString();
    }

    public static String decryptStr(String in_str) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        int j = 0;
        if (in_str == null || in_str.length() == 0) {
            return null;
        }

        count = in_str.length();
        if (count > MAX_ENCRYPT_COUNT) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            for (j = 0; j < MAX_ENCRYPT_COUNT; j++) {
                if (in_str.charAt(i) == szKey[j]) {
                    break;
                }
            }
            builder.append(0x21 + j);
        }
        return builder.toString();
    }
}
