package io.yz.yzhttp.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.yz.yzhttp.constant.HttpMethod;
import io.yz.yzhttp.constant.HttpVersion;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpRequest {
    private final String host;
    private final int port;
    private final HttpMethod method;
    private final String path;
    private final HttpVersion version;
    private final Map<String, String> header;

    private ByteBuf cache;

    private HttpRequest(String host, int port, HttpMethod method, String path, HttpVersion version, Map<String, String> header) {
        this.host = host;
        this.port = port;
        this.method = method;
        this.path = path;
        this.version = version;
        this.header = header;
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    ByteBuf cache() {
        if (cache == null) {
            buildCache();
        }
        return cache;
    }

    private void buildCache() {
        if (cache != null) {
            return;
        }

        cache = Unpooled.directBuffer();

        switch (method) {
            case GET:
                cache.writeCharSequence("GET ", StandardCharsets.US_ASCII);
                break;
            case HEAD:
                cache.writeCharSequence("HEAD ", StandardCharsets.US_ASCII);
                break;
            case POST:
                cache.writeCharSequence("POST ", StandardCharsets.US_ASCII);
                break;
            case PUT:
                cache.writeCharSequence("PUT ", StandardCharsets.US_ASCII);
                break;
            case DELETE:
                cache.writeCharSequence("DELETE ", StandardCharsets.US_ASCII);
                break;
            case CONNECT:
                cache.writeCharSequence("CONNECT ", StandardCharsets.US_ASCII);
                break;
            case OPTIONS:
                cache.writeCharSequence("OPTIONS ", StandardCharsets.US_ASCII);
                break;
            case TRACE:
                cache.writeCharSequence("TRACE ", StandardCharsets.US_ASCII);
                break;
            case PATCH:
                cache.writeCharSequence("PATCH ", StandardCharsets.US_ASCII);
        }

        cache.writeCharSequence(path, StandardCharsets.UTF_8);

        switch (version) {
            case HTTP_1_0:
                cache.writeCharSequence(" HTTP/1.0", StandardCharsets.US_ASCII);
                break;
            case HTTP_1_1:
                cache.writeCharSequence(" HTTP/1.1", StandardCharsets.US_ASCII);
                break;
        }

        cache.writeByte(0x0D);
        cache.writeByte(0x0A);

        header.forEach((k, v) -> {
            cache.writeCharSequence(k, StandardCharsets.UTF_8);
            cache.writeCharSequence(": ", StandardCharsets.US_ASCII);
            cache.writeCharSequence(v, StandardCharsets.UTF_8);
            cache.writeByte(0x0D);
            cache.writeByte(0x0A);
        });

        cache.writeByte(0x0D);
        cache.writeByte(0x0A);
    }

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private String host;
        private Integer port;
        private HttpMethod method;
        private String path;
        private HttpVersion version;
        private final Map<String, String> header = new HashMap<>();

        Builder() {
            header.put("User-Agent", "YzClient");
        }

        public String getHost() {
            return host;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public Builder setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public String getPath() {
            return path;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public HttpVersion getVersion() {
            return version;
        }

        public Builder setVersion(HttpVersion version) {
            this.version = version;
            return this;
        }

        public Map<String, String> getHeader() {
            return header;
        }

        public Builder addHeaders(Map<String, String> header) {
            this.header.putAll(header);
            return this;
        }

        public Builder addHeader(final String key, final String value) {
            this.header.put(key, value);
            return this;
        }

        public HttpRequest build() {
            Objects.requireNonNull(host);
            Objects.requireNonNull(host);

            return new HttpRequest(
                    host,
                    port,
                    method,
                    path,
                    version,
                    header
            );
        }
    }
}
