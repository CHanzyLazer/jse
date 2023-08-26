package com.jtool.rareevent.atom;

import com.jtool.atom.IAtom;
import com.jtool.atom.IAtomData;
import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.math.MathEX;
import com.jtool.math.vector.ILogicalVector;

import java.util.ArrayList;
import java.util.List;

import static com.jtool.code.CS.R_NEAREST_MUL;


/**
 * 一种参数计算器，计算体系中的最大的固体团簇的尺寸
 * <p>
 * 处理有多种成分的合金的团簇计算，对于复杂的合金效果更好，当然会有更高的计算开销（2 ~ 3 倍）
 * @author liqa
 */
public final class MultiTypeClusterSizeCalculator extends AbstractClusterSizeCalculator {
    private final static double DEFAULT_TYPE_CAL_THRESHOLD = 0.15;
    private final static int DEFAULT_L_IN_BOOP = 6;
    private final static double DEFAULT_CONNECT_THRESHOLD = 0.83;
    private final static int DEFAULT_SOLID_THRESHOLD = 7;
    
    private double mQ6CutoffMul, mRClusterMul;
    private int mNnn;
    
    private double mTypeCalThreshold; // 某种原子种类的占比必须要超过此值才会用于计算，默认为 0.15
    private int mLInBOOP; // 计算 BOOP 所用的 l，默认为 6
    private double mConnectThreshold; // 用来判断两个原子是否是相连接的阈值，默认为 0.83
    private int mSolidThreshold; // 用来根据最近邻原子中，连接数大于或等于此值则认为是固体的阈值，默认为 7
    
    /**
     * 构造一个团簇大小计算器，使用
     * {@link MonatomicParameterCalculator#calConnectCountABOOP}
     * 来判断是否是团簇
     * @param aQ6CutoffMul 计算 Q6 所使用的截断半径倍率，默认为 1.5
     * @param aNnn 计算 Q6 所使用的最近邻数目限制，默认不做限制
     * @param aRClusterMul 判断是否两原子是团簇的距离倍率，如果只传一个参数则与 aQ6CutoffMul 相同，否则默认为 1.5
     */
    public MultiTypeClusterSizeCalculator(double aQ6CutoffMul, int aNnn, double aRClusterMul) {
        mQ6CutoffMul = aQ6CutoffMul;
        mRClusterMul = aRClusterMul;
        mNnn = aNnn;
        mTypeCalThreshold = DEFAULT_TYPE_CAL_THRESHOLD;
        mLInBOOP = DEFAULT_L_IN_BOOP;
        mConnectThreshold = DEFAULT_CONNECT_THRESHOLD;
        mSolidThreshold = DEFAULT_SOLID_THRESHOLD;
    }
    public MultiTypeClusterSizeCalculator(double aQ6CutoffMul, int aNnn) {this(aQ6CutoffMul, aNnn, R_NEAREST_MUL);}
    public MultiTypeClusterSizeCalculator(double aRNearestMul) {this(aRNearestMul, -1, aRNearestMul);}
    public MultiTypeClusterSizeCalculator() {this(R_NEAREST_MUL);}
    
    /** 将一些设置参数放在这里避免过于复杂的构造函数 */
    public MultiTypeClusterSizeCalculator setQ6CutoffMul(double aQ6CutoffMul) {mQ6CutoffMul = Math.max(0.1, aQ6CutoffMul); return this;}
    public MultiTypeClusterSizeCalculator setRClusterMul(double aRClusterMul) {mRClusterMul = Math.max(0.1, aRClusterMul); return this;}
    public MultiTypeClusterSizeCalculator setNnn(int aNnn) {mNnn = aNnn; return this;}
    public MultiTypeClusterSizeCalculator setTypeCalThreshold(double aTypeCalThreshold) {mTypeCalThreshold = MathEX.Code.toRange(0.0, 1.0, aTypeCalThreshold); return this;}
    public MultiTypeClusterSizeCalculator setLInBOOP(int aLInBOOP) {mLInBOOP = Math.max(1, aLInBOOP); return this;}
    public MultiTypeClusterSizeCalculator setConnectThreshold(double aConnectThreshold) {mConnectThreshold = MathEX.Code.toRange(0.0, 1.0, aConnectThreshold); return this;}
    public MultiTypeClusterSizeCalculator setSolidThreshold(int aSolidThreshold) {mSolidThreshold = Math.max(0, aSolidThreshold); return this;}
    
    
    @Override protected double getRCluster_(MonatomicParameterCalculator aMPC) {return aMPC.unitLen()*mRClusterMul;}
    @Override protected ILogicalVector getIsSolid_(MonatomicParameterCalculator aMPC, IAtomData aPoint) {
        // 先计算整体的
        ILogicalVector rIsSolid = aMPC.calConnectCountABOOP(mLInBOOP, aMPC.unitLen()*mQ6CutoffMul, mNnn, mConnectThreshold).greaterOrEqual(mSolidThreshold);
        // 再计算每种种类的，这里手动遍历过滤
        int tTypeNum = aPoint.atomTypeNum();
        List<List<Integer>> tTypeIndices = new ArrayList<>(tTypeNum);
        for (int i = 0; i < tTypeNum; ++i) tTypeIndices.add(new ArrayList<>());
        int tIdx = 0;
        for (IAtom tAtom : aPoint.atoms()) {
            tTypeIndices.get(tAtom.type()-1).add(tIdx);
            ++tIdx;
        }
        // 需要这种种类的原子数超过指定阈值才去计算
        int tThreshold = Math.min((int)Math.ceil(aPoint.atomNum()*mTypeCalThreshold), aPoint.atomNum());
        for (List<Integer> tIndices : tTypeIndices) if (tIndices.size() >= tThreshold) {
            try (MonatomicParameterCalculator tMPC = aPoint.operation().filterIndices(tIndices).getMonatomicParameterCalculator()) {
                ILogicalVector tIsSolid = tMPC.calConnectCountABOOP(mLInBOOP, tMPC.unitLen()*mQ6CutoffMul, mNnn, mConnectThreshold).greaterOrEqual(mSolidThreshold);
                // 使用 refSlicer 来合并不同种类的
                rIsSolid.refSlicer().get(tIndices).or2this(tIsSolid);
            }
        }
        return rIsSolid;
    }
}
