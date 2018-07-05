package com.mmall.dao;

import com.mmall.pojo.Product;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * Created by 10353 on 2018/1/6.
 */
public class ProductMapperTest extends BaseTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    public void testSelectProductByIdOrName(){
        List<Product> productList = productMapper.selectProductByIdOrName(26,null);
        System.out.print(productList.size());
    }
}
