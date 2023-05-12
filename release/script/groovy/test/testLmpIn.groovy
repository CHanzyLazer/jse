package test

import com.guan.code.UT
import com.guan.lmp.LmpIn


/** 测试 lammps 输入文件相关 */
// 创建输出目录
UT.IO.mkdir('groovy.lmp/.temp');

// 创建 in 文件
lmpIn = LmpIn.INIT_MELT_NPT_Cu();

// 直接写入
lmpIn.write('groovy.lmp/.temp/in-init-1');

// 修改参数，groovy 中支持直接使用这种写法
lmpIn.vT = 3000;
lmpIn.vSeed = 123456;

// 写入修改参数的结果
lmpIn.write('groovy.lmp/.temp/in-init-2');
