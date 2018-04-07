package com.lwyy.wheel;

import android.content.Context;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.lwyy.wheel.util.LogUtil;
import com.lwyy.wheel.util.UnitUtil;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private Button btnShow;
    private LinearLayout llParent;
    private Context context;
    private Calendar calendar;
    private int dateType = PublicConstant.DATE_TYPE_DAY;
    private int sportType = PublicConstant.SPORT_TYPE_DISTANCE;
    private SelectDateWheelWindow selectDatePopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        btnShow = findViewById(R.id.btn_show);
        llParent = findViewById(R.id.ll_parent);
        btnShow.setOnClickListener(this);
        llParent.setOnClickListener(this);

        calendar = Calendar.getInstance();
    }

    private void showPopupWindow() {
        if (null == selectDatePopupWindow)
            selectDatePopupWindow = new SelectDateWheelWindow(context, calendar, dateType, sportType);
        else{
            selectDatePopupWindow.updateDateAndType(calendar, dateType, sportType);
        }

        selectDatePopupWindow.setCallBack(new SelectDateWheelWindow.CallBack() {
            @Override
            public void callBack(Calendar setCalendar, int type) {
                calendar = setCalendar;
                dateType = type;
                LogUtil.i(TAG, "设置时间为:" + setCalendar.get(Calendar.YEAR) + "-" + setCalendar.get(Calendar.MONTH) + "-" + setCalendar.get(Calendar.DAY_OF_MONTH));
            }
        });
        selectDatePopupWindow.showAtLocation(btnShow, Gravity.CENTER, 0, UnitUtil.dp2px(context, 0));
    }

    private void gonePopupWindow() {
        if (null != selectDatePopupWindow && selectDatePopupWindow.isShowing())
            selectDatePopupWindow.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show:
                showPopupWindow();
                break;
            case R.id.ll_parent:
                gonePopupWindow();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gonePopupWindow();
    }
}
