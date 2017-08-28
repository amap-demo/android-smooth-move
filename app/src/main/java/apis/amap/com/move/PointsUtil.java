//package apis.amap.com.move;
//
//import android.util.Pair;
//
//import com.amap.api.maps.model.LatLng;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class PointsUtil {
//
//    /**
//     * 如果在垂足在端点，直接返回端点的下标
//     *
//     * @param points
//     * @param point
//     * @return
//     */
//    public static Pair<Integer, LatLng> calShortestDistancePoint(List<LatLng> points, LatLng point) {
//        List<DPoint> dPoints = new ArrayList<DPoint>();
//        int index = 0;
//        for (LatLng latLng : points) {
//            dPoints.add(new DPoint(latLng.latitude, latLng.longitude));
//            if (latLng.equals(point)) {
//                return new Pair(index, point);
//            }
//            index++;
//        }
//        DPoint dPoint = new DPoint(point.latitude, point.longitude);
//        Pair<Integer, DPoint> pair = calShortestDistancePoint(dPoints, dPoint);
//        if (pair != null) {
//
//            return new Pair(pair.first, new LatLng(pair.second.x, pair.second.y));
//        }
//        return null;
//    }
//
//    public static Pair<Integer, DPoint> calShortestDistancePoint(List<DPoint> points, DPoint point) {
//        int size = points.size();
//        if (size < 2) {
//            return null;
//        }
//        Pair<Integer, DPoint> result = null;
//        DPoint FP = point;
//
//        DPoint FA = null;
//        DPoint FB = null;
//        double temp = 0;
//        for (int i = 0; i < points.size() - 1; i++) {
//            if (i == 0) {
//                FA = points.get(i);
//                if (FA.equals(point)) {
//                    return new Pair<Integer, DPoint>(i, point);
//                }
//            } else {
//                FA = FB;
//            }
//            FB = points.get(i + 1);
//            if (FB.equals(point)) {
//                return new Pair<Integer, DPoint>(i + 1, point);
//            }
//            // 使用FPoint
//            Pair<Double, DPoint> pair = pointToSegDist(FP.x, FP.y, FA.x, FA.y, FB.x, FB.y);
//            if (result == null) {
//                temp = pair.first;
//                result = new Pair<Integer, DPoint>(i, pair.second);
//            } else if (temp > pair.first) {
//                temp = pair.first;
//                result = new Pair<Integer, DPoint>(i, pair.second);
//            }
//        }
//        return result;
//    }
//
//
//    /**
//     * 点P到线段AB的最短距离，不是点到直线的距离<br>
//     * 点P到AB上的垂足为C<br>
//     * 如果C在AB的两侧则，最短距离为PA或PB，否则为PC<br>
//     *
//     * @param x  点P的坐标
//     * @param y
//     * @param x1 点A的坐标
//     * @param y1
//     * @param x2 点B的坐标
//     * @param y2
//     * @return
//     */
//    private static Pair<Double, DPoint> pointToSegDist(double x, double y, double x1, double y1,
//                                                       double x2, double y2) {
//        double cross = (x2 - x1) * (x - x1) + (y2 - y1) * (y - y1);
//        if (cross <= 0)
//            return new Pair<Double, DPoint>(Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1)), new DPoint(x1, y1));
//
//        double d2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
//        if (cross >= d2)
//            return new Pair<Double, DPoint>(Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2)), new DPoint(x2, y2));
//        ;
//
//        double r = cross / d2;
//        double px = x1 + (x2 - x1) * r;
//        double py = y1 + (y2 - y1) * r;
//        double dis = Math.sqrt((x - px) * (x - px) + (py - y) * (py - y));
//        Pair<Double, DPoint> pair = new Pair<Double, DPoint>(dis, new DPoint(px, py));
//        return pair;
//    }
//
//
//    public static class DPoint {
//        public double x, y;
//
//        public DPoint() {
//
//        }
//
//        public DPoint(double ax, double ay) {
//            x = ax;
//            y = ay;
//        }
//    }
//
//
//}
