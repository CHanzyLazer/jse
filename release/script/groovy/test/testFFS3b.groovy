package test

import com.jtool.code.UT
import com.jtool.math.vector.Vectors
import com.jtool.plot.Plotters
import com.jtool.rareevent.BufferedFullPathGenerator
import com.jtool.rareevent.ForwardFluxSampling
import rareevent.BiClusterGrowth


/**
 * 用来测试 FFS 准确性
 */

int N0 = 1000;


def biPathGen = new BiClusterGrowth.PathGenerator(10, 0.4, 0.5, 0.10);
def biCal = new BiClusterGrowth.ParameterCalculator();

//def fullPath = new BufferedFullPathGenerator<>(biPathGen, biCal);
//def pi = fullPath.fullPathInit();
//def sizes = Vectors.zeros(N0);
//sizes.assign {pi.next(); pi.lambda();}
//
//plt = Plotters.get();
//plt.plot(sizes);
//plt.show();

def FFS = new ForwardFluxSampling<>(biPathGen, biCal, 0, (5..105).step(5), N0).setPruningProb(0.5);

UT.Timer.tic();
FFS.run();
println("k0 = ${FFS.getK0()}");
int i = 0;
while (!FFS.finished()) {
    FFS.run();
    println("prob = ${FFS.getProb(i++)}");
}
UT.Timer.toc("1, k = ${FFS.getK()}, totPointNum = ${FFS.totalPointNum()}, totPathNum = ${FFS.totalPathNum()},");

FFS.shutdown();
FFS = new ForwardFluxSampling<>(biPathGen, biCal, 0, (5..105).step(5), N0*10).setPruningProb(0.5);

UT.Timer.tic();
FFS.run();
println("k0 = ${FFS.getK0()}");
i = 0;
while (!FFS.finished()) {
    FFS.run();
    println("prob = ${FFS.getProb(i++)}");
}
UT.Timer.toc("2, k = ${FFS.getK()}, totPointNum = ${FFS.totalPointNum()}, totPathNum = ${FFS.totalPathNum()},");


