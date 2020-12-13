package com.changgou.goods.service;

import com.changgou.goods.goods.pojo.Brand;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BrandService {

    /**
     * 分页+条件搜索
     * @param page 当前页
     * @param size 每页显示条数
     */
    PageInfo<Brand> findPage(Brand brand, Integer page, Integer size);

    /**
     * 分页搜索
     * @param page 当前页
     * @param size 每页显示条数
     */
    PageInfo<Brand> findPage(Integer page,Integer size);

    /**
     * 根据品牌信息多条件搜索
     * @param brand
     */
    List<Brand> findList(Brand brand);

    /**
     * 根据ID删除
     * @param id
     */
    void delete(Integer id);

    /**
     * 根据ID修改品牌数据
     * @param brand
     */
    void update(Brand brand);

    /**
     * 增加品牌
     * @param brand
     */
    void add(Brand brand);

    /**
     *  根据 id 查询
     */
    Brand findById(Integer id);

    /**
     * 查询所有
     */
    List<Brand> findAll();
}
