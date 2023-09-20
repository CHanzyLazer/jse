package test.ffs

import com.jtool.code.UT
import com.jtool.math.vector.Vectors
import com.jtool.plot.Plotters
import com.jtool.rareevent.BufferedFullPathGenerator
import com.jtool.rareevent.ForwardFluxSampling
import rareevent.BiClusterGrowth
import rareevent.NoiseClusterGrowth


/**
 * 用来测试 FFS 准确性
 */

int N0 = 100;


def biPathGen = new NoiseClusterGrowth.PathGenerator(2, 0.00045, 0.00050, 0.50, -0.10, 5000);
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

def FFS = new ForwardFluxSampling<>(biPathGen, biCal, 10, (20..200).step(5), N0).setPruningProb(0.5).setPruningThreshold(3).setMaxPathNum(N0*1000);

UT.Timer.tic();
FFS.run();
println("k0 = ${FFS.getK0()}");
int i = 0;
while (!FFS.finished()) {
    FFS.run();
    println("prob = ${FFS.getProb(i++)}");
}
UT.Timer.toc("1, k = ${FFS.getK()}, realValue = ${FFS.pickPath().last().value},");

FFS.shutdown();

