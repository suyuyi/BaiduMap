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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static com.example.song_xinbai.baidumap.globaldata.*;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class AdminLog extends Activity {
    final String HOST=globaldata.getHOST();
    final int port=globaldata.getPORT();
    int admin_log=12;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.arg1==1)
            {
                Toast.makeText(AdminLog.this,"登录成功",Toast.LENGTH_SHORT).show();
                loc myloc=new loc(-1,-1,-1);
                Intent intent = new Intent();
                intent.setClass(AdminLog.this,infolist.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("location",myloc);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(AdminLog.this,"登录失败",Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);
        final EditText adminid=(EditText)findViewById(R.id.adminid);
        final EditText adminpwd=(EditText)findViewById(R.id.adminpwd);
        Button login=(Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name=adminid.getText().toString();
                final String pwd=adminpwd.getText().toString();
                new Thread() {
                    public void run() {
                        try {
                            Socket socket = new Socket(HOST, port);
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            JSONObject inf = new JSONObject();
                            inf.put("request",String.valueOf(admin_log));
                            inf.put("name",name);
                            inf.put("pwd",pwd);
                            out.write(String.valueOf(String.valueOf(inf).length()));
                            out.write(String.valueOf(inf));
                            out.flush();
                            Message message = Message.obtain(handler);
                            message.what = admin_log;
                            String line;
                            StringBuilder stringBuilder = new StringBuilder();
                            while ((line = in.readLine()) != null)
                                stringBuilder.append(line);
                            JSONObject res = new JSONObject(stringBuilder.toString());
                            //                                System.out.println(res);
                            message.arg1 = res.getInt("result");
                            message.obj = "";
                            message.sendToTarget();
                            socket.close();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }
}
