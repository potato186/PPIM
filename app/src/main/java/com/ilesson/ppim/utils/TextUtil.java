package com.ilesson.ppim.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.ilesson.ppim.R;

public class TextUtil {

    public static SpannableStringBuilder getFei(Context context,int price){
        String text = String.format(context.getResources().getString(R.string.all_fee),BigDecimalUtil.format(Double.valueOf((double)price/100))+"");
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.gray_text333_color)), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        style.setSpan(new ForegroundColorSpan(Color.RED), 3, text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return style;
    }
    public static SpannableStringBuilder getSpecialText(Context context,String key,String value){
        String text = key+value;
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.gray_text333_color)), 0, key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.gray_text_color)), key.length(), text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return style;
    }

}
