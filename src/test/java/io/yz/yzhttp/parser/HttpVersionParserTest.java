package io.yz.yzhttp.parser;

import io.yz.yzhttp.constant.HttpVersion;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpVersionParserTest {
    @Test
    void test() {
        final ByteBuffer httpVersion = ByteBuffer.wrap("HTTP/1.1".getBytes(StandardCharsets.US_ASCII));
        final HttpVersionParser httpVersionParser = new HttpVersionParser();

        final int feed = httpVersionParser.feed(httpVersion);

        assertEquals(httpVersion.capacity(), feed);

        final HttpVersion result = httpVersionParser.getResult();

        assertEquals(HttpVersion.HTTP_1_1, result);
    }

    @Test
    void test1() {
        final ByteBuffer httpVersion = ByteBuffer.wrap("HTTP/1.1 ".getBytes(StandardCharsets.US_ASCII));
        final HttpVersionParser httpVersionParser = new HttpVersionParser();

        final int feed = httpVersionParser.feed(httpVersion);

        assertEquals(httpVersion.capacity() - 1, feed);

        final HttpVersion result = httpVersionParser.getResult();

        assertEquals(HttpVersion.HTTP_1_1, result);
    }
}
