package com.mmall.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 10353 on 2018/1/30.
 */
public class AuthorityInterceptor implements HandlerInterceptor
{
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        //拦截器中的Object o为HandlerMethod类型
        HandlerMethod handlerMethod = (HandlerMethod) o;

        //解析handlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        //解析参数
        StringBuilder requestParamMap = new StringBuilder();
        Map parameterMap = httpServletRequest.getParameterMap();
        Iterator it = parameterMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            String mapKey = (String)entry.getKey();
            String mapValue = StringUtils.EMPTY;

            //请求中参数的map的value是一个String数组
            Object obj = entry.getValue();
            if(obj instanceof String[]){
                String[] paramValues = (String[])obj;
                mapValue = Arrays.toString(paramValues);
                requestParamMap.append(mapKey).append("=").append(mapValue);
            }
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
