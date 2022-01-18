package io.yz.yzhttp.client;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpHeader implements Iterable<HttpHeaderField> {
    private final List<HttpHeaderField> list;

    private HttpHeader(final List<HttpHeaderField> list) {
        this.list = Collections.unmodifiableList(list);
    }

    public Optional<String> getHeader(final String fieldName) {
        checkFieldName(fieldName);

        return list.stream()
                .filter(e -> fieldName.equalsIgnoreCase(e.fieldName()))
                .findFirst()
                .map(HttpHeaderField::fileValue);
    }

    public Iterable<String> getCookies() {
        return list.stream()
                .filter(e -> "set-cookie".equalsIgnoreCase(e.fieldName()))
                .map(HttpHeaderField::fileValue)
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<HttpHeaderField> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return "HttpHeader{" +
                "list=" + list +
                '}';
    }

    private static String checkFieldName(final String fieldName) {
        return Objects.requireNonNull(fieldName, "fieldName can not be null");
    }

    private static String checkFieldValue(final String fieldValue) {
        return Objects.requireNonNull(fieldValue, "fieldValue can not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, List<HttpHeaderField>> inner;

        private Builder() {
            this.inner = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }

        public Builder addField(final String fieldName, final String fieldValue) {
            inner.computeIfAbsent(checkFieldName(fieldName), k -> new LinkedList<>())
                    .add(new HttpHeaderField(fieldName, checkFieldName(fieldValue)));
            return this;
        }

        public Optional<String> getField(final String fieldName) {
            final List<HttpHeaderField> httpHeaderFields = inner.get(checkFieldName(fieldName));
            if (httpHeaderFields == null) {
                return Optional.empty();
            } else {
                return Optional.of(httpHeaderFields.get(httpHeaderFields.size() - 1).fileValue());
            }
        }

        public Iterable<String> getFields(final String fieldName) {
            return inner.getOrDefault(checkFieldValue(fieldName), Collections.emptyList())
                    .stream()
                    .map(HttpHeaderField::fileValue)
                    .collect(Collectors.toList());
        }

        public boolean removeField(final String fieldName, final String fieldValue) {
            checkFieldValue(fieldValue);

            final List<HttpHeaderField> httpHeaderFields = inner.get(checkFieldName(fieldName));
            if (httpHeaderFields == null) {
                return false;
            }

            httpHeaderFields.removeIf(httpHeaderField -> fieldValue.equals(httpHeaderField.fileValue()));
            if (httpHeaderFields.isEmpty()) {
                inner.remove(fieldName);
            }

            return true;
        }

        public Iterable<String> removeFields(final String fieldName) {
            final List<HttpHeaderField> removed =
                    inner.remove(checkFieldValue(fieldName));

            return removed != null ? removed.stream().map(HttpHeaderField::fileValue).collect(Collectors.toList()) : Collections.emptyList();
        }

        public Iterator<HttpHeaderField> iterator() {
            return inner.values().stream().flatMap(List::stream).iterator();
        }

        public HttpHeader build() {
            return new HttpHeader(
                    inner.entrySet()
                            .stream()
                            .flatMap(e -> {
                                final String key = e.getKey();
                                final List<HttpHeaderField> value = e.getValue();

                                if ("set-cookie".equalsIgnoreCase(key)) {
                                    return value.stream();
                                } else {
                                    return Stream.of(value.get(value.size() - 1));
                                }
                            }).collect(Collectors.toList())
            );
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "inner=" + inner +
                    '}';
        }
    }
}
