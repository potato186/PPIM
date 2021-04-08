package com.ilesson.ppim.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MenuListView extends ListView {
    public MenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public MenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuListView(Context context) {
        super(context);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}