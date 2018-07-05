package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by 10353 on 2018/1/5.
 * 后台管理商品模块
 * 1.保存和更新商品信息（保存时主图取自子图集的第一个）
 * 2.商品上下架
 * 3.查看商品细节（使用vo封装product）
 * 4.商品列表（pagehelper动态分页）
 * 5.模糊检索商品
 * 6.上传图片到FTP服务器
 * 7.富文本图片上传
 */

@Controller
@RequestMapping(value = "/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 保存和更新商品信息（保存时主图取自子图集的第一个）
     * @param session
     * @param product
     * @return
     */
    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> productSave(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iProductService.addOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }

    /**
     * 2.商品上下架
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer productId, Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iProductService.setSaleStatus(productId, status);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }

    /**
     * 查看商品细节（使用vo封装product返回数据）
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> manageProductDetail(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iProductService.queryProductDetail(productId);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }

    /**
     * 商品列表（pagehelper动态分页）
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> manageProductList(HttpSession session,
                                                      @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iProductService.queryProductList(pageNum, pageSize);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }

    /**
     * 模糊检索商品
     * @param session
     * @param pageNum
     * @param pageSize
     * @param productId
     * @param productName
     * @return
     */
    @RequestMapping(value = "search.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> manageSearchProduct(HttpSession session,
                                                        @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum,
                                                        @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize,
                                                        Integer productId, String productName){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            return iProductService.queryProductByIdOrName(pageNum, pageSize, productId, productName);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }

    /**
     * 上传图片到FTP服务器
     * 1.将图片上传到tomcat服务器中
     * 2.将服务器中文件上传到FTP服务器
     * 3.删除Tomcat服务器中图片
     * @param session
     * @param multipartFile
     * @return
     */
    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Map> uploadImage(HttpSession session,@RequestParam(value = "upload_file", required = false) MultipartFile multipartFile, HttpServletRequest request){

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        //验证用户权限
        if(iUserService.checkAdmin(user).isSuccess()){
            //获取文件上传路径
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(path, multipartFile);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map<String, String> fileMap =  Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);
        }else{
            return ServerResponse.createByErrorMessage("用户没有权限");
        }
    }


    /**
     *
     * @param session
     * @param multipartFile
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "richtext_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map uploadRichTextImg(HttpSession session,@RequestParam(value = "upload_file",required = false) MultipartFile multipartFile,
                                                 HttpServletRequest request, HttpServletResponse response){
        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员");
            return resultMap;
        }
        //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
//        {
//            "success": true/false,
//                "msg": "error message", # optional
//            "file_path": "[real file path]"
//        }
        if (iUserService.checkAdmin(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(path, multipartFile);
            if (StringUtils.isBlank(targetFileName)) {
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "无权限操作");
            return resultMap;
        }
    }
}
