#### 一个简单的虚拟机系统，但是可以帮助深入理解一个虚拟机的实现

#### 虚拟机是什么？
* 虚拟机其实就是模拟cpu运行的一个环境
* 建立在一些低级语言的基础上，减少开发和控制量
* 比如JVM，免去了你需要自己管理内存容易泄露，提供封装好的多种GC方法
* 比如JVM，提供更多并发控制工具，比C来控制更舒服，从基础的volitale/sync/lock
到封装一些高阶的线程安全数据结构，到future/promise的框架，再到actor等模型。

#### 关于语言的理解？
* 任何语言的开发是为了适用一种应用场景。
* 比如JAVA的开发是为了减少程序员的工作量，提高容错性
* 比如SCALA的开发是忍受不了JAVA没有函数式变成，不是一个标准的面向对象语言(static不是)
* 比如PHP的开发，弱类型，易用快速开发，不提供线程(这样就没有并发的问题)，减少GC工作，
* 比如PYTHON的开发，有可能是因为无法忍受一些一些语法，和进一步通过弱类型降低开发成本，另外由于历史原因
PYTHON的解释器有一个GIL锁，由于锁的存在，你也几乎碰不到PYTHON并发的问题，如果加入并发，那么PYTHON解释器
将会有大量的工作要做，比如GC。事实上PYTHON里面的线程和协程更多的是为了在IO的时候利用CPU资源。

#### 为什么写这个？
* 对JVM有一个深入理解
* 比如GC，你可以理解为什么GC需要算法去管理，因为JVM通过GC免去了自己管理内存。缺点是灵活性降低了。
* 比如并发，比如你在Future调用的时候，你可以深入理解Future回调的实现原理，底层是填充堆区的占位变量，回调是在子线程执行。而promise只是桥梁 

#### 运行
* 直接运行Test.java即可

#### 指令集
```
* 一共提供了18个计算指令集,目的是为了理解原理。
public static final short IADD = 1;     // 两个int加
public static final short ISUB = 2;   // 剪发
public static final short IMUL = 3;  // 乘法
public static final short ILT  = 4;     // int大小
public static final short IEQ  = 5;     // int是否相对
public static final short BR   = 6;     // 方法指令PC地址跳转
public static final short BRT  = 7;     // false的时候跳转
public static final short BRF  = 8;     // true的时候跳转
public static final short ICONST = 9;   // 常量定义入栈
public static final short LOAD   = 10;  // 从本地方法区上下文加载数据
public static final short GLOAD  = 11;  // 从全局内存取数(可以理解为java堆)
public static final short STORE  = 12;  // 保存
public static final short GSTORE = 13;  // 保存
public static final short PRINT  = 14;  // 打印栈顶元素并出栈
public static final short POP  = 15;    // 出栈一个
public static final short CALL = 16;   // 方法调用，PC变更
public static final short RET  = 17;    // 返回父方法区上下文的指令PC位置

public static final short HALT = 18;  //结束运行
```

```editorconfig
模块一： 这个模块内包含的是解释器是如何工作的，有助于对java的JVM有深入的理解
模块二： 完成的利用java实现了一个jvm，可以执行简单的java代码。
模块三： spark sql解释以后执行的原理， 以join进行举例（待完成）。
```

#### 基于Antlr自己实现一个sql解析工具
#### 学习sparkSql是如何将一个sql转换为 => unresolved逻辑执行计划的
#### 有助于帮助了解sql的整个执行过程和优化
#### 举一反三： sqlpaser得益于离散数学的反正，已经有了很成熟的框架
#### 也可以类比到presto、hive、mysql等其他的数据查询工具上面去。帮助自己去了解



```
SQL作为现代计算机行业的数据处理事实标准，一直倍受数据分析师和软件开发者所青睐，
从最早应用SQL的单机DBMS（如MySQL、Oracle），
到NoSQL提供SQL兼容层（如HBase的Phoenix），到最近火热的NewSQL（如Spanner、TiDB），
还有几乎所有主流的计算框架（如Spark、Flink）都兼容了SQL。
```

```editorconfig
SQL是一种描述语言的规范，有SQL86、SQL89、SQL92、SQL1999、SQL2003、
SQL2006、SQL2008、SQL2011等标准，而MySQL、Oracle甚至Spark也有自己支持的SQL规范，
所谓SQL兼容其实也不是和SQL社区定义的标准100%一样，和Python定义语法规范但还有CPython、
Jython、Cython等多种虚拟机实现类似。
而SQL描述语言相比其他编程语言更简单一些，一般分为DDL和DML，
大部分计算系统只是支持DML中的查询语法，也就是SELECT FROM语句，
因为对于计算系统来说不需要管理Schema的修改以及数据的增删改，
如果是兼容SQL的存储系统就需要兼容几乎所有DDL和DML语句。
```
```editorconfig
那么要解析用户编写的SQL字符串一般有以下几种方法：

1、简单字符串处理，使用字符串查找或者正则表达式来提取SQL中的字段，
对于简单的SQL可以这样实现，但SQL规范还有复杂的开闭括号以及嵌套查询，
复杂SQL几乎不可能通过字符串匹配来实现。
2、使用已有的开源SQL解释器，不同开源系统解析SQL后的应用是不一样的，
一般会转化成自己实现的抽象语法树，这些数据结构包含自己定义的树节点以及表达式节点，
要兼容使用这些数据结构是非常困难的，
这也是为什么开源项目中支持SQL的很多但基本不可以复用同一套实现的原因。
3、从头实现SQL解释器，对SQL做词法解析、语法解析的项目已经有很多了，
通过遍历抽象语法树来解析SQL实现对应的业务逻辑，
从头实现一个SQL解释器并没有想象中困难，而且还可以选择增加或减少支持的SQL语法范围。
```


