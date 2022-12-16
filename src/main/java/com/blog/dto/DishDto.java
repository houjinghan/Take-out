package com.blog.dto;


import com.blog.entity.Dish;
import com.blog.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
    DTO（Data Transfer Object）：数据传输对象，Service 或 Manager 向外传输的对象。
    DTO 只能用于前端、RPC 的请求参数
 */
@Data
public class DishDto extends Dish {
    //菜品口味
    private List<DishFlavor> flavors = new ArrayList<>();
    //菜品分类名称
    private String categoryName;

    private Integer copies;
}
