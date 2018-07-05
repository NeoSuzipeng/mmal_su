package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 10353 on 2018/7/4.
 *
 * mmall商城订单管理
 */

@Controller
@RequestMapping(value = "/order/")
public class OrderController {


    @Autowired
    private IOrderService iOrderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * 订单支付
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping(value = "pay.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        //获取图片储存地址
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId() ,orderNo, path);
    }

    /**
     * 支付宝回调函数
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping(value = "alipay_callback.do",method = RequestMethod.POST)
    @ResponseBody
    public Object alipayCallBack(HttpSession session, Long orderNo, HttpServletRequest request){

        Map<String, String> parameters = Maps.newHashMap();
        //回调参数重组
        Map requestParameters = request.getParameterMap();
        for (Iterator iterator = requestParameters.keySet().iterator(); iterator.hasNext();){
            String name = (String)iterator.next();
            String[]  values = (String [])requestParameters.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++){
                //拼接技巧
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            parameters.put(name,valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",parameters.get("sign"),parameters.get("trade_status"),parameters.toString());

        //验证回调正确性
        //第一步： 在通知返回参数列表中，除去sign、sign_type两个参数外，凡是通知返回回来的参数皆是待验签的参数
        parameters.remove("trade_status");
        //第二步：利用支付宝提供的API进行验签
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(parameters, Configs.getAlipayPublicKey(), "utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2){
                return ServerResponse.createByErrorMessage("非法请求");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //todo 验证数据正确性

        //判断回调是否通过，并给支付宝发送通知
        ServerResponse serverResponse = iOrderService.alipayCallBack(parameters);
        if (serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 前端轮询查询订单支付状态
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "query_order_pay_status.do", method = RequestMethod.POST)
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse =  iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }

}
