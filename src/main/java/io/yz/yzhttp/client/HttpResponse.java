package io.yz.yzhttp.client;

import io.yz.yzhttp.constant.HttpVersion;

import java.nio.ByteBuffer;

public class HttpResponse {
    private final HttpVersion httpVersion;
    private final int statusCode;
    private final String reasonPhrase;
    private final HttpHeader httpHeader;
    private final ByteBuffer messageBody;

    public HttpResponse(HttpVersion httpVersion, int statusCode, String reasonPhrase, HttpHeader httpHeader, ByteBuffer messageBody) {
        this.httpVersion = httpVersion;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.httpHeader = httpHeader;
        this.messageBody = messageBody;
    }
}
