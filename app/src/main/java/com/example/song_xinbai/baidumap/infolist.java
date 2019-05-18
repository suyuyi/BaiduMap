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
public class infolist extends Activity {
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int arg1 = msg.arg1;//success or failed
            int arg2 = msg.arg2;//UserID or fail type
            int what = msg.what;
            String result =(String) msg.obj;
            if(what==print_info)
                Toast.makeText(infolist.this,result,Toast.LENGTH_SHORT).show();
            else if(what==read_request&&arg1==1&&arg2!=0)
            {
                Toast.makeText(infolist.this,"载入完成",Toast.LENGTH_SHORT).show();
                List<titlepluscontent> listofcomment=new ArrayList<titlepluscontent>();
                String[] tmp=result.split("/");
                //id+title+content
                for(int i=0;i<tmp.length;++i)
                {
                    String[] tmp1=tmp[i].split("~");
                    titlepluscontent t=new titlepluscontent(tmp1[1],tmp1[2],Integer.parseInt(tmp1[0]));
                    listofcomment.add(t);
                }
                myAdapter = new MyAdapter(infolist.this,listofcomment);
                listview.setAdapter(myAdapter);
            }
            else if(what==read_request&&arg1==1&&arg2==0)
                Toast.makeText(infolist.this,"没有评论，抢一个沙发吧！",Toast.LENGTH_SHORT).show();
            else if(what==delete_request)
            {
                myAdapter.delete(arg1);
                listview.setAdapter(myAdapter);
            }
        }
    };
    public static MyAdapter myAdapter;
    private ListView listview;
    final String HOST="45.76.196.92";
    final int port=8088;
    public static int print_info=0,read_request=5,delete_request=10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selfinfo);
        listview=(ListView)findViewById(R.id.listofinfo);
        Intent intent =getIntent();
        Bundle bundle = intent.getExtras();
        final loc myloc = (loc) bundle.get("location");
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象
                    Socket socket = new Socket(HOST, port);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    String data=String.valueOf(read_request)+'-'+String.valueOf(myloc.uid);
                    out.write(data);
                    out.flush();
                    Message message = Message.obtain(handler);
                    message.what = read_request;
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

                holder = new infolist.MyAdapter.ViewHolder();
                convertView = mInflater.inflate(R.layout.infoitem, null);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.content = (TextView)convertView.findViewById(R.id.content);
                holder.delete=(Button)convertView.findViewById(R.id.delete);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText(list.get(position).title);
            holder.content.setText(list.get(position).content);
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
            return convertView;
        }


        public final class ViewHolder{
            public TextView title,content;
            public Button delete;
        }

        public void delete(int position)
        {
            list.remove(position);
        }

    }
    public class titlepluscontent
    {
        public String title,content;
        public int id;
        public titlepluscontent(String title,String content,int id)
        {
            this.title=title;
            this.content=content;
            this.id=id;
        }
    }
}
