package jse.opt;

import jse.math.MathEX;
import jse.math.vector.IVector;
import jse.math.vector.Vector;
import jse.math.vector.Vectors;

import static jse.math.MathEX.Code.DBL_EPSILON;

/**
 * 通用的优化器类，用于减少重复实现
 * @see IOptimizer
 * @author liqa
 */
public abstract class AbstractOptimizer implements IOptimizer {
    protected IVector mParameter = null;
    protected Vector mParameterStep = null, mGradBuf = null;
    
    private ILossFunc mLossFunc = null;
    private ILossFuncGrad mLossFuncGrad = null;
    
    protected double mC1 = Double.NaN, mC2 = Double.NaN;
    protected int mMaxIter = -1;
    protected boolean mLineSearch = false;
    
    private Vector mGrad = null;
    private boolean mGradValid = false;
    
    
    /**
     * 调用此方法来进行损失函数值计算
     * @param aRequireGrad 是否要求顺便计算梯度值，默认为 {@code false}
     * @return 得到的损失函数值
     */
    protected double eval(boolean aRequireGrad) {
        if (aRequireGrad) {
            if (mLossFuncGrad == null) throw new IllegalStateException("no loss func gradient set");
            mGradValid = true;
            return mLossFuncGrad.call(mGrad);
        }
        if (mLossFunc == null) throw new IllegalStateException("no loss func set");
        return mLossFunc.call();
    }
    /**
     * 调用此方法来进行损失函数值计算
     * @return 得到的损失函数值
     */
    protected double eval() {
        return eval(false);
    }
    /**
     * 获取当前的梯度值，如果缓存不合法则自动重新计算
     * @return 当前的梯度值
     */
    protected Vector grad() {
        if (!mGradValid) eval(true);
        return mGrad;
    }
    /**
     * 清空当前缓存的梯度值，表明此时已经不合法；
     * 之后需要梯度时则会自动重新计算
     */
    protected void invalidGrad() {
        mGradValid = false;
    }
    
    
    /**
     * {@inheritDoc}
     * @param aParameter {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public AbstractOptimizer setParameter(IVector aParameter) {
        mParameter = aParameter;
        if (aParameter != null) {
            mParameterStep = Vectors.zeros(aParameter.size());
            mGrad = Vectors.zeros(aParameter.size());
            mGradBuf = Vectors.zeros(aParameter.size());
        }
        invalidGrad();
        reset();
        return this;
    }
    /**
     * {@inheritDoc}
     * @param aLossFunc {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public AbstractOptimizer setLossFunc(ILossFunc aLossFunc) {
        mLossFunc = aLossFunc;
        return this;
    }
    /**
     * {@inheritDoc}
     * @param aLossFuncGrad {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public AbstractOptimizer setLossFuncGrad(ILossFuncGrad aLossFuncGrad) {
        mLossFuncGrad = aLossFuncGrad;
        return this;
    }
    /**
     * 设置需要使用
     * <a href="https://en.wikipedia.org/wiki/Wolfe_conditions">strong Wolfe 线搜索</a>
     * @param aC1 Armijo 线搜索中的参数 c1，默认为 {@code 0.0001}
     * @param aC2 Wolfe 线搜索中的参数 c2，默认为 {@code 0.1}
     * @param aMaxIter 限制的最大迭代次数，默认为 {@code 20}
     * @return 自身方便链式调用
     */
    public AbstractOptimizer setLineSearchStrongWolfe(double aC1, double aC2, int aMaxIter) {
        mC1 = aC1;
        mC2 = aC2;
        mMaxIter = aMaxIter;
        mLineSearch = true;
        return this;
    }
    @Override public AbstractOptimizer setLineSearch() {
        return setLineSearchStrongWolfe(0.0001, 0.1, 20);
    }
    @Override public AbstractOptimizer setNoLineSearch() {
        mLineSearch = false;
        return this;
    }
    
    /**
     * {@inheritDoc}
     * @param aMaxStep {@inheritDoc}
     * @param aPrintLog {@inheritDoc}
     */
    @Override public void run(int aMaxStep, boolean aPrintLog) {
        // 通用的优化器执行步骤，重写来实现特殊的迭代步骤
        checkSetting();
        double oLoss = Double.NaN;
        for (int step = 0; step < aMaxStep; ++step) {
            double tLoss = calStep(step);
            int tLineSearchStep = mLineSearch ? lineSearch(step, tLoss) : 0;
            if (checkBreak(step, tLoss, oLoss)) break;
            applyStep(step);
            printLog(step, tLineSearchStep, tLoss);
            oLoss = tLoss;
        }
    }
    
