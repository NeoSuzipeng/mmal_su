package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by 10353 on 2018/1/5.
 *
 */
public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties properties;

    static{
        String fileName = "mmall.properties";
        properties = new Properties();
        try {
            properties.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("properties 装配失败");
        }
    }

    public static String getProperty(String key){
        String value = properties.getProperty(key.trim()).trim();
        if(StringUtils.isBlank(value))
            return null;
        return value;
    }

    public static String getProperty(String key, String defaultValue){
        String value = properties.getProperty(key.trim()).trim();
        if(StringUtils.isBlank(value))
            return defaultValue;
        return value;
    }
    public static void main(String[] args){
        System.out.print(PropertiesUtil.getProperty("alipay.callback.url"));
    }
}
