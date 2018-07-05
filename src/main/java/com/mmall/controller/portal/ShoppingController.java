package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by 10353 on 2018/7/3.
 * mmall商城收货地址管理
 *
 * 注意：收货地址属于用户隐私，横向越权问题是关键
 */

@Controller
@RequestMapping(value = "/shipping/")
public class ShoppingController {

    @Autowired
    private IShippingService iShoppingService;

    /**
     * 添加收货地址
     * 添加成功后将ShippingId传给前端
     * 使用SpringMVC对象绑定
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShoppingService.add(user.getId(),shipping);
    }


    /**
     * 删除收货地址
     * 注意：避免用户随意删除收货地址（横向越权）
     * 手段：ShippingId与UserId关联进行操作
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "del.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse del(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShoppingService.del(user.getId(),shippingId);
    }

    /**
     * 更新收货地址
     * 注意：横向越权（前端传入的Shipping中可能存在UserId而原生的SQL中并不判断是否是登录用户的UserId,这样就会给恶意用户模仿他人的机会）
     * 手段：
     * 1)重覆盖shipping对象中的ShippingId
     * 2)重写原生SQL
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping(value = "update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upadte(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShoppingService.update(user.getId(),shipping);
    }


    /**
     * 查询收货地址详情
     * 注意：横向越权
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse select(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShoppingService.select(user.getId(),shippingId);
    }


    /**
     * 分页查询收货地址列表
     * 注意：横向越权
     * @param pageNum
     * @param pageSize
     * @param session
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse select(@RequestParam(value = "pageNum" ,defaultValue = "1" ) int pageNum,
                                 @RequestParam(value = "pageSize" ,defaultValue = "10" ) int pageSize,
                                 HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShoppingService.selectList(user.getId(),pageNum,pageSize);
    }

}
