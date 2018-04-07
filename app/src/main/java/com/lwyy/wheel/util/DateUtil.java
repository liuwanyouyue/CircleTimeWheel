package com.lwyy.wheel.util;

import android.content.Context;

import com.lwyy.wheel.R;

import java.util.Calendar;

/**
 * Created by ll on 2018/4/7.
 */

public enum DateUtil {
    INSTANCE;
    /**
     * 获取这个月的最大天数
     * @param monthPos month的index
     * @param yearDate 实际的年，例如2018等
     * @return
     */
    public int updateLastMaxDays(int monthPos, int yearDate) {
        int lastMaxDays = 31;
        switch (monthPos) {
            case 3:
            case 5:
            case 8:
            case 10:
                lastMaxDays = 30;
                break;
            case 1:
                lastMaxDays = initFebMonth(yearDate);
                break;
            default:
                lastMaxDays = 31;
                break;
        }
        return lastMaxDays;
    }

    public int initFebMonth(int year) {
        if (year % 4 == 0 && (year % 400 == 0 || year % 100 != 0)) {
            return 29;
        }
        return 28;
    }

    /**
     * 获取month对应的index的名称
     *
     * @param context
     * @param month
     * @return
     */
    public String getMonthStr(Context context, int month) {
        String monthStr = "";
        switch (month) {
            case 0:
                monthStr = context.getString(R.string.s_public_full_month_January);
                break;
            case 1:
                monthStr = context.getString(R.string.s_public_full_month_February);
                break;
            case 2:
                monthStr = context.getString(R.string.s_public_full_month_March);
                break;
            case 3:
                monthStr = context.getString(R.string.s_public_full_month_April);
                break;
            case 4:
                monthStr = context.getString(R.string.s_public_full_month_May);
                break;
            case 5:
                monthStr = context.getString(R.string.s_public_full_month_June);
                break;
            case 6:
                monthStr = context.getString(R.string.s_public_full_month_July);
                break;
            case 7:
                monthStr = context.getString(R.string.s_public_full_month_August);
                break;
            case 8:
                monthStr = context.getString(R.string.s_public_full_month_September);
                break;
            case 9:
                monthStr = context.getString(R.string.s_public_full_month_October);
                break;
            case 10:
                monthStr = context.getString(R.string.s_public_full_month_November);
                break;
            case 11:
                monthStr = context.getString(R.string.s_public_full_month_December);
                break;
        }
        return monthStr;
    }

    /**
     * 获取这月这天的周一日期
     *
     * @param week
     * @return
     */
    public int returnMondayWeekOfMonth(int week) {
        int end = 1;
        if (week == Calendar.MONDAY) {
            end = 1;
        } else if (week == Calendar.SUNDAY)//周日；
            end = 2;
        else if (week == Calendar.TUESDAY) {
            end = 7;
        } else if (week == Calendar.WEDNESDAY) {
            end = 6;
        } else if (week == Calendar.THURSDAY) {
            end = 5;
        } else if (week == Calendar.FRIDAY) {
            end = 4;
        } else if (week == Calendar.SATURDAY) {//周六
            end = 3;
        }
        return end;
    }

    /**
     * 获取这周这天的周一日期
     *
     * @param week
     * @param endPos
     * @return
     */
    public int returnWeekStartMonday(int week, int endPos) {
        int startDay = 1;
        if (week == Calendar.MONDAY)
            startDay = endPos + 1;
        else if (week == Calendar.TUESDAY)
            startDay = endPos;
        else if (week == Calendar.WEDNESDAY)
            startDay = endPos - 1;
        else if (week == Calendar.THURSDAY)
            startDay = endPos - 2;
        else if (week == Calendar.FRIDAY)
            startDay = endPos - 3;
        else if (week == Calendar.SATURDAY)
            startDay = endPos - 4;
        else if (week == Calendar.SUNDAY)
            startDay = endPos - 5;
        return startDay;
    }
}
