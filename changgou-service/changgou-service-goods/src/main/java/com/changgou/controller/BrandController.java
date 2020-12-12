package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Brand;
import com.changgou.service.BrandService;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
@CrossOrigin
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 条件分页搜索
     */
    @PostMapping("/search/{page}/{size}")
    public Result<PageInfo<Brand>> findPage(@RequestBody Brand brand,
                                            @PathVariable("page") Integer page,
                                            @PathVariable("size") Integer size){
        PageInfo<Brand> pageInfo = brandService.findPage(brand, page, size);
        return new Result<>(true,StatusCode.OK,"分页查询成功",pageInfo);
    }

    /**
     * 分页查询实现
     */
    @GetMapping("/search/{page}/{size}")
    public Result<PageInfo<Brand>> findPage(@PathVariable("page") Integer page,
                                            @PathVariable("size") Integer size){
        PageInfo<Brand> pageInfo = brandService.findPage(page, size);
        return new Result<>(true,StatusCode.OK,"分页查询成功",pageInfo);
    }

    /**
     * 条件查询
     */
    @PostMapping("/search")
    public Result<List<Brand>> findList(@RequestBody Brand brand){
        List<Brand> brands = brandService.findList(brand);
        return new Result<>(true,StatusCode.OK,"条件搜索查询",brands);
    }

    /**
     * 根据ID删除
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id){
        brandService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /**
     * 品牌修改实现
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Integer id, @RequestBody Brand brand){
        brand.setId(id);
        brandService.update(brand);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /**
     * 增加品牌
     */
    @PostMapping
    public Result add(@RequestBody Brand brand){
        brandService.add(brand);
        return new Result(true,StatusCode.OK,"增加品牌成功");
    }

    /**
     * 根据主键 id 查询
     */
    @GetMapping("/{id}")
    public Result<Brand> findById(@PathVariable("id") Integer id){

        Brand brand = brandService.findById(id);

        return new Result<>(true,StatusCode.OK,"根据ID查询Brand成功",brand);
    }

    /**
     * 查询所有品牌
     */
    @GetMapping
    public Result<List<Brand>> findAll(){

        List<Brand> brands = brandService.findAll();

        return new Result<>(true, StatusCode.OK,"查询所有品牌信息成功",brands);
    }
}
