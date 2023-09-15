package com.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * 每多一层结构块，就会在外层多出一层环境，最大的块的变量是放在最里面的环境。
 * 如下:
 * class {
 *     var x = 1;
 *
 *     func f1() {
 *         var x = 2;
 *         print x;
 *         {
 *             var x = 3;
 *             print x;
 *
 *         }
 *     }
 * }
 *
 * 上述代码中会放三层环境
 * 最外层是 块环境->方法环境->类环境
 *
 * @author peiyou
 * @version 1.0
 * @className Environment
 * @date 2023/9/13 14:31
 **/
public class Environment {
    // 变量表示更上一次的环境
    final Environment enclosing;

    public Environment() {
        this(null);
    }
    public Environment(Environment environment) {
        this.enclosing = environment;
    }
    private final Map<String, Object> values = new HashMap<>();

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "未定义的变量名'" + name.lexeme + "'；");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "未定义变量'" + name.lexeme + "'；");
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }
}
