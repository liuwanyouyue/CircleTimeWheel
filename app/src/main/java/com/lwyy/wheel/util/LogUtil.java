package com.lwyy.wheel.util;

import android.util.Log;

/**
 * Created by ll on 2018/4/7.
 */

public class LogUtil {
    private static boolean isDebug = true;
    public static void i(String TAG,String msg){
        if(isDebug)
            Log.i(TAG,msg);
    }
    public static void w(String TAG,String msg){
        if(isDebug)
            Log.w(TAG,msg);
    }
    public static void e(String TAG,String msg){
        if(isDebug)
            Log.e(TAG,msg);
    }
}
