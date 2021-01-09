package com.changgou.search.controller;

import com.changgou.goods.entity.Page;
import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 实现搜索调用
     */
    @GetMapping("/list")
    public String search(@RequestParam(required = false) Map<String,String> searchMap, Model model) throws Exception {
        //替换特殊字符
        handlerSearchMap(searchMap);

        //调用搜索微服务
        Map resultMap = skuFeign.search(searchMap);
        model.addAttribute("result",resultMap);

        //计算分页
        Page<SkuInfo> pageInfo = new Page<>(Long.parseLong(resultMap.get("total").toString()), Integer.parseInt(resultMap.get("pageNumber").toString())+1, Integer.parseInt(resultMap.get("pageSize").toString()));
        model.addAttribute("pageInfo",pageInfo);

        //讲条件存储，用于页面回显数据
        model.addAttribute("searchMap",searchMap);

        //获取上次请求地址
        String[] urls = url(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sorturl",urls[1]);

        return "search";
    }

    /**
     * 拼接用户请求的url
     * 获取用户每次请求的utl+页面需要在这次请求地址上添加额外搜索条件
     */
    public String[] url(Map<String,String> searchMap){
        String url = "/search/list";//初始化地址
        String sortUrl = "/search/list";//排序地址
        if (searchMap!=null && searchMap.size()>0){
            url+="?";
            sortUrl+="?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //搜索条件对象
                String key = entry.getKey();
                //搜索的值
                String value = entry.getValue();
                url = url + key +"=" + value + "&";

                //跳过分页参数
                if (key.equalsIgnoreCase("pageNum")){
                    continue;
                }
                //排序参数 跳过
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")){
                    continue;
                }
                sortUrl = sortUrl + key + "=" + value + "&";
            }
            //去掉最后一个"&"
            url = url.substring(0,url.length()-1);
            sortUrl = sortUrl.substring(0,url.length()-1);
        }
        return new String[]{url,sortUrl};
    }

    /**
     * 替换特殊字符
     */
    public void handlerSearchMap(Map<String,String> searchMap){
        if (searchMap != null){
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if (entry.getKey().startsWith("spec_"))
                    entry.setValue(entry.getValue().replace("+","%2B"));
            }
        }
    }
}
