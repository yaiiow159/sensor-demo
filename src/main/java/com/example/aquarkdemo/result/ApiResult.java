package com.example.aquarkdemo.result;

import com.example.aquarkdemo.entity.SensorData;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 統一api 回應結果處理類
 * @param <T>
 */
@Getter
@ToString
@NoArgsConstructor
public class ApiResult<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;


    private ApiResult(Apimessage apimessage, T data) {
        this.code = apimessage.getCode();
        this.message = apimessage.getMessage();
        this.data = data;
    }

    public ApiResult(final String message) {
        this.message = message;
    }

    public ApiResult(final String message, final T data) {
        this.message = message;
        this.data = data;
    }

    public ApiResult(final Integer code, final String message) {
        this.code = code;
        this.message = message;
    }

    public static ApiResult<?> customize(Integer code, String message) {
        return new ApiResult<>(code, message);
    }

    public static <T> ApiResult<?> customize(String message, T data) {
        return new ApiResult<>(message, data);
    }

    public static ApiResult<?> success() {
        return new ApiResult<>(Apimessage.SUCCESS, null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(Apimessage.SUCCESS, data);
    }

    public static ApiResult<?> fail() {
        return new ApiResult<>(Apimessage.FAIL, null);
    }

    public static <T> ApiResult<T> fail(Apimessage apimessage,T data) {
        return new ApiResult<>(Objects.requireNonNullElse(apimessage, Apimessage.ERROR), data);
    }

    public static ApiResult<?> timeout() {
        return new ApiResult<>(Apimessage.TIMEOUT, null);
    }

    public static <T> ApiResult<T> timeout(T data) {
        return new ApiResult<>(Apimessage.TIMEOUT, data);
    }

    public static ApiResult<?> error() {
        return new ApiResult<>(Apimessage.ERROR, null);
    }

    public static <T> ApiResult<T> error(T data) {
        return new ApiResult<>(Apimessage.ERROR, data);
    }

    public static ApiResult<?> forbidden() {
        return new ApiResult<>(Apimessage.FORBIDDEN, null);
    }

    public static <T> ApiResult<T> forbidden(T data) {
        return new ApiResult<>(Apimessage.FORBIDDEN, data);
    }

    public static ApiResult<?> unauthorized() {
        return new ApiResult<>(Apimessage.UNAUTHORIZED, null);
    }

    public static <T> ApiResult<T> unauthorized(T data) {
        return new ApiResult<>(Apimessage.UNAUTHORIZED, data);
    }
    public boolean isSuccess() {
        return Apimessage.SUCCESS.getCode().equals(this.code);
    }

    public boolean isFail() {
        return Apimessage.FAIL.getCode().equals(this.code);
    }

    public boolean isTimeout() {
        return Apimessage.TIMEOUT.getCode().equals(this.code);
    }

    public boolean isError() {
        return Apimessage.ERROR.getCode().equals(this.code);
    }

    public boolean isForbidden() {
        return Apimessage.FORBIDDEN.getCode().equals(this.code);
    }

    public boolean isUnauthorized() {
        return Apimessage.UNAUTHORIZED.getCode().equals(this.code);
    }
}
