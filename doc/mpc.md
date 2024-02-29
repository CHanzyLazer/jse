- [原子结构参量计算](mpc.md)
    - [参量计算器初始化](#参量计算器初始化)
    - [RDF 和 SF 的计算](#rdf-和-sf-的计算)
    - [计算 BOOP 和 ABOOP]()
    - [Voronoi 分析]()
    - [近邻列表获取]()
- [**⟶ 目录**](contents.md)

# 原子结构参量计算

jse 中使用 [`jse.atom.MPC`](../src/main/java/jse/atom/MPC.java) /
[`jse.atom.MonatomicParameterCalculator`](../src/main/java/jse/atom/MonatomicParameterCalculator.java)
来实现原子结构的参量计算，两者完全一致，
`MPC` 只是 `MonatomicParameterCalculator` 的简称。


## 参量计算器初始化

jse 中可以通过构造函数 `<init>` 直接创建一个参数计算器，
也可以通过静态方法 `withOf` 来使用一个自动关闭的参数计算器。

- **`<init>`**
    
    描述：`jse.atom.MPC` 的构造函数。
    
    输入1：根据输入类型重载，具体为：
    
    - `IAtomData`，jse 使用的任意的原子数据
    - `Collection<? extends IXYZ>, IXYZ`，原子坐标数据 `IXYZ`
      组成的数组以及模拟盒的大小 `IXYZ`（实际为两个输入）
    
    输入2（可选）：`int`，计算器使用的线程数，默认为 1（不开启并行）
    
    输入3（可选）：`double`，用于获取近邻列表进行分划 cell 的步长，
    默认为 `1.26`（更小的值会分划更细的 cell 从而提高近邻列表获取速度，
    但是会占用更多的内存）
    
    输出：`MPC`，创建的参量计算器对象
    
    例子：`example/mpc/rdf`
    [⤤](../release/script/groovy/example/mpc/rdf.groovy)，
    `example/mpc/rdfmulti`
    [⤤](../release/script/groovy/example/mpc/rdfmulti.groovy)
    
    > 注意：创建后记得在使用完成后显式调用 `shutdown()` 关闭 MPC 回收资源，
    > 或者使用 [*try-with-resources*](https://www.baeldung.com/java-try-with-resources)
    > 实现自动回收。
    > 
    
    -----------------------------
    
- **`withOf`**
    
    描述：根据输入参数构造一个 `jse.atom.MPC`，
    并将其作为一个闭包的输入，在闭包内进行任意计算后自动关闭此 MPC；
    相比直接使用构造函数，这种方法一般更加简洁。
    
    输入1：根据输入类型重载，具体为：
    
    - `IAtomData`，jse 使用的任意的原子数据
    - `Collection<? extends IXYZ>, IXYZ`，原子坐标数据 `IXYZ`
      组成的数组以及模拟盒的大小 `IXYZ`（实际为两个输入）
    
    输入2（可选）：`int`，计算器使用的线程数，默认为 1（不开启并行）
    
    输入3（可选）：`double`，用于获取近邻列表进行分划 cell 的步长，
    默认为 `1.26`（更小的值会分划更细的 cell 从而提高近邻列表获取速度，
    但是会占用更多的内存）
    
    输入end：`IUnaryFullOperator<T, MPC>`，一个输入 MPC
    并输出任意结果（一般是计算结果）的闭包
    
    输出：`T`，通过闭包定义的输出结果，一般是使用 MPC 计算的结果
    
    例子：`example/mpc/rdf`
    [⤤](../release/script/groovy/example/mpc/rdf.groovy)

    
## RDF 和 SF 的计算

MPC 可以计算单个结构的 RDF（radial distribution function）
以及 SF（structural factor），并提供了相互转换的方法。

具体定义和应用可以
[参考文献 10.1088/0034-4885/69/1/R05](https://doi.org/10.1088/0034-4885/69/1/R05)。

对于有限温度需要进行时间平均的情况，可以参考 `example/mpc/rdfmulti`
[⤤](../release/script/groovy/example/mpc/rdfmulti.groovy)
的方法对所有帧进行计算并取平均。

- **`calRDF`**
    
    描述：计算 RDF (radial distribution function，即 g(r))，
    只计算一个固定结构的值，因此不包含温度信息。
    
    输入1（可选）：`int`，指定分划的份数（默认为 160）
    
    输入2（可选）：`double`，指定计算的最大半径（默认为 6 倍*单位长度*）
    
    输出：`IFunc1`，计算得到的 g(r)
    
    例子：`example/mpc/rdf`
    [⤤](../release/script/groovy/example/mpc/rdf.groovy)，
    `example/mpc/rdfmulti`
    [⤤](../release/script/groovy/example/mpc/rdfmulti.groovy)
    
    > 注意：会按照周期边界条件处理边界，
    > 理论上能够正确处理原子模拟盒小于输入最大半径的情况。
    > 
    > *单位长度*定义：$\text{uintLen} = (\text{volume} / \text{natoms})^{1/3}$，
    > 可以通过 `MPC.unitLen()` 获取。
    >
    
    -----------------------------
    
- **`calRDF_AB`**
    
    描述：计算自身与输入的原子坐标数据之间的 RDF，
    只计算一个固定结构的值，因此不包含温度信息；
    主要用于计算两种不同元素之间的 RDF。
    
    输入1：根据输入类型重载，具体为：
    
    - `Collection<? extends IXYZ> `，另一个种类的原子坐标数据 `IXYZ` 数组
    - `MPC`，另一个种类的原子坐标数据构建的 MPC
    
    输入2（可选）：`int`，指定分划的份数（默认为 160）
    
    输入3（可选）：`double`，指定计算的最大半径（默认为 6 倍*单位长度*）
    
    输出：`IFunc1`，计算得到的 g(r)
    
    例子：`example/mpc/rdf`
    [⤤](../release/script/groovy/example/mpc/rdf.groovy)
    
    -----------------------------
    
- **`calRDF_G`**
    
    描述：使用带有一定展宽的高斯分布代替直接计数来计算 RDF；
    用于获得更加连续光滑的函数。
    
    输入1（可选）：`int`，指定分划的份数（默认为 1000）
    
    输入2（可选）：`double`，指定计算的最大半径（默认为 6 倍*单位长度*）
    
    输入3（可选）：`int`，高斯分布的一个标准差宽度对应的分划份数（默认为 4）
    
    输出：`IFunc1`，计算得到的 g(r)
    
    例子：`example/mpc/rdf`
    [⤤](../release/script/groovy/example/mpc/rdf.groovy)
    
    -----------------------------
    
- **`calRDF_AB_G`**
    
    描述：使用带有一定展宽的高斯分布代替直接计数来计算 RDF；
    用于获得更加连续光滑的函数。
    
    输入1：根据输入类型重载，具体为：
    
    - `Collection<? extends IXYZ> `，另一个种类的原子坐标数据 `IXYZ` 数组
    - `MPC`，另一个种类的原子坐标数据构建的 MPC
    
    输入2（可选）：`int`，指定分划的份数（默认为 1000）
    
    输入3（可选）：`double`，指定计算的最大半径（默认为 6 倍*单位长度*）
    
    输入4（可选）：`int`，高斯分布的一个标准差宽度对应的分划份数（默认为 4）
    
    输出：`IFunc1`，计算得到的 g(r)
    
    例子：`example/mpc/rdf`
    [⤤](../release/script/groovy/example/mpc/rdf.groovy)
    
    -----------------------------
    
- **`calSF`**
    
    描述：计算 SF（structural factor，即 S(q)），
    只计算一个固定结构的值，因此不包含温度信息。
    
    输入1（可选）：`double`，额外指定最大计算的 q 的位置（默认为 6 倍*单位长度*）
    
    输入2（可选）：`int`，指定分划的份数（默认为 160）
    
    输入3（可选）：`double`，指定计算的最大半径（默认为 6 倍*单位长度*）
    
    输入4（可选）：`double`，指定最小的截断的 q（由于 pbc 的原因，过小的结果会发散，
    默认为 0.6 倍*单位长度*）
    
    输出：`IFunc1`，计算得到的 S(q)
    
    例子：`example/mpc/sf`
    [⤤](../release/script/groovy/example/mpc/sf.groovy)
    
    > 注意：会按照周期边界条件处理边界，
    > 理论上能够正确处理原子模拟盒小于输入最大半径的情况。
    > 
    > *单位长度*定义：$\text{uintLen} = (\text{volume} / \text{natoms})^{1/3}$，
    > 可以通过 `MPC.unitLen()` 获取；
    > 对于 q 值会取倒数，具体为：$\text{uintLenQ} = 2\pi / \text{uintLen}$。
    >
    > 直接计算耗时且收敛性较差，建议使用 `MPC.RDF2SF`
    > 通过傅里叶变换来将 RDF 转为 SF 来间接计算。
    >
    
    -----------------------------
    
- **`calSF_AB`**
    
    描述：计算自身与输入的原子坐标数据之间的 SF，
    只计算一个固定结构的值，因此不包含温度信息；
    主要用于计算两种不同元素之间的 SF。
    
    输入1：根据输入类型重载，具体为：
    
    - `Collection<? extends IXYZ> `，另一个种类的原子坐标数据 `IXYZ` 数组
    - `MPC`，另一个种类的原子坐标数据构建的 MPC
    
    输入2（可选）：`double`，额外指定最大计算的 q 的位置（默认为 6 倍*单位长度*）
    
    输入3（可选）：`int`，指定分划的份数（默认为 160）
    
    输入4（可选）：`double`，指定计算的最大半径（默认为 6 倍*单位长度*）
    
    输入5（可选）：`double`，指定最小的截断的 q（由于 pbc 的原因，过小的结果会发散，
    默认为 0.6 倍*单位长度*）
    
    输出：`IFunc1`，计算得到的 S(q)
    
    例子：`example/mpc/sf`
    [⤤](../release/script/groovy/example/mpc/sf.groovy)
    
    -----------------------------
    
- **`RDF2SF`**
    
    描述：转换 g(r) 到 S(q)，这是主要计算 S(q) 的方法。
    
    输入1：`IFunc1`，已经计算得到的 RDF
    
    输入2（可选）：`double`，原子数密度（默认会使用 MPC 存储的值）
    
    输入3（可选）：`int`，指定分划的份数（默认为 160）
    
    输入4（可选）：`double`，额外指定最大计算的 q 的位置（默认为 7.6 倍 g(r) 第一峰的距离）
    
    输入5（可选）：`double`，指定最小的截断的 q（默认为 0.5 倍 g(r) 第一峰的距离）
    
    输出：`IFunc1`，计算得到的 S(q)
    
    例子：`example/mpc/rdfsf`
    [⤤](../release/script/groovy/example/mpc/rdfsf.groovy)
    
    > 注意：在指定原子数密度后为静态方法，可以不创建 MPC 对象直接使用；
    > 对于两种不同类型的 RDF/SF，需要指定密度为 $\rho = \sqrt{\rho_A \rho_B}$
    > 才能得到正确的结果。
    >
    
    -----------------------------
    
- **`SF2RDF`**
    
    描述：转换 S(q) 到 g(r)。
    
    输入1：`IFunc1`，已经计算得到的 SF
    
    输入2（可选）：`double`，原子数密度（默认会使用 MPC 存储的值）
    
    输入3（可选）：`int`，指定分划的份数（默认为 160）
    
    输入4（可选）：`double`，额外指定最大计算的 r 的位置（默认为 7.6 倍 S(q) 第一峰的距离）
    
    输入5（可选）：`double`，指定最小的截断的 r（默认为 0.5 倍 S(q) 第一峰的距离）
    
    输出：`IFunc1`，计算得到的 S(q)
    
    例子：`example/mpc/rdfsf`
    [⤤](../release/script/groovy/example/mpc/rdfsf.groovy)
    
    > 注意：在指定原子数密度后为静态方法，可以不创建 MPC 对象直接使用；
    > 对于两种不同类型的 RDF/SF，需要指定密度为 $\rho = \sqrt{\rho_A \rho_B}$
    > 才能得到正确的结果。
    >
    

