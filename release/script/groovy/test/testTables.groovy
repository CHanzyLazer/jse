package test

import com.jtool.lmp.Thermo
import com.jtool.math.table.Tables

import static com.jtool.code.CS.*


/** 测试表格的创建读写合并 */

def table1 = Tables.from(10, 3, {i, j -> RANDOM.nextDouble()}, 'a', 'b', 'c');
def table2 = Tables.from(10, 3, {i, j -> RANDOM.nextDouble()}, 'a', 'b', 'c');
def table3 = Tables.from(10, 3, {i, j -> RANDOM.nextDouble()}, 'a', 'b', 'c');

def table4 = Tables.from(10, 3, {i, j -> RANDOM.nextDouble()}, 'd', 'e', 'f');
def table5 = Tables.from(10, 3, {i, j -> RANDOM.nextDouble()}, 'd', 'e', 'f');

def thermo = Thermo.fromTableList([table1, table2, table3, table4, table5]);

thermo.write('.temp/table.csv');
thermo.merge();
thermo.write('.temp/table-merge.csv');

