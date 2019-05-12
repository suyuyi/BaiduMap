package com.example.song_xinbai.baidumap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class sendcomment extends Activity {
    final String HOST="45.76.196.92";
    final int port=8088;
    public static int print_info=0,comment_request=6;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {  //这个是发送过来的消息
            // 处理从子线程发送过来的消息
            int arg1 = msg.arg1;//success or failed
            int arg2 = msg.arg2;//UserID or fail type
            int what = msg.what;
            if(what==print_info)
            {
                String result =(String) msg.obj;
                Toast.makeText(sendcomment.this,result,Toast.LENGTH_SHORT).show();
            }
            else if(what==comment_request)
            {
                Toast.makeText(sendcomment.this,"发送成功",Toast.LENGTH_SHORT).show();
                sendcomment.this.finish();
            }

        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final locwithcontent mylocon = (locwithcontent) bundle.get("locwithcontent");
        setContentView(R.layout.commentsend);
        TextView title=(TextView)findViewById(R.id.retitle);
        final EditText content=(EditText)findViewById(R.id.recontent);
        title.setText("Re:"+mylocon.title);
        Button send_comment=(Button)findViewById(R.id.send_comment);
        send_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String c=content.getText().toString();
                if(c.length()==0)
                    Toast.makeText(sendcomment.this,"评论不能为空",Toast.LENGTH_SHORT).show();
                else
                {
                    int targetID=mylocon.uid;
                    final String data=String.valueOf(comment_request)+'-'+targetID+'-'+c;
                    new Thread() {
                        public void run() {
                            try {
                                //创建客户端对象
                                Socket socket = new Socket(HOST, port);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                out.write(data);
                                out.flush();
                                String[] res=in.readLine().split("/");
                                Message message = Message.obtain(handler);
                                message.what = comment_request;
                                message.arg1 = Integer.parseInt(res[0]);
                                message.arg2 = Integer.parseInt(res[1]);
                                message.sendToTarget();
                                socket.close();
                            }
                            catch(Exception e)
                            {
                                Message message = Message.obtain(handler);
                                message.what = print_info;
                                message.arg1 = 0;
                                message.arg2 = 0;
                                message.obj = "服务器连接失败";
                                message.sendToTarget();
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        });
    }
}
