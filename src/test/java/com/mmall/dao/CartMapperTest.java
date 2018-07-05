package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by 10353 on 2018/1/3.
 */

public class CartMapperTest extends BaseTest {

    @Autowired
    private CartMapper cartMapper;

    @Test
    public void testSelectByPrimaryKey(){
        Cart cart = cartMapper.selectByPrimaryKey(126);
    }
}
