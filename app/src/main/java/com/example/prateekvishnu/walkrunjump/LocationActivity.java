package com.example.prateekvishnu.walkrunjump;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.prateekvishnu.walkrunjump.util.BitmapUtil;
import com.example.prateekvishnu.walkrunjump.util.DateUtil;
import com.example.prateekvishnu.walkrunjump.util.FileUtil;
import com.example.prateekvishnu.walkrunjump.util.MapUtils;
import com.example.prateekvishnu.walkrunjump.util.ToastUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    private boolean flag;
    public LocationClient mLocationClient = null;
    private LocationClientOption option;
    private double lastLa = -1;
    private double lastLo = -1;
    private MapView mBmapView;
    private TextView mTvTime;
    private TextView mTvLen;
    private TextView mTvSpeed;
    private Button mBtStart;
    private Button mBtStop;
    private long lastTime;


    private MapView mMapView;
    private int s = 0;
    private double l = 0;
    BaiduMap mBaiduMap;
    List<LatLng> points = new ArrayList<LatLng>();
    boolean startFlag = false;
    boolean endFlag = false;
    boolean start = false;
    BDLocation startLocation;
    DecimalFormat df = new DecimalFormat("#0.00");
    private String basePath = Environment.getExternalStorageDirectory() + "/Android/Data/CHALLENGE1";
    private String speed = "";
    private String road = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_location);

        initPermission();
        // getGPSLocation();

        mLocationClient = new LocationClient(getApplicationContext());

        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                float radius = location.getRadius();
                String addr = location.getAddrStr();
                String country = location.getCountry();
                String province = location.getProvince();
                String city = location.getCity();
                //好的
                String district = location.getDistrict();
                String street = location.getStreet();
                String coorType = location.getCoorType();

                System.out.println(city);
                int errorCode = location.getLocType();
                s++;

                if (startLocation == null) {
                    points.add(new LatLng(latitude, longitude));
                    startLocation = location;
                }

                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())

                        .direction(0).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();

                mBaiduMap.setMyLocationData(locData);
                if (startFlag) {
                    startFlag = false;
                    LatLng point = new LatLng(latitude, longitude);
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_st);
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap);
                    mBaiduMap.addOverlay(option);
                }
                if (endFlag) {
                    endFlag = false;
                    LatLng point = new LatLng(latitude, longitude);
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_en);
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap);
                    mBaiduMap.addOverlay(option);
                }

                if (lastLo != -1) {
                    double v = MapUtils.GetDistance(latitude, longitude, lastLa, lastLo);

                    if (v < 100) {
                        if (start) {
                            l += v;
                            lastLa = latitude;
                            lastLo = longitude;
                            mTvTime.setText(DateUtil.secToTime((int) ((System.currentTimeMillis() - lastTime) / 1000)));  //  mResult.setText("移动了：" + l + "米\n 平均速度：" + (l / s) + " m/s");
                            speed = df.format(l * 3.6 / s);
                            road = df.format(l / 1000);
                            mTvSpeed.setText(speed + " km/h");
                            mTvLen.setText(road);

                            points.add(new LatLng(latitude, longitude));

                            if (points.size() > 2) {

                                OverlayOptions ooPolyline = new PolylineOptions().width(10)
                                        .color(0xAAFF0000).points(points);
                                mBaiduMap.addOverlay(ooPolyline);
                            }
                        }

                    } else {

                    }
                    scale(location, 19);
                } else {
                    scale(location, 19);
                    lastLa = latitude;
                    lastLo = longitude;
                }
            }
        });
        initLocation();

        initView();

        mLocationClient.start();
    }

    void scale(BDLocation location, int max) {

        LatLng llCentre = new LatLng(location.getLatitude(), location.getLongitude());
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(llCentre)
                .zoom(max);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory
                .newMapStatus(builder.build()));
    }

    @SuppressLint("NewApi")
    private void initView() {
        mBmapView = (MapView) findViewById(R.id.bmapView);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvLen = (TextView) findViewById(R.id.tv_len);
        mTvSpeed = (TextView) findViewById(R.id.tv_speed);
        mBtStart = (Button) findViewById(R.id.bt_start);
        mBtStop = (Button) findViewById(R.id.bt_stop);
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_gcoding);
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromBitmap(BitmapUtil.setImgSize(bitmap, 30, 30));

        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker,
                0x55FFFF88, 0xAA00FF00));

        mBaiduMap.setMyLocationEnabled(true);
        mBtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!start) {
                    s = 0;
                    l = 0;
                    lastLa = -1;
                    lastLo = -1;
                    startFlag = true;
                    start = true;
                    startLocation = null;
                    lastTime = System.currentTimeMillis();
                    mBaiduMap.clear();
                    points.clear();
                } else {
                    ToastUtil.toast(getApplicationContext(), "Being carried out!");
                }

            }
        });
        mBtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start) {
                    String content = android.os.Build.MODEL + "," + road + "," + speed + "\n";
                    FileUtil.write(basePath, android.os.Build.MODEL+"activity_record.txt", content, true);
                    endFlag = true;
                    start = false;
                } else {
                    ToastUtil.toast(getApplicationContext(), "It hasn't started yet!");
                }

                //     mLocationClient.stop();
            }
        });
    }

    private void initLocation() {
        option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        option.setOpenGps(true);


        option.setScanSpan(1000);


        option.setLocationNotify(true);


        option.setIgnoreKillProcess(true);


        option.SetIgnoreCacheException(true);


        option.setWifiCacheTimeOut(5 * 60 * 1000);


        option.setEnableSimulateGps(true);


        mLocationClient.setLocOption(option);


    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                flag = true;
            }
        } else {
            flag = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            flag = grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMapView.onPause();
    }

}
