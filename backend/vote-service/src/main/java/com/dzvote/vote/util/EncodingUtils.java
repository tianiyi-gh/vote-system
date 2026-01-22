package com.dzvote.vote.util;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 编码修复工具类
 * 用于修复从数据库读取的中文乱码问题
 */
public class EncodingUtils {

    /**
     * 尝试修复可能编码错误的字符串
     * 常见情况：数据在插入时被错误地双重编码了（UTF-8 -> ISO-8859-1 -> UTF-8）
     *
     * @param input 可能编码错误的字符串
     * @return 修复后的字符串，如果无法修复则返回原字符串
     */
    public static String fixEncoding(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        // 尝试多种编码修复策略
        String[] strategies = {
            "ISO-8859-1->UTF-8",      // 最常见的双重编码
            "windows-1252->UTF-8",    // Windows变体
            "GBK->UTF-8",             // GBK到UTF-8
            "double-utf8"             // UTF-8被再次UTF-8编码
        };

        for (String strategy : strategies) {
            try {
                String fixed = applyFix(result, strategy);
                if (isValidChinese(fixed) && !fixed.equals(result)) {
                    System.out.println("编码修复成功，使用策略: " + strategy + ", 原始: " + result + ", 修复: " + fixed);
                    return fixed;
                }
            } catch (Exception e) {
                // 忽略，尝试下一个策略
            }
        }

        return result;
    }

    /**
     * 应用特定的编码修复策略
     */
    private static String applyFix(String input, String strategy) throws Exception {
        switch (strategy) {
            case "ISO-8859-1->UTF-8":
                // 将字符串转换为ISO-8859-1字节，然后用UTF-8解码
                byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
                return new String(bytes, StandardCharsets.UTF_8);

            case "windows-1252->UTF-8":
                // 类似ISO-8859-1，但是Windows编码
                Charset windows1252 = Charset.forName("windows-1252");
                byte[] bytes2 = input.getBytes(windows1252);
                return new String(bytes2, StandardCharsets.UTF_8);

            case "GBK->UTF-8":
                // GBK到UTF-8
                byte[] bytes3 = input.getBytes("GBK");
                return new String(bytes3, StandardCharsets.UTF_8);

            case "double-utf8":
                // UTF-8被再次UTF-8编码（较少见）
                byte[] bytes1 = input.getBytes(StandardCharsets.UTF_8);
                byte[] bytes4 = new String(bytes1, StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.UTF_8);
                return new String(bytes4, StandardCharsets.UTF_8);

            default:
                return input;
        }
    }

    /**
     * 检查字符串是否看起来是有效的中文
     */
    private static boolean isValidChinese(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        int chineseCharCount = 0;
        int invalidCharCount = 0;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 检查是否是中文字符范围
            if ((c >= 0x4E00 && c <= 0x9FA5) ||  // 基本汉字
                (c >= 0x3400 && c <= 0x4DBF) ||  // 扩展A
                (c >= 0x20000 && c <= 0x2A6DF) || // 扩展B（代理对）
                (c >= 0xF900 && c <= 0xFAFF) ||  // 兼容汉字
                (c >= 0x3000 && c <= 0x303F) ||  // 中文标点
                (c >= 0xFF00 && c <= 0xFFEF)) {  // 全角字符
                chineseCharCount++;
            } else if (c < 32 || (c >= 127 && c < 160)) {
                // 控制字符或不可打印字符
                invalidCharCount++;
            }
        }

        // 如果有足够的汉字且没有太多无效字符，认为是有效的中文
        return chineseCharCount > 0 && invalidCharCount == 0;
    }
}

