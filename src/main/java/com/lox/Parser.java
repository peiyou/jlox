package com.lox;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.lox.TokenType.*;

/**
 * program        → declaration* EOF ;
 *
 * declaration    → classDecl
 *                | funDecl
 *                | varDecl
 *                | statement ;
 *
 * classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function*  | ("class" function)* | (IDENTIFIER block) "}" ;
 * funDecl        → "fun" function ;
 * function       → IDENTIFIER "(" parameters? ")" block;
 * parameters     → IDENTIFIER ( "," IDENTIFIER)* ;
 *
 *
 * varDecl        → "var" IDENTIFIER ( "=" comma )? ";" ;
 *
 * statement      → exprStmt
 *                | whileStmt
 *                | forStmt
 *                | doWhile
 *                | breakStmt
 *                | continueStmt
 *                | returnStmt
 *                | ifStmt
 *                | printStmt
 *                | block ;
 *
 * whileStmt      → "while" "(" comma ")" statement ;
 *
 * forStmt        → "for" "(" (varDecl | exprStmt | ";")
 *                      comma? ";"
 *                      comma? ")" statement;
 *
 * doWhileStmt    → "do" statement
 *                   "while" "(" comma ")" ;
 *
 * breakStmt      → "break" ";" ;
 *
 * continueStmt   → "continue" ";";
 *
 * returnStmt     → "return" comma? ";"  ;
 *
 * ifStmt         → "if" "(" comma ")" statement
 *                ( "else" statement )? ;
 *
 * exprStmt       → comma ";" ;
 * printStmt      → "print" comma ";" ;
 * block          → "{" declaration* "}" ;
 *
 * comma          → ternary (, ternary) *
 * ternary        → expression ? (expression | ternary) : (expression | ternary);
 * expression     → assignment ;
 * assignment     → (call "." )? IDENTIFIER "=" assignment
 *                | or ;
 * or             → and ( "or" and) *;
 * and            → equality ("and" equality) *;
 * equality       → logic ( ( "!=" | "==" ) logic )* ;
 * logic          → comparison (("&" | "|") comparison) * ;
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           → factor ( ( "-" | "+" ) factor )* ;
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 * unary          → ( "!" | "-" ) unary | call | selfIncOrDecr | lambda ;
 * lambda         → "fun" "(" parameters? ")" block;
 * selfIncOrDecr  → primary "++" | primary "--"
 * call           → primary ( "(" arguments ")" | "." IDENTIFIER ) *;
 * primary        → NUMBER | STRING | "true" | "false" | "nil"
 *                | "(" comma ")"
 *                | IDENTIFIER | "this" | "super" "." IDENTIFIER;
 * arguments      → ternary ( "," ternary ) * ;
 *
 * @author peiyou
 * @version 1.0
 * @className Parser
 * @date 2023/9/8 16:49
 **/
