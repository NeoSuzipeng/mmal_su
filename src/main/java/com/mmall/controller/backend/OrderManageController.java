package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by 10353 on 2018/7/7.
 *
 * 后端订单管理
 */

@Controller
@RequestMapping(value = "/manage/")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;


    @Autowired
    private IOrderService iOrderService;

    /**
     * 用户订单列表管理后端接口
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                                         @RequestParam(value = "pageNum" ,defaultValue = "1" ) int pageNum,
                                         @RequestParam(value = "pageSize" ,defaultValue = "10" ) int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //检查管理员权限
        if (!iUserService.checkAdmin(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无操作权限");
        }

        return iOrderService.manageList(pageSize, pageNum);
    }


    /**
     * 用户订单详情管理后端接口
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //检查管理员权限
        if (!iUserService.checkAdmin(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无操作权限");
        }

        return iOrderService.manageOrderDetail(orderNo);
    }


    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpSession session, Long orderNo,
                                               @RequestParam(value = "pageNum" ,defaultValue = "1" ) int pageNum,
                                               @RequestParam(value = "pageSize" ,defaultValue = "10" ) int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //检查管理员权限
        if (!iUserService.checkAdmin(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无操作权限");
        }

        return iOrderService.manageOrderSearch(orderNo, pageSize, pageNum);
    }


    /**
     * 用户订单发货管理后端接口
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "send.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderSendGoods(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //检查管理员权限
        if (!iUserService.checkAdmin(user).isSuccess()){
            return ServerResponse.createByErrorMessage("无操作权限");
        }

        return iOrderService.manageOrderSendGoods(orderNo);
    }

}
