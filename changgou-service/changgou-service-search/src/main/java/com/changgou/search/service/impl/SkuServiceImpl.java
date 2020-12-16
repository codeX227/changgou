package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SkuEsMapper skuEsMapper;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 条件搜索
     */
    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        //封装搜索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //执行搜索
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //分页参数-总记录数
        long totalElements = page.getTotalElements();
        //总页数
        int totalPages = page.getTotalPages();
        //获取数据结果集

        List<SkuInfo> contents = page.getContent();
        //封装所有数据
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows",contents);
        resultMap.put("total",totalElements);
        resultMap.put("totalPages",totalPages);

        return resultMap;
    }

    /**
     * 导入数据到索引库
     */
    @Override
    public void importData() {
        //feign调用，查询List<Sku>
        Result<List<Sku>> skuResult = skuFeign.findAll();

        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()),SkuInfo.class);
        //遍历skuInfoList
        for (SkuInfo skuinfo : skuInfoList) {
            //获取spec类型->Map类型
            Map<String,Object> specMap = JSON.parseObject(skuinfo.getSpec(), Map.class);
            //生成动态域
            skuinfo.setSpecMap(specMap);
        }
        //调用dao实现批量导入
        skuEsMapper.saveAll(skuInfoList);
    }
}
