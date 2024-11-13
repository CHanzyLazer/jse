- [命令行参数](commandline.md)
    - [*None*](#none)
    - [-t/-text](#t-text)
    - [-f/-file](#f-file)
    - [-i/-invoke](#i-invoke)
    - [-v/-version](#v-version)
    - [-?/-help](#help)
    - [-idea](#idea)
    - [-groovy](#groovy)
    - [-python](#python)
    - [-groovytext](#groovytext)
    - [-pythontext](#pythontext)
    - [-jupyter](#jupyter)
- [**⟶ 目录**](contents.md)

# 命令行参数

在命令行中使用 jse，这里设计成类似 python
的使用方法，即通过：

```shell
jse path/to/script
```

来运行一个脚本，而直接运行：

```shell
jse
```

则会打开一个交互式的 jse 终端。


## *None*

当不增加任何输入参数时，则会启动一个
[Groovy Shell](https://docs.groovy-lang.org/latest/html/documentation/groovysh.html)，
用于在 shell 中执行 groovy 脚本（类似直接输入 `python`）。

- **执行**
    
    ```shell
    jse
    ```
    
- **输出**
    ```
    JSE Shell (Groovy: 4.0.19, JVM: 17.0.8)
    Type ':help' or ':h' for help.
    --------------------------------------------------
    groovy:000>
    ```

> 在 shell 模式中，会增加一些默认导入来方便使用，
> 在 groovy shell 中执行 `:show imports` 来查看这些导入。
> 


## -t/-text

增加 `-t` 或 `-text` 后，jse 会将后续输入的字符串当作 groovy 指令执行。
注意这里一般需要将整个指令使用单引号 `'` 包围，
保证整个指令被当作一个字符串传入。

- **执行**
    
    ```shell
    jse -t 'println(/hello world/)'
    ```
    
- **输出**
    ```
    hello world
    ```

> 这里不会增加默认导入，因此如果需要使用 jse 的方法则需要使用完整包名或者手动导入，
> 如：
>
> ```shell
> jse -t 'println(jse.code.CS.VERSION)'
> ```
>
> 或：
>
> ```shell
> jse -t 'import static jse.code.CS.*; println(VERSION)'
> ```
> 
> 可以通过 groovy 的 [斜杠字符串](https://groovy-lang.org/syntax.html#_slashy_string)
> 或者 [美元斜杠字符串](https://groovy-lang.org/syntax.html#_dollar_slashy_string)
> 来避免命令行中以及 [使用 `system` 提交任务](system.md) 
> 时烦人的引号转义问题，如：
>
> ```groovy
> import static jse.code.OS.*
> 
> if (IS_WINDOWS) {
>     system("jse -t 'println(''hello world'')'")
> } else {
>     system("jse -t \"println('hello world')\"")
> }
> ```
> 
> 可以改为：
> 
> ```groovy
> import static jse.code.OS.*
> 
> system("jse -t 'println(/hello world/)'")
> ```
>
> 或：
>
> ```groovy
> import static jse.code.OS.*
> 
> system("jse -t 'println(\$/hello world/\$)'")
> ```
>


## -f/-file

增加 `-f` 或 `-file` 后，jse 会将输入的参数作为 groovy 或 python 脚本文件的路径，
会根据后缀自动检测脚本类型并并执行。

此参数可以省略。

- **执行**
    
    ```shell
    jse -f helloWorld.groovy
    ```
    
    或：
    
    ```shell
    jse helloWorld.groovy
    ```
    
- **输出**
    ```
    hello world
    ```


> 可以省略 groovy 脚本的 `.groovy` 后缀，例如：
>
> ```shell
> jse helloWorld
> ```
> 
> 对于 python 脚本则不能省略后缀，因为需要根据 `.py`
> 后缀来识别为 python 脚本并通过内部的 jep 来执行：
>
> ```shell
> jse helloWorld.py
> ```
>



## -i/-invoke

增加 `-i` 或 `-invoke` 后，jse 会将输入的参数作为一个 java 的静态函数
（或者静态成员的函数）直接调用，而后续参数则作为函数的参数传入。

此行为不通过 groovy 而是直接使用 java 的反射机制来实现，
避免了 groovy 的初始化可以让指令迅速执行，但是也需要使用完整的包名。

- **执行**
    
    ```shell
    jse -i java.lang.System.out.println 'hello world'
    ```
    
- **输出**
    ```
    hello world
    ```

> 此行为偏向内部使用，因此目前仅支持调用 String 类型输入的方法。
> 

## -v/-version

增加 `-v` 或 `-version` 后 jse 会输出当前的 jse 版本以及
groovy 版本以及 java 版本并退出。

- **执行**
    
    ```shell
    jse -v
    ```
    
- **输出**
    ```
    jse version: 2.8.3 (java: 17.0.8)
    ```


## -?/-help

增加 `-?`， `-help` 或任意非法的参数后，jse 会输出帮助信息。

- **执行**
    
    ```shell
    jse -?
    ```
    
- **输出**
    ```
    Usage:    jse [-option] value [args...]
    Such as:  jse path/to/script.groovy [args...]
    Or:       jse -t 'println(/hello world/)'

    The options can be:
        -t -text      Run the groovy text script
        -f -file      Run the groovy/python file script (default behavior when left blank)
        -i -invoke    Invoke the internal java static method directly
        -v -version   Print version number
        -? -help      Print help message
        -groovy       Run the groovy file script
        -python       Run the python file script
        -groovytext   Run the groovy text script
        -pythontext   Run the python text script
        -jupyter      Install current jse to the jupyter kernel

    You can also using another scripting language such as MATLAB or Python with Py4J and import jse-*.jar
    ```


## -idea

初始化当前文件夹为 [IntelliJ IDEA](https://www.jetbrains.com/idea/)
项目文件夹，从而可以在 idea 中调试 groovy 脚本。

- **执行**
    
    ```shell
    jse -idea
    ```
    
- **输出**
    ```
    The current directory has been initialized as an Intellij IDEA project,
    now you can open this directory through Intellij IDEA.
    ```

> 第一次打开文件夹需要设置 jdk 的路径：
>     
> ```
> 左上角“文件” ⟶ 项目结构 ⟶ 左边栏选择“项目设置-项目” ⟶ SDK 选择本地安装的 JDK
> ⟶ 语言级别：8 - lambda、类型注解等 ⟶ 右下角“确定”
> ```
>


## -groovy

增加 `-groovy` 后，jse 会将输入的参数作为 groovy 脚本文件的路径，
会尝试找到此脚本文件并执行。

和 `-f`/`-file`，不同，此时不会自动判断脚本类型，
永远将输入的文件当作 groovy 脚本执行。


## -python

增加 `-python` 后，jse 会将输入的参数作为 python 脚本文件的路径，
会尝试找到此脚本文件并执行。

和 `-f`/`-file`，不同，此时不会自动判断脚本类型，
永远将输入的文件当作 python 脚本执行。


## -groovytext

增加 `-groovytext` 后，jse 会将后续输入的字符串当作 groovy 指令执行。

和 `-t`/`-text` 行为完全一致。


## -pythontext

增加 `-pythontext` 后，jse 会将后续输入的字符串当作 python 指令执行。

和 `-t`/`-text` 不同，此时会认为输入的指令为 python
指令并通过 jep 来运行。

> 由于 groovy 指令会先进行编译，因此一般来说 python
> 指令启动更快，而 groovy 指令对于长时的任务会更快
> 


## -jupyter

将当前的 jse 安装到 jupyter 内核中，从而可以在 jupyter notebook
中使用 jse 来解析 groovy 或者 python 代码块。

- **执行**
    
    ```shell
    jse -jupyter
    ```
    
- **输出**
    ```
    The jupyter kernel for JSE has been initialized,
    now you can open the jupyter notebook through `jupyter notebook`
    ```

此指令使用 jpyter 提供的 `KernelSpecManager.install_kernel_spec`
函数来安装 jupyter 内核，此函数还存在两个可调参数 `user`
以及 `prefix`，这里可以直接通过在命令行后面添加来进行设置，
例如：

```shell
jse -jupyter user=True
```

表示为当前用户安装；

```shell
jse -jupyter prefix=sys.prefix
```

表示设置 `prefix` 为 `sys.prefix`，详细可参考
[jupyter 官方文档](https://jupyter-client.readthedocs.io/en/latest/api/jupyter_client.html#jupyter_client.kernelspec.KernelSpecManager.install_kernel_spec) 。

