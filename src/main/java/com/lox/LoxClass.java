package com.lox;

import java.util.List;
import java.util.Map;

/**
 * @author peiyou
 * @version 1.0
 * @className LoxClass
 * @date 2023/10/7 10:52
 **/
public class LoxClass implements LoxCallable {

    final String name;

    private final Map<String, LoxFunction> methods;

    private final Map<String, LoxFunction> staticMethods;

    private final Map<String, LoxFunction> getter;

    private final LoxClass superclass;

    LoxClass(String name, Map<String, LoxFunction> methods, Map<String, LoxFunction> staticMethods, Map<String, LoxFunction> getter, LoxClass superclass) {
        this.name = name;
        this.methods = methods;
        this.staticMethods = staticMethods;
        this.getter = getter;
        this.superclass = superclass;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    public LoxFunction findStaticMethod(String name) {
        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }
        return null;
    }

    public LoxFunction findGetterMethod(String name) {
        if (getter.containsKey(name)) {
            return getter.get(name);
        }
        return null;
    }
}
