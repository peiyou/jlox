package com.lox;

/**
 * @author peiyou
 * @version 1.0
 * @className Token
 * @date 2023/9/8 09:53
 **/
public class Token {
    final TokenType type;
    // 词位、语义
    final String lexeme;
    // 字面量
    final Object literal;

    // 所在行
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
