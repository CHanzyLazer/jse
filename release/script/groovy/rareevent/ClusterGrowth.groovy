package rareevent

import com.jtool.math.MathEX
import com.jtool.rareevent.IParameterCalculator
import com.jtool.rareevent.IPathGenerator


/**
 * 用来测试 FFS 准确性的实例
 */
class ClusterGrowth {
    static class Point {
        final int value;
        Point(int value) {this.value = value;}
        
        @Override String toString() {return value;}
    }
    static class PointWithTime extends Point {
        final int time;
        PointWithTime(int value, int time) {super(value); this.time = time;}
    }
    
    
    static class PathGenerator implements IPathGenerator<PointWithTime> {
        private final int pathLen;
        private final double smallProb, largeProb;
        private final def RNG = new Random();
        PathGenerator(int pathLen, double smallProb, double largeProb) {
            this.pathLen = pathLen;
            this.smallProb = smallProb;
            this.largeProb = largeProb;
        }
        
        @Override PointWithTime initPoint() {return new PointWithTime(0, 0);}
        @Override List<PointWithTime> pathFrom(PointWithTime point) {
            def path = new ArrayList<PointWithTime>(pathLen);
            path.add(point);
            for (_ in 1..<pathLen) {
                def rand = RNG.nextDouble();
                double scale = MathEX.Fast.pow(point.value+1, -0.2);
                if (rand < largeProb*scale) {
                    point = new PointWithTime(point.value+5, point.time+1);
                } else
                if (rand < (largeProb+smallProb)*scale) {
                    point = new PointWithTime(point.value+1, point.time+1);
                } else
                if (point.value > 0) {
                    point = new PointWithTime(point.value-1, point.time+1);
                }
                path.add(point);
            }
            return path;
        }
        @Override double timeOf(PointWithTime point) {return point.time;}
    }
    
    static class ParameterCalculator implements IParameterCalculator<Point> {
        @Override double lambdaOf(Point point) {
            return point.value;
        }
    }
    
}
