package com.changgou.search.controller;

import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import com.changgou.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 调用搜索实现
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String,String> searchMap) throws Exception{

        return skuService.search(searchMap);
    }

    @GetMapping("/import")
    public Result importData(){
        skuService.importData();

        return new Result(true, StatusCode.OK,"执行操作成功");
    }
}
