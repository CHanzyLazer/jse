package test.ffs

import jtool.code.UT
import jtool.math.vector.Vectors
import jtool.plot.Plotters
import jtool.rareevent.ForwardFluxSampling
import rareevent.RandomWalk


/**
 * 用来测试 FFS 准确性
 */

int N0 = 10000;


def biPathGen = new RandomWalk.PathGenerator(10);
def biCal = new RandomWalk.ParameterCalculator();

def lambda = 1..10;
def k = Vectors.zeros(lambda.size());

def FFS = new ForwardFluxSampling<>(biPathGen, biCal, 0, lambda, N0).setPruningProb(0.3).setPruningThreshold(2);

UT.Timer.tic();
FFS.run();
k[0] = FFS.getK0();
println("k0 = ${k[0]}");
int i = 0;
while (!FFS.finished()) {
    FFS.run();
    k[i+1] = FFS.getProb(i);
    println("prob = ${k[i]}");
    ++i;
}
UT.Timer.toc("4, k = ${FFS.getK()}, step1PointNum = ${FFS.step1PointNum()}, step1PathNum = ${FFS.step1PathNum()}, totPointNum = ${FFS.totalPointNum()},");
FFS.shutdown();


// 绘制
k = k.opt().cumprod();

def plt = Plotters.get();
plt.semilogy(lambda, k, 'RandomWalk').marker('s');
plt.show();
