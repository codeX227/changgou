package com.changgou.service.impl;

import com.changgou.dao.BrandMapper;
import com.changgou.goods.pojo.Brand;
import com.changgou.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页+条件搜索
     * @param brand 条件
     * @param page 当前页
     * @param size 每页显示条数
     */
    @Override
    public PageInfo<Brand> findPage(Brand brand, Integer page, Integer size) {
        PageHelper.startPage(page,size);
        Example example = createExample(brand);
        List<Brand> brands = brandMapper.selectByExample(example);
        return new PageInfo<>(brands);
    }

    /**
     * 条件搜索
     * @param page 当前页
     * @param size 每页显示条数
     */
    @Override
    public PageInfo<Brand> findPage(Integer page, Integer size) {
        PageHelper.startPage(page,size);
        List<Brand> brands = brandMapper.selectAll();
        //封装 PageInfo -> list
        return new PageInfo<>(brands);
    }

    /**
     * 多条件搜索
     * @param brand
     */
    @Override
    public List<Brand> findList(Brand brand) {
        Example example = createExample(brand);
        return brandMapper.selectByExample(example);
    }

    /**
     * 条件构建
     * @param brand
     * @return
     */
    public Example createExample(Brand brand){
        //自定义搜索对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();//多条件构造器

        if (brand != null){
            //brand.name != null根据名字模糊搜索where name like %华为%
            if (!StringUtils.isEmpty(brand.getName())){
                criteria.andLike("name","%"+brand.getName()+"%");
            }
            //brand.letter != null根据首字母搜索and letter = "H"
            if (!StringUtils.isEmpty(brand.getLetter())) {
                criteria.andEqualTo("letter",brand.getLetter());
            }
        }
        return example;
    }


    /**
     * 根据ID删除
     * @param id
     */
    @Override
    public void delete(Integer id) {
        //使用通用Mapper.deleteByPrimaryKey(id)
        brandMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Brand brand) {
        //使用通用Mapper.updateByPrimaryKeySelective(brand)会忽略空值
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 增加品牌
     * @param brand
     */
    @Override
    public void add(Brand brand) {
        //查询所有 -> 通用Mapper.insertSelective(T)
        brandMapper.insertSelective(brand);
    }

    /**
     * 根据 id 查询
     * @param id
     * @return
     */
    @Override
    public Brand findById(Integer id) {
        //查询所有 -> 通用Mapper.selectByPrimaryKey()
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询所有
     * @return
     */
    @Override
    public List<Brand> findAll() {
        //查询所有 -> 通用Mapper.selectAll()
        return brandMapper.selectAll();
    }
}
