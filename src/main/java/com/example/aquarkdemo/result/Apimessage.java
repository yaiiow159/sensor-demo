package com.example.aquarkdemo.result;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * 統一 api 回應結果
 */
@Getter
@ToString
public enum Apimessage {
    SUCCESS(200, "呼叫回傳成功", HttpStatus.OK),
    FAIL(400, "呼叫回傳失敗", HttpStatus.BAD_REQUEST),
    TIMEOUT(408, "請求超時", HttpStatus.REQUEST_TIMEOUT),
    ERROR(500, "呼叫發生錯誤", HttpStatus.INTERNAL_SERVER_ERROR),
    FORBIDDEN(403, "請求被拒絕", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(401, "未知錯誤", HttpStatus.UNAUTHORIZED);

    private final Integer code;
    private final String message;
    private final HttpStatus status;

    Apimessage(Integer code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}