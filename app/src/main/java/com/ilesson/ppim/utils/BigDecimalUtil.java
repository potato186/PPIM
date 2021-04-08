package com.ilesson.ppim.utils;



import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * **********************************************************
 *
 * @Project:  金融BigDecimal数据类型的工具类
 * @ver : version 1.0
 * @Date : 2019-10-14 08:22
 * @description:
 ************************************************************/
public class BigDecimalUtil {


    private BigDecimalUtil() {

    }

    public static BigDecimal add(double v1, double v2) {// v1 + v2
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }
    public static String format(double v1) {
        DecimalFormat decimalFormat = new DecimalFormat("###################.##");
        String res = decimalFormat.format(v1).replace(".00","");
        if(res.contains(".")){
            int index = res.length()-res.indexOf(".");
            if(index<3) {
                res+="0";
            }
        }else{
            res+=".00";
        }
        return res;
    }

    public static BigDecimal div(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        // 2 = 保留小数点后两位   ROUND_HALF_UP = 四舍五入
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);// 应对除不尽的情况
    }

}


