package com.example.song_xinbai.baidumap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Socket;
import org.json.JSONObject;

import static com.example.song_xinbai.baidumap.globaldata.*;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class sendmessage extends Activity {
    final String HOST=getHOST();
    final int port=getPORT();
    /***
     * 使用照相机拍照获取图片
     */
    public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
    /***
     * 使用相册中的图片
     */
    public static final int SELECT_PIC_BY_PICK_PHOTO = 2;

    /***
     * 从Intent获取图片路径的KEY
     */
    public static final String KEY_PHOTO_PATH = "photo_path";

    private static final String TAG = "SelectPicActivity";

    private LinearLayout dialogLayout;
    private Button takePhotoBtn,pickPhotoBtn,cancelBtn;

    /**获取到的图片路径*/
    private String picPath;

    private Intent lastIntent ;

    private Uri photoUri;
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
            else if(what==send_request&&arg1==1)
            {
                Toast.makeText(sendmessage.this,"发送成功"+String.valueOf(arg2),Toast.LENGTH_SHORT).show();
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
        final Button selectimage=(Button)findViewById(R.id.button2);
        final ImageView imageplay=(ImageView)findViewById(R.id.imageplay);
        Button showimage=(Button)findViewById(R.id.showimage);
        selectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selImage();
            }
        });
        showimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageplay.setImageURI(photoUri);
            }
        });
        sendtext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String t=title.getText().toString();
                final String c=content.getText().toString();
                if(t.length()==0||c.length()==0)
                    Toast.makeText(sendmessage.this,"标题和内容不能为空",Toast.LENGTH_SHORT).show();
                else {
                    final double mlat = myloc.la;
                    final double mlon = myloc.ln;
                    final int UID = myloc.uid;
                    //userid-lon-lat-title-content
                    new Thread() {
                        public void run() {
                            try {
                                //创建客户端对象
                                Socket socket = new Socket(HOST, port);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                OutputStream out2 = socket.getOutputStream();
                                JSONObject inf = new JSONObject();
                                inf.put("request",send_request);
                                inf.put("userid",UID);
                                inf.put("lon",mlon);
                                inf.put("lat",mlat);
                                inf.put("title",t);
                                inf.put("content",c);
                                int i=picPath.length();
                                inf.put("suffix",picPath.substring(i-3));
                                out.write(String.valueOf(String.valueOf(inf).length()));
                                out.write(String.valueOf(inf));
                                out.write(' ');
                                File file=new File(picPath);
                                out.write(String.valueOf(file.length()));
                                out.write(' ');
                                out.flush();
                                InputStream filein = new FileInputStream(file);
                                int tmpsize=1024, sz;
                                byte[] tmpbytes = new byte[tmpsize];
                                while ((sz = filein.read(tmpbytes)) != -1) {
                                    out2.write(tmpbytes, 0, sz);
                                }
//                                out.close();
                                Message message = Message.obtain(handler);
                                message.what = send_request;
                                String line;
                                StringBuilder stringBuilder = new StringBuilder();
                                while ((line = in.readLine()) != null)
                                    stringBuilder.append(line);
                                JSONObject res = new JSONObject(stringBuilder.toString());
                                message.arg1 = res.getInt("result");
                                message.arg2 = res.getInt("floatingID");
                                message.obj = "";
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

    // 进入选择图片的界面
    private void selImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,SELECT_PIC_BY_PICK_PHOTO);
    }
    // 在onActivityResult()回调方法中进行数据获取
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // ... 进行一些判断处理
        photoUri = data.getData();
        // ... 接下来进行图片显示
        picPath=getRealPathFromUri(this,photoUri);
    }
    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())){
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}
