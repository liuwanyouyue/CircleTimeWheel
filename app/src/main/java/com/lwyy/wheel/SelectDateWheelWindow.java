package com.lwyy.wheel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lwyy.wheel.util.DateUtil;
import com.lwyy.wheel.util.LogUtil;
import com.lwyy.wheel.util.UnitUtil;
import com.lwyy.wheel.view.CircleWheelView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.lwyy.wheel.PublicConstant.DATE_TYPE_DAY;
import static com.lwyy.wheel.PublicConstant.DATE_TYPE_MONTH;
import static com.lwyy.wheel.PublicConstant.DATE_TYPE_WEEK;

public class SelectDateWheelWindow extends PopupWindow implements View.OnClickListener, PopupWindow.OnDismissListener {
    private final String TAG = SelectDateWheelWindow.class.getSimpleName();
    private LinearLayout llDetailDay, llDetailWeek, llDetailMonth;
    private TextView tvDetailDay, tvDetailWeek, tvDetailMonth;
    private CircleWheelView pwvDetailShow, pwvDetailShowLeft, pwvDetailShowRight;
    private ImageView ivDetailCircle, ivDetailCircleLeft, ivDetailCircleRight;

    private Context context;
    private Calendar calendar;
    private View contentView;
    private int[] typeColors = new int[]{R.color.colorYellow, R.color.colorGreen, R.color.colorRed, R.color.colorBlue};
    private int[] typeCircleResIds = new int[]{R.mipmap.activity_detail_date_yellow, R.mipmap.activity_detail_date_green, R.mipmap.activity_detail_date_red, R.mipmap.activity_detail_date_blue};
    private List<String> arrDayDay;
    private List<String> arrDayMonth;
    private List<String> arrDayYear;
    private List<String> arrWeek;
    private List<String> arrMonth;
    private int dayDayPos, dayYearPos, dayMonthPos, weekPos;
    private int dateType, sportType;
    private int currentMonth, currentYear, currentDay;
    private List<String> currentDayArray;
    private List<String> currentMonthArray;
    private List<String> selectMonArray;
    private List<String> selectDayArray;
    private DateUtil mDateUtil;
    private CallBack callBack;

    public SelectDateWheelWindow(Context context, Calendar initCalendar, int dateType1, int sportType) {
        super(context);
        contentView = LayoutInflater.from(context).inflate(R.layout.dg_select_date_wheel_window, null);
        this.context = context;
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        mDateUtil = DateUtil.INSTANCE;
        this.setContentView(contentView);
        this.setWidth(LayoutParams.MATCH_PARENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new BitmapDrawable());//设置为null则无法消失
        setPopupWindowTouchModal(this, false);
        this.setOnDismissListener(this);
        initView();
        initListener();
        updateDateAndType(initCalendar, dateType1, sportType);
    }

    public void updateDateAndType(Calendar initCalendar, int dateType1, int sportType) {
        this.calendar = (Calendar) initCalendar.clone();
        dayYearPos = calendar.get(Calendar.YEAR) - currentYear + 99;
        dayMonthPos = calendar.get(Calendar.MONTH);
        dayDayPos = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        this.dateType = dateType1;
        this.sportType = sportType;
        initList();
        showView();
    }

    private void initView() {
        llDetailDay = contentView.findViewById(R.id.ll_activity_detail_day);
        llDetailWeek = contentView.findViewById(R.id.ll_activity_detail_week);
        llDetailMonth = contentView.findViewById(R.id.ll_activity_detail_month);
        tvDetailDay = contentView.findViewById(R.id.tv_activity_detail_day);
        tvDetailWeek = contentView.findViewById(R.id.tv_activity_detail_week);
        tvDetailMonth = contentView.findViewById(R.id.tv_activity_detail_month);
        ivDetailCircle = contentView.findViewById(R.id.iv_activity_detail_circle);
        pwvDetailShow = contentView.findViewById(R.id.pwv_activity_detail_show);
        pwvDetailShowLeft = contentView.findViewById(R.id.pwv_activity_detail_show_left);
        pwvDetailShowRight = contentView.findViewById(R.id.pwv_activity_detail_show_right);
        ivDetailCircleLeft = contentView.findViewById(R.id.iv_activity_detail_circle_left);
        ivDetailCircleRight = contentView.findViewById(R.id.iv_activity_detail_circle_right);
    }

