package com.ilesson.ppim.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.ilesson.ppim.utils.PPScreenUtils;

/**
 * FileName: DragButton
 * Author: Administrators
 * Time: 2019/2/23 9:34
 * Desc: TODO
 */
public class DragFloatActionButton extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "DragButton";
    private int parentHeight;
    private int parentWidth;

    private int lastX;
    private int lastY;

    private boolean isDrag;
    private ViewGroup parent;

    public DragFloatActionButton(Context context) {
        super(context);
    }

    public DragFloatActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragFloatActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isDrag = false;
                this.setAlpha(0.9f);
                setPressed(true);
                getParent().requestDisallowInterceptTouchEvent(true);
                lastX = rawX;
                lastY = rawY;
                if (getParent() != null) {
                    parent = (ViewGroup) getParent();
                    parentHeight = parent.getHeight();
                    parentWidth = parent.getWidth();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                this.setAlpha(0.9f);
                int dx = rawX - lastX;
                int dy = rawY - lastY;
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                if (distance > 2 && !isDrag) {
                    isDrag = true;
                }

                float x = getX() + dx;
                float y = getY() + dy;
                //检测是否到达边缘 左上右下
                x = x < 0 ? 0 : x > parentWidth - getWidth() ? parentWidth - getWidth() : x;
                y = getY() < 0 ? 0 : getY() + getHeight() > parentHeight ? parentHeight - getHeight() : y;
                setX(x);
                setY(y);
                lastX = rawX;
                lastY = rawY;
                break;
            case MotionEvent.ACTION_UP:
                if (isDrag) {
                    //恢复按压效果
                    setPressed(false);
                    moveHide(rawX);
                }
                break;
        }
        //如果是拖拽则消耗事件，否则正常传递即可。
        return isDrag || super.onTouchEvent(event);
    }


    private void moveHide(int rawX) {
        if (rawX >= parentWidth / 2) {
            //靠右吸附
            animate().setInterpolator(new DecelerateInterpolator())
                    .setDuration(500)
                    //.xBy(parentWidth - getWidth() - getX())
                    .xBy(parentWidth - getWidth() - getX() - PPScreenUtils.dip2px(getContext(), 20))
                    .start();
        } else {
            //靠左吸附
            //ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), 0);
            ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(),
                    PPScreenUtils.dip2px(getContext(), 20));
            oa.setInterpolator(new DecelerateInterpolator());
            oa.setDuration(500);
            oa.start();

        }
    }
}