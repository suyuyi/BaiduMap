package com.example.song_xinbai.baidumap;

import java.io.Serializable;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class locwithcontent implements Serializable {
    public double la,ln;
    public int uid;//此处的id指的是每一个漂流瓶自带的ID便于之后评论相关
    public String title,content;
    public locwithcontent(double lat,double lon,int userid,String title,String content)
    {
        this.la=lat;
        this.ln=lon;
        this.uid=userid;
        this.title=title;
        this.content=content;
    }
}
