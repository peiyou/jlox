package com.lox;

import java.util.List;

/**
 * @author peiyou
 * @version 1.0
 * @className LoxGetterFunction
 * @date 2023/10/7 16:15
 **/
public class LoxGetterFunction extends LoxFunction {

    public LoxGetterFunction(Stmt.Function function, Environment closure, boolean isInitializer) {
        super(function, closure, isInitializer);
    }

    @Override
    public int arity() {
        return super.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return super.call(interpreter, arguments);
    }

    @Override
    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment();
        environment.define("this", instance);
        return new LoxGetterFunction(super.function, environment, super.isInitializer);
    }
}
