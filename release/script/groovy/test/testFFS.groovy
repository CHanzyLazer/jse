package test

import com.jtool.code.UT
import com.jtool.rareevent.ForwardFluxSampling
import rareevent.AsymmetryWalk
import rareevent.RandomWalk


/**
 * 用来测试 FFS 准确性
 */

int N0 = 1000;


def biPathGen = new RandomWalk.PathGenerator(10);
def biCal = new RandomWalk.ParameterCalculator();

def FFS = new ForwardFluxSampling<>(biPathGen, biCal, 0, [10], N0).setMinProb(0.0001);

UT.Timer.tic();
while (!FFS.finished()) FFS.run();
UT.Timer.toc("0, k = ${FFS.getK()}, step1PointNum = ${FFS.step1PointNum()}, totPointNum = ${FFS.totalPointNum()},");

FFS = new ForwardFluxSampling<>(biPathGen, biCal, 0, [5, 10], N0).setMinProb(0.0001);

UT.Timer.tic();
while (!FFS.finished()) FFS.run();
UT.Timer.toc("1, k = ${FFS.getK()}, step1PointNum = ${FFS.step1PointNum()}, totPointNum = ${FFS.totalPointNum()},");


FFS = new ForwardFluxSampling<>(biPathGen, biCal, 0, [2, 4, 6, 8, 10], N0).setMinProb(0.0001);

UT.Timer.tic();
while (!FFS.finished()) FFS.run();
UT.Timer.toc("3, k = ${FFS.getK()}, step1PointNum = ${FFS.step1PointNum()}, totPointNum = ${FFS.totalPointNum()},");

