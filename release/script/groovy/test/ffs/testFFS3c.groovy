package test.ffs

import jtool.code.UT
import jtool.math.vector.Vectors
import jtool.plot.Plotters
import jtool.rareevent.BufferedFullPathGenerator
import jtool.rareevent.ForwardFluxSampling
import rareevent.NoiseClusterGrowth


/**
 * 用来测试 FFS 准确性
 */

int N0 = 100;


def biPathGen = new NoiseClusterGrowth.PathGenerator(2, 0.00045, 0.00050, 0.50, -0.10, 1000);
def biCal = new NoiseClusterGrowth.ParameterCalculator();

//def fullPath = new BufferedFullPathGenerator<>(biPathGen, biCal);
//def pi = fullPath.fullPathInit();
//def sizes = Vectors.zeros(10000);
//def sizesReal = Vectors.zeros(10000);
//for (i in 0..<sizes.size()) {
//    def p = pi.next();
//    sizes[i] = pi.lambda();
//    sizesReal[i] = p.value;
//}
//
//plt = Plotters.get();
//plt.plot(sizes, 'lambda').color('k');
//plt.plot(sizesReal, 'real').type('..').color('r');
//plt.axis(0, sizes.size(), 0, 60);
//plt.show();

def lambda = (20..200).step(5);
def k3 = Vectors.zeros(lambda.size());
def k5 = Vectors.zeros(lambda.size());
def kRef = Vectors.zeros(lambda.size());

def FFS = new ForwardFluxSampling<>(biPathGen, biCal, 10, lambda, N0).setPruningProb(0.5).setPruningThreshold(3).setMaxPathNum(N0*1000);
UT.Timer.tic();
FFS.run();
k3[0] = FFS.getK0();
println("k0 = ${k3[0]}");
int i = 0;
while (!FFS.finished()) {
    FFS.run();
    k3[i+1] = FFS.getProb(i);
    println("prob = ${k3[i]}");
    ++i;
}
UT.Timer.toc("3, k = ${FFS.getK()}, realValue = ${FFS.pickPath().last().value},");
FFS.shutdown();

FFS = new ForwardFluxSampling<>(biPathGen, biCal, 10, lambda, N0).setPruningProb(0.5).setPruningThreshold(5).setMaxPathNum(N0*1000);
UT.Timer.tic();
FFS.run();
k5[0] = FFS.getK0();
println("k0 = ${k5[0]}");
i = 0;
while (!FFS.finished()) {
    FFS.run();
    k5[i+1] = FFS.getProb(i);
    println("prob = ${k5[i]}");
    ++i;
}
UT.Timer.toc("5, k = ${FFS.getK()}, realValue = ${FFS.pickPath().last().value},");
FFS.shutdown();

biPathGen = new NoiseClusterGrowth.PathGenerator(2, 0.00045, 0.00050, 0.50, -0.10, 5000);
FFS = new ForwardFluxSampling<>(biPathGen, biCal, 10, lambda, N0).setPruningProb(0.5).setPruningThreshold(5).setMaxPathNum(N0*1000);
UT.Timer.tic();
FFS.run();
kRef[0] = FFS.getK0();
println("k0 = ${kRef[0]}");
i = 0;
while (!FFS.finished()) {
    FFS.run();
    kRef[i+1] = FFS.getProb(i);
    println("prob = ${kRef[i]}");
    ++i;
}
UT.Timer.toc("ref, k = ${FFS.getK()}, realValue = ${FFS.pickPath().last().value},");
FFS.shutdown();


// 绘制
k3 = k3.opt().cumprod();
k5 = k5.opt().cumprod();
kRef = kRef.opt().cumprod();

def plt = Plotters.get();
plt.semilogy(lambda, k3  , 'PruningThreshold-3');
plt.semilogy(lambda, k5  , 'PruningThreshold-5');
plt.semilogy(lambda, kRef, 'PruningThreshold-ref');
plt.show();

