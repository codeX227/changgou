package com.changgou.search.service;

import java.util.Map;

public interface SkuService {

    /**
     * 条件搜索
     */
    Map<String,Object> search(Map<String,Object> searchMap);

    /**
     * 导入数据到索引库
     */
    void importData();
}
