package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by 10353 on 2018/1/7.
 *
 */
@Service("iCartService")
public class ICartServiceImpl implements ICartService{

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    @Override
    @Transactional
    public ServerResponse<CartVo> addCartItem(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null)
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "添加购物车失败");
        //判断数据库中是否已有此商品信息
        Cart cart = null;
        cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(cart == null){
            //添加信息
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setChecked(Const.Cart.CHECKED);
            cart.setQuantity(count);
            cart.setCreateTime(new Date());
            cart.setUpdateTime(new Date());
            int resultCount = cartMapper.insert(cart);
            if(resultCount <= 0)
                return ServerResponse.createByErrorMessage("添加购物车失败");
        }else{
            //更新数量
            int newCount = cart.getQuantity() + count;
            cart.setQuantity(newCount);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.getCart(userId);
    }

    @Override
    public ServerResponse<CartVo> getCart(Integer userId) {
        CartVo cartVo = this.getCartVo(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    @Transactional
    public ServerResponse<CartVo> deleteProductFromCart(Integer userId, String productIds) {
        if(StringUtils.isBlank(productIds))
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        cartMapper.deleteByUserIdAndProductIdList(userId, productIdList);
        return this.getCart(userId);
    }

    @Override
    @Transactional
    public ServerResponse<CartVo> updateProductFromCart(Integer userId, Integer count, Integer productId) {
        if(count == null || productId == null)
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(cart != null){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.getCart(userId);
    }

    @Override
    @Transactional
    public ServerResponse<CartVo> selectOrUnselectProductFromCart(Integer userId, Integer productId, Integer checkStatus) {
        cartMapper.changeCheckStatus(userId, productId, checkStatus);
        return this.getCart(userId);
    }

    @Override
    public ServerResponse<Integer> selectProductCount(Integer userId) {
        Integer count = cartMapper.selectProductCount(userId);
        return ServerResponse.createBySuccess(count);
    }


    /**
     * 高复用购物车封装对象
     * @param userId
     * @return
     */
    private CartVo getCartVo(Integer userId){
        //查找用户购物车信息
        List<Cart> carts = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVos = Lists.newArrayList();
        CartVo cartVo = new CartVo();
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(carts)){
            CartProductVo cartProductVo = new CartProductVo();
            for(Cart cartItem : carts){
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());


                //填充商品信息
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    cartProductVo.setProductChecked(cartItem.getChecked());
                    //检查选中数量是否大于库存
                    int buyCount = 0;
                    if(product.getStock() >=  cartItem.getQuantity()){
                        buyCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_TOTAL);
                    }else{
                        cartProductVo.setLimitQuantity(Const.Cart.UN_LIMIT_TOTAL);
                        //如果超过库存将数量置为库存数量
                        buyCount = product.getStock();
                        cartProductVo.setQuantity(buyCount);
                        //更新数据库信息
                        Cart updateCart = new Cart();
                        updateCart.setId(cartItem.getId());
                        updateCart.setQuantity(buyCount);
                        cartMapper.updateByPrimaryKeySelective(updateCart);
                    }
                    cartProductVo.setQuantity(buyCount);
                    //计算单条购物车商品总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(cartProductVo.getProductPrice().doubleValue(),
                            cartProductVo.getQuantity().doubleValue()));
                }
                //计算购物车中商品总价
                if(cartProductVo.getProductChecked() == Const.Cart.CHECKED)
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
            }
            cartProductVos.add(cartProductVo);
        }
        cartVo.setCartProductVoList(cartProductVos);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://106.14.0.93/"));
        cartVo.setAllChecked(checkAllCartStatus(userId));
        return cartVo;

    }

    private boolean checkAllCartStatus(Integer userId) {
        if(userId == null)
            return false;
        return cartMapper.selectAllCheckedCart() == 0;
    }


}
