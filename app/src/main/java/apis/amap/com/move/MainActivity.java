package apis.amap.com.move;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

public class MainActivity extends Activity {

    private MapView mMapView;
    private AMap mAmap;
    private Polyline mVirtureRoad;
    private Marker mMoveMarker;

    private boolean mIsRunning=true;

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 80;
    private static final double DISTANCE = 0.0001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);

        mMapView.onCreate(savedInstanceState);
        mAmap = mMapView.getMap();
        initRoadData();
        moveLooper();
    }

    private void initRoadData() {
        double centerLatitude = 39.916049;
        double centerLontitude = 116.399792;
        double deltaAngle = Math.PI / 180 * 5;
        double radius = 0.02;
        PolylineOptions polylineOptions = new PolylineOptions();
        for (double i = 0; i < Math.PI * 2; i = i + deltaAngle) {
            float latitude = (float) (-Math.cos(i) * radius + centerLatitude);
            float longtitude = (float) (Math.sin(i) * radius + centerLontitude);
            polylineOptions.add(new LatLng(latitude, longtitude));
            if (i > Math.PI) {
                deltaAngle = Math.PI / 180 * 30;
            }
        }
        float latitude = (float) (-Math.cos(0) * radius + centerLatitude);
        float longtitude = (float) (Math.sin(0) * radius + centerLontitude);
        polylineOptions.add(new LatLng(latitude, longtitude));

        polylineOptions.width(10);
        polylineOptions.color(Color.RED);
        mVirtureRoad = mAmap.addPolyline(polylineOptions);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.setFlat(true);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.marker));
        markerOptions.position(new LatLng(latitude,longtitude));
        mMoveMarker = mAmap.addMarker(markerOptions);
        mMoveMarker.setRotateAngle((float) getAngle(0));

        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        builder.include(new LatLng(39.896049, 116.379792));
        builder.include(new LatLng(39.936049, 116.419792));
        mAmap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),10));

    }

    /**
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
        LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }



    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsRunning=false;
        mMapView.onDestroy();
    }

    /**
     * 计算每次移动的距离
     */
    private double getMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE||slope==0) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 判断是否为反序
     * */
    private boolean isReverse(LatLng startPoint, LatLng endPoint, double slope){
        if(slope==0){
            return	startPoint.longitude>endPoint.longitude;
        }
        return (startPoint.latitude > endPoint.latitude);

    }

    /**
     * 获取循环初始值大小
     * */
    private double getStart(LatLng startPoint,double slope){
        if(slope==0){
            return	startPoint.longitude;
        }
        return  startPoint.latitude;
    }

    /**
     * 获取循环结束大小
     * */
    private double getEnd(LatLng endPoint,double slope){
        if(slope==0){
            return	endPoint.longitude;
        }
        return  endPoint.latitude;
    }



    /**
     * 循环进行移动逻辑
     */
    public void moveLooper() {
        new Thread() {

            public void run() {
                while (mIsRunning) {
                    for (int i = 0; i < mVirtureRoad.getPoints().size() - 1; i++) {


                        LatLng startPoint = mVirtureRoad.getPoints().get(i);
                        LatLng endPoint = mVirtureRoad.getPoints().get(i + 1);
                        mMoveMarker
                                .setPosition(startPoint);
                        mMoveMarker.setRotateAngle((float) getAngle(startPoint,
                                endPoint));

                        double slope = getSlope(startPoint, endPoint);
                        boolean isReverse =isReverse(startPoint, endPoint, slope);
                        double moveDistance = isReverse ? getMoveDistance(slope) : -1 * getMoveDistance(slope);
                        double intercept = getInterception(slope, startPoint);
                        for(double j=getStart(startPoint, slope); (j > getEnd(endPoint, slope))==isReverse;j = j
                                - moveDistance){
                            LatLng latLng = null;
                            if(slope==0){
                                latLng = new LatLng(startPoint.latitude, j);
                            }
                            else if (slope == Double.MAX_VALUE) {
                                latLng = new LatLng(j, startPoint.longitude);
                            }

                            else {

                                latLng = new LatLng(j, (j - intercept) / slope);
                            }
                            mMoveMarker.setPosition(latLng);
                            try {
                                Thread.sleep(TIME_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }

        }.start();
    }
}
