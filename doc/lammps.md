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




