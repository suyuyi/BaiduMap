package com.example.song_xinbai.baidumap;

import java.io.Serializable;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class loc implements Serializable {
    public double la,ln;
    public int uid;
    public loc(double lat,double lon,int userid)
    {
        this.la=lat;
        this.ln=lon;
        this.uid=userid;
    }
}