    private void initListener() {
        llDetailDay.setOnClickListener(this);
        llDetailMonth.setOnClickListener(this);
        llDetailWeek.setOnClickListener(this);
        pwvDetailShow.setOnItemSelectedListener(new CircleWheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(CircleWheelView view, Object data, int position) {
                if (dateType == DATE_TYPE_MONTH) {
                    if (dayMonthPos != position) {
                        if (position > currentMonth)
                            dayYearPos = arrDayYear.size() - 2;
                        else
                            dayYearPos = arrDayYear.size() - 1;
                        dayMonthPos = position;
                        dayDayPos = 0;
                        resetWheelCalendarValue();
                    }
                } else if (dateType == DATE_TYPE_DAY) {
                    if (dayMonthPos != position) {
                        dayMonthPos = position;
                        if (dayYearPos == 99 && dayMonthPos == currentMonth) {
                            if (dayDayPos > currentDay - 1)
                                dayDayPos = currentDay - 1;
                        }
                        updateMonthDayArray(mDateUtil.updateLastMaxDays(dayMonthPos, Integer.parseInt(arrDayYear.get(dayYearPos))));
                        setDayAndMonth();
                        resetWheelCalendarValue();
                    }
                } else {//WEEK
                    resetWeekCal(data);
                    resetWheelCalendarValue();
                }
                LogUtil.i(TAG, "194-dayDayPos:" + dayDayPos + ",dayMonthPos:" + dayMonthPos + ",dayYearPos:" + dayYearPos);
//                LogUtil.i(TAG, "pwv_activity_detail_show---year:" + calendarSimple.get(Calendar.YEAR) + ",month:" + calendarSimple.get(Calendar.MONTH) + ",day:" + calendarSimple.get(Calendar.DAY_OF_MONTH));
            }
        });

        pwvDetailShowRight.setOnItemSelectedListener(new CircleWheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(CircleWheelView view, Object data, int position) {
                if (dayYearPos != position) {
                    dayYearPos = position;
                    if (dayYearPos == arrDayYear.size() - 1) {
                        if (dayMonthPos > currentMonth)
                            dayMonthPos = currentMonth;
                        if (dayMonthPos == currentMonth) {
                            if (dayDayPos > currentDay - 1)
                                dayDayPos = currentDay - 1;
                        } else {
                            updateMonthDayArray(mDateUtil.updateLastMaxDays(dayMonthPos, Integer.parseInt(arrDayYear.get(dayYearPos))));
                        }
                    } else {
                        updateMonthDayArray(mDateUtil.updateLastMaxDays(dayMonthPos, Integer.parseInt(arrDayYear.get(dayYearPos))));
                    }
                    setDayAndMonth();
                    resetWheelCalendarValue();
                }
                LogUtil.i(TAG, "211-dayDayPos:" + dayDayPos + ",dayMonthPos:" + dayMonthPos + ",dayYearPos:" + dayYearPos);
            }
        });

        pwvDetailShowLeft.setOnItemSelectedListener(new CircleWheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(CircleWheelView view, Object data, int position) {
                if (dayDayPos != position) {
                    dayDayPos = position;
                    resetWheelCalendarValue();
                }
                LogUtil.i(TAG, "226-dayDayPos:" + dayDayPos + ",dayMonthPos:" + dayMonthPos + ",dayYearPos:" + dayYearPos);
            }
        });
    }

    private void resetWeekCal(Object data) {
        String split[] = String.valueOf(data).split(" ");
        int monthSelectPos = 0;
        for (int i = 0; i < arrMonth.size(); i++) {
            if (split[0].equals(arrMonth.get(i))) {
                monthSelectPos = i;
                break;
            }
        }
        String valueStr = split[1].split("-")[0];
        valueStr = valueStr.substring(1, valueStr.length());
        dayYearPos = monthSelectPos > currentMonth ? arrDayYear.size() - 2 : arrDayYear.size() - 1;
        dayMonthPos = monthSelectPos;
        dayDayPos = Integer.parseInt(valueStr) - 1;
    }

    private void initSelectArray() {
        selectDayArray = (dayMonthPos == currentMonth) && (dayYearPos == arrDayYear.size() - 1) ? currentDayArray : arrDayDay;
        selectMonArray = (dayYearPos == arrDayYear.size() - 1) ? currentMonthArray : arrDayMonth;
    }

    private void setDayAndMonth() {
        initSelectArray();
        if (pwvDetailShow.getListSize() != selectMonArray.size())
            pwvDetailShow.setData(selectMonArray, dayMonthPos, true, false);
        if (pwvDetailShowLeft.getListSize() != selectDayArray.size())
            pwvDetailShowLeft.setData(selectDayArray, dayDayPos, true, false);
    }

    public static void setPopupWindowTouchModal(PopupWindow popupWindow, boolean touchModal) {
        if (null == popupWindow) {
            return;
        }
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal", boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetWheelCalendarValue() {
        calendar.set(Calendar.YEAR, Integer.parseInt(arrDayYear.get(dayYearPos)));
        calendar.set(Calendar.MONTH, dayMonthPos);
        calendar.set(Calendar.DAY_OF_MONTH, dayDayPos + 1);
    }

    private void updateMonthDayArray(int lastMaxDays) {
        LogUtil.i(TAG, "194-------------lastMaxDays:" + lastMaxDays + ",arrDayDay.size():" + arrDayDay.size());
        if (arrDayDay.size() < lastMaxDays) {
            for (int i = arrDayDay.size(); i < lastMaxDays; i++) {
                arrDayDay.add(i, String.valueOf(i + 1));
            }
        } else if (arrDayDay.size() > lastMaxDays) {
            for (int i = arrDayDay.size() - 1; i > lastMaxDays - 1; i--) {
                arrDayDay.remove(i);
            }
        }
        if (dayDayPos > arrDayDay.size() - 1)
            dayDayPos -= arrDayDay.size();
        LogUtil.i(TAG, "194--2--------lastMaxDays:" + lastMaxDays + ",arrDayDay.size():" + arrDayDay.size());
    }

    private void showView() {
        ivDetailCircle.setImageResource(typeCircleResIds[sportType]);
        ivDetailCircleLeft.setImageResource(typeCircleResIds[sportType]);
        ivDetailCircleRight.setImageResource(typeCircleResIds[sportType]);

        pwvDetailShow.setTextSelectColor(context.getResources().getColor(typeColors[sportType]));
        pwvDetailShowLeft.setTextSelectColor(context.getResources().getColor(typeColors[sportType]));
        pwvDetailShowRight.setTextSelectColor(context.getResources().getColor(typeColors[sportType]));

        pwvDetailShowLeft.setItemAlign(CircleWheelView.ALIGN_RIGHT);
        pwvDetailShowRight.setItemAlign(CircleWheelView.ALIGN_LEFT);

        pwvDetailShowLeft.setTurnToCenter(UnitUtil.dp2px(context, 1f));
        pwvDetailShowRight.setTurnToCenter(UnitUtil.dp2px(context, 1f));
        showDateTypeView();
    }

    /**
     * 滚轮的显示
     */
    private void showDateTypeView() {
        pwvDetailShowLeft.setVisibility(dateType == DATE_TYPE_DAY ? View.VISIBLE : View.GONE);
        pwvDetailShowRight.setVisibility(dateType == DATE_TYPE_DAY ? View.VISIBLE : View.GONE);
        tvDetailDay.setTextColor(dateType == DATE_TYPE_DAY ? context.getResources().getColor(typeColors[sportType]) : Color.GRAY);
        tvDetailWeek.setTextColor(dateType == DATE_TYPE_WEEK ? context.getResources().getColor(typeColors[sportType]) : Color.GRAY);
        tvDetailMonth.setTextColor(dateType == DATE_TYPE_MONTH ? context.getResources().getColor(typeColors[sportType]) : Color.GRAY);

        ivDetailCircleLeft.setVisibility(dateType == DATE_TYPE_DAY ? View.VISIBLE : View.GONE);
        ivDetailCircle.setVisibility(dateType == DATE_TYPE_WEEK ? View.VISIBLE : View.GONE);
        ivDetailCircleRight.setVisibility(dateType == DATE_TYPE_MONTH ? View.VISIBLE : View.GONE);

        switch (dateType) {
            case DATE_TYPE_DAY:
                pwvDetailShowLeft.setData(selectDayArray, dayDayPos, true, false);
                pwvDetailShow.setData(selectMonArray, dayMonthPos, true, false);
                pwvDetailShowRight.setData(arrDayYear, dayYearPos, false, false);
                break;
            case DATE_TYPE_WEEK:
                pwvDetailShow.setData(arrWeek, weekPos, true, false);
                break;
            case DATE_TYPE_MONTH:
                pwvDetailShow.setData(arrMonth, dayMonthPos, true, false);
                break;
        }
    }

    private void initList() {
        if (null == selectMonArray)
            selectMonArray = new ArrayList<>();
        if (null == selectDayArray)
            selectDayArray = new ArrayList<>();

        if (null == arrDayYear) {                                                              //直接获取年，不用处理
            arrDayYear = new ArrayList<>();
        }
        if (arrDayYear.size() > 0)
            arrDayYear.clear();
        for (int i = currentYear - 99; i <= currentYear; i++) {
            arrDayYear.add(String.valueOf(i));
        }

        int maxDay = mDateUtil.updateLastMaxDays(dayMonthPos, Integer.parseInt(arrDayYear.get(dayYearPos)));
        LogUtil.i(TAG, "maxDay:" + maxDay);
        if (null == arrDayDay)
            arrDayDay = new ArrayList<>();
        if (arrDayDay.size() > 0)
            arrDayDay.clear();

        for (int i = 0; i < maxDay; i++) {
            arrDayDay.add(String.valueOf(i + 1));
        }

        if (null == arrWeek)
            arrWeek = new ArrayList<>();
        if (arrWeek.size() > 0)
            arrWeek.clear();
        Calendar calendarC = Calendar.getInstance();
        int monthDay = 0, end = 0;
        String monthStr = "";

        if (null == arrMonth) {
            arrMonth = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                arrMonth.add(mDateUtil.getMonthStr(context, i));
            }
        }
        if (null == arrDayMonth)
            arrDayMonth = new ArrayList<>();
        arrDayMonth = arrMonth;

        if (null == currentDayArray)
            currentDayArray = new ArrayList<>();
        if (currentDayArray.size() > 0)
            currentDayArray.clear();

        for (int i = 0; i < currentDay; i++) {
            currentDayArray.add(String.valueOf(i + 1));
        }

        if (null == currentMonthArray)
            currentMonthArray = new ArrayList<>();
        if (currentMonthArray.size() > 0)
            currentMonthArray.clear();

        for (int i = 0; i <= currentMonth; i++) {
            currentMonthArray.add(mDateUtil.getMonthStr(context, i));
        }

        Calendar calCopy;
        int year = 0, week = 0;
        for (int i = 0; i < 12; i++) {
            monthStr = mDateUtil.getMonthStr(context, i);
            if (i > currentMonth) {
                calendarC.set(Calendar.YEAR, currentYear - 1);
                year = currentYear - 1;
            } else {
                calendarC.set(Calendar.YEAR, currentYear);
                year = currentYear;
            }

            calendarC.set(Calendar.MONTH, i);
            calendarC.set(Calendar.DATE, 1);
            monthDay = mDateUtil.updateLastMaxDays(i, year);
            week = calendarC.get(Calendar.DAY_OF_WEEK);
            end = mDateUtil.returnMondayWeekOfMonth(week);
            int extraNum = monthDay - end + 1;
            for (int j = 0; j < extraNum / 7; j++) {
                if (year == currentYear && i == currentMonth && end > currentDay)
                    continue;
                if (end + 6 > monthDay) {
                    break;
                }
                arrWeek.add(monthStr + " (" + (end) + "-" + (end + 6) + ")");
                end = end + 7;
            }
//            LogUtil.i(TAG, "下月：" + monthStr + ",nextMaxDay:" + monthDay + ",extraNum:" + extraNum);
            if (year == currentYear && i == currentMonth && end > currentDay)
                continue;
            calCopy = (Calendar) calendarC.clone();
            calCopy.set(Calendar.DAY_OF_MONTH, monthDay);
            if (calCopy.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                int endDay = end + 6 - monthDay;
                if (endDay == 0)
                    endDay = monthDay;
                arrWeek.add(monthStr + " (" + (end) + "-" + (endDay) + ")");
            }
        }
        initWeekPosition();
        initSelectArray();
    }

    private void initWeekPosition() {
        String append = "";
        int end = 0, start = 0;
        Calendar calCopy = (Calendar) calendar.clone();
        calCopy.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = mDateUtil.returnMondayWeekOfMonth(calCopy.get(Calendar.DAY_OF_WEEK));//开始的天数，非index
        int monthDay = mDateUtil.updateLastMaxDays(dayMonthPos, calCopy.get(Calendar.YEAR));
        String monthStr = mDateUtil.getMonthStr(context, dayMonthPos);
        calCopy.set(Calendar.DAY_OF_MONTH, dayDayPos + 1);
        start = mDateUtil.returnWeekStartMonday(calCopy.get(Calendar.DAY_OF_WEEK), dayDayPos);
        end = start + 6;

        //小于
        if (dayDayPos + 1 < startDay) {
            int lastMonth = dayMonthPos - 1;
            int lastMonthDay = 0;
            if (lastMonth < 0) {
                lastMonth += 12;
                dayYearPos = arrDayYear.size() - 2;

            }
            calCopy.set(Calendar.YEAR, Integer.valueOf(arrDayYear.get(dayYearPos)));
            lastMonthDay = mDateUtil.updateLastMaxDays(lastMonth, lastMonth > currentMonth ? currentYear - 1 : currentYear);
            if (start < 0) {
                start += lastMonthDay;
                monthStr = mDateUtil.getMonthStr(context, lastMonth);
            }
            append = String.valueOf(monthStr) + " (" + start + "-" + (end) + ")";

        } else {
            if (end > monthDay) {
                end -= monthDay;
            }
            monthStr = mDateUtil.getMonthStr(context, dayMonthPos);
            append = String.valueOf(monthStr) + " (" + start + "-" + (end) + ")";
        }
        for (int i = 0; i < arrWeek.size(); i++) {
            if (arrWeek.get(i).contains(append)) {
                weekPos = i;
                break;
            }
        }
        LogUtil.i(TAG, "weekPos:" + weekPos + ",append:" + append + ",end:" + end + ",startDay:" + startDay +
                ",year:" + calCopy.get(Calendar.YEAR) + ",month:" + calCopy.get(Calendar.MONTH));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_activity_detail_day:
                dateType = DATE_TYPE_DAY;
                setDayAndMonth();
                break;
            case R.id.ll_activity_detail_week:
                dateType = DATE_TYPE_WEEK;
                initWeekPosition();
                resetWeekCal(arrWeek.get(weekPos));
                break;
            case R.id.ll_activity_detail_month:
                dateType = DATE_TYPE_MONTH;
                dayDayPos = 0;
                break;
        }
        showDateTypeView();
        resetWheelCalendarValue();
    }

    @Override
    public void onDismiss() {
        if (null != callBack) {
            callBack.callBack(calendar, dateType);
        }
    }

    public interface CallBack {
        void callBack(Calendar c, int dateType);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public int getDateType() {
        return this.dateType;
    }
}
