package jse.plot;

import java.awt.*;

import static jse.plot.Anchors.AnchorType;
import static jse.plot.Anchors.toAnchorType;

/**
 * @author liqa
 * <p> {@link IPlotter#legend} 得到的图例对象 </p>
 * <p> 主要用于方便的设置图例的位置，大小，和字体 </p>
 */
@SuppressWarnings("UnusedReturnValue")
public interface ILegend {
    IPlotter plotter();
    
    boolean isShowing();
    ILegend hide();
    ILegend show();
    ILegend font(Font aFont);
    ILegend maxWidth(double aMax);
    ILegend maxHeight(double aMax);
    ILegend vertical();
    ILegend horizontal();
    
    default ILegend location(String aLocation) {return location(toAnchorType(aLocation));}
    default ILegend location(double aX, double aY) {return location(aX, aY, AnchorType.CENTER);}
    default ILegend location(double aX, double aY, String aAnchor) {return location(aX, aY, toAnchorType(aAnchor));}
    ILegend location(AnchorType aLocation);
    ILegend location(double aX, double aY, AnchorType aAnchor);
}
