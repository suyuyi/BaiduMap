package com.example.song_xinbai.baidumap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Application;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends Activity {
    private MapView mMapView = null;
    final String HOST="45.76.196.92";
    final int port=8088;
    static int success=1,fail=0;
    static int print_info=0,login_request=1,regi_request=2;
//    public LocationClient mLocationClient = null;
//    private MyLocationListener myListener = new MyLocationListener();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {  //这个是发送过来的消息
            // 处理从子线程发送过来的消息
            int arg1 = msg.arg1;//success or failed
            int arg2 = msg.arg2;//UserID or fail type
            int what = msg.what;
            String result =(String) msg.obj;
            if(what==print_info)
                Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
            else if(what==login_request)
            {
                if(arg1==success)
                {
                    Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,showmap.class);
                    showmap.userID=arg2;
                    startActivity(intent);
                }
                else if(arg1==fail)
                {
                    if(arg2==-1)
                        Toast.makeText(MainActivity.this,"用户名不存在"+result,Toast.LENGTH_SHORT).show();
                    else if(arg2==-2)
                        Toast.makeText(MainActivity.this,"密码错误"+result,Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(MainActivity.this,"读取失败",Toast.LENGTH_SHORT).show();
            }
            else if(what==regi_request)
            {
                if(arg1==success)
                    Toast.makeText(MainActivity.this,"注册成功"+String.valueOf(arg2),Toast.LENGTH_SHORT).show();
                else if(arg1==fail)
                    Toast.makeText(MainActivity.this,"用户名已存在"+result,Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this,"读取失败",Toast.LENGTH_SHORT).show();
            }
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_reg);
        final EditText name=(EditText)findViewById(R.id.name);
        final EditText password=(EditText)findViewById(R.id.password);
        Button login=(Button) findViewById(R.id.loin);
        Button reg=(Button) findViewById(R.id.regi);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username=name.getText().toString();
                final String userpwd=password.getText().toString();
                if(username.length()==0||userpwd.length()==0)
                    Toast.makeText(MainActivity.this,"用户名及密码不能为空",Toast.LENGTH_SHORT).show();
                else {
                    new Thread() {
                        public void run() {
                            try {
                                //创建客户端对象
                                Socket socket = new Socket(HOST, port);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                String data = String.valueOf(login_request) + "-" + username + "-" + userpwd;
                                out.write(data);
                                out.flush();
                                Message message = Message.obtain(handler);
                                message.what = login_request;
                                message.arg1 = 0;
                                message.arg2 = 0;
                                String[] res = in.readLine().split("/");
                                message.arg1 = Integer.parseInt(res[0]);
                                message.arg2 = Integer.parseInt(res[1]);
                                message.obj = "nothing";
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
                }
            }
        });
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username=name.getText().toString();
                final String userpwd=password.getText().toString();
                if(username.length()==0||userpwd.length()==0)
                    Toast.makeText(MainActivity.this,"用户名及密码不能为空",Toast.LENGTH_SHORT).show();
                else {
                    new Thread() {
                        public void run() {
                            try {
                                //创建客户端对象
                                Socket socket = new Socket(HOST, port);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                String data = String.valueOf(regi_request) + "-" + username + "-" + userpwd;
                                out.write(data);
                                out.flush();
                                Message message = Message.obtain(handler);
                                message.what = regi_request;
                                message.arg1 = 0;
                                message.arg2 = 0;
                                String[] res = in.readLine().split("/");
                                message.arg1 = Integer.parseInt(res[0]);
                                message.arg2 = Integer.parseInt(res[1]);
                                message.obj = "nothing";
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
                }
            }
        });
//        setContentView(R.layout.activity_main);
//        //获取地图控件引用
//        mMapView = (MapView) findViewById(R.id.bmapView);


    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
//        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
//        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
//        mMapView.onDestroy();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            System.out.println(latitude);
            System.out.println(longitude);
            System.out.println(errorCode);
        }
    }
}
