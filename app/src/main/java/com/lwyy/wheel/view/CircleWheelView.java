package com.lwyy.wheel.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.lwyy.wheel.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ll on 2017/9/25.
 */

public class CircleWheelView extends View {
    private static final String TAG = CircleWheelView.class.getSimpleName();
    private int mItemAlign;
    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;
    private Paint mPaint;
    private OnItemSelectedListener mOnItemSelectedListener;
    private int mTextMaxWidth, mTextMaxHeight;

    private Rect mRectDrawn;
    private int mDrawnCenterX, mDrawnCenterY;                                                  //滚轮选择器绘制中心坐标
    private int mWheelCenterX, mWheelCenterY;                                                  //滚轮选择器中心坐标
    private int mItemTextColor, mSelectedItemTextColor;                                      //数据项文本颜色以及被选中的数据项文本颜色
    private int mItemHeight;
    private float mItemTextSize;

    private int mLastPointY;                                                                      //用户手指上一次触摸事件发生时事件Y坐标
    private float moveY;

    private List mData = new ArrayList();
    private List mDataCC = new ArrayList();
    private int mTurnToCenterX;

    private int mCurrentItemPosition;
    private int mDefaultHalfNum = 7, mDefaultVisibleNum = 13;                                         //设置当前view默认可见的item个数
    private int mVisibleHalfNum, mVisibleCount;                                                 //视图区域内的展示的item个数
    private int mDataSize;

    private boolean isCyclic;                                                                    //数据是否循环展示  针对list的数目小于mDefaultVisibleNum
    private boolean isLoopDisplay;                                                              //所有的数据是否是球形展示,即永远的头尾衔接,如果isCyclic是true,则该值也是true

    private float[] rates = null;
    private float[] textSizeRates = null;

    public CircleWheelView(Context context) {
        this(context, null);
    }

    public CircleWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleWheelView);
        mItemTextSize = a.getDimensionPixelSize(R.styleable.CircleWheelView_wheel_item_text_size,
                getResources().getDimensionPixelSize(R.dimen.WheelItemTextSize_Default));
        mItemAlign = a.getInt(R.styleable.CircleWheelView_wheel_item_align, ALIGN_CENTER);
        mItemTextColor = a.getColor(R.styleable.CircleWheelView_wheel_item_text_color, 0xFF888888);
        mSelectedItemTextColor = a.getColor(R.styleable.CircleWheelView_wheel_selected_item_text_color, 0xFF888899);
        mTurnToCenterX = a.getInt(R.styleable.CircleWheelView_wheel_turn_to_centerx, 0);             //转向中间,偏移的距离
        isCyclic = a.getBoolean(R.styleable.CircleWheelView_wheel_cyclic, false);
        isLoopDisplay = a.getBoolean(R.styleable.CircleWheelView_wheel_loop_display, false);
        a.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mRectDrawn = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 计算原始内容尺寸
        int resultWidth = mTextMaxWidth;
        int resultHeight = mTextMaxHeight * mVisibleCount;

        // 考虑内边距对尺寸的影响
        resultWidth += getPaddingLeft() + getPaddingRight();
        resultHeight += getPaddingTop();

        // 考虑父容器对尺寸的影响
        resultWidth = measureSize(modeWidth, sizeWidth, resultWidth);
        resultHeight = measureSize(modeHeight, sizeHeight, resultHeight);
        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 获取内容区域中心坐标
        mRectDrawn.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        mWheelCenterX = mRectDrawn.centerX();
        mWheelCenterY = mRectDrawn.centerY();
        computeDrawnCenter();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataCC.size() > 0) {
            mPaint.setStrokeWidth(1);
            mPaint.setSubpixelText(true);                                                               //设置该项为true，将有助于文本在LCD屏幕上的显示效果
            canvas.save();
            int indexBottom = 0;
            float distanceX = 0, mDrawnItemCenterY = 0;
            int endIndex = isCyclic ? 13 : Math.min(mDefaultVisibleNum, mDataCC.size());
            if (endIndex > mData.size())
                endIndex = mData.size();
            for (int i = 0; i < endIndex; i++) {
                if (i == 0) mPaint.setColor(mSelectedItemTextColor);
                else mPaint.setColor(mItemTextColor);
                if (isCyclic) {
                    if (i < mVisibleHalfNum)
                        indexBottom = i;
                    else
                        indexBottom = i - mVisibleHalfNum + 1;

                    if (i < mVisibleHalfNum)
                        mDrawnItemCenterY = (int) (mDrawnCenterY - (mItemHeight * rates[indexBottom]));
                    else
                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]));
                    if (i == endIndex - 1)
                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]) - 10);
                } else if (mData.size() < 13) {
                    if (i < mVisibleHalfNum + 1)
                        indexBottom = i;
                    else
                        indexBottom = i - mVisibleHalfNum;

                    if (i < mVisibleHalfNum + 1)
                        mDrawnItemCenterY = (int) (mDrawnCenterY - (mItemHeight * rates[indexBottom]));
                    else
                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]));
                    if (i == endIndex - 1)
                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]) - 10);
