# JJvm
##### 引用另外一个同学的作品，可以作为simple虚拟机的升级版本，
##### 使用jvm7的规范编写，可以真正了解到jvm类的加载，解释过程，执行过程，代码也并不复杂
这是一个Java实现的JAVA虚拟机，它会非常简单，实际上简单的只够运行HelloWorld。虽然简单，但尽量符合 JVM 标准，目前主要参考依据是[《Java虚拟机规范 （Java SE 7 中文版）》](http://www.iteye.com/topic/1117824)。

关于此项目的说明，[详见此文](http://www.jianshu.com/p/4d81465c2fb8)。

## 运行

先写一个HelloWorld，代码如下：

```java
package org.caoym;

public class HelloWorld {
    public static void main(String[] args){
        System.out.println("Hello World");
    }
}
```

可以通过以下命令运行：

```shell
$ java /home/myclasspath org.caoym.jjvm.JJvm org.caoym.HelloWorld
Hello World
```

## 可运行的示例


