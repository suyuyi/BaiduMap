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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class AdminWindow extends Activity {
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int arg1 = msg.arg1;//success or failed
            int arg2 = msg.arg2;//UserID or fail type
            int what = msg.what;
            String result =(String) msg.obj;
            if(what==print_info)
                Toast.makeText(AdminWindow.this,result,Toast.LENGTH_SHORT).show();
            else if(what==admin_request&&arg1==1&&arg2!=0)
            {
                Toast.makeText(AdminWindow.this,"载入完成",Toast.LENGTH_SHORT).show();
                List<titlepluscontent> listofcomment=new ArrayList<titlepluscontent>();
                //id+title+content
                int prev=0;
                int i=0;
                while(i<result.length())
                {
                    if(result.charAt(i)=='{')
                    {
                        JSONObject target=null;
                        int len=Integer.parseInt(result.substring(prev,i));//??+1
                        String tmp=result.substring(i,i+len);
                        try {
                            target=new JSONObject(tmp);
                            prev=i+len;
                            titlepluscontent t=new titlepluscontent(target.getString("title"),target.getString("content"),target.getInt("id"),target.getInt("love"),target.getInt("hate"));
                            listofcomment.add(t);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        i=i+len;
                        System.out.println(i);
                    }
                    else
                    {
                        i=i+1;
                    }
                }
                System.out.println("OK1");
                myAdapter = new MyAdapter(AdminWindow.this,listofcomment);
                System.out.println("OK2");
                listview.setAdapter(myAdapter);
                System.out.println("OK3");
            }
            else if(what==read_request&&arg1==1&&arg2==0)
                Toast.makeText(AdminWindow.this,"没有评论，抢一个沙发吧！",Toast.LENGTH_SHORT).show();
            else if(what==delete_request)
            {
                myAdapter.delete(arg1);
                listview.setAdapter(myAdapter);
            }
        }
    };
    public static MyAdapter myAdapter;
    private ListView listview;
    final String HOST=globaldata.getHOST();
    final int port=globaldata.getPORT();
    public static int print_info=0,read_request=5,delete_request=10,admin_request=13,top_request=14;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selfinfo);
        listview=(ListView)findViewById(R.id.listofinfo);
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象
                    Socket socket = new Socket(HOST, port);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    JSONObject inf = new JSONObject();
                    inf.put("request",String.valueOf(admin_request));
                    out.write(String.valueOf(String.valueOf(inf).length()));
                    out.write(String.valueOf(inf));
                    out.flush();
                    Message message = Message.obtain(handler);
                    message.what = admin_request;
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = in.readLine()) != null)
                        stringBuilder.append(line);
                    String s=stringBuilder.toString();
                    message.arg1 = 1;
                    message.arg2 = 1;
                    message.obj = s;
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
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<titlepluscontent> list;
        public MyAdapter(Context context , List<titlepluscontent> list){
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            ViewHolder holder = null;

            if (convertView == null) {

                holder = new AdminWindow.MyAdapter.ViewHolder();
                convertView = mInflater.inflate(R.layout.adminitem, null);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.content = (TextView)convertView.findViewById(R.id.content);
                holder.delete=(Button)convertView.findViewById(R.id.delete);
                holder.cnt_likes=(TextView)convertView.findViewById(R.id.cnt_likes);
                holder.cnt_comments=(TextView)convertView.findViewById(R.id.cnt_comment);
                holder.top=(Button)convertView.findViewById(R.id.top);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText("标题："+list.get(position).title);
            holder.content.setText("内容："+list.get(position).content);
            holder.cnt_likes.setText("喜爱："+String.valueOf(list.get(position).love));
            holder.cnt_comments.setText("举报："+String.valueOf(list.get(position).hate));
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread() {
                        public void run() {
                            try {
                                //创建客户端对象
                                Socket socket = new Socket(HOST, port);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                String data=String.valueOf(delete_request)+'-'+String.valueOf(list.get(position).id);
                                out.write(data);
                                out.flush();
                                Message message = Message.obtain(handler);
                                message.what = delete_request;
                                message.arg1 = position;
                                message.arg2 = 0;
                                message.obj = "deleted";
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
            });
            holder.top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread() {
                        public void run() {
                            try {
                                //创建客户端对象
                                Socket socket = new Socket(HOST, port);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                String data=String.valueOf(top_request)+'-'+String.valueOf(list.get(position).id);
                                out.write(data);
                                out.flush();
                                Message message = Message.obtain(handler);
                                message.what = delete_request;
                                message.arg1 = position;
                                message.arg2 = 0;
                                message.obj = "deleted";
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
            });
            return convertView;
        }


        public final class ViewHolder{
            public TextView title,content,cnt_likes,cnt_comments;
            public Button delete,top;
        }

        public void delete(int position)
        {
            list.remove(position);
        }

    }
    public class titlepluscontent
    {
        public String title,content;
        public int id,love,hate;
        public titlepluscontent(String title,String content,int id,int love,int hate)
        {
            this.title=title;
            this.content=content;
            this.id=id;
            this.love=love;
            this.hate=hate;
        }
    }
}
