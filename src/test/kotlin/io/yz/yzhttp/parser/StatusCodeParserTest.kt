package io.yz.yzhttp.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer


internal class StatusCodeParserTest {
    @Test
    fun test() {
        val statusCode = ByteBuffer.wrap("200".toByteArray(Charsets.US_ASCII))

        val parser = StatusCodeParser()

        parser.feed(statusCode)

        assertEquals(200, parser.result)
    }
}