    /**
     * 简单测试是否设置完全
     */
    protected void checkSetting() {
        if (mParameter == null) throw new IllegalStateException("no parameter set");
    }
    /**
     * 计算参数需要的迭代长度，并将计算结果写入 {@link #mParameterStep}。
     * 会借用内部缓存的梯度从而避免重复计算
     * @param aStep 当前的迭代步数
     * @return 顺便返回得到的 loss 值
     */
    protected abstract double calStep(int aStep);
    /**
     * 应用线搜索，现在统一使用 strong Wolfe 条件的线搜索。
     * 将线搜索结果写入 {@link #mParameterStep}
     * @param aStep 当前的迭代步数
     * @return 进行线搜索的步数
     */
    protected int lineSearch(int aStep, double aLoss) {
        double tGradA0 = grad().operation().dot(mParameterStep);
        if (tGradA0 >= 0) throw new IllegalStateException("positive gradient");
        int tStep = 0;
        double tAlpha = 1.0;
        double tAlphaL = 0.0, tAlphaR = Double.NaN;
        double tLossL = aLoss, tLossR = Double.NaN, tGradL = tGradA0;
        for (; tStep < mMaxIter; ++tStep) {
            // 先简单判断中间分点是否满足 Armijo + strong Wolfe
            mParameter.operation().mplus2this(mParameterStep, tAlpha);
            double tLoss = mLossFuncGrad.call(mGradBuf);
            mParameter.operation().mplus2this(mParameterStep, -tAlpha);
            double tGradA = mGradBuf.operation().dot(mParameterStep);
            final boolean tArmijoOK = tLoss <= aLoss + mC1*tGradA0*tAlpha;
            if (tArmijoOK) {
                if (Math.abs(tGradA) <= -mC2*tGradA0) {
                    mParameterStep.multiply2this(tAlpha);
                    return tStep;
                }
            }
            // 判断中间点是可以作为左端还是右端
            if (!tArmijoOK || tGradA>0) {
                // 如果中间点不满足 Armijo 则一定为右端点
                // 如果中间点斜率为正则一定为右端点
                tAlphaR = tAlpha; tLossR = tLoss;
                tAlpha = lineSearchChoose(tAlphaL, tAlphaR, tLossL, tLossR, tGradL);
            } else
            if (tGradA < mC2*tGradA0) {
                // 如果中间点斜率过低则总是可以作为左端点
                tAlphaL = tAlpha; tLossL = tLoss; tGradL = tGradA;
                tAlpha = Double.isNaN(tAlphaR) ? tAlphaL*2.0 :
                    lineSearchChoose(tAlphaL, tAlphaR, tLossL, tLossR, tGradL);
            }
        }
        return tStep;
    }
    protected double lineSearchChoose(double aAlphaL, double aAlphaR, double aLossL, double aLossR, double aGradL) {
        // 采用二次样条插值，例外情况这里简单回退到二分
        double tAlphaGap = aAlphaR - aAlphaL;
        double tA = (aLossR - aLossL - aGradL*tAlphaGap) / (tAlphaGap*tAlphaGap);
        if (tA <= 0) return (aAlphaL+aAlphaR) * 0.5;
        double tAlpha = aAlphaL - aGradL / (2*tA);
        if (tAlpha<aAlphaL || tAlpha>aAlphaR) return (aAlphaL+aAlphaR) * 0.5;
        return tAlpha;
    }
    
    /**
     * 应用迭代步长，默认直接运算 {@code mParameter.plus2this(mParameterStep)}。
     * 重写来实现自定义的更新策略
     * @param aStep 当前的迭代步数
     */
    protected void applyStep(int aStep) {
        mParameter.plus2this(mParameterStep);
        invalidGrad();
    }
    /**
     * 打印输出，重写实现自定义的打印需求
     * @param aStep 当前的迭代步数
     * @param aLineSearchStep 当前步进行的线搜索步数
     * @param aLoss 当前的 loss 值
     */
    protected void printLog(int aStep, int aLineSearchStep, double aLoss) {
        if (aStep == 0) System.out.printf("%12s %18s %12s\n", "step", "loss", "max_step");
        double tMaxStep = 0.0;
        final int tSize = mParameterStep.size();
        for (int i = 0; i < tSize; ++i) {
            double tStep = Math.abs(mParameterStep.get(i));
            if (tStep > tMaxStep) tMaxStep = tStep;
        }
        if (aLineSearchStep > 0) {
            System.out.printf("%12d %18.12g %12.6g  (line search: %d)\n", aStep, aLoss, tMaxStep, aLineSearchStep);
        } else {
            System.out.printf("%12d %18.12g %12.6g\n", aStep, aLoss, tMaxStep);
        }
    }
    /**
     * 测试当前优化步是否可以终止，默认为优化到数值精度，即
     * {@link MathEX.Code#DBL_EPSILON}
     * @param aStep 当前的迭代步数
     * @param aLoss 当前的 loss 值
     * @param aLastLoss 上一步的 loss 值，如果是第一步则为 {@link Double#NaN}
     * @return 是否进行中断
     */
    protected boolean checkBreak(int aStep, double aLoss, double aLastLoss) {
        if (aStep==0 || Double.isNaN(aLastLoss)) return false;
        return Math.abs(aLastLoss-aLoss) < Math.abs(aLastLoss)*DBL_EPSILON;
    }
}
