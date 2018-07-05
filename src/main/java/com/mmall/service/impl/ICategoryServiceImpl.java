package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by 10353 on 2018/1/5.
 *
 */

@Service("iCategoryService")
public class ICategoryServiceImpl implements ICategoryService{

    private Logger logger = LoggerFactory.getLogger(ICategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查找所有parentId为 @param parentId的同级品类
     * @param parentId
     * @return
     */
    @Override
    public ServerResponse<List<Category>> queryCategoryWithSomeParentId(Integer parentId) {
        List<Category> categories =  categoryMapper.selectCategoryByParentId(parentId);
        if(CollectionUtils.isEmpty(categories))
            logger.info("未找到商品品类");
        return ServerResponse.createBySuccess(categories);
    }

    /**
     * 添加品类信息
     * @param categoryName
     * @param parentId
     * @return
     */
    @Override
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if(categoryName == null || StringUtils.isBlank(categoryName))
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"商品类别名不能为空");

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        //todo sort-order设置

        int resultCount = categoryMapper.insertSelective(category);
        if(resultCount > 0)
            return ServerResponse.createBySuccess("添加品类成功");
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    /**
     * 通过categoryI修改品类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    @Override
    public ServerResponse<String> modifyCategoryName(Integer categoryId, String categoryName) {

        boolean inValidArgument = categoryName == null || categoryId == null || StringUtils.isBlank(categoryName) || categoryId <= -1 ;
        if(inValidArgument)
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"信息不能为空");

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        category.setUpdateTime(new Date());
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount > 0)
            return ServerResponse.createBySuccess("更新品类成功");
        return ServerResponse.createByErrorMessage("更新品类失败");
    }

    /**
     * 递归查询本节点的id及孩子节点的id(不重复)
     * @return
     */
    @Override
    public ServerResponse<List<Integer>> queryAllCategoryByCategoryId(Integer categoryId) {

        if(categoryId == null || categoryId <= -1)
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),"参数错误");
        List<Integer> caIntegerIdList = Lists.newArrayList();
        Set<Category> categorySet = Sets.newHashSet();

        findChildCategory(categorySet, categoryId);

        for(Category category : categorySet)
            caIntegerIdList.add(category.getId());

        return ServerResponse.createBySuccess(caIntegerIdList);
    }

    /**
     * 递归查询子节点算法(不可重复)
     * @param categories
     * @param categoryId
     * @return
     */
    public Set<Category> findChildCategory(Set<Category> categories, Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null)
            categories.add(category);

        //核心（结束条件隐含及categoryList == null）
        List<Category> categoryList = categoryMapper.selectCategoryByParentId(categoryId);
        for (Category category1 : categoryList)
            findChildCategory(categories, category1.getId());

        return categories;
    }

}
