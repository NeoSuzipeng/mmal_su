package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by 10353 on 2018/1/5.
 * 后台商品类别管理模块
 * 1.获取商品类别子节点
 * 2.添加商品类别
 * 3.修改商品类别名
 * 4.获取当前类别Id及递归查询子节点（树级）
 * 注意：管理员权限高需要严谨
 */

@Controller
@RequestMapping(value = "/manage/category/")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 获取商品类别子节点(平级)
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenCategory(HttpSession session,
                                                      @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iCategoryService.queryCategoryWithSomeParentId(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }


    /**
     * 添加商品类别
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, String categoryName,
                                              @RequestParam(value = "parentId", defaultValue = "0") Integer parentId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iCategoryService.addCategory(categoryName, parentId);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }

    /**
     * 修改商品类别名
     * @param categoryId
     * @param categoryName
     * @param session
     * @return
     */
    @RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName, HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iCategoryService.modifyCategoryName(categoryId, categoryName);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }

    /**
     * 获取当前类别Id及递归查询子节点
     * @param categoryId
     * @param session
     * @return 所有子节点的Id
     */
    @RequestMapping(value = "get_deep_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Integer>> getDeepCategory(Integer categoryId, HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iCategoryService.queryAllCategoryByCategoryId(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }
}
