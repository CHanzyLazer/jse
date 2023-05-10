package com.guan.plot;

import com.guan.math.MathEX;

import java.awt.*;
import java.awt.geom.*;


/**
 * @author liqa
 * <p> {@link java.awt.Shape} 中的一些用到的实例 </p>
 * <p> 目前用于 plot 的 Markers 来使用 </p>
 */
public class Shapes {
    /** 各种 marker 形状的实现 */
    public static class NullShape extends AbstractResizableShape {
        public NullShape(double aSize) {super(aSize);}
        @Override protected Shape getShape(double aSize) {return null;}
    }
    public static class Square extends Rectangle2D.Double implements IResizableShape {
        public Square(double aSize) {super(-aSize*0.5, -aSize*0.5, aSize, aSize);}
        @Override public double getSize() {return super.width;}
        @Override public void setSize(double aSize) {
            super.width = aSize;
            super.height = aSize;
            super.x = -aSize*0.5;
            super.y = -aSize*0.5;
        }
    }
    public static class Circle extends Ellipse2D.Double implements IResizableShape {
        public Circle(double aSize) {super(-aSize*0.5, -aSize*0.5, aSize, aSize);}
        @Override public double getSize() {return super.width;}
        @Override public void setSize(double aSize) {
            super.width = aSize;
            super.height = aSize;
            super.x = -aSize*0.5;
            super.y = -aSize*0.5;
        }
    }
    public static class Plus extends AbstractResizableShape {
        public Plus(double aSize) {super(aSize);}
        @Override protected Shape getShape(double aSize) {
            double tLen = aSize*0.5;
            Line2D hLine = new Line2D.Double(0-tLen, 0     , 0+tLen, 0     );
            Line2D vLine = new Line2D.Double(0     , 0-tLen, 0     , 0+tLen);
            GeneralPath tPlus = new GeneralPath();
            tPlus.append(hLine, false);
            tPlus.append(vLine, false);
            return tPlus;
        }
    }
    private final static double SQRT2 = MathEX.Fast.sqrt(2.0);
    private final static double SQRT2_INV = 1.0/SQRT2;
    public static class Asterisk extends AbstractResizableShape {
        public Asterisk(double aSize) {super(aSize);}
        @Override protected Shape getShape(double aSize) {
            double tLen = aSize*0.5;
            double dLen = tLen*SQRT2_INV;
            Line2D hLine  = new Line2D.Double(-tLen, 0.0  , +tLen, 0.0  );
            Line2D vLine  = new Line2D.Double(0.0  , -tLen, 0.0  , +tLen);
            Line2D dLine1 = new Line2D.Double(-dLen, -dLen, +dLen, +dLen);
            Line2D dLine2 = new Line2D.Double(-dLen, +dLen, +dLen, -dLen);
            GeneralPath tAsterisk = new GeneralPath();
            tAsterisk.append(hLine , false);
            tAsterisk.append(vLine , false);
            tAsterisk.append(dLine1, false);
            tAsterisk.append(dLine2, false);
            return tAsterisk;
        }
    }
    public static class Cross extends AbstractResizableShape {
        public Cross(double aSize) {super(aSize);}
        @Override protected Shape getShape(double aSize) {
            double tLen = aSize*0.5;
            double dLen = tLen*SQRT2_INV;
            Line2D dLine1 = new Line2D.Double(-dLen, -dLen, +dLen, +dLen);
            Line2D dLine2 = new Line2D.Double(-dLen, +dLen, +dLen, -dLen);
            GeneralPath tCross = new GeneralPath();
            tCross.append(dLine1, false);
            tCross.append(dLine2, false);
            return tCross;
        }
    }
    public static class Diamond extends AbstractResizableShape {
        public Diamond(double aSize) {super(aSize);}
        @Override protected Shape getShape(double aSize) {
            double tLen = aSize*0.5*SQRT2;
            GeneralPath tDiamond = new GeneralPath();
            tDiamond.moveTo(0.0  , +tLen);
            tDiamond.lineTo(+tLen, 0.0  );
            tDiamond.lineTo(0.0  , -tLen);
            tDiamond.lineTo(-tLen, 0.0  );
            tDiamond.closePath();
            return tDiamond;
        }
    }
    private final static double TRIANGLE_MUL_X = 0.66;
    private final static double TRIANGLE_MUL_Y = TRIANGLE_MUL_X/MathEX.Fast.sqrt(3.0);
    public static class Triangle extends AbstractResizableShape {
        public Triangle(double aSize) {super(aSize);}
        @Override protected Shape getShape(double aSize) {
            double tLenX = aSize*TRIANGLE_MUL_X;
            double tLenY = aSize*TRIANGLE_MUL_Y;
            GeneralPath tTriangle = new GeneralPath();
            tTriangle.moveTo(0.0   , -tLenY*2.0);
            tTriangle.lineTo(+tLenX, +tLenY    );
            tTriangle.lineTo(-tLenX, +tLenY    );
            tTriangle.closePath();
            return tTriangle;
        }
    }
    
    /** Marker stuffs */
    enum MarkerType {
          NULL
        , CIRCLE
        , PLUS
        , ASTERISK
        , CROSS
        , SQUARE
        , DIAMOND
        , TRIANGLE
        , ELSE
    }
    public static MarkerType toMarkerType(String aMarkerType) {
        switch (aMarkerType) {
        case "o": case "circle": case "CIRCLE":
            return MarkerType.CIRCLE;
        case "+": case "plus": case "PLUS":
            return MarkerType.PLUS;
        case "*": case "asterisk": case "ASTERISK":
            return MarkerType.ASTERISK;
        case "x": case "cross": case "CROSS":
            return MarkerType.CROSS;
        case "s": case "square": case "SQUARE":
            return MarkerType.SQUARE;
        case "d": case "diamond": case "DIAMOND":
            return MarkerType.DIAMOND;
        case "^": case "triangle": case "TRIANGLE":
            return MarkerType.TRIANGLE;
        case "none": case "null": case "NULL":
            return MarkerType.NULL;
        default:
            return DEFAULT_MARKER_TYPE;
        }
    }
    
    /** 全局常量记录默认值 */
    public final static MarkerType DEFAULT_MARKER_TYPE = MarkerType.NULL;
    public final static double DEFAULT_MARKER_SIZE = 12.0;
    public final static IResizableShape NULL_SHAPE = new NullShape(DEFAULT_MARKER_SIZE);
    public final static IResizableShape DEFAULT_MARKER_SHAPE = toShape(DEFAULT_MARKER_TYPE, DEFAULT_MARKER_SIZE);
    
    public static IResizableShape toShape(MarkerType aMarkerType, double aMarkerSize) {
        switch (aMarkerType) {
        case NULL:              return NULL_SHAPE;
        case CIRCLE:            return new Circle(aMarkerSize);
        case PLUS:              return new Plus(aMarkerSize);
        case ASTERISK:          return new Asterisk(aMarkerSize);
        case CROSS:             return new Cross(aMarkerSize);
        case SQUARE:            return new Square(aMarkerSize);
        case DIAMOND:           return new Diamond(aMarkerSize);
        case TRIANGLE:          return new Triangle(aMarkerSize);
        default:                return toShape(DEFAULT_MARKER_TYPE, aMarkerSize);
        }
    }
}
