package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by 10353 on 2018/1/7.
 * 购物车模块
 * 1.购物车中添加一条商品信息
 * 2.获取购物车内全部信息
 * 3.删除购物车内一条或多条信息
 * 4.更新购物车内一条商品的商品数量
 * 5.单选中购物车的中的一条商品信息
 */

@Controller
@RequestMapping(value = "/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     *购物车中添加一条商品信息
     * @param session
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> addCartItem(HttpSession session, Integer productId, Integer count){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.addCartItem(user.getId(),productId, count);
    }

    /**
     * 获取购物车内全部信息
     * @param session
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> getCartList(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.getCart(user.getId());
    }


    /**
     * 删除购物车内一条或多条信息
     * @param session
     * @param productIds
     * @return
     */
    @RequestMapping(value = "delete_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> deleteProductFromCart(HttpSession session, String productIds){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.deleteProductFromCart(user.getId(),productIds);
    }

    /**
     * 更新购物车内一条商品的商品数量
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> updateProductFromCart(HttpSession session, Integer count, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.updateProductFromCart(user.getId(),count, productId);
    }

    /**
     * 单选中购物车的中的一条商品信息
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "select.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectProductFromCart(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.selectOrUnselectProductFromCart(user.getId(),productId,1);
    }

    /**
     * 取消单选中购物车的中的一条商品信息
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "un_select.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectProductFromCart(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.selectOrUnselectProductFromCart(user.getId(),productId,0);
    }

    /**
     * 单选中购物车的中的一条商品信息
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectAllProductFromCart(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.selectOrUnselectProductFromCart(user.getId(),null,1);
    }

    /**
     * 取消全选中购物车的中的一条商品信息
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "un_select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectAllProductFromCart(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        return iCartService.selectOrUnselectProductFromCart(user.getId(),null,0);
    }

    /**
     * 查询购物车中商品数量
     * @param session
     * @return
     */
    @RequestMapping(value = "get_cart_product_count.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createBySuccess(0);
        return iCartService.selectProductCount(user.getId());
    }

}
