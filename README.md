本工程为基于高德地图Android SDK进行封装，实现了点沿线平滑移动的效果。
## 前述 ##
- [高德官网申请Key](http://lbs.amap.com/dev/#/).
- 阅读[参考手册](http://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html).
- 工程基于Android 3D地图SDK实现

## 功能描述 ##
基于3D地图SDK进行封装，实现了Marker点在线上进行平滑移动的例子。

## 效果图如下 ##

![Screenshot](https://raw.githubusercontent.com/amap-demo/android-smooth-move/master/resource/screenshot.png)
             
## 扫一扫安装##
![Screenshot]( https://raw.githubusercontent.com/amap-demo/android-smooth-move/master/resource/download.png)  


## 使用方法##
###1:配置搭建AndroidSDK工程###
- [Android Studio工程搭建方法](http://lbs.amap.com/api/android-sdk/guide/creat-project/android-studio-creat-project/#add-jars).
- [通过maven库引入SDK方法](http://lbsbbs.amap.com/forum.php?mod=viewthread&tid=18786).

###2:思路介绍###

- 计算斜率
``` java
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
```
 
- 计算移动距离
``` java
/**
 * 计算每次移动的距离
 */
private double getMoveDistance(double slope) {
    if (slope == Double.MAX_VALUE||slope==0) {
        return DISTANCE;
    }
    return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
}
```
- 计算marker角度
``` java
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
```