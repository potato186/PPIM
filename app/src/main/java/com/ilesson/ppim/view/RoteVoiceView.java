package com.ilesson.ppim.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
public class RoteVoiceView extends DragView implements Runnable {
        Bitmap bitmap = null;
    int bitmapWidth = 0;
    int bitmapHeight = 0;
    float angle = 0.0f;
    Matrix matrix = new Matrix();

    public RoteVoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoteVoiceView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        //获取图像资源
        BitmapDrawable drawable = (BitmapDrawable) getBackground();

        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        new Thread(this).start();
    }

    public void run() {
        // TODO Auto-generated method stub
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: handle exception
                Thread.currentThread().interrupt();
            }
            angle+=10;
            if(angle>360){
                angle=0;
            }
            postInvalidate(); //可以直接在线程中更新界面
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        matrix.reset();
        matrix.setRotate(angle); //设置旋转
        //按照matrix的旋转构建新的Bitmap
//        Bitmap bitmapcute = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
//        //绘制旋转之后的图像
//        RoteVoiceView.DrawImage(canvas, bitmapcute, (320 - bitmapWidth) / 2, 10);
//        bitmapcute = null;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // TODO Auto-generated method stub
//        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
//            angle--;
//        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//            angle++;
//        }
//        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * 绘制一个Bitmap
     * canvas  画布
     * bitmap  图片
     * x      屏幕上的x坐标
     * y      屏幕上的y坐标
     */
    public static void DrawImage(Canvas canvas, Bitmap _bitmap, int x, int y) {
        /* 绘制图像 */
        canvas.drawBitmap(_bitmap, x, y, null);
    }
}

