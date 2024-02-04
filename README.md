# 简介
使用 [java](https://en.wikipedia.org/wiki/Java_(programming_language))
编写的项目管理工具，目前提供了：
- 通用的任务提交接口（本地，WSL，powershell，mpi，srun，ssh，...）
- 针对 Lammps 输出文件的读写（data，dump，log）
- 针对 Lammps 运行的输入文件支持并内置常用的 Lammps 运行方式的实现
- 通用的原子参量计算（RDF，SF，Q4/Q6/Ql）
- 其他常见操作（文件读写，读写 json、csv、yaml，常见的数学运算，简单的绘图，...）
- 简单跨语言编程支持（python，matlab，java/groovy）
- 进阶的模拟相关算法（FFS，KMC）

<!--
jtool 使用 [java](https://en.wikipedia.org/wiki/Java_(programming_language)) 编写，
并通过 jdk (Java SE Development Kit) 编译成 java 字节码（`jtool-all.jar` 文件）。
在运行时，首先会创建一个 java 虚拟机 jvm (Java Virtual Machine)，
然后通过 jvm 来运行编译好的 java 字节码文件。
jvm 由一个规范来详细说明，该规范要求 jvm 需要实现的内容，
此规范可确保 java 程序在不同实现之间的互操作性。

这种方式可以让代码以及编译后的 java 字节码都不用担心底层硬件平台的特性，
可以为不同的平台发布同一个编译后的 jar 文件，兼顾效率和兼容性。
同时 java 内置的垃圾回收器可以在编程时不去担心内存泄漏的问题。 -->

<!-- # 为何选择 java
- **较高性能**：由于经过了编译操作，性能更加接近 C++ 而不是 python 这种脚本语言。并且有成熟的 api 实现并行。
- **高兼容性**：相比 C++，其不受平台和编译器的影响，不用在迁移平台时考虑兼容性的问题。
- **其他语言的兼容性**：在 matlab 中可以原生的兼容 java 程序，在 python 中可以使用类似。
  [py4j](https://www.py4j.org/) 这种库来简单的实现对 java 程序的支持，实际使用体验和原生程序基本没有区别。
- **成熟的编辑器支持**：[IntelliJ IDEA](https://www.jetbrains.com/idea/) 等编辑器都对 java 提供了成熟的支持，
  开发会更加高效。
- **丰富的第三方库**：[Maven 仓库](https://mvnrepository.com/) 中有大量的 java 的第三方库可供使用，
  可以避免重复造轮子。
- **成熟的项目管理工具**：[Gradle](https://gradle.org/) 或者 [Maven](https://maven.apache.org/)
  工具现在可以非常方便的管理项目，（相比 C++ 中的 cmake）不需要担心其他人无法成功完成编译。 -->


# 如何使用
参考 [基本使用方式](doc/usage.md)，
详细接口介绍参考 [使用文档（正在编写）](doc/contents.md)。


# 编译项目
本项目使用 [Gradle](https://gradle.org/) 进行管理（不需要安装 Gradle）。

在根目录运行 `./gradlew build` 即可进行编译，
默认会将 jar 文件输出到 `release/lib` 文件夹。

