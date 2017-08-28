本工程为基于高德地图Android SDK进行封装，实现了点沿线平滑移动的效果。
## 前述 ##
- [高德官网申请Key](http://lbs.amap.com/dev/#/).
- 阅读[参考手册](http://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html).
- 工程基于Android 3D地图SDK实现

## 功能描述 ##
基于3D地图SDK进行封装，实现了Marker点在线上进行平滑移动的例子。

## 效果图如下 ##

![Screenshot](https://raw.githubusercontent.com/amap-demo/android-smooth-move/master/resource/screenshot.png)
             
## 扫一扫安装 ##
![Screenshot]( https://raw.githubusercontent.com/amap-demo/android-smooth-move/master/resource/download.png)  


## 使用方法 ##
### 1:配置搭建AndroidSDK工程 ###
- [Android Studio工程搭建方法](http://lbs.amap.com/api/android-sdk/guide/creat-project/android-studio-creat-project/#add-jars).
- [通过maven库引入SDK方法](http://lbs.amap.com/api/android-sdk/guide/create-project/android-studio-create-project#gradle_sdk).

### 2:实现方法 ###

``` 
// 获取轨迹坐标点
List<LatLng> points = readLatLngs();
LatLngBounds bounds = new LatLngBounds(points.get(0), points.get(points.size() - 2));
mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

SmoothMoveMarker smoothMarker = new SmoothMoveMarker(mAMap);
// 设置滑动的图标
smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.drawable.icon_car));

LatLng drivePoint = points.get(0);
Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
points.set(pair.first, drivePoint);
List<LatLng> subList = points.subList(pair.first, points.size());

// 设置滑动的轨迹左边点
smoothMarker.setPoints(subList);
// 设置滑动的总时间
smoothMarker.setTotalDuration(40);
// 开始滑动
smoothMarker.startSmoothMove();
```
