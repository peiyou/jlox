package com.lox;


/**
 * @author peiyou
 * @version 1.0
 * @className ReplInterpreter
 * @date 2023/9/13 16:02
 **/
public class ReplInterpreter extends Interpreter {

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        // System.out.println(stringify(value));
        return null;
    }
}
