package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

/**
 * Created by 10353 on 2018/7/4.
 * 订单服务接口
 */
public interface IOrderService {

    ServerResponse pay(Integer userId, Long orderNo, String path);

    ServerResponse alipayCallBack(Map<String, String> params);

    ServerResponse<Boolean> queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse create(Integer userId, Integer shippingId);

    ServerResponse cancel(Integer userId, Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse getOrderDetail(Integer userId, Long orderNo);

    ServerResponse<PageInfo> getOrderlist(Integer userId, Integer pageSize, Integer pageNum);



    // backend

    ServerResponse<PageInfo> manageList(Integer pageSize, Integer pageNum);

    ServerResponse<OrderVo> manageOrderDetail(Long orderNo);

    ServerResponse<PageInfo> manageOrderSearch(Long orderNo, Integer pageSize, Integer pageNum);

    ServerResponse manageOrderSendGoods(Long orderNo);
}
