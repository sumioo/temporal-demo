package com.example.temporal.demo.common;

public class JsonIllegalEscapeFixer {
    public static String fix(String json) {
        if (json == null || json.isEmpty()) return json;
        StringBuilder out = new StringBuilder(json.length() + 16);
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (!inString) {
                out.append(c);
                if (c == '"') inString = true;
                continue;
            }

            if (c == '"') {
                out.append(c);
                inString = false;
                continue;
            }

            if (c == '\\') {
                if (i + 1 >= json.length()) {
                    out.append('\\').append('\\');
                    continue;
                }
                char next = json.charAt(i + 1);
                if (isSimpleValidEscape(next)) {
                    out.append('\\').append(next);
                    i++;
                    continue;
                }
                if (next == 'u') {
                    if (i + 5 < json.length() && isHex(json.charAt(i + 2)) && isHex(json.charAt(i + 3)) && isHex(json.charAt(i + 4)) && isHex(json.charAt(i + 5))) {
                        out.append("\\u").append(json, i + 2, i + 6);
                        i += 5;
                        continue;
                    } else {
                        out.append('\\').append('\\');
                        continue;
                    }
                }
                out.append('\\').append('\\');
                continue;
            }

            out.append(c);
        }
        return out.toString();
    }

    private static boolean isSimpleValidEscape(char c) {
        return c == '"' || c == '\\' || c == '/' || c == 'b' || c == 'f' || c == 'n' || c == 'r' || c == 't';
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}

