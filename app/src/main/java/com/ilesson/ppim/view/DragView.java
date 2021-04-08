package com.ilesson.ppim.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.lang.reflect.Field;

/**
 *随意拖动的view
 */

@SuppressLint("AppCompatCustomView")
public class DragView extends ImageView {

    private int width;
    private int height;
    private int screenWidth;
    private int screenHeight;
    private Context context;
    private int leftLocation, topLocation;

    public int getLeftLocation() {
        return leftLocation;
    }

    public void setLeftLocation(int leftLocation) {
        this.leftLocation = leftLocation;
    }

    public int getTopLocation() {
        return topLocation;
    }

    public void setTopLocation(int topLocation) {
        this.topLocation = topLocation;
    }

    //是否拖动
    private boolean isDrag=false;

    public boolean isDrag() {
        return isDrag;
    }
    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    public DragView(Context context) {
        super(context);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        return localDisplayMetrics.widthPixels;
    }
    public static int getScreenHeight(Context context) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        return localDisplayMetrics.heightPixels - getStatusBarHeight(context);
    }
    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width=getMeasuredWidth();
        height=getMeasuredHeight();
        screenWidth= getScreenWidth(context);
        screenHeight= getScreenHeight(context)-getStatusBarHeight();

    }
    public int getStatusBarHeight(){
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return getResources().getDimensionPixelSize(resourceId);
    }


    private float downX;
    private float downY;

    public void setLocation(int left,int top){
        this.layout(left,top,left+width,top+height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (this.isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDrag=false;
                    downX = event.getX();
                    downY = event.getY();
                    if(null!=onPressListener&&!isDrag){
                        onPressListener.onPressDown();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.e("kid","ACTION_MOVE");
                    final float xDistance = event.getX() - downX;
                    final float yDistance = event.getY() - downY;
                    int l,r,t,b;
                    //当水平或者垂直滑动距离大于10,才算拖动事件
                    if (Math.abs(xDistance) >30 ||Math.abs(yDistance)>30) {
                        Log.v("kid","Drag");
                        isDrag=true;
                         l = (int) (getLeft() + xDistance);
                         r = l+width;
                         t = (int) (getTop() + yDistance);
                         b = t+height;
                        //不划出边界判断,此处应按照项目实际情况,因为本项目需求移动的位置是手机全屏,
                        // 所以才能这么写,如果是固定区域,要得到父控件的宽高位置后再做处理
                        if(l<0){
                            l=0;
                            r=l+width;
                        }else if(r>screenWidth){
                            r=screenWidth;
                            l=r-width;
                        }
                        if(t<0){
                            t=0;
                            b=t+height;
                        }else if(b>screenHeight){
                            b=screenHeight;
                            t=b-height;
                        }

                        this.layout(l, t, r, b);
                        setLeftLocation(l);
                        setTopLocation(t);
                        if(null!=onLocationListener){
                            onLocationListener.onLocation(l,t);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(null!=onPressListener){
                        onPressListener.onPressUp(isDrag);
                    }
                    setPressed(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    setPressed(false);
                    break;
            }
            return true;
        }
        return false;
    }
    public interface OnLocationListener{
        void onLocation(int l, int t);
    }
    private OnLocationListener onLocationListener;

    public OnLocationListener getOnLocationListener() {
        return onLocationListener;
    }

    public void setOnLocationListener(OnLocationListener onLocationListener) {
        this.onLocationListener = onLocationListener;
    }

    public interface OnPressListener{
        void onPressUp(boolean isDrag);
        void onPressDown();
    }
    private OnPressListener onPressListener;

    public void setOnPressListener(OnPressListener onPressListener) {
        this.onPressListener = onPressListener;
    }
}
