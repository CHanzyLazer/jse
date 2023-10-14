package test.mpc

import com.jtool.atom.MPC
import com.jtool.code.UT
import com.jtool.lmp.Lmpdat
import com.jtool.plot.Plotters

import static com.jtool.math.MathEX.Fast.*

/** 测试计算 RDF */

// 设置线程数
nThreads = 4;

// 首先导入 Lmpdat
data = Lmpdat.read('lmp/data/data-glass');
// 获取 MPC 计算单原子数据
mpcCu = data.getTypeMPC(1, nThreads);
mpcZr = data.getTypeMPC(2, nThreads);

// 计算 RDF
UT.Timer.tic();
gr = mpcCu.calRDF_AB(mpcZr);
UT.Timer.toc("${nThreads} threads, RDF");

// 计算 SF
UT.Timer.tic();
Sq = mpcCu.calSF_AB(mpcZr);
UT.Timer.toc("${nThreads} threads, SF");

// 使用 FT 来计算
UT.Timer.tic();
grFT = MPC.SF2RDF(Sq, sqrt(mpcCu.rou()*mpcZr.rou()));
SqFT = MPC.RDF2SF(gr, sqrt(mpcCu.rou()*mpcZr.rou()));
UT.Timer.toc("FT");

// 计算完毕关闭 MPC
mpcCu.shutdown();
mpcZr.shutdown();

// 输出为 csv 文件
UT.IO.data2csv(gr, 'lmp/.temp/gr.csv');
UT.IO.data2csv(Sq, 'lmp/.temp/Sq.csv');
UT.IO.data2csv(grFT, 'lmp/.temp/grFT.csv');
UT.IO.data2csv(SqFT, 'lmp/.temp/SqFT.csv');

// 使用 Plotter 绘图
plt = Plotters.get();

plt.plot(gr, 'g(r)').color('k').lineType('-');
plt.plot(grFT, 'g(r)-FT').color('k').lineType('--');

plt.plot(Sq, 'S(q)').color('r').lineType('-');
plt.plot(SqFT, 'S(q)-FT').color('r').lineType('--');

plt.show();