//                    if (i < mVisibleHalfNum)
//                        indexBottom = i;
//                    else
//                        indexBottom = i - mVisibleHalfNum + 1;
//
//                    if (i < mVisibleHalfNum)
//                        mDrawnItemCenterY = (int) (mDrawnCenterY - (mItemHeight * rates[indexBottom]));
//                    else
//                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]));
//                    if (i == endIndex - 1)
//                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]) - 10);
                } else {
                    if (i < mVisibleHalfNum)
                        indexBottom = i;
                    else
                        indexBottom = i - mVisibleHalfNum + 1;

//                    LogUtil.e(TAG, "indexBottom:" + indexBottom + ",mVisibleHalfNum:" + mVisibleHalfNum + ",i:" + i);
                    if (indexBottom >= mDefaultHalfNum)
                        continue;
                    if (i < mVisibleHalfNum)
                        mDrawnItemCenterY = (int) (mDrawnCenterY - (mItemHeight * rates[indexBottom]));
                    else
                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]));
                    if (i == endIndex - 1)
                        mDrawnItemCenterY = (int) (mDrawnCenterY + (mItemHeight * rates[indexBottom]) - 10);
                }


                mPaint.setTextSize(mItemTextSize * textSizeRates[indexBottom]);
                paintText(canvas, distanceX, rates, mDrawnItemCenterY, indexBottom, i);
            }
        }
    }

    private void paintText(Canvas canvas, float distanceX, float[] rates, float drawnCenterY, int indexBottom, int i) {
        float mDistanceSubX = mDrawnCenterX - mTurnToCenterX * rates[indexBottom] * rates[indexBottom];
        float mDistanceAddX = mDrawnCenterX + mTurnToCenterX * rates[indexBottom] * rates[indexBottom];
        switch (mItemAlign) {
            case ALIGN_CENTER:
                distanceX = mDrawnCenterX;
                break;
            case ALIGN_LEFT:
                distanceX = mData.get(i).toString().length() <= 1 ?
                        mDistanceSubX + 4 * (mVisibleHalfNum - indexBottom) : mDistanceSubX;
                break;
            case ALIGN_RIGHT:
                distanceX = mData.get(i).toString().length() <= 1 ?
                        mDistanceAddX - 4 * (mVisibleHalfNum - indexBottom) : mDistanceAddX;
                break;
        }
        String text = "";
        if (!isLoopDisplay && !isCyclic && mDataSize > mDefaultHalfNum) {
            if (mCurrentItemPosition < mDefaultHalfNum - 1 && (i >= mDefaultHalfNum || i <= mCurrentItemPosition)) {
                text = String.valueOf(mData.get(i));
            } else if (mCurrentItemPosition > mDefaultHalfNum - 1 && i < mDefaultHalfNum + (mDataSize - mCurrentItemPosition) - 1)
                text = String.valueOf(mData.get(i));
            else if (mCurrentItemPosition == mDefaultHalfNum - 1)
                text = String.valueOf(mData.get(i));
        } else if (isCyclic && !isLoopDisplay) {
            if (mCurrentItemPosition < mDefaultHalfNum - 1 && ((i >= mDefaultHalfNum && i < mDataCC.size() - mCurrentItemPosition - 1 + mDefaultHalfNum) || i <= mCurrentItemPosition)) {
                text = String.valueOf(mData.get(i));
            } else if (mCurrentItemPosition > mDefaultHalfNum - 1 && i < mDefaultHalfNum + (mDataCC.size() - mCurrentItemPosition) - 1)
                text = String.valueOf(mData.get(i));
            else if (mCurrentItemPosition == mDefaultHalfNum - 1 && i < mDataCC.size())
                text = String.valueOf(mData.get(i));
        } else {
            text = String.valueOf(mData.get(i));
//            LogUtil.i(TAG, "text:" + mData.get(i) + ",i:" + i);
        }
        canvas.drawText(text, distanceX, drawnCenterY, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastPointY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = event.getY() - mLastPointY;
                if (Math.abs(moveY) < 1) break;
                if (mDataCC.size() < mDefaultHalfNum)
                    break;
                if (!isLoopDisplay) {
                    if (mCurrentItemPosition == 0 && moveY > 0)
                        break;
                    if (isCyclic && mDataCC.size() < mDefaultVisibleNum && mCurrentItemPosition == mDataCC.size() - 1 && moveY < 0)
                        break;
                    else if (mCurrentItemPosition == mDataSize - 1 && moveY < 0)
                        break;
                }

                changeMoveItemPosition(event, mItemHeight * 3 / 2);
                break;
            case MotionEvent.ACTION_UP:
                if (mDataCC.size() < mDefaultHalfNum) {
                    changeMoveItemPosition(event, mItemHeight / 2);
                }

                if (null != mOnItemSelectedListener && mCurrentItemPosition < mDataCC.size() && mDataCC.size() > 0 && mCurrentItemPosition >= 0)
                    mOnItemSelectedListener.onItemSelected(this, mDataCC.get(mCurrentItemPosition), mCurrentItemPosition);
                break;
        }
        return true;
    }

    private void changeMoveItemPosition(MotionEvent event, int moveHeight) {
        if (Math.abs(moveY) > moveHeight) {
            mLastPointY = (int) event.getY();
            if (moveY > 0)
                mCurrentItemPosition--;
            else
                mCurrentItemPosition++;

            if (mCurrentItemPosition > mDataCC.size() - 1)
                mCurrentItemPosition -= mDataCC.size();
            if (mCurrentItemPosition < 0)
                mCurrentItemPosition += mDataCC.size();
            if (mCurrentItemPosition >= 0 && mCurrentItemPosition < mDataCC.size())
                setSelectedItemPosition(mDataCC, mCurrentItemPosition);
        }
    }

    private void computeTextSize() {
        mTextMaxWidth = mTextMaxHeight = 0;
        for (Object obj : mData) {
            String text = String.valueOf(obj);
            int width = (int) mPaint.measureText(text);
            mTextMaxWidth = Math.max(mTextMaxWidth, width);
        }
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        mTextMaxHeight = (int) (metrics.bottom - metrics.top);
    }

    private int measureSize(int mode, int sizeExpect, int sizeActual) {
        int realSize;
        if (mode == MeasureSpec.EXACTLY) {
            realSize = sizeExpect;
        } else {
            realSize = sizeActual;
            if (mode == MeasureSpec.AT_MOST)
                realSize = Math.min(realSize, sizeExpect);
        }
        return realSize;
    }

    //固定的等比高度
    private void computeDrawnCenter() {
        int num = 7;
        mItemHeight = mRectDrawn.height() / (num * 2 - 2);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mDrawnCenterX = mWheelCenterX;
        mDrawnCenterY = mWheelCenterY + mItemHeight / num;
    }

    public void updateVisibleItemCount(int num) {
        if (isCyclic) {
            mVisibleCount = mDefaultVisibleNum = 13;
            mVisibleHalfNum = mDefaultHalfNum = 7;
        } else {
            if (num >= mDefaultHalfNum)
                mVisibleHalfNum = mDefaultHalfNum;
            else {
                mVisibleHalfNum = num % 2 == 1 ? num / 2 + 1 : num / 2;
            }
        }

//        LogUtil.e(TAG, "mVisibleHalfNum:" + mVisibleHalfNum);
        mVisibleCount = Math.min(mDefaultVisibleNum, num);
        mDataSize = isCyclic ? mDefaultVisibleNum : num;
    }

    public void setData(List mData, int pos) {
        setData(mData, pos, true);
    }

    public void setData(List mData, int pos, boolean isLoopDisplay) {
        setData(mData, pos, isLoopDisplay, false, 13);
    }

    public void setData(List mData, int pos, boolean isLoopDisplay, boolean isCycle) {
        setData(mData, pos, isLoopDisplay, isCycle, 13);
    }

    /**
     * @param mData          传入的展示列表数据
     * @param pos            当前view的中间item展示的内容
     * @param isLoopDisplay  所有的数据是否是球形展示,即永远的头尾衔接,即item由mDataSize - 1下滑变为0,默认true
     * @param isCycle        数据是否循环展示  针对list的数目小于mDefaultVisibleNum. 默认false
     * @param visibleShowNum 当前界面展示的item个数
     */
    public void setData(List mData, int pos, boolean isLoopDisplay, boolean isCycle, int visibleShowNum) {
        if (mData.size() <= 0 || pos < 0 || pos >= mData.size())
            return;
        if (visibleShowNum % 2 == 1)
            visibleShowNum++;
        if (mData.size() > 12 && isCycle)
            isCycle = false;
        else if (mData.size() < 13 && !isCycle && !isLoopDisplay) {
            isCycle = true;
        }

        mDefaultVisibleNum = mData.size() < 13 && !isCycle ? mData.size() : 13;
        mDefaultHalfNum = mDefaultVisibleNum < 13 ? mDefaultVisibleNum / 2 : 7;
        this.isLoopDisplay = isLoopDisplay;
        this.isCyclic = isCycle;

        if (this.mDataCC.size() > 0)
            this.mDataCC.clear();
        this.mDataCC.addAll(mData);
        this.mCurrentItemPosition = pos;

        updateVisibleItemCount(mData.size());
        setSelectedItemPosition(mData, pos);
        updateRates();
        computeTextSize();
        requestLayout();
        invalidate();
    }

    public void updateDataPos(int pos) {
        if (pos < 0)
            return;
        if (pos >= mDataCC.size())
            pos = mDataCC.size() / 2 - 1;
        this.mCurrentItemPosition = pos;
        setSelectedItemPosition(mDataCC, pos);
        invalidate();
    }

    private void updateRates() {
        int num = 7;
        if (null == rates)
            rates = new float[num];
        if (null == textSizeRates)
            textSizeRates = new float[num];

        float rate = 0.0f;
        for (int i = 0; i < num; i++) {
            if (i > 0) {
                rate = (3 * (11 * i - (i - 1) * (i - 1))) / 22f;
                if (i == num - 1)
                    rate = (3 * (11 * i - (i - 1) * (i - 1)) + 4) / 22f;
            }
            rates[i] = rate;
            textSizeRates[i] = ((18 - 7 * i / 3) / 13f);
        }
    }

    public void setItemAlign(int align) {
        this.mItemAlign = align;
        invalidate();
    }

    public void setTurnToCenter(int turnToCenterX) {
        this.mTurnToCenterX = turnToCenterX;
        requestLayout();
        invalidate();
    }

    public void setSelectedItemPosition(List mDatas, int centerPos) {
        if (mData.size() > 0)
            mData.clear();
        mDataSize = isCyclic ? mDefaultVisibleNum : mDatas.size();
        int halfIndex = isCyclic ? 7 : Math.min(mVisibleHalfNum, mDefaultHalfNum);
//        LogUtil.i(TAG, "endIndex:" + endIndex + ",topPos:" + topPos + ",centerPos:" + centerPos + ",bottomPos:" + bottomPos + ",halfIndex:" + halfIndex);
//        LogUtil.i(TAG, "endIndex:" + endIndex + ",mVisibleHalfNum:" + mVisibleHalfNum + ",mDefaultHalfNum:" + mDefaultHalfNum + ",mDefaultVisibleNum:" + mDefaultVisibleNum);
        int bottom = 0;
        mData.add(0, mDatas.get(centerPos));
        if ((isCyclic && mDatas.size() < 13) || mDatas.size() >= 13) {
            for (int i = 1; i < halfIndex; i++) {
                bottom = centerPos - i;
                bottom = chooseIndex(bottom, mDatas.size());
                mData.add(mDatas.get(bottom));
            }

            for (int i = mDatas.size() - 1; i >= halfIndex; i--) {
                bottom = centerPos + mDatas.size() - i;
                bottom = chooseIndex(bottom, mDatas.size());
                mData.add(mDatas.get(bottom));
            }

            if (isCyclic)
                for (int i = mDatas.size(); i < 13; i++) {
                    bottom = i - halfIndex + 1 + centerPos;
                    bottom = chooseIndex(bottom, mDatas.size());
//                    LogUtil.e(TAG, "bottom:" + bottom + ",:" + mDatas.get(bottom));
                    mData.add(mDatas.get(bottom));
                }

        } else if (mDatas.size() < 13) {
            //下面个数比上面多
            for (int i = 1; i <= halfIndex; i++) {
                bottom = centerPos - i;
                bottom = chooseIndex(bottom, mDatas.size());
                mData.add(mDatas.get(bottom));
            }

            for (int i = mDatas.size() - 1; i > halfIndex; i--) {
                bottom = centerPos + mDatas.size() - i;
                bottom = chooseIndex(bottom, mDatas.size());
                mData.add(mDatas.get(bottom));
            }
            //上面个数比下面多
//                if (mDatas.size() % 2 == 0) {
//                    for (int i = 1; i < halfIndex; i++) {
//                        dexx = centerPos - i;
//                        dexx = chooseIndex(dexx, mDatas.size());
//                        mData.add(mDatas.get(dexx));
//                    }
//
//                    for (int i = mDatas.size() - 1; i >= halfIndex; i--) {
//                        bottom = centerPos + mDatas.size() - i;
//                        bottom = chooseIndex(bottom, mDatas.size());
//                        mData.add(mDatas.get(bottom));
//                    }
//                } else {
//
//                }
        }
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.mItemTextSize = textSize;
        invalidate();
    }

    public void setTextSelectColor(int colorRes) {
        this.mSelectedItemTextColor = colorRes;
        requestLayout();
        invalidate();
    }

    public int chooseIndex(int index, int dataSize) {
        if (index > dataSize - 1)
            index -= dataSize;
        if (index < 0)
            index += dataSize;
        return index;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }

    public int getListSize() {
        if (null != mDataCC)
            return mDataCC.size();
        return 0;
    }

    /**
     * 滚轮选择器Item项被选中时监听接口
     */
    public interface OnItemSelectedListener {
        void onItemSelected(CircleWheelView view, Object data, int position);
    }
}
