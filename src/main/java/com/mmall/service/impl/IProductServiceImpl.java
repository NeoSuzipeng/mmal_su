package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 10353 on 2018/1/5.
 *
 */
@Service("iProductService")
public class IProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 更新和保存商品业务的封装
     * @param product
     * @return
     */
    @Override
    public ServerResponse<String> addOrUpdateProduct(Product product) {
        if(product != null){
            //如果子图集不为空将第一个子图赋值给主图
            if(StringUtils.isNotBlank(product.getSubImages())){
                String[] subImages = product.getSubImages().split(",");
                if(subImages.length > 0)
                    product.setMainImage(subImages[0]);
            }

            //插入逻辑
            if(product.getId() != null){
                int resultCount = productMapper.updateByPrimaryKeySelective(product);
                if(resultCount > 0)
                    return ServerResponse.createBySuccess("更新商品成功");
                else
                    return ServerResponse.createByErrorMessage("更新商品失败");
            }

            //更新逻辑
            int resultCount = productMapper.insert(product);
            if(resultCount > 0)
                return ServerResponse.createBySuccess("添加商品成功");
            else
                return ServerResponse.createByErrorMessage("添加商品失败");
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        boolean inValidParameter = productId == null || status == null;
        if(inValidParameter)
            return ServerResponse.createByErrorMessage("参数错误");
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        product.setUpdateTime(new Date());
        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if(resultCount > 0)
            return ServerResponse.createBySuccess("上下架成功");
        else
            return ServerResponse.createByErrorMessage("上下架失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> queryProductDetail(Integer productId) {
        if(productId == null)
            return ServerResponse.createByErrorMessage("请选择查看的商品");
        Product product = productMapper.selectByPrimaryKey(productId);
        //管理员可以控制上下架所以将下架商品呈现出来
        if(product == null)
            return ServerResponse.createBySuccess("商品已下架");

        ProductDetailVo productDetailVo = assembleProductVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 使用Pagehepler完成动态分页
     * @return
     */
    @Override
    public ServerResponse<PageInfo> queryProductList(Integer pageNum, Integer pageSize) {
        /**
         * pagehepler逻辑
         * 1.PageHelper注入开始信息
         * 2.获取数据
         * 3.PageInfo包装数据
         */
        PageHelper.startPage(pageNum, pageSize);
        List<Product> products = productMapper.selectProductList();
        PageInfo pageInfo = new PageInfo(products);

        List<ProductListVo> productListVos = new ArrayList<>();
        for (Product product : products)
             productListVos.add(assembleProductListVo(product));

        pageInfo.setList(productListVos);

        //前端不需要这么多商品信息，再次封装一个ProductListVo

        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<PageInfo> queryProductByIdOrName(Integer pageNum, Integer pageSize, Integer productId, String productName) {
        PageHelper.startPage(pageNum, pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }else{
            productName = null;
        }
        List<Product> productList = productMapper.selectProductByIdOrName(productId, productName);

        List<ProductListVo> productListVos = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVos.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVos);


        //前端不需要这么多商品信息，再次封装一个ProductListVo

        return ServerResponse.createBySuccess(pageInfo);

    }


    /**
     * 封装商品列表类
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setStatus(product.getStatus());
        productListVo.setPrice(product.getPrice());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://106.14.0.93/"));
        return productListVo;
    }

    /**
     * 封装商品详情类
     * @param product
     * @return
     */
    private ProductDetailVo assembleProductVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://106.14.0.93/"));
        productDetailVo.setCreateTime(DateUtil.dateToString(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateUtil.dateToString(product.getUpdateTime()));

        Category parentCategory = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        //默认parentId为根节点
        if(parentCategory == null)
            productDetailVo.setParentCategoryId(0);
        productDetailVo.setParentCategoryId(parentCategory.getParentId());
        return productDetailVo;
    }

    //fronted

    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null)
            return ServerResponse.createByErrorMessage("请选择查看的商品");
        Product product = productMapper.selectByPrimaryKey(productId);
        //管理员可以控制上下架所以将下架商品呈现出来
        if(product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode())
            return ServerResponse.createBySuccess("商品已下架");

        ProductDetailVo productDetailVo = assembleProductVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> getProductList(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy) {
        if(categoryId == null && StringUtils.isBlank(keyword)){
            return ServerResponse.createByErrorMessage("参数错误");
        }

        List<Integer> categoryIdList = new ArrayList<>();
        //通过分类ID查询
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null){
                PageHelper.startPage(pageNum, pageSize);
                List<ProductDetailVo> productDetailVos = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productDetailVos);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.queryAllCategoryByCategoryId(category.getId()).getData();
        }

        //通过关键字模糊查询
        String keywordStr = null;
        if(StringUtils.isNotBlank(keyword)){
             keywordStr = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        //动态排序
        PageHelper.startPage(pageNum, pageSize);
        if(StringUtils.isNotBlank(orderBy)){
            //PageHelper的排序格式 ”price esc“, 而前端传来格式的是 price_esc;
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orders = orderBy.split("_");
                PageHelper.orderBy(orders[0] + " " + orders[1]);
            }
        }
        List<Product> productList = productMapper.selectProductByKeywordOrCategtryList(keywordStr,categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


}
