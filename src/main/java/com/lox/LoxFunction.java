package com.lox;

import java.util.List;

/**
 * @author peiyou
 * @version 1.0
 * @className LoxFunction
 * @date 2023/9/15 11:33
 **/
public class LoxFunction implements LoxCallable {
    private final Environment closure;
    private final Stmt.Function function;

    public LoxFunction(Stmt.Function function, Environment closure) {
        this.closure = closure;
        this.function = function;
    }

    @Override
    public int arity() {
        return function.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < function.params.size(); i++) {
            environment.define(function.params.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(function.body, environment);
        } catch (ReturnStmt r) {

            return r.value;
        }
        return null;
    }
}
