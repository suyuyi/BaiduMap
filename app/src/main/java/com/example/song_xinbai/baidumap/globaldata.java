package com.example.song_xinbai.baidumap;

/**
 * @author Administrator
 * @des ${TODO}
 * @vresion $Rev$
 * @updateauthor $Author$
 * @updatedes ${TODO}
 */
public class globaldata {
    private static String HOST="152.136.125.236";
    private static int PORT=32769;
    public static int print_info=0,login_request=1,regi_request=2,send_request=3,scan_request=4;
    public static int getPORT()
    {
        return PORT;
    }
    public static String getHOST()
    {
        return HOST;
    }
}
