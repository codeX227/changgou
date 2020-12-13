package com.changgou.goods.controller;

import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import com.changgou.goods.file.FastDFSFile;
import com.changgou.goods.util.FastDFSUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {
    /**
     * 文件上传
     */
    @PostMapping
    public Result upload(@RequestParam("file")MultipartFile file) throws Exception{
        //封装文件信息
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(), //文件名称
                file.getBytes(),            //文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename()) //获取文件扩展名
        );

        //调用 FastDFSUtil工具类将文件传入FastDFS中
        String[] uploads = FastDFSUtil.upload(fastDFSFile);

        //拼接访问地址 url = http:localhost:8080/group1/M00/00/00/daT9oj8i6oYP2ji2ojP.jpeg
        //String url = "http:localhost:8080/"+uploads[0]+"/"+uploads[1];
        String url = FastDFSUtil.getTrackerInfo()+"/"+uploads[0]+"/"+uploads[1];

        return new Result(true, StatusCode.OK,"上传成功",url);
    }
}
