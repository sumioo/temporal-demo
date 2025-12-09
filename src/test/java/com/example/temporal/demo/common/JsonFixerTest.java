package com.example.temporal.demo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonFixerTest {

    @Test
    void fix_example_parens() {
        String input = "{\"content\": \"\\(1+2=3\\)\"}";
        String expected = "{\"content\": \"\\\\(1+2=3\\\\)\"}";
        String actual = JsonFixer.fix(input);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void keep_valid_escapes() throws Exception {
        // \f在编译时会被替换为换页符，这里需要用\\f表示才能在json字符串中保留
        String input = "{\"content\":\"line\\nnext\\tend \\fa\\rb\"}";
        String expected = input;
        String fixed = JsonFixer.fix(input);
        Assertions.assertEquals(expected, fixed);
        System.out.println("fixed json: " + fixed);
        ObjectMapper mapper = new ObjectMapper();
        Pojo obj = mapper.readValue(fixed, Pojo.class);
        System.out.println("obj content: " + obj.content);
    }

    @Test
    void fix_invalid_comma() {
        String input = "{\"a\":\"value\\,more\"}";
        String expected = "{\"a\":\"value\\\\,more\"}";
        Assertions.assertEquals(expected, JsonFixer.fix(input));
    }

    @Test
    void keep_valid_unicode() {
        String input = "{\"a\":\"\\u1234\"}";
        System.out.println(input);
        String expected = input;
        String actual = JsonFixer.fix(input);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void fix_invalid_unicode() {
        String input = "{\"a\":\"\\underline{\\hspace{2cm}} \"}";
        String expected = "{\"a\":\"\\\\underline{\\\\hspace{2cm}} \"}";
        Assertions.assertEquals(expected, JsonFixer.fix(input));
    }

    @Test
    void actual_json_parsed_after_fix() throws Exception {
        String raw = "{\"content\": \"中\\t2\"}";
        String fixed = JsonFixer.fix(raw);
        System.out.println(fixed);
        ObjectMapper mapper = new ObjectMapper();
        Pojo obj = mapper.readValue(fixed, Pojo.class);
        Assertions.assertEquals("中\t2", obj.content);
        System.out.println(obj.content);
    }

    @Test
    void fix_unescaped_quotes() {
        String input = "{\"content\": \"<p class=\"main\">content</p>\"}";
        String expected = "{\"content\": \"<p class=\\\"main\\\">content</p>\"}";
        String actual = JsonFixer.fix(input);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void fix_unescaped_quotes_2() {
        String input = "{\"content\": \"<p class=\"main\">content</p>\", \"other_content\": \"<p>\\underline{\\hspace{2cm}}<p class=\"res_area\"></p></p>\"}";
        System.out.println("input: " + input);
        String expected = "{\"content\": \"<p class=\\\"main\\\">content</p>\", \"other_content\": \"<p>\\\\underline{\\\\hspace{2cm}}<p class=\\\"res_area\\\"></p></p>\"}";
        System.out.println("expected: " + expected);
        String actual = JsonFixer.fix(input);
        System.out.println("actual: " + actual);
        Assertions.assertEquals(expected, actual);
    }

    static class Pojo {
        public String content;
    }
}

