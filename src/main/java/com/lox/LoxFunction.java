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
    protected final boolean isInitializer;
    protected final Stmt.Function function;

    public LoxFunction(Stmt.Function function, Environment closure, boolean isInitializer) {
        this.closure = closure;
        this.function = function;
        this.isInitializer = isInitializer;
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
        if (isInitializer) return closure.getAt(0, "this");
        return null;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment();
        environment.define("this", instance);
        return new LoxFunction(function, environment, isInitializer);
    }
}
