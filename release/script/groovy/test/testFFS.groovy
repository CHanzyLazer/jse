package test

import com.jtool.code.UT
import com.jtool.rareevent.ForwardFluxSampling
import rareevent.BiEvent


/**
 * 用来测试 FFS 准确性
 */


def biPathGen = new BiEvent.PathGenerator(10);
def biCal = new BiEvent.ParameterCalculator();

def FFS = new ForwardFluxSampling(biPathGen, biCal, 0, [20], 1000, 2000);

UT.Timer.tic();
FFS.run();
UT.Timer.toc("1, k = ${FFS.getK()},");

FFS = new ForwardFluxSampling(biPathGen, biCal, 0, [10, 20], 1000, 2000);

UT.Timer.tic();
FFS.run();
UT.Timer.toc("2, k = ${FFS.getK()},");


FFS = new ForwardFluxSampling(biPathGen, biCal, 0, [5, 10, 15, 20], 1000, 2000);

UT.Timer.tic();
FFS.run();
UT.Timer.toc("4, k = ${FFS.getK()},");


FFS = new ForwardFluxSampling(biPathGen, biCal, 0, 0..20, 1000, 2000);

UT.Timer.tic();
FFS.run();
UT.Timer.toc("20, k = ${FFS.getK()},");

