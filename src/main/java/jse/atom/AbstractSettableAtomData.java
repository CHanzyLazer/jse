package jse.atom;

import jse.code.collection.AbstractRandomAccessList;
import jse.math.vector.IVector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 可以修改的原子数据抽象类，方便子类实现接口 {@link ISettableAtomData}
 * <p>
 * 具体来说，子类最少需要实现：
 * <pre>
 *    {@link #atom(int)}: 获取指定索引的可以设置的原子引用 {@link ISettableAtom}
 *    {@link #box()}: 获取模拟盒对象
 *    {@link #atomNumber()}: 获取原子总数
 *    {@link #atomTypeNumber()}: 获取总原子种类数目
 * </pre>
 * @see IAtomData IAtomData: 通用的原子数据接口
 * @see ISettableAtomData ISettableAtomData: 可以修改的原子数据接口
 * @see SettableAtomData SettableAtomData: 一般的可以修改的原子数据实现
 * @see AbstractAtomData AbstractAtomData: 一般的原子数据抽象类
 * @author liqa
 */
public abstract class AbstractSettableAtomData extends AbstractAtomData implements ISettableAtomData {
    /**
     * 直接获取指定索引的原子，可以避免一次创建匿名列表的过程；
     * 有关系 {@code atoms()[i] == atom(i)}
     * <p>
     * 由于返回的原子是引用，因此对其的修改会同时反应到原子数据内部：
     * <pre> {@code
     * def atom = data.atom(i)
     * atom.x = 3.14
     * assert data.atom(i).x() == 3.14
     * } </pre>
     *
     * @param aIdx {@inheritDoc}
     * @return {@inheritDoc}
     * @see ISettableAtom
     * @see AbstractSettableAtom_
     * @see SettableAtomData SettableAtomData: 关于具体实现的例子
     *
     * @implSpec 一般来说需要返回一个引用可设置的原子，并且保证内部的 {@link ISettableAtom#index()},
     * {@link ISettableAtom#hasVelocity()} 等方法调用到 {@link ISettableAtom} 自身；一般通过返回
     * {@link AbstractSettableAtom_} 的匿名类来实现：
     * <pre> {@code
     * @Override ISettableAtom atom(int idx) {
     *     // get reference of the stored atom
     *     def atom = atoms.get(idx)
     *     return new AbstractSettableAtom_() {
     *         @Override double x() {return atom.x()}
     *         @Override double y() {return atom.y()}
     *         @Override double z() {return atom.z()}
     *         @Override int id_() {return atom.id()}
     *         @Override int type_() {return atom.type()}
     *         @Override double vx_() {return atom.vx()}
     *         @Override double vy_() {return atom.vy()}
     *         @Override double vz_() {return atom.vz()}
     *         /// ISettableAtom stuffs
     *         @Override void setX_(double x) {atom.setX(x)}
     *         @Override void setY_(double y) {atom.setY(y)}
     *         @Override void setZ_(double z) {atom.setZ(z)}
     *         @Override void setID_(int id) {atom.setID(id)}
     *         @Override void setType_(int type) {atom.setType(type)}
     *         @Override void setVx_(double vx) {atom.setVx(vx)}
     *         @Override void setVy_(double vy) {atom.setVy(vy)}
     *         @Override void setVz_(double vz) {atom.setVz(vz)}
     *         // make sure index() returns the correct value
     *         @Override int index() {return idx}
     *     }
     * }
     * } </pre>
     */
    @Override public abstract ISettableAtom atom(int aIdx);
    
    /**
     * {@inheritDoc}
     * @param aAtomTypeNum {@inheritDoc}
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException {@inheritDoc}
     * @see #atomTypeNumber()
     * @see IAtom#type()
     * @see SettableAtomData SettableAtomData: 关于具体实现的例子
     */
    @Override public AbstractSettableAtomData setAtomTypeNumber(int aAtomTypeNum) {throw new UnsupportedOperationException("setAtomTypeNumber");}
    /**
     *{@inheritDoc}
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException {@inheritDoc}
     * @see #hasVelocity()
     * @see #setHasVelocity()
     */
    @Override public AbstractSettableAtomData setNoVelocity() {throw new UnsupportedOperationException("setNoVelocity");}
    /**
     *{@inheritDoc}
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException {@inheritDoc}
     * @see #hasVelocity()
     * @see #setNoVelocity()
     */
    @Override public AbstractSettableAtomData setHasVelocity() {throw new UnsupportedOperationException("setHasVelocity");}
    /**
     * {@inheritDoc}
     * @param aSymbols {@inheritDoc}
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException {@inheritDoc}
     * @see #symbols()
     * @see IAtom#symbol()
     * @see SettableAtomData SettableAtomData: 关于具体实现的例子
     */
    @Override public AbstractSettableAtomData setSymbols(String... aSymbols) {throw new UnsupportedOperationException("setSymbols");}
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException {@inheritDoc}
     * @see #hasSymbol()
     * @see #setSymbols(String...)
     * @see SettableAtomData SettableAtomData: 关于具体实现的例子
     */
    @Override public AbstractSettableAtomData setNoSymbol() {throw new UnsupportedOperationException("setNoSymbol");}
    /**
     * {@inheritDoc}
     * @param aMasses {@inheritDoc}
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException {@inheritDoc}
     * @see #masses()
     * @see IAtom#mass()
     */
    @Override public AbstractSettableAtomData setMasses(double... aMasses) {throw new UnsupportedOperationException("setMasses");}
    /**
     * {@inheritDoc}
     * @see #setMasses(double...)
     * @see Collection
     */
    @Override public AbstractSettableAtomData setMasses(Collection<? extends Number> aMasses) {throw new UnsupportedOperationException("setMasses");}
    /**
     * {@inheritDoc}
     * @see #setMasses(double...)
     * @see IVector
     */
    @Override public AbstractSettableAtomData setMasses(IVector aMasses) {throw new UnsupportedOperationException("setMasses");}
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException {@inheritDoc}
     * @see #hasMass()
     * @see #setMasses(double...)
     */
    @Override public AbstractSettableAtomData setNoMass() {throw new UnsupportedOperationException("setNoMass");}
    
    /**
     * 对于 {@link ISettableAtomData} 内部的原子的一个一般原子实现，帮助实现重复的部分；
     * 主要转发了 {@link IAtom#hasVelocity()}, {@link IAtom#symbol()},
     * {@link IAtom#hasSymbol()}, {@link IAtom#mass()} 以及 {@link IAtom#hasMass()}
     * 到相对应的 {@link ISettableAtomData} 内的方法；并且对于一些边界情况进行自动处理
     * @see #atom(int)
     */
    protected abstract class AbstractSettableAtom_ extends AbstractSettableAtom {
        @Override public boolean hasVelocity() {return AbstractSettableAtomData.this.hasVelocity();}
        @Override public @Nullable String symbol() {return AbstractSettableAtomData.this.symbol(type());}
        @Override public boolean hasSymbol() {return AbstractSettableAtomData.this.hasSymbol();}
        @Override public double mass() {return AbstractSettableAtomData.this.mass(type());}
        @Override public boolean hasMass() {return AbstractSettableAtomData.this.hasMass();}
        
        @Override public int id() {int tID = id_(); return tID<=0 ? (index()+1) : tID;}
        @Override public int type() {return Math.min(type_(), atomTypeNumber());}
        /** 会复写掉内部的 hasVelocities 数据 */
        @Override public double vx() {return hasVelocity() ? vx_() : 0.0;}
        @Override public double vy() {return hasVelocity() ? vy_() : 0.0;}
        @Override public double vz() {return hasVelocity() ? vz_() : 0.0;}
        @Override public ISettableAtom setX(double aX) {setX_(aX); return this;}
        @Override public ISettableAtom setY(double aY) {setY_(aY); return this;}
        @Override public ISettableAtom setZ(double aZ) {setZ_(aZ); return this;}
        @Override public ISettableAtom setID(int aID) {setID_(aID); return this;}
        @Override public ISettableAtom setType(int aType) {
            // 对于设置种类需要特殊处理，设置种类同时需要更新内部的原子种类计数
            if (aType > atomTypeNumber()) setAtomTypeNumber(aType);
            setType_(aType);
            return this;
        }
        /** 会复写掉内部的 hasVelocities 数据 */
        @Override public ISettableAtom setVx(double aVx) {if (!hasVelocity()) throw new UnsupportedOperationException("setVx"); setVx_(aVx); return this;}
        @Override public ISettableAtom setVy(double aVy) {if (!hasVelocity()) throw new UnsupportedOperationException("setVy"); setVy_(aVy); return this;}
        @Override public ISettableAtom setVz(double aVz) {if (!hasVelocity()) throw new UnsupportedOperationException("setVz"); setVz_(aVz); return this;}
        
        /// stuff to override
        protected abstract int id_();
        protected abstract int type_();
        protected double vx_() {return 0.0;}
        protected double vy_() {return 0.0;}
        protected double vz_() {return 0.0;}
        protected abstract void setX_(double aX);
        protected abstract void setY_(double aY);
        protected abstract void setZ_(double aZ);
        protected abstract void setID_(int aID);
        protected abstract void setType_(int aType);
        protected void setVx_(double aVx) {throw new RuntimeException();}
        protected void setVy_(double aVy) {throw new RuntimeException();}
        protected void setVz_(double aVz) {throw new RuntimeException();}
        /** 注意一定要复写掉内部的 index 数据 */
        @Override public abstract int index();
    }
    
    /**
     * {@inheritDoc}
     * @param aIdx {@inheritDoc}
     * @param aAtom {@inheritDoc}
     * @see #atoms()
     * @see #atom(int)
     */
    @Override public void setAtom(int aIdx, IAtom aAtom) {
        ISettableAtom tAtom = this.atom(aIdx);
        tAtom.setXYZ(aAtom).setID(aAtom.id()).setType(aAtom.type());
        if (aAtom.hasVelocity()) tAtom.setVxyz(aAtom.vx(), aAtom.vy(), aAtom.vz());
    }
    
    /**
     * 对于 {@link ISettableAtomData}，这里会获取到一个可以设置的原子对象
     * {@link ISettableAtom}，由于返回的原子是引用，因此对其的修改会同时反应到原子数据内部。
     * <p>
     * 这里返回的列表本身同样也是一个引用对象，对列表的修改也会反应到原子数据内部，即
     * {@code this.atoms().set(idx, atom)} 和 {@code this.setAtom(idx, atom)}
     * 操作等价
     *
     * @return {@inheritDoc}
     * @see ISettableAtom
     * @see #atom(int)
     * @see #setAtom(int, IAtom)
     */
    @Override public List<? extends ISettableAtom> atoms() {
        return new AbstractRandomAccessList<ISettableAtom>() {
            @Override public ISettableAtom get(int index) {return AbstractSettableAtomData.this.atom(index);}
            @Override public ISettableAtom set(final int index, ISettableAtom element) {
                ISettableAtom oAtom = AbstractSettableAtomData.this.atom(index).copy();
                setAtom(index, element);
                return oAtom;
            }
            @Override public int size() {return atomNumber();}
        };
    }
    
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @see IAtomDataOperation
     * @see ISettableAtomDataOperation
     */
    @Override public ISettableAtomDataOperation operation() {return new AbstractSettableAtomDataOperation() {
        @Override protected ISettableAtomData thisAtomData_() {return AbstractSettableAtomData.this;}
        @Override protected ISettableAtomData newSameSettableAtomData_() {return newSame_();}
        @Override protected ISettableAtomData newSettableAtomData_(int aAtomNum) {return newZeros_(aAtomNum);}
        @Override protected ISettableAtomData newSettableAtomData_(int aAtomNum, IBox aBox) {return newZeros_(aAtomNum, aBox);}
    };}
}
