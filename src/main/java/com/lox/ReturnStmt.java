package com.lox;

/**
 * @author peiyou
 * @version 1.0
 * @className ReturnStmt
 * @date 2023/9/15 12:17
 **/
public class ReturnStmt extends RuntimeError {
    public final Object value;
    public ReturnStmt(Token token, Object value) {
        super(token, "此处不能存在return语句。");
        this.value = value;
    }
}
