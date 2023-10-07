package com.lox;

/**
 * @author peiyou
 * @version 1.0
 * @className RPNPrinter
 * @date 2023/9/8 15:38
 **/
public class RPNPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return reversePolishNotation( expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return reversePolishNotation("", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return reversePolishNotation(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return reversePolishNotation("三目运算", expr.condition, expr.left, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return reversePolishNotation("var " + expr.name);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return reversePolishNotation(expr.name.lexeme + " = ", expr.value);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public String visitSelfIncOrDecrExpr(Expr.SelfIncOrDecr expr) {
        return null;
    }

    @Override
    public String visitLambdaExpr(Expr.Lambda expr) {
        return null;
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return null;
    }


    @Override
    public String visitThisExpr(Expr.This expr) {
        return null;
    }

    private String reversePolishNotation(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        for (Expr expr: exprs) {
            builder.append(expr.accept(this));
            builder.append(" ");
        }
        builder.append(name);
        return builder.toString();
    }


    @Override
    public String visitGetExpr(Expr.Get expr) {
        return null;
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Grouping(new Expr.Binary(new Expr.Literal(1), new Token(TokenType.PLUS, "+", null, 1), new Expr.Literal(2))),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Binary(new Expr.Literal(4), new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(3))));
        RPNPrinter printer = new RPNPrinter();
        System.out.println(printer.print(expression));
    }
}
