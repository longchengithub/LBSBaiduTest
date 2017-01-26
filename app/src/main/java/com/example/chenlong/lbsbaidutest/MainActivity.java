package com.example.chenlong.lbsbaidutest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    public LocationClient mLocationClient;  //百度的location类
    private TextView position;
    private MapView mapView;
    private BaiduMap baiduMap;

    private TextInputLayout latitude;
    private TextInputLayout longitude;
    private Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //在设置contentView之前 必须做这个操作 否则报错 没有为什么
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        position = (TextView) findViewById(R.id.position);
        mapView = (MapView) findViewById(R.id.baiduMap);    //找到地图控件
        latitude = (TextInputLayout) findViewById(R.id.latitude);
        longitude = (TextInputLayout) findViewById(R.id.longitude);
        search = (Button) findViewById(R.id.search);

        baiduMap = mapView.getMap();    //拿到地图具体操作对象

        baiduMap.setMyLocationEnabled(true);    //在地图上显示自己的坐标的点

        baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);  //卫星地图

        baiduMap.setTrafficEnabled(true); //开启交通图

        baiduMap.setBaiduHeatMapEnabled(true); //开启热力图

        //设置地图的缩放级别 支持3到19之间的数字 小数点也可以
        MapStatusUpdate update = MapStatusUpdateFactory.zoomTo(18);
        baiduMap.animateMapStatus(update);

        checkPermission();

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        search.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String jingdu = latitude.getEditText().getText().toString();
                String weidu = longitude.getEditText().getText().toString();

                LatLng point = new LatLng(Float.parseFloat(jingdu), Float.parseFloat(weidu));
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.mipmap.ic_launcher);

                //在地图上添加标记 new 一个子类 有mark arc dot等类型
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .draggable(true);
                baiduMap.addOverlay(option);

                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(point);
                baiduMap.animateMapStatus(update);
            }
        });
    }

    /**
     * 初始化时 运行时权限判断
     */
    private void checkPermission()
    {
        //存放权限的集合
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        //有权限没有拿到
        if (!permissionList.isEmpty())
        {
            //拆分集合成数组
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else
        {
            //去拿坐标
            requestLocation();
        }
    }

    /**
     * 有效的保证资源回收
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 有效的保证资源回收
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 使用api去请求定位
     */
    private void requestLocation()
    {
        //不加上这句初始化的代码 只会定位一次
        initLocation();
        //开启定位  结果会回调掉监听的回调
        mLocationClient.start();
    }

    /**
     * 有效的保证资源回收
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //记得在生命周期关掉定位服务
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        baiduMap.setTrafficEnabled(false);
        baiduMap.setBaiduHeatMapEnabled(false);
    }

    /**
     * 初始化定位的一些参数
     */
    private void initLocation()
    {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);       //初始化并设置了更新的间隔 5秒
        //指定GPS定位
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        //设置将经纬度坐标转换成地理名称
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * 请求权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1:
                if (grantResults.length > 0)
                {
                    for (int grantResult : grantResults)
                    {
                        if (grantResult != PackageManager.PERMISSION_GRANTED)
                        {
                            Toast.makeText(this, "亲!定位需要权限啊", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else
                {
                    Toast.makeText(this, "未知错误", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 定位的回调
     */
    private class MyLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation bdLocation)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("纬度:").append(bdLocation.getLatitude()).append("  ,");
            sb.append("经度:").append(bdLocation.getLongitude()).append("  ,");
            sb.append("国家:").append(bdLocation.getCountry()).append("  ,");
            sb.append("省:").append(bdLocation.getProvince()).append("  ,");
            sb.append("城市:").append(bdLocation.getCity()).append("  ,");
            sb.append("区:").append(bdLocation.getDistrict()).append("  ,");
            sb.append("街道:").append(bdLocation.getStreet()).append("  ,");

            sb.append("定位方式:");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation)
            {
                sb.append("GPS");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation)
            {
                sb.append("网络");
            }

            position.setText(sb.toString());
            navigationTo(bdLocation);
        }
    }

    private boolean isFirstLocate = true; //第一次打开默认显示北京市 所以是true

    /**
     * 根据坐标移动到指定的地图位置
     * LatLng 就是个保存经纬度的 二维数组
     *
     * @param location
     */
    private void navigationTo(BDLocation location)
    {
        if (isFirstLocate)
        {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }

        //将经纬度填充到显示自己坐标的类中
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData build = locationBuilder.build();
        baiduMap.setMyLocationData(build);
    }
}
