package com.lwyy.wheel.util;

import android.content.Context;

/**
 * Created by ll on 2018/4/7.
 */

public class UnitUtil {
    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dpValue 要转换dp的值
     * @return 转换后px的值
     */
    public static int dp2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
