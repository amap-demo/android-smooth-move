package apis.amap.com.move;

import android.view.animation.LinearInterpolator;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.TranslateAnimation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 按照指定的经纬度数据和时间，平滑移动
 */
public class SmoothMarker {

    private AMap mAMap;
    //默认总时长为10000ms
    private long duration = 10000L;

    //每段点的队列，第一个点为起点
    private LinkedList<LatLng> points = new LinkedList<LatLng>();
    //每段距离队列  大小为points.size() - 1
    private LinkedList<Double> eachDistance = new LinkedList<Double>();
    private double totalDistance = 0;
    private double remainDistance = 0; // 剩余距离
    private LatLng endPoint, lastEndPoint;

    //Marker位置
    private Marker marker = null;

    private BitmapDescriptor descriptor;

    //移动动画
    private TranslateAnimation animation;

    //当前动画在第几段，points中上一个点的下标
    private int index = 0;

    private boolean useDefaultDescriptor = false;

    private Thread moveThread = null;
    private Timer timer = null;
    boolean exitFlag = false;

    private SmoothMarkerMoveListener moveListener;

    public interface SmoothMarkerMoveListener {
        void move(double distance);
    }

    public SmoothMarker(AMap mAMap) {
        this.mAMap = mAMap;
    }


    public void setPoint(LatLng point) {
        if (point == null)
            return;
        List<LatLng> list = new ArrayList<LatLng>();
        list.add(point);
        setPoints(list);
    }


    /**
     * 设置平滑移动的经纬度数组
     *
     * @param points
     */
    public void setPoints(List<LatLng> points) {
        this.points.clear();
        for (LatLng latLng : points) {
            this.points.add(latLng);
        }

        if (points.size() > 1) {
            endPoint = points.get(points.size() - 1);
            lastEndPoint = points.get(points.size() - 2);
        }

        eachDistance.clear();
        totalDistance = 0;

        //计算比例
        for (int i = 0; i < points.size() - 1; i++) {
            double distance = AMapUtils.calculateLineDistance(points.get(i), points.get(i + 1));
            eachDistance.add(distance);
            totalDistance += distance;
        }

        remainDistance = totalDistance;

        LatLng markerPoint = this.points.removeFirst();

        if (marker != null) {
            marker.setPosition(markerPoint);
            //判断是否使用正确的图标
            checkMarkerIcon();
        } else {
            if (descriptor == null) {
                useDefaultDescriptor = true;
            }
            marker = mAMap.addMarker(new MarkerOptions().belowMaskLayer(true).position(markerPoint).icon(descriptor).title("").anchor(0.5f, 0.5f));
        }

    }

    /**
     * 判断是否使用的是设置的icon
     */
    private void checkMarkerIcon() {
        if (useDefaultDescriptor) {
            if (descriptor == null) {
                useDefaultDescriptor = true;
            } else {
                marker.setIcon(descriptor);
                useDefaultDescriptor = false;
            }
        }
    }

    /**
     * 设置平滑移动的总时间
     *
     * @param duration  单位: 毫秒
     */
    public void setTotalDuration(long duration) {
        this.duration = duration;
    }

    /**
     * 开始平滑移动
     */
    public void startSmoothMove() {
        if (points.size() < 1) {
            return;
        }
        index = 0;
        exitFlag = false;
        if (moveThread != null) {
            moveThread.interrupt();
        }
        moveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startRun();
            }

            private void startRun() {
                try {
                    if (points.size() < 1) {
                        setEndRotate();
                        return;
                    }

                    double dis = eachDistance.poll();
                    long time = (long) (duration * (dis / totalDistance));

                    remainDistance = remainDistance - dis;
                    if (moveListener != null) {
                        if (remainDistance < 0)
                            remainDistance = 0;
                        moveListener.move(remainDistance);
                    }

                    //计算旋转
                    LatLng curPos = marker.getPosition();
                    LatLng nextPos = points.poll();

                    //避免动画衔接处的停顿，在本次动画即将结束的时候开启下一个动画
                    if (timer != null) {
                        timer.cancel();
                    }
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //一段结束，开始下一段
                            //如果不是最后一段
                            if (points.size() > 0) {
                                index++;
                                startRun();
                            } else {
                                setEndRotate();
                            }
                        }
                    }, time);

                    float rotate = getRotate(curPos, nextPos);
                    marker.setRotateAngle(360 - rotate + mAMap.getCameraPosition().bearing);

                    animation = new TranslateAnimation(nextPos);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.setDuration(time);

                    if (exitFlag || Thread.interrupted()) {
                        marker.setAnimation(null);
                        return;
                    }

                    marker.setAnimation(animation);
                    marker.startAnimation();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        moveThread.start();
    }

    /**
     * 设置运行时间过短导致的 终点及角度问题
     */
    private void setEndRotate() {
        float rotate = getRotate(lastEndPoint, endPoint);
        marker.setRotateAngle(360 - rotate + mAMap.getCameraPosition().bearing);
        marker.setPosition(endPoint);
    }

    /**
     * 根据经纬度计算需要偏转的角度
     *
     * @param curPos
     * @param nextPos
     * @return
     */
    private float getRotate(LatLng curPos, LatLng nextPos) {
        double x1 = curPos.latitude;
        double x2 = nextPos.latitude;
        double y1 = curPos.longitude;
        double y2 = nextPos.longitude;

        float rotate = (float) (Math.atan2(y2 - y1, x2 - x1) / Math.PI * 180);
        return rotate;
    }

    /**
     * 停止平滑移动
     */
    public void stopMove() {
        exitFlag = true;

        if (marker != null) {
            marker.setAnimation(null);
        }

        if (timer != null) {
            timer.cancel();
        }

        if (moveThread != null) {
            moveThread.interrupt();
        }
        index = 0;
    }

    public Marker getMarker() {
        return marker;
    }

    public LatLng getPosition() {
        if (marker == null)
            return null;
        return marker.getPosition();
    }

    public int getIndex() {
        return index;
    }


    public void destroy() {
        stopMove();

        if (descriptor != null) {
            descriptor.recycle();
        }
        if (marker != null) {
            marker.destroy();
            marker = null;
        }

        points.clear();

        eachDistance.clear();
    }

    public void setDescriptor(BitmapDescriptor descriptor) {
        if (this.descriptor != null) {
            this.descriptor.recycle();
        }
        this.descriptor = descriptor;
        if (marker != null) {
            marker.setIcon(descriptor);
        }
    }

    public void setRotate(float rotate) {
        if (marker != null) {
            marker.setRotateAngle(360 - rotate);

        }
    }

    public void setVisible(boolean b) {
        if (marker != null) {
            marker.setVisible(b);
        }
    }

    public void setMoveListener(SmoothMarkerMoveListener moveListener) {
        this.moveListener = moveListener;
    }
}