public class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;

    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        /*try {
            return comma();
        } catch (ParseError error) {
            synchronize();
            return comma();
        }*/

        return statements;
    }

    // comma → ternary (, ternary) *
    private Expr comma() {
        Expr expr = ternary();
        while (match(COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // ternary    → expression ? ternary : ternary;
    private Expr ternary() {
        Expr expr = expression();
        if (match(TERNARY_QUESTION_MARK)) {
            Expr left = ternary();
            if (match(TERNARY_COLON)) {
                Expr right = ternary();
                expr = new Expr.Ternary(expr, left, right);
            }
        }
        return expr;
    }

    private Expr expression() {
        return assignment();
    }

    /**
     *  assignment     → (call "." )? IDENTIFIER "=" assignment
     *                | or ;
     * @return
     */
    private Expr assignment() {
        Expr expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }
            error(equals, "无效的赋值.");
        }
        return expr;
    }

    //  * or             → and ( "or" and) *;
    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // * and            → equality ("and" equality) *;
    private Expr and() {
        Expr expr = equality();
        while(match(AND)) {
            Token token = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, token, right);
        }
        return expr;
    }
    /**
     * declaration    →
     *                | funDecl
     *                | varDecl
     *                | statement ;
     * @return
     */
    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (check(FUN) && checkNext(IDENTIFIER) && match(FUN)) return funDecl();
            if (match(VAR)) return varDecl();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    // classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function*  | ("class" function)* | (IDENTIFIER block) "}" ;
    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        Expr.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> staticMethods = new ArrayList<>();
        List<Stmt.Function> getter = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            if (match(CLASS)) {
                staticMethods.add(function("staticMethod"));
            } else if (check(IDENTIFIER) && checkNext(LEFT_BRACE)) {
                Token getterName = consume(IDENTIFIER, "");
                consume(LEFT_BRACE, "Expect '{' before block body.");
                List<Stmt> block = block();
                List<Token> parameters = new ArrayList<>();
                Stmt.Function function = new Stmt.Function(getterName, parameters, block);
                getter.add(function);
            } else {
                methods.add(function("method"));
            }
        }
        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, methods, staticMethods, getter, superclass);
    }

    /**
     *  * funDecl        → "fun" function ;
     *
     *  * parameters     → IDENTIFIER ( "," IDENTIFIER)* ;
     * @return
     */
    private Stmt funDecl() {
        return function("function");
    }

    //   function       → IDENTIFIER? "(" parameters? ")" block;

    private Stmt.Function function(String kind) {
        Token name = null;
        if (check(IDENTIFIER)) {
            name = consume(IDENTIFIER, "Expect " + kind + " name.");
        }
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = parseFunctionParameters();
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    // "var" IDENTIFIER ( "=" comma )? ";" ;
    private Stmt varDecl() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = comma();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    /**
     * statement      → exprStmt
     *                | whileStmt
     *                | forStmt
     *                | doWhile
     *                | breakStmt
     *                | continueStmt
     *                | ifStmt
     *                | printStmt
     *                | block ;
     * @return
     */
    private Stmt statement() {
        if (match(DO)) return doWhileStmt();
        if (match(CONTINUE)) return continueStmt();
        if (match(BREAK)) return breakStmt();
        if (match(RETURN)) return returnStmt();
        if (match(WHILE)) return whileStmt();
        if (match(FOR)) return forStmt();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    // returnStmt     → "return" comma? ";"  ;
    private Stmt returnStmt() {
        Token token = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = comma();
        }
        consume(SEMICOLON, "Expect ';' after return.");
        return new Stmt.Return(token, value);
    }

    private Stmt breakStmt() {
        Stmt stmt = new Stmt.Break(previous());
        consume(SEMICOLON, "Expect ';' after break.");
        return stmt;
    }

    private Stmt continueStmt() {
        Stmt stmt = new Stmt.Break(previous());
        consume(SEMICOLON, "Expect ';' after continue.");
        return stmt;
    }

    // whileStmt → while "(" comma ")" statement ;
    private Stmt whileStmt() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = comma();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    /**
     * forStmt        → "for" "(" (varDecl | exprStmt | ";")
     *                      comma? ";"
     *                      comma? ")" statement;
     * @return
     */
    private Stmt forStmt() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDecl();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = comma();
        }
        consume(SEMICOLON, "Expect ';' after for condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = comma();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(
                    initializer,
                    body
            ));
        }
        return body;
    }

    /**
     * doWhileStmt    → "do" statement
     *                   "while" "(" comma ")" ;
     * @return
     */
    private Stmt doWhileStmt() {
        Stmt body = statement();
        consume(WHILE, "Expect 'while' after 'do'.");
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = comma();
        consume(RIGHT_PAREN, "Expect ')' after 'while'.");
        consume(SEMICOLON, "Expect ';' after while clauses.");
        Stmt whileStmt = new Stmt.While(condition, body);
        return new Stmt.Block(Arrays.asList(
                body,
                whileStmt
        ));
    }

    /**
     * ifStmt         → "if" "(" comma ")" statement
     *                 ( "else" statement )? ;
     * @return
     */
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = comma();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    /**
     * block          → "{" declaration* "}" ;
     * @return
     */
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }
    private Stmt printStatement() {
        Expr value = comma();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = comma();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }


    // equality  → logic ( ( "!=" | "==" ) logic )* ;
    private Expr equality() {
        Expr expr = logic();

        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = logic();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // logic  → comparison (("&" | "|") comparison) * ;
    private Expr logic() {
        Expr expr = comparison();
        while(match(LOGIC_AND, LOGIC_OR)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // term → factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor → unary ( ( "/" | "*" ) unary )*
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary  → ( "!" | "-" ) unary | call | selfIncOrDecr | lambda
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        } else if (checkNext(PLUS_PLUS, MINUS_MINUS)) {
            return selfIncOrDecr();
        } else if (match(FUN)) {
            return lambda();
        }
        return call();
    }

    // lambda         → "fun" "(" parameters? ")" block;
    private Expr lambda() {
        Token token = previous();
        consume(LEFT_PAREN, "Expect '(' after lambda.");
        List<Token> parameters = parseFunctionParameters();
        consume(LEFT_BRACE, "Expect '{' before lambda body.");
        List<Stmt> body = block();
        return new Expr.Lambda(token, parameters, body);
    }

    /**
     * 解析方法中的参数
     * @return
     */
    private List<Token> parseFunctionParameters() {
        List<Token> parameters = new ArrayList<>();
        // 如果不是 ) 号,说明有参数
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        return parameters;
    }

    // selfIncOrDecr  → primary "++" | primary "--"
    private Expr selfIncOrDecr() {
        Expr expr = primary();
        if (match(PLUS_PLUS, MINUS_MINUS)) {
            Token operator = previous();
            return new Expr.SelfIncOrDecr(operator, expr);
        }
        return expr;
    }

    // call           → primary ( "(" arguments ")" | "." IDENTIFIER ) *;
    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "参数最多只能有254个。");
                }
               arguments.add(ternary());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    // primary        → NUMBER | STRING | "true" | "false" | "nil"
    //               | "(" comma ")" | IDENTIFIER | "this" | "super" "." IDENTIFIER;
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super'.");
            Token method = consume(IDENTIFIER,
                    "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(THIS)) return new Expr.This(previous());

        if (match(LEFT_PAREN)) {
            Expr expr = comma();
            consume(RIGHT_PAREN, "表达示缺少)号");
            return new Expr.Grouping(expr);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }


    private boolean match(TokenType... types) {
        for (TokenType type: types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }


    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean checkNext(TokenType... types) {
        for (TokenType type: types) {
            Token token = next();
            if (token != null && token.type == type) {
                return true;
            }
        }
        return false;
    }

    private Token next() {
        if (!isAtEnd()) {
            return tokens.get(current + 1);
        } else {
            return null;
        }
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}


