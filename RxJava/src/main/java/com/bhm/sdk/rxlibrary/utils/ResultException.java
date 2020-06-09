package com.bhm.sdk.rxlibrary.utils;

import java.io.IOException;

import androidx.annotation.Nullable;

//后台返回非200时，抛出此异常
public class ResultException extends IOException {
    private int code;
    private String message;
    private String realJson;//原来的json

    public ResultException() {
        super();
    }

    public ResultException(String message) {
        super(message);
    }

    public ResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultException(Throwable cause) {
        super(cause);
    }

    public ResultException(int code, String message, String realJson){
        super(message);
        this.code = code;
        this.message = message;
        this.realJson = realJson;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRealJson() {
        return realJson;
    }

    public void setRealJson(String realJson) {
        this.realJson = realJson;
    }
}
