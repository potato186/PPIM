package com.ilesson.ppim.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.ilesson.ppim.R;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

public class TextUtil {

    public static SpannableString getKeyWordsColorString(Context context,String source,String keyWords){
        SpannableString spannableString = new SpannableString(source);
        int start = source.indexOf(keyWords);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.theme_color)), start, start+keyWords.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

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

    public static String getPinyin(String text){
        StringBuffer buffer = new StringBuffer(); //储存结果

        //转换函数用到的一些配置
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);  //转小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); //不带音标
        char[] chars = text.toCharArray();
        for(int i = 0; i < chars.length; ++i){
            if(chars[i] > 128){
                try{
                    String[] arr = PinyinHelper.toHanyuPinyinStringArray(chars[i],format);
                    buffer.append(arr[0]);  //转换出的结果包含了多音字，这里简单粗暴的取了第一个拼音。
                    if(i<chars.length-1){
                        buffer.append(" ");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{ //非汉字
                buffer.append(chars[i]);
            }
        }
        return buffer.toString();
    }

    public static String getServerId(String id){
        return id.replace("+","").replace("86_","").replace("852_","").replace("853_","").replace("886_","");
    }
}
