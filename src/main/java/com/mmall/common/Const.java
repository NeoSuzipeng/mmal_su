package com.mmall.common;

import com.google.common.collect.Sets;
import java.util.Set;

/**
 * Created by 10353 on 2018/1/4.
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";

    public static final String USERNAME = "username";

    public static class Role{
        public static final int ROLE_CUSTOMER = 0;
        public static final int ROLE_ADMIN = 1;
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }


    public class Cart{
        public static final int CHECKED = 1;
        public static final int UNCHECKED = 0;

        public static final String LIMIT_TOTAL = "LIMIT_NUM_SUCCESS";
        public static final String UN_LIMIT_TOTAL = "LIMIT_NUM_FAIL";
    }


    public enum ProductStatusEnum{
        ON_SALE(1,"商品在售");

        private int code;

        private String desc;

        ProductStatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");


        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有对应枚举");
        }
    }


    public interface  AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }



    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }



    public enum PaymentTypeEnum{

        ONLINE_PAY(1,"在线支付");

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum codeOf(Integer paymentType) {
            for (PaymentTypeEnum paymentTypeEnum : values()){
                if (paymentTypeEnum.getCode() == paymentType)
                    return paymentTypeEnum;
            }
            throw new RuntimeException("没有对应枚举");
        }
    }
}
