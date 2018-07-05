package com.mmall.util;

import java.math.BigDecimal;


/**
 * Created by 10353 on 2018/1/7.
 * 高精度浮点数计算
 */
public class BigDecimalUtil {

    public static BigDecimal add(Double a, Double b){
        BigDecimal aStr = new BigDecimal(Double.toString(a));
        BigDecimal bStr = new BigDecimal(Double.toString(b));
        return aStr.add(bStr);
    }

    public static BigDecimal sub(Double a, Double b){
        BigDecimal aStr = new BigDecimal(Double.toString(a));
        BigDecimal bStr = new BigDecimal(Double.toString(b));
        return aStr.subtract(bStr);
    }

    public static BigDecimal mul(Double a, Double b){
        BigDecimal aStr = new BigDecimal(Double.toString(a));
        BigDecimal bStr = new BigDecimal(Double.toString(b));
        return aStr.multiply(bStr);
    }

    public static BigDecimal div(Double v1,Double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数

        //除不尽的情况
    }
}
