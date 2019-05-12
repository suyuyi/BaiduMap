package com.example.song_xinbai.baidumap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Socket;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class sendmessage extends Activity {
    final String HOST="45.76.196.92";
    final int port=8088;
    public static int print_info=0,send_request=3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {  //这个是发送过来的消息
            // 处理从子线程发送过来的消息
            int arg1 = msg.arg1;//success or failed
            int arg2 = msg.arg2;//UserID or fail type
            int what = msg.what;
            String result =(String) msg.obj;
            if(what==print_info)
                Toast.makeText(sendmessage.this,result,Toast.LENGTH_SHORT).show();
            else if(what==send_request&&arg1==1&&arg2==1)
            {
                Toast.makeText(sendmessage.this,"发送成功",Toast.LENGTH_SHORT).show();
                sendmessage.this.finish();
            }
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent =getIntent();
        Bundle bundle = intent.getExtras();
        final loc myloc = (loc) bundle.get("location");
        setContentView(R.layout.sendtext);
        final EditText title=(EditText)findViewById(R.id.title);
        final EditText content=(EditText)findViewById(R.id.content);
        Button sendtext=(Button)findViewById(R.id.msend);
        sendtext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t=title.getText().toString();
                String c=content.getText().toString();
                if(t.length()==0||c.length()==0)
                    Toast.makeText(sendmessage.this,"标题和内容不能为空",Toast.LENGTH_SHORT).show();
                else {
                    double mlat = myloc.la;
                    double mlon = myloc.ln;
                    int UID = myloc.uid;
                    //userid-lon-lat-title-content
                    final String data = String.valueOf(send_request) + '-' + String.valueOf(UID) + '-' + String.valueOf(mlon) + '-' + String.valueOf(mlat) + '-' + t + '-' + c;
                    new Thread() {
                        public void run() {
                            try {
                                //创建客户端对象
                                Socket socket = new Socket(HOST, port);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                out.write(data);
                                out.flush();
                                Message message = Message.obtain(handler);
                                message.what = send_request;
//                                message.arg1 = 1;
//                                message.arg2 = 1;
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
    }
}
