package com.example.temporal.demo.common;

public class JsonFixer {
    /**
     * 修复一个可能格式不正确的JSON字符串。
     * 主要解决两个问题：
     * 1. 字符串值中未转义的双引号。
     * 2. 不正确的转义序列。
     *
     * @param json 输入的JSON字符串。
     * @return 修复后的JSON字符串。
     */
    public static String fix(String json) {
        if (json == null || json.isEmpty()) return json;
        StringBuilder out = new StringBuilder(json.length() + 16);
        // 用于跟踪当前解析位置是否在JSON字符串字面量内部的标志。
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            // 如果不在字符串内部，则直接追加字符。
            // 当遇到第一个双引号时，标记进入字符串内部。
            if (!inString) {
                out.append(c);
                if (c == '"') inString = true;
                continue;
            }

            // --- 核心逻辑：处理在字符串内部的字符 ---

            // 如果在字符串内部遇到双引号，需要判断它是字符串的结束符还是一个未转义的普通字符。
            if (c == '"') {
                // 向前查找，跳过所有空白字符，找到下一个非空白字符。
                int nextNonWsIndex = i + 1;
                while (nextNonWsIndex < json.length() && Character.isWhitespace(json.charAt(nextNonWsIndex))) {
                    nextNonWsIndex++;
                }

                if (nextNonWsIndex == json.length()) { // 到达整个字符串的末尾
                    out.append(c);
                    inString = false;
                } else {
                    char nextChar = json.charAt(nextNonWsIndex);
                    // 如果下一个非空白字符是JSON的结构分隔符（如 : , } ]），
                    // 那么当前双引号就被视为字符串的结束符。
                    if (nextChar == ':' || nextChar == ',' || nextChar == '}' || nextChar == ']') {
                        out.append(c);
                        inString = false; // 标记退出字符串
                    } else {
                        // 否则，该双引号被视为字符串内容的一部分，需要进行转义。
                        out.append('\\').append(c);
                    }
                }
                continue;
            }

            // 处理转义字符 \
            if (c == '\\') {
                if (i + 1 >= json.length()) { // 处理末尾的单个反斜杠
                    out.append('\\').append('\\');
                    continue;
                }
                char next = json.charAt(i + 1);
                if (isSimpleValidEscape(next)) { // 处理合法的简单转义，如 \", \\, \n
                    out.append('\\').append(next);
                    i++; // 跳过下一个字符
                    continue;
                }
                if (next == 'u') { // 处理Unicode转义
                    if (i + 5 < json.length() && isHex(json.charAt(i + 2)) && isHex(json.charAt(i + 3)) && isHex(json.charAt(i + 4)) && isHex(json.charAt(i + 5))) {
                        out.append("\\u").append(json, i + 2, i + 6);
                        i += 5; // 跳过u和4个十六进制数字
                        continue;
                    } else { // 非法的Unicode转义，将反斜杠本身转义
                        out.append('\\').append('\\');
                        continue;
                    }
                }
                // 其他所有非法转义，都将反斜杠本身转义
                out.append('\\').append('\\');
                continue;
            }

            // 追加字符串中的普通字符
            out.append(c);
        }
        return out.toString();
    }

    /**
     * 检查字符是否为简单的合法JSON转义字符。
     */
    private static boolean isSimpleValidEscape(char c) {
        return c == '"' || c == '\\' || c == '/' || c == 'b' || c == 'f' || c == 'n' || c == 'r' || c == 't';
    }

    /**
     * 检查字符是否为十六进制字符。
     */
    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}

