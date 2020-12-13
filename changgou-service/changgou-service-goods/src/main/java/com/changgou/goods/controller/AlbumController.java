package com.changgou.goods.controller;

import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import com.changgou.goods.goods.pojo.Album;
import com.changgou.goods.service.AlbumService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    /***
     * Album分页条件搜索实现
     * @param album
     * @param page
     * @param size
     * @return
     */
    @PostMapping("/search/{page}/{size}")
    public Result<PageInfo<Album>> findPage(@RequestBody(required = false) Album album,
                                     @PathVariable("page") int page,
                                     @PathVariable("size") int size){
        PageInfo<Album> pageInfo = albumService.findPage(album, page, size);

        return new Result<>(true, StatusCode.OK,"查询成功",pageInfo);
    }
}
