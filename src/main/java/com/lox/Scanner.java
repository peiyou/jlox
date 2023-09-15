package com.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.TokenType.*;

/**
 * @author peiyou
 * @version 1.0
 * @className Scanner
 * @date 2023/9/8 09:58
 **/
public class Scanner {

    private final String source;

    private final List<Token> tokens = new ArrayList<>();

    // 正在扫描的词位中的第一个字符
    private int start = 0;
    // 当前正在被扫描的字符
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("do",  DO);
        keywords.put("break", BREAK);
        keywords.put("continue", CONTINUE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            // 下一个词位的开头
            start = current;
            scanToken(0);
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private int scanToken(int condition) {
        char c = advance();
        switch(c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT);break;
            case '-':
                if (match('-')) {
                    addToken(MINUS_MINUS);
                } else {
                    addToken(MINUS);
                }
                break;
            case '+':
                if (match('+')) {
                    addToken(PLUS_PLUS);
                } else {
                    addToken(PLUS);
                }
                break;
            case ';': addToken(SEMICOLON);break;
            case '*': addToken(STAR);break;
            case '&': addToken(LOGIC_AND); break;
            case '|': addToken(LOGIC_OR); break;
            case '!':
                addToken(match('=')? BANG_EQUAL: BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL: LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL: GREATER);
                break;
            case '/':
                if (match('/')) {
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    while (!isAtEnd() && !(peek() == '*' && peekNext()=='/')) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (!isAtEnd()) {
                        // 消耗 *号
                        advance();
                    } else {
                        Lox.error(line, "未结束的注释。");
                        return 0;
                    }
                    if (!isAtEnd()) {
                        // 消耗 / 号
                        advance();
                    } else {
                        Lox.error(line, "未结束的注释。");
                        return 0;
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            case '?':
                int tempLine = line;
                // 记录当前的token应该添加的位置
                int index = tokens.size();
                // 等待一个 冒号
                int count = condition + 1;
                int res = 0;
                do {
                    // 下一个词位的开头
                    start = current;
                    res = scanToken(count);
                } while (!isAtEnd() && res != count);
                if (!isAtEnd()) {
                    addToken(TERNARY_COLON);
                    addToken(index, TERNARY_QUESTION_MARK, "?", null, tempLine);
                    return res;
                } else {
                    Lox.error(line, "未结束的三目运算符");
                    return 0;
                }
            case ':':
                if (condition == 0) {
                    Lox.error(line, "多余的:号。");
                }
                return condition;
            case ' ':
            case '\r':
            case '\t':
                // 忽略
                break;
            case '\n':
                line++;
                break;
            case '"': string();break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "不支持的字符.");
                }
                break;
        }
        return 0;
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        // 看是关键字还是普通的变量名之类的
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            // 消耗掉 . 号
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "未结束的字符串。");
            return;
        }
        // 找到结束的 右引号 "
        advance();
        // 只取双引号里面的内容
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * 查找并匹配下一个字符是否是预期的字符
     * @author Peiyou
     * @date 2023/9/8 10:25
     * @param expected
     * @return boolean
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }


    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char previous() {
        return source.charAt(current - 1);
    }
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void addToken(int index,TokenType type, String text, Object literal, int line) {
        tokens.add(index, new Token(type, text, literal, line));
    }
}
