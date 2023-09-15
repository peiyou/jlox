package com.lox;

import com.sun.org.apache.bcel.internal.generic.RETURN;

/**
 * @author peiyou
 * @version 1.0
 * @className TokenType
 * @date 2023/9/8 09:38
 **/
public enum TokenType {
    // 三元表示式
    // 问号
    TERNARY_QUESTION_MARK,
    // 冒号
    TERNARY_COLON,

    // 单个字符的标记
    // 左圆括号 (
    LEFT_PAREN,
    // 右圆括号 )
    RIGHT_PAREN,
    // 左大括号 {
    LEFT_BRACE,
    // 右大括号 }
    RIGHT_BRACE,

    // 逗号 ,
    COMMA,
    // 点 .
    DOT,
    // 减号 -
    MINUS,
    // 加号 +
    PLUS,
    // 分号 ;
    SEMICOLON,
    // 斜杆 /
    SLASH,
    // 星号 *
    STAR,

    // ++
    PLUS_PLUS,
    // --
    MINUS_MINUS,

    // 逻辑运算符
    // &
    LOGIC_AND,
    // |
    LOGIC_OR,
    // 一个或两个字符标记。
    // 非 !
    BANG,
    // 不等于 !=
    BANG_EQUAL,
    // 等于
    EQUAL,
    // 等等于 ==
    EQUAL_EQUAL,
    // 大于 > 和 大于等于 >=
    GREATER, GREATER_EQUAL,
    // 小于 < 和 小于等于 <=
    LESS, LESS_EQUAL,

    // 字面值， 标识符，字符串， 数字
    IDENTIFIER, STRING, NUMBER,

    // 关键字.
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
    BREAK, CONTINUE, DO,
    EOF
}
