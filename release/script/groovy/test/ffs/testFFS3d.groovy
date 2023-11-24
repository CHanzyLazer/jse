package test.ffs

import jtool.code.UT
import jtool.jobs.StepJobManager
import jtool.math.table.Tables
import jtool.math.vector.Vectors
import jtool.plot.Plotters
import jtoolex.rareevent.BufferedFullPathGenerator
import jtoolex.rareevent.ForwardFluxSampling
import rareevent.NoiseClusterGrowth


/**
 * 统计一下快慢过程的时间差距
 */

int N0 = 100;
def biCal = new NoiseClusterGrowth.ParameterCalculator();
int threadNum = 4;

def lambda = (20..200).step(5);


def biPathGen = new NoiseClusterGrowth.PathGenerator(100, 0.000045, 0.000050, 0.60, -0.10);
def FFS = new ForwardFluxSampling<>(biPathGen, biCal, threadNum, 5, lambda, N0).setPruningProb(0.5).setPruningThreshold(5).setMaxPathNum(N0*10000);
UT.Timer.tic();
FFS.run();
def k0N = FFS.getK0();
FFS.shutdown();

biPathGen = new NoiseClusterGrowth.PathGenerator(100, 0.000045, 0.000050, 0.00, -0.10);
FFS = new ForwardFluxSampling<>(biPathGen, biCal, threadNum, 5, lambda, N0).setPruningProb(0.5).setPruningThreshold(5).setMaxPathNum(N0*10000);
UT.Timer.tic();
FFS.run();
def k0R = FFS.getK0();
FFS.shutdown();

println("k0N: $k0N, k0R: $k0R, k0N/k0R: ${k0N/k0R}");

