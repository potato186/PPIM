package com.ilesson.ppim.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ilesson.ppim.utils.PPScreenUtils;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int space;  //位移间距
    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) %3 == 0) {
            outRect.left = 0; //第一列左边贴边
        } else {
            if (parent.getChildAdapterPosition(view) %3 == 1) {
                outRect.left = space;//第二列移动一个位移间距
            } else {
                outRect.left = space * 2;//由于第二列已经移动了一个间距，所以第三列要移动两个位移间距就能右边贴边，且item间距相等
            }
        }

        if (parent.getChildAdapterPosition(view) >= 3) {
            outRect.top = PPScreenUtils.dip2px(view.getContext(), 10);
        } else {
            outRect.top = 0;
        }
    }

}