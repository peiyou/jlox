package com.lox;

import java.util.List;

/**
 * @author peiyou
 * @version 1.0
 * @className LoxCallable
 * @date 2023/9/15 10:48
 **/
public interface LoxCallable {

    /**
     * 方法或类的参数个数
     * @author Peiyou
     * @date 2023/9/15 10:52
     * @return
     */
    int arity();

    /**
     * 调用
     * @param interpreter
     * @param arguments
     * @return
     */
    Object call(Interpreter interpreter, List<Object>arguments);
}
