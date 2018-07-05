package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;

import java.util.List;

/**
 * Created by 10353 on 2018/1/5.
 */
public interface IProductService {
    ServerResponse<String> addOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> queryProductDetail(Integer productId);

    ServerResponse<PageInfo> queryProductList(Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> queryProductByIdOrName(Integer pageNum, Integer pageSize, Integer productId, String productName);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy);
}
