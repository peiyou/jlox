package com.lox;

import java.util.List;

/**
 * @author peiyou
 * @version 1.0
 * @className LoxLambda
 * @date 2023/9/15 15:39
 **/
public class LoxLambda implements LoxCallable {

    private final Environment closure;
    private final Expr.Lambda lambda;

    public LoxLambda(Expr.Lambda lambda, Environment closure) {
        this.closure = closure;
        this.lambda = lambda;
    }

    @Override
    public int arity() {
        return lambda.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < lambda.params.size(); i++) {
            environment.define(lambda.params.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(lambda.body, environment);
        } catch (ReturnStmt r) {

            return r.value;
        }
        return null;
    }
}
