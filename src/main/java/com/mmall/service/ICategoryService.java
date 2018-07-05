package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * Created by 10353 on 2018/1/5.
 *
 */
public interface ICategoryService {

    ServerResponse<List<Category>> queryCategoryWithSomeParentId(Integer parentId);

    ServerResponse<String> addCategory(String categoryName, Integer parentId);

    ServerResponse<String> modifyCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Integer>> queryAllCategoryByCategoryId(Integer categoryId);
}
