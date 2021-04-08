package com.ilesson.ppim.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import com.ilesson.ppim.R;

/**
 * Des:
 * Created by kele on 2020/9/30.
 * E-mail:984127585@qq.com
 */
public class RaeSeekBar extends AppCompatSeekBar {

    //  刻度说明文本，数组数量跟刻度数量一致，跟mTextSize的长度要一致
    private String[] mTickMarkTitles = new String[]{
            "A",
            "标准",
            "",
            "A"
    };
    // 刻度代表的字体大小
    private float[] mTextSize = new float[]{
            0.8f,
            1.0f,
            1.15f,
            1.3f
    };

    // 刻度画笔
    private final Paint mTickMarkTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    //文本画笔
    private final Paint mTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    //  刻度文本字体大小
    private float mTickMarkTitleTextSize = 18;
    // 刻度文本跟刻度之间的间隔
    private float mOffsetY = 40;
    // 刻度线的高度
    private int mLineHeight = 10;
    // 保存位置大小信息
    private final Rect mRect = new Rect();
    private int mThumbHeight;
    private int mThumbWidth;

    public RaeSeekBar(Context context) {
        this(context, null);
    }

    public RaeSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RaeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        mTickMarkTitleTextSize = getSize(mTickMarkTitleTextSize);
        mOffsetY = getSize(mOffsetY);
        mLineHeight = getSize(mLineHeight);
        mTickMarkTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTickMarkTitlePaint.setColor(ContextCompat.getColor(getContext(), R.color.color_66989FC3));
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setColor(ContextCompat.getColor(getContext(), R.color.color_303132));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int maxLength = getMax();
        int width = getWidth();
        int height = getHeight();
        int h2 = height / 2; // 居中

        // 画刻度背景
        mRect.left = getPaddingLeft();
        mRect.right = width - getPaddingRight();
        mRect.top = h2 - getSize(1); // 居中
        mRect.bottom = mRect.top + getSize(1.5f); // 1.5f为直线的高度
        // 直线的长度
        int lineWidth = mRect.width();
        // 画直线
        canvas.drawRect(mRect, mTickMarkTitlePaint);

        //  遍历刻度，画分割线和刻度文本
        for (int i = 0; i <= maxLength-1; i++) {

            // 刻度的起始间隔 = 左间距 + (线条的宽度 * 当前刻度位置 / 刻度长度)
            int thumbPos = getPaddingLeft() + (lineWidth * i / maxLength);
            Log.d(TAG, "onDraw: thumbPos="+thumbPos);
            Log.d(TAG, "onDraw: i="+i);
            // 画分割线
            mRect.top = h2 - mLineHeight / 2;
            mRect.bottom = h2 + mLineHeight / 2;
            mRect.left = thumbPos;
            mRect.right = thumbPos + getSize(1.5f); // 直线的宽度为1.5
            canvas.drawRect(mRect, mTickMarkTitlePaint);

            // 画刻度文本
            String title = mTickMarkTitles[i/25]; // 拿到刻度文本
            mTickMarkTitlePaint.getTextBounds(title, 0, title.length(), mRect); // 计算刻度文本的大小以及位置
            mTickMarkTitlePaint.setTextSize(getSize(mTextSize[i/25])); // 设置刻度文字大小
            // 画文本
            canvas.drawText(title, thumbPos, getSize(mTextSize[mTextSize.length - 1]), mTickMarkTitlePaint);
        }
    }

    private static final String TAG = "RaeSeekBar";
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mThumbWidth = getThumb().getIntrinsicWidth();
        mThumbHeight = getThumb().getIntrinsicHeight();
        // 加上字体大小
        int wm = MeasureSpec.getMode(widthMeasureSpec);
        int hm = MeasureSpec.getMode(heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        h += mTextSize[mTextSize.length - 1] * mTickMarkTitleTextSize; // 最大的字体
        h += mOffsetY;
        // 保存
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(w, wm), MeasureSpec.makeMeasureSpec(h, hm));

    }

    protected int getSize(float size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources().getDisplayMetrics());
    }

    public float getRawTextSize(int progress) {
        return mTextSize[progress % mTextSize.length];
    }

    public void setTextSize(float size) {
        for (int i = 0; i < mTextSize.length; i++) {
            float textSize = mTextSize[i];
            if (textSize == size) {
                setProgress(i);
                break;
            }
        }
    }
}