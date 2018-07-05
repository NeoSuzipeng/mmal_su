package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * Created by 10353 on 2018/1/7.
 */
public interface ICartService {

    ServerResponse<CartVo> addCartItem(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> getCart(Integer userId);

    ServerResponse<CartVo> deleteProductFromCart(Integer userId, String productIds);

    ServerResponse<CartVo> updateProductFromCart(Integer userId, Integer count, Integer productId);


    ServerResponse<CartVo> selectOrUnselectProductFromCart(Integer userId, Integer productId, Integer checkStatus);

    ServerResponse<Integer> selectProductCount(Integer userId);
}
