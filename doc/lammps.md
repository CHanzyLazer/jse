- [Lammps 相关](lammps.md)
    - [data 文件读写](#data-文件读写)
    - [dump 文件读写](#dump-文件读写)
    - [原子数据类型转换](#原子数据类型转换)
    - [原子数据修改](#原子数据修改)
    - [log 文件读写]()
    - [输入文件管理]()
    - [通用 lammps 运行器]()
    - [原生运行 lammps]()
- [**⟶ 目录**](contents.md)

# Lammps 相关

几乎所有 lammps 相关的功能都位于 `jse.lmp` 包中，
涉及读写 lammps 输出的 data 和 dump 文件，
读取 lammps 输出的 log 文件，
以及管理 lammps 的输入文件并以此来运行 lammps。


## data 文件读写

jse 中使用 [`jse.lmp.Lmpdat`](../src/main/java/jse/lmp/Lmpdat.java) /
[`jse.lmp.Data`](../src/main/java/jse/lmp/Data.java)
来实现 lammps 的 data 文件读写：

- 输入脚本（`jse example/lmp/data`
  [⤤](../release/script/groovy/example/lmp/data.groovy)）：
    
    ```groovy
    import jse.lmp.Data
    
    def data = Data.read('lmp/data/CuFCC108.lmpdat')
    
    println('atom number: ' + data.natoms())
    println('masses: ' + data.masses())
    println('atom at 10: ' + data.pickAtom(10))
    
    data.write('.temp/example/lmp/dataFCC')
    ```
    
- 输出：
    
    ```
    atom number: 108
    masses: 2-length Vector:
       63.55   91.22
    atom at 10: {id: 11, type: 1, xyz: (9.025, 0.000, 1.805)}
    ```

> 可以使用 `jse.lmp.Lmpdat` 替换 `jse.lmp.Data`，
> 两者使用方法完全相同；使用 `Lmpdat` 可以指定为 lammps 的数据，
> 用于区分其他的 `Data` 类。
> 

-----------------------------

对于 lammps 的原子数据，其中的模拟盒还会存在一个下边界（`xlo`, `ylo`, `zlo`），
但是这对于计算是不必要的，因此这里通过 `pickAtom(index)` 获取到的原子坐标，
以及通过 `box()` 获取到的模拟盒大小都是经过平移的（将 `xlo`, `ylo`, `zlo` 设为 0 ），
因此可能会和文件中的数据有所不同。

如果希望获得到原始的未经平移的数据，可以通过 `lmpBox()` 获得 lammps 格式的模拟盒信息，
以及通过 `positions()` 来直接获得存储原子位置的矩阵：

```groovy
println('box: ' + data.box())
println('lmpBox: ' + data.lmpBox())
```


## dump 文件读写

jse 中使用 [`jse.lmp.Lammpstrj`](../src/main/java/jse/lmp/Lammpstrj.java) /
[`jse.lmp.Dump`](../src/main/java/jse/lmp/Dump.java)
来实现 lammps 的 dump 文件读写：

- 输入脚本（`jse example/lmp/dump`
  [⤤](../release/script/groovy/example/lmp/dump.groovy)）：
    
    ```groovy
    import jse.lmp.Dump
    
    def dump = Dump.read('lmp/dump/CuFCC108.lammpstrj')
    
    println('frame number: ' + dump.size())
    def frame = dump[4]
    println('atom number: ' + frame.natoms())
    println('time step: ' + frame.timeStep())
    println('atom at 10: ' + frame.pickAtom(10))
    
    dump.write('.temp/example/lmp/dumpFCC')
    ```
  
- 输出：
    
    ```
    frame number: 21
    atom number: 108
    time step: 4000
    atom at 10: {id: 50, type: 1, xyz: (2.623, 4.457, 2.003), vxvyvz: (-2.409, 0.3910, -0.5409)}
    ```

> 可以使用 `jse.lmp.Lammpstrj` 替换 `jse.lmp.Dump`，
> 两者使用方法完全相同；使用 `Lammpstrj` 可以指定为 lammps 的数据，
> 用于区分其他的 `Dump` 类。
> 

-----------------------------

有时会使用只有一帧原子数据的 dump 文件，此时使用
`dump[0]` 或者 `dump.first()` 来专门获取第一帧数据会有些繁琐，
jse 支持直接使用 `dump` 来获取数据（`Dump` 本身也继承了 `IAtomData`），
此时获取的数据默认就是第一帧的原子数据：

- 原始：
    ```groovy
    def first = dump[0]
    println('atom number: ' + first.natoms())
    println('time step: ' + first.timeStep())
    println('atom at 10: ' + first.pickAtom(10))
    ```
    
- 简化：
    
    ```groovy
    println('atom number: ' + dump.natoms())
    println('time step: ' + dump.timeStep())
    println('atom at 10: ' + dump.pickAtom(10))
    ```


## 原子数据类型转换

`jse.lmp.Lmpdat`/`jse.lmp.Data` 类以及
`jse.lmp.Lammpstrj`/`jse.lmp.Dump` 类都继承了
jse 中通用的原子数据接口
[`jse.atom.IAtomData`](../src/main/java/jse/atom/IAtomData.java)，
而 `Lmpdat` 和 `Lammpstrj` 都实现了通过 `IAtomData`
来初始化的方法 `of`，从而可以以此来实现相互转换：

- 输入脚本（`jse example/lmp/transform`
  [⤤](../release/script/groovy/example/lmp/transform.groovy)）：
    
    ```groovy
    import jse.lmp.Data
    import jse.lmp.Dump
    import static jse.code.CS.MASS
    
    def dump = Dump.read('lmp/dump/CuFCC108.lammpstrj')
    
    // 通过 `of` 来转换，转为 data 时可以这样来指定每个原子种类的质量
    def data = Data.of(dump[4], [MASS.Cu, MASS.Zr])
    println('atom number: ' + data.natoms())
    println('masses: ' + data.masses())
    
    data.write('.temp/example/lmp/dump2data')
    
    // 同样通过 `of` 来将单个或多个 data 转为 dump
    def dump1 = Dump.of(data)
    println('frame number of dump1: ' + dump1.size())
    def dump4 = Dump.of([data, dump[3], dump[0], dump.last()])
    println('frame number of dump4: ' + dump4.size())
    
    dump4.write('.temp/example/lmp/data2dump')
    ```
    
- 输出：
    
    ```
    atom number: 108
    masses: 2-length Vector:
       63.55   91.22
    frame number of dump1: 1
    frame number of dump4: 4
    ```

> `of` 方法直接接收 `IAtomData`，因此任何类型的原子数据（如 POSCAR，XDATCAR），
> 只要继承了 `IAtomData` 就可以通过此方法来转换。
> 


## 原子数据修改

`jse.lmp.Lmpdat`/`jse.lmp.Data` 类以及
`jse.lmp.Lammpstrj`/`jse.lmp.Dump` 类都继承了
jse 中通用的可修改的原子数据接口
[`jse.atom.ISettableAtomData`](../src/main/java/jse/atom/ISettableAtomData.java)，
从而可以使用 `ISettableAtomData` 提供的接口来修改原子数据：

- 输入脚本（`jse example/lmp/setatom`
  [⤤](../release/script/groovy/example/lmp/setatom.groovy)）：
    
    ```groovy
    import jse.lmp.Data
    
    def data = Data.read('lmp/data/CuFCC108.lmpdat')
    
    def atom10 = data.pickAtom(10)
    println('origin atom10: ' + atom10)
    atom10.type = 2
    atom10.x = 3.14
    println('new atom at 10: ' + data.pickAtom(10))
    
    for (atom in data.asList()) atom.y += 10
    println('new atom10: ' + atom10)
    
    data.write('.temp/example/lmp/dataSet')
    ```
    
- 输出：
    
    ```
    origin atom10: {id: 11, type: 1, xyz: (9.025, 0.000, 1.805)}
    new atom at 10: {id: 11, type: 2, xyz: (3.140, 0.000, 1.805)}
    new atom10: {id: 11, type: 2, xyz: (3.140, 10.00, 1.805)}
    ```

> - **注意1:**
>   
>   这里通过 `pickAtom(index)` 获取到的原子实际为 `data` 中原子数据的引用，
>   对任意一方的修改都会同时反应在对方（实际只有一份原子数据）。
>   
> - **注意2:**
>   
>   遍历所有原子需要先调用 `asList()` 方法将原子数据转为 `List`。
>   
> - **注意3:**
>   
>   这里为了让代码简洁，使用了类似 `atom10.type = 2` 以及 `atom.y += 10`
>   之类的写法，实际 groovy 运行时会分别调用 `atom10.setType(2)` 以及
>   `atom.setY(atom.getY() + 10)`（groovy 会自动将 getter/setter
>   转换成成员变量的形式）。
>   
>   但是注意到 jse 内部并不是使用此标准（也就是所谓的 Java Bean 命名约定，
>   具体可以参考 [jse java 部分的代码规范](codeJava.md#gettersetter)），
>   因此 jse 不一定提供了完整的 getter/setter，因此许多属性依旧通过调用方法
>   （如 `data.natoms()`）来获取而不是调用成员变量（如 `data.natoms`）
>   的方式来获取。
>   
>   一般来说，对于简单的属性，jse 中也会提供一套 getter/setter
>   来方便 groovy 中的使用。
> 


