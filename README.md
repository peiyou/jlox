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

    5、```class { functionName() {...} } ``` 定义普通方法，也可以定义类似java的静态方法
  - 5.1 静态方法定义 ``` class { class functionName() {...}}  ``` 与普通方法的区别在于 前面加了class关键字. 表示该方法为类级别的。
  - 5.2 getter 方法，``` class { propertiesName {...} } ``` 直接写属性名加大括号，可以表示getter的方法，调用时像访问属性一样。
  - 5.3 init 方法 ，相当于构造函数，在创建对象时，传入对应的参数，暂时不支持重载。如: ``` init(a, b) {...}``` 两个参数的构造函数。



## 运行说明
入口文件：com.lox.Lox

运行main方法，打开一个交互式端。 

也可以在运行时 在后面带一个文件 后缀无所谓。



### 原文
- https://www.craftinginterpreters.com/
