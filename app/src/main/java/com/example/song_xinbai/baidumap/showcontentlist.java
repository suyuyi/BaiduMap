package com.example.song_xinbai.baidumap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.song_xinbai.baidumap.globaldata.login_request;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class showcontentlist extends Activity {
    private ListView listview;
    final String HOST=globaldata.getHOST();
    final int port=globaldata.getPORT();
    public static int print_info=0,comment_list_request=7,like_request=8,dislike_request=9;
    public boolean liked=false,disliked=false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int arg1 = msg.arg1;//success or failed
            int arg2 = msg.arg2;//UserID or fail type
            int what = msg.what;
            String result =(String) msg.obj;
            if(what==print_info)
                Toast.makeText(showcontentlist.this,result,Toast.LENGTH_SHORT).show();
            else if(what==comment_list_request&&arg1==1&&arg2!=0)
            {
                List<String> listofcomment=new ArrayList<String>();
                String[] tmp=result.split("/");
                for(int i=0;i<tmp.length;++i)
                    listofcomment.add(tmp[i]);
                MyAdapter myAdapter = new MyAdapter(showcontentlist.this,listofcomment);
                listview.setAdapter(myAdapter);
            }
            else if(what==comment_list_request&&arg1==1&&arg2==0)
                Toast.makeText(showcontentlist.this,"没有评论，抢一个沙发吧！",Toast.LENGTH_SHORT).show();
            else if(what==like_request)
                Toast.makeText(showcontentlist.this,"点赞成功！",Toast.LENGTH_SHORT).show();
            else if(what==dislike_request)
                Toast.makeText(showcontentlist.this,"举报成功！",Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("OK");
        setContentView(R.layout.item_maker);
        Intent intent =getIntent();
        final Bundle bundle = intent.getExtras();
        final locwithcontent mylocon = (locwithcontent) bundle.get("locwithcontent");
        TextView tv_title = (TextView)findViewById(R.id.title);
        TextView tv_content = (TextView)findViewById(R.id.content);
        tv_title.setText(mylocon.title);
        tv_content.setText(mylocon.content);
        Button comment=(Button)findViewById(R.id.mark_comment);
        Button like=(Button)findViewById(R.id.like);
        final Button dislike=(Button)findViewById(R.id.dislike);
        listview=(ListView)findViewById(R.id.comment_list);
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象
                    Socket socket = new Socket(HOST, port);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    JSONObject inf = new JSONObject();
                    inf.put("request",String.valueOf(comment_list_request));
                    inf.put("uid",String.valueOf(mylocon.uid));
                    out.write(String.valueOf(String.valueOf(inf).length()));
                    out.write(String.valueOf(inf));
                    out.flush();
                    Message message = Message.obtain(handler);
                    message.what = comment_list_request;
                    String res = in.readLine();
                    String[] tmp = res.split("/");
                    message.arg1 = 1;
                    message.arg2 = tmp.length;
                    message.obj = res;
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
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent new_intent = new Intent();
//                new_intent.setClass(showcontentlist.this,sendcomment.class);
//                Bundle new_bundle = new Bundle();
//                new_bundle.putSerializable("locwithcontent",mylocon);
//                new_intent.putExtras(new_bundle);
//                startActivity(new_intent);
            }
        });
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!liked)
                {
                    new Thread() {
                        public void run() {
                            try {
//                                Socket socket = new Socket(HOST, port);
//                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                                JSONObject inf = new JSONObject();
//                                inf.put("request",String.valueOf(like_request));
//                                inf.put("uid",String.valueOf(mylocon.uid));
//                                out.write(String.valueOf(String.valueOf(inf).length()));
//                                out.write(String.valueOf(inf));
//                                out.flush();
//                                liked=true;
//                                socket.close();
//                                Message message = Message.obtain(handler);
//                                message.what = like_request;
//                                message.arg1 = 0;
//                                message.arg2 = 0;
//                                message.obj = "finish";
//                                message.sendToTarget();
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
                else
                    Toast.makeText(showcontentlist.this,"您已点赞！",Toast.LENGTH_SHORT).show();

            }
        });
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!disliked)
                {
                    new Thread() {
                        public void run() {
                            try {
//                                Socket socket = new Socket(HOST, port);
//                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                                JSONObject inf = new JSONObject();
//                                inf.put("request",String.valueOf(like_request));
//                                inf.put("uid",String.valueOf(mylocon.uid));
//                                out.write(String.valueOf(String.valueOf(inf).length()));
//                                out.write(String.valueOf(inf));
//                                out.flush();
//                                disliked=true;
//                                socket.close();
//                                Message message = Message.obtain(handler);
//                                message.what = dislike_request;
//                                message.arg1 = 0;
//                                message.arg2 = 0;
//                                message.obj = "finish";
//                                message.sendToTarget();
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
                else
                    Toast.makeText(showcontentlist.this,"您已举报！",Toast.LENGTH_SHORT).show();
            }
        });

    }
    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<String> list;


        public MyAdapter(Context context , List<String> list){

            this.mInflater = LayoutInflater.from(context);
            this.list = list;
        }


        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            ViewHolder holder = null;

            if (convertView == null) {

                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item, null);
                holder.comment_content = (TextView)convertView.findViewById(R.id.comment_content);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.comment_content.setText((String)list.get(position));
            return convertView;
        }


        public final class ViewHolder{
            public TextView comment_content;
        }

    }
}
