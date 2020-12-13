package com.changgou.goods.controller;

import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import com.changgou.goods.goods.pojo.Para;
import com.changgou.goods.service.ParaService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/para")
public class ParaController {

    @Autowired
    private ParaService paraService;

    /***
     * Para分页条件搜索实现
     * @param para
     * @param page
     * @param size
     * @return
     */
    @PostMapping("/search/{page}/{size}" )
    public Result<PageInfo<Para>> findPage(@RequestBody(required = false) Para para,
                                     @PathVariable  int page,
                                     @PathVariable  int size){
        PageInfo<Para> pageInfo = paraService.findPage(para, page, size);

        return new Result<>(true, StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * Para分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping("/search/{page}/{size}" )
    public Result<PageInfo<Para>> findPage(@PathVariable int page, @PathVariable int size){
        PageInfo<Para> pageInfo = paraService.findPage(page, size);

        return new Result<>(true, StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param para
     * @return
     */
    @PostMapping("/search" )
    public Result<List<Para>> findList(@RequestBody(required = false)  Para para){
        List<Para> list = paraService.findList(para);

        return new Result<>(true, StatusCode.OK,"查询成功",list);
    }
    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping("/{id}" )
    public Result delete(@PathVariable Integer id){
        paraService.delete(id);

        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改Para数据
     * @param para
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    public Result update(@RequestBody  Para para,@PathVariable Integer id){
        para.setId(id);
        paraService.update(para);

        return new Result(true,StatusCode.OK,"更新成功");
    }

    /***
     * 新增Para数据
     * @param para
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Para para){
        paraService.add(para);

        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询Para数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Para> findById(@PathVariable Integer id){
        Para para = paraService.findById(id);

        return new Result(true,StatusCode.OK,"查询成功",para);
    }

    /***
     * 查询Para全部数据
     * @return
     */
    @GetMapping
    public Result<Para> findAll(){
        List<Para> list = paraService.findAll();

        return new Result(true,StatusCode.OK,"查询成功",list);
    }
}
