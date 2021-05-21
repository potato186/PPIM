package com.ilesson.ppim.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.ilesson.ppim.R;

/**
 * Created by Haron on 2017/11/14.
 */

public class GradientColorTextView extends android.support.v7.widget.AppCompatTextView {

    private LinearGradient mLinearGradient;
    private Paint mPaint;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private Rect mTextBound = new Rect();

    public GradientColorTextView (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
        mPaint = getPaint();
        String mTipText = getContext().getResources().getString(R.string.app_name);
        mPaint.getTextBounds(mTipText, 0, mTipText.length(), mTextBound);
        mLinearGradient = new LinearGradient(0, 0, mViewWidth, mViewHeight,
                new int[]{0xeb0c6e, 0x652d8f},
                null, Shader.TileMode.REPEAT);
        mPaint.setShader(mLinearGradient);
        canvas.drawText(mTipText, getMeasuredWidth() / 2 - mTextBound.width() / 2, getMeasuredHeight() / 2 + mTextBound.height() / 2, mPaint);
    }
}
