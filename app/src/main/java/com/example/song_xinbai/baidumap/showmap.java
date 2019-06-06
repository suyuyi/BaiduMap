package com.example.song_xinbai.baidumap;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import static com.example.song_xinbai.baidumap.globaldata.*;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class showmap extends Activity {
//    final String HOST="45.76.196.92";
//    final int port=8088;
    final String HOST=getHOST();
    final int port=getPORT();
    private MapView mMapView = null;
    public static int userID=-1;
    public double lon,lat;
    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    public BitmapDescriptor mbitmap=BitmapDescriptorFactory.fromResource(R.drawable.example);
    public boolean isFirstLoc=true,send_or_text=false;
    public LinearLayout send_menu;
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg) {
            int arg1 = msg.arg1;//success or failed
            int arg2 = msg.arg2;//UserID or fail type
            int what = msg.what;
            if(what==print_info)
            {
                String result =(String) msg.obj;
                Toast.makeText(showmap.this,result,Toast.LENGTH_SHORT).show();
            }
            else if(what==scan_request){
                String result =(String) msg.obj;
                if(arg1==0)//没有漂流瓶
                {
                    Toast.makeText(showmap.this,"附近没有漂流瓶，请移步至其他地区",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(showmap.this,"附近共有"+String.valueOf(arg1)+"个漂流瓶，正在加载，请稍候",Toast.LENGTH_SHORT).show();
                    String[] tmp=result.split("/");
                    mBaiduMap.clear();
                    LatLng latLng = null;
                    OverlayOptions overlayOptions = null;
                    Marker marker = null;
                    for(int i=0;i<tmp.length;++i)
                    {//UserID,lat,lon,title,Content
                        //double lat,double lon,int userid,String title,String content
                        String[] data=tmp[i].split("~");//!attention!
                        locwithcontent mlocon=new locwithcontent(Double.parseDouble(data[1]),Double.parseDouble(data[2]),Integer.parseInt(data[0]),
                                data[3],data[4]);
                        latLng = new LatLng(mlocon.la, mlocon.ln);
                        // 图标
                        overlayOptions = new MarkerOptions().position(latLng)
                                .icon(mbitmap).zIndex(5);
                        marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("locwithcontent", mlocon);
                        marker.setExtraInfo(bundle);
                    }
                    latLng=new LatLng(lat,lon);
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
                    mBaiduMap.setMapStatus(u);
                }

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,false,mbitmap));
        Button send_text=(Button)findViewById(R.id.send_text);
        Button info=(Button)findViewById(R.id.info);
        Button scan=(Button)findViewById(R.id.scan);
        initLocation();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(final Marker marker)
            {
                //获得marker中的数据
                locwithcontent mylocon = (locwithcontent) marker.getExtraInfo().get("locwithcontent");
                Intent intent = new Intent();
                intent.setClass(showmap.this,showcontentlist.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("locwithcontent",mylocon);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });
        send_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loc myloc=new loc(lat,lon,userID);
                Intent intent = new Intent();
                intent.setClass(showmap.this,sendmessage.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("location",myloc);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loc myloc=new loc(lat,lon,userID);
                Intent intent = new Intent();
                intent.setClass(showmap.this,infolist.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("location",myloc);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lat==4.9E-324||lon==4.9E-324)
                {
                    Message message = Message.obtain(handler);
                    message.what = print_info;
                    message.arg1 = 0;
                    message.arg2 = 0;
                    message.obj = "GPS定位失败，请重启GPS，此次定位将使用之前的历史位置";
                    message.sendToTarget();
                }
                mBaiduMap.clear();
                CircleOptions circleOptions = new CircleOptions();
                // 2.设置数据 以世界之窗为圆心，1000米为半径绘制
                circleOptions.center(new LatLng(lat, lon))//中心
                        .radius(10)  //半径
                        .fillColor(0x60FF0000)//填充圆的颜色
                        .stroke(new Stroke(10, 0x600FF000));  //边框的宽度和颜色
                //把绘制的圆添加到百度地图上去
                mBaiduMap.addOverlay(circleOptions);
                LatLng ll = new LatLng(lat, lon);
                MapStatus.Builder builder = new MapStatus.Builder();
                //设置缩放中心点；缩放比例；
                builder.target(ll).zoom(18.0f);
                new Thread() {
                    public void run() {
                        try {
                            //创建客户端对象
                            Socket socket = new Socket(HOST, port);
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            JSONObject inf = new JSONObject();
                            inf.put("request",String.valueOf(scan_request));
                            inf.put("lon",lon);
                            inf.put("lat",lat);
                            out.write(String.valueOf(String.valueOf(inf).length()));
                            out.write(String.valueOf(inf));
                            out.flush();
                            Message message = Message.obtain(handler);
                            message.what = scan_request;
                            String res = in.readLine();
                            String[] tmp=res.split("/");
                            message.arg1 = tmp.length;
                            message.obj = res;
                            message.sendToTarget();
                            socket.close();
                        } catch (Exception e) {
                            Message message = Message.obtain(handler);
                            message.what = print_info;
                            message.arg1 = 0;
                            message.arg2 = 0;
                            message.obj = "服务器连接失败";
                            message.sendToTarget();
                            e.printStackTrace();
                        }
                    }
                    //启动线程
                }.start();
                //给地图设置状态
//                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//                loc myloc=new loc(lat,lon,userID);
//                Intent intent = new Intent();
//                intent.setClass(showmap.this,scanlist.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("location",myloc);
//                intent.putExtras(bundle);
//                startActivity(intent);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            if(location.getLatitude()!=4.9E-324&&location.getLongitude()!=4.9E-324)
            {
                lat = location.getLatitude();    //获取纬度信息
                lon = location.getLongitude();    //获取经度信息
            }
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            String coorType = location.getCoorType();
            int errorCode = location.getLocType();
//            System.out.print("lat:"+lat+'\n');
//            System.out.print("lon:"+lon+'\n');
//            System.out.print("err:"+errorCode+'\n');
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                //设置缩放中心点；缩放比例；
                builder.target(ll).zoom(18.0f);
                //给地图设置状态
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                CircleOptions circleOptions = new CircleOptions();
                // 2.设置数据 以世界之窗为圆心，1000米为半径绘制
                circleOptions.center(new LatLng(lat, lon))//中心
                        .radius(3)  //半径
                        .fillColor(0x60FF0000)//填充圆的颜色
                        .stroke(new Stroke(10, 0x600FF000));  //边框的宽度和颜色
                mBaiduMap.addOverlay(circleOptions);
            }
        }
    }
    public void initLocation()
    {
        mLocationClient = new LocationClient(this);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(3000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5*60*1000);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }
}
