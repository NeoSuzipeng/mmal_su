package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by 10353 on 2018/1/4.
 */
public class TokenCache {

    private static final Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public  static final String TOKEN_PREFIX = "token_";

    private static LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder().initialCapacity(1000)
            .maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key, String value){
        loadingCache.put(key, value);
    }

    public static String getKey(String key){
        String value = null;
        try{
            value = loadingCache.get(key);
            if("null".equals(value))
                return null;
            return value;
        }catch (Exception e){
            logger.error("loading ger error :", e.getMessage());
        }
        return null;
    }
}
