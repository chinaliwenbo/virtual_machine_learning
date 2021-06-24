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
