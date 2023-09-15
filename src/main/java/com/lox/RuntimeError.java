package com.lox;

/**
 * @author peiyou
 * @version 1.0
 * @className RuntimeError
 * @date 2023/9/11 14:47
 **/
public class RuntimeError extends RuntimeException {
    final Token token;
    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
