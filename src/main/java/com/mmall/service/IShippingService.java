package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by 10353 on 2018/7/3.
 *
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse update(Integer id, Shipping shipping);

    ServerResponse select(Integer id, Integer shippingId);

    ServerResponse selectList(Integer id, int pageNum, int pageSize);
}
