# jlox
用JAVA实现一个新的语言，是一种动态类型的语言，在运行过程中计算出类型。

- 支持类型  boolean、double、string ， 所有类型当做boolean类型，空和false之外 都为true。
- 语句需要用 ; （英文）号结束。


当前支持的语法
- 基本表达式
    
    1、加、减、乘、除、后自增（如 i ++）、后自减（如 i --）。 不支持 前自增 如  ++ i 这种不支持。
    
    2、逻辑 and 和 or 和 ! (非) 

    3、二进制操作 & 和 | 

    4、三目表示式  如  ```x == y ?  xx : yy;```
- 条件控制

    1、if 判断

    2、for 循环

    3、while 循环

    4、do while 循环

    5、break、continue 和 return 

- 结构定义

    1、fun 函数定义 如 ```fun functionName(parameter) {}```

    2、var 变量定义 如 ```var name = "xx";```

    3、lambda 如  ```var a = fun(parameter) {} ``` 或 在函数调用时，直接传入匿名函数

    4、闭包 



## 运行说明
入口文件：com.lox.Lox

运行main方法，打开一个交互式端。 

也可以在运行时 在后面带一个文件 后缀无所谓。

