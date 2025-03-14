package jse.atom;

import jse.parallel.IAutoShutdown;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 通用的势函数接口
 *
 * @author liqa
 */
public interface IPotential extends IHasSymbol, IAutoShutdown {
    
    /** @return {@inheritDoc} */
    @Override default int atomTypeNumber() {return 1;}
    /** @return {@inheritDoc}；如果存在则会自动根据元素符号重新映射种类 */
    @Override default boolean hasSymbol() {return false;}
    /**
     * {@inheritDoc}
     * @param aType {@inheritDoc}
     * @return {@inheritDoc}；如果存在则会自动根据元素符号重新映射种类
     */
    @Override default @Nullable String symbol(int aType) {return null;}
    /** @return {@inheritDoc}；如果存在则会自动根据元素符号重新映射种类 */
    @Override default @Nullable List<@Nullable String> symbols() {return IHasSymbol.super.symbols();}
    
    
    
    @Override default void shutdown() {/**/}
}
