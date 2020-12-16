package com.changgou.search.controller;

import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import com.changgou.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    @GetMapping("/import")
    public Result importData(){
        skuService.importData();

        return new Result(true, StatusCode.OK,"执行操作成功");
    }
}
