package com.mmall.pojo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 有些业务场景商品类需要放入Set所有全写equals()和hashCode()方法
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Product {

    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private String subImages;

    private String detail;

    private BigDecimal price;

    private Integer stock;

    private Integer status;

    private Date createTime;

    private Date updateTime;


}