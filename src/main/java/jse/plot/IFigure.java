package jse.plot;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author liqa
 * <p> {@link IPlotter#show()} 得到的图像的对象 </p>
 * <p> 主要用于方便的管理图像等操作 </p>
 */
public interface IFigure {
    boolean isShowing();
    void dispose();
    
    IPlotter plotter();
    
    IFigure name(String aName);
    IFigure size(int aWidth, int aHeight);
    IFigure location(int aX, int aY);
    
    IFigure insets(double aTop, double aLeft, double aBottom, double aRight);
    IFigure insetsTop(double aTop);
    IFigure insetsLeft(double aLeft);
    IFigure insetsBottom(double aBottom);
    IFigure insetsRight(double aRight);
    
    void save(@Nullable String aFilePath, int aWidth, int aHeight) throws IOException;
    void save(@Nullable String aFilePath) throws IOException;
    default void save() throws IOException {save(null);}
    byte[] encode(int aWidth, int aHeight) throws IOException;
    byte[] encode() throws IOException;
    
    static IFigure of(final IPlotter aPlotter) {
        return new IFigure() {
            @Override public boolean isShowing() {return false;}
            @Override public void dispose() {aPlotter.dispose();}
            @Override public IPlotter plotter() {return aPlotter;}
            @Override public IFigure name(String aName) {return this;}
            @Override public IFigure size(int aWidth, int aHeight) {aPlotter.size(aWidth, aHeight); return this;}
            @Override public IFigure location(int aX, int aY) {return this;}
            @Override public IFigure insets(double aTop, double aLeft, double aBottom, double aRight) {aPlotter.insets(aTop, aLeft, aBottom, aRight); return this;}
            @Override public IFigure insetsTop(double aTop) {aPlotter.insetsTop(aTop); return this;}
            @Override public IFigure insetsLeft(double aLeft) {aPlotter.insetsLeft(aLeft); return this;}
            @Override public IFigure insetsBottom(double aBottom) {aPlotter.insetsBottom(aBottom); return this;}
            @Override public IFigure insetsRight(double aRight) {aPlotter.insetsRight(aRight); return this;}
            @Override public void save(@Nullable String aFilePath, int aWidth, int aHeight) throws IOException {aPlotter.save(aFilePath, aWidth, aHeight);}
            @Override public void save(@Nullable String aFilePath) throws IOException {aPlotter.save(aFilePath);}
            @Override public byte[] encode(int aWidth, int aHeight) throws IOException {return aPlotter.encode(aWidth, aHeight);}
            @Override public byte[] encode() throws IOException {return aPlotter.encode();}
        };
    }
    
}
