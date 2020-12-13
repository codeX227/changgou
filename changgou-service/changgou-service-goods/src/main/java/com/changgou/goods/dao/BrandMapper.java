package com.changgou.goods.dao;

import com.changgou.goods.goods.pojo.Brand;
import tk.mybatis.mapper.common.Mapper;

/**
 *   DAO 使用通用 Mapper,Dao接口需要继承 tk.mybatis.mapper.common.Mapper
 */
public interface BrandMapper extends Mapper<Brand> {
}
