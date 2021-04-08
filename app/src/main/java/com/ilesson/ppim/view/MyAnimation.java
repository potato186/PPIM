package com.ilesson.ppim.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Created by lixuce on 2017/4/10.
 */

public class MyAnimation extends Animation {
    int centerX, centerY;
    Camera camera = new Camera();


    @Override
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        //获取中心点坐标
        centerX = width/ 2;
        centerY = height / 2;
        //动画执行时间  自行定义
        setDuration(2500L);
        setInterpolator(new AccelerateInterpolator());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final Matrix matrix = t.getMatrix();
        camera.save();
        //中心是绕Y轴旋转  这里可以自行设置X轴 Y轴 Z轴
        camera.rotateY(1080 * interpolatedTime);
        //把我们的摄像头加在变换矩阵上
        camera.getMatrix(matrix);
        //设置翻转中心点
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
        camera.restore();
    }
}