package io.yz.yzhttp.client;

import java.util.Objects;

public final class HttpHeaderField {
    private final String fieldName;
    private final String fieldValue;

    public HttpHeaderField(final String fieldName, final String fieldValue) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName can not be null");
        this.fieldValue = Objects.requireNonNull(fieldValue, "fieldValue can not be null");
    }

    public String fieldName() {
        return fieldName;
    }

    public String fileValue() {
        return fieldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpHeaderField that = (HttpHeaderField) o;
        return fieldName.equals(that.fieldName) && fieldValue.equals(that.fieldValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldValue);
    }

    @Override
    public String toString() {
        return "HttpHeaderField{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldValue='" + fieldValue + '\'' +
                '}';
    }
}
