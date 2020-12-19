package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import io.swagger.models.auth.In;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

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
     * @param searchMap
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //搜索条件封装
        NativeSearchQueryBuilder builder = buildBasicQuery(searchMap);
        //集合搜索
        HashMap<String, Object> resultMap = searchList(builder);

        //当用户选择了分类，将分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据用于户显示分组条件
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))){
            //分类分组查询
            List<String> categoryList = searchCategoryList(builder);
            resultMap.put("categoryList",categoryList);
        }

        //当用户选择了品牌，将品牌作为搜索条件，则不需要对品牌进行分组搜索，因为分组搜索的数据用于户显示分组条件
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            //查询品牌集合
            List<String> brandList = searchBrandList(builder);
            resultMap.put("brandList", brandList);
        }

        //规格查询
        HashMap<String, Set<String>> specList = searchSpecList(builder);
        resultMap.put("specList",specList);

        return resultMap;
    }

    /**
     * 搜索条件封装
     * @param searchMap
     * @return
     */
    public NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //封装搜索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (searchMap != null && searchMap.size() > 0){
            //根据关键词搜索
            String keywords = searchMap.get("keywords");
            //关键词不为空，就搜索关键词数据
            if (!StringUtils.isEmpty(keywords)){
                //builder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQuery.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }
            //输入了分类 category
            if (!StringUtils.isEmpty(searchMap.get("category"))){
                boolQuery.must(QueryBuilders.termQuery("name",searchMap.get("category")));
            }
            //输入了品牌 brand
            if (!StringUtils.isEmpty(searchMap.get("brand"))){
                boolQuery.must(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }
            //规格过滤实现：spec_网络=联通3G&spec_颜色=红
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果key以spec_开始，则表示规格筛选
                if (key.startsWith("spec_")){
                    //规格条件的值
                    String value = entry.getValue();
                    // spec_网络 去掉前五个字符
                    boolQuery.must(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }
            //price 0-500元 500-1000元 1000-2000元
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)){
                //去掉中文"元"和"以上" 0-500 500-1000 1000-2000
                price = price.replace("元", "").replace("以上", "");
                //根据"-"分割    [0-500] [500-1000] [1000-2000]
                String[] prices = price.split("-");
                //x一定不为空，y有可能为空
                if (prices != null && prices.length>0){
                    //price > prices[0]
                    boolQuery.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    //prices[1]!=null price<prices[1]
                    if (prices.length == 2){
                        boolQuery.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }
            //排序实现
            String sortField = searchMap.get("sortField");
            String sortRule = searchMap.get("sortRule");
            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
                builder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }

        //分页，不传参数则默认第一页
        Integer pageNum = coverterPage(searchMap);  //默认第一页
        Integer size = 3;     //默认查询条数
        builder.withPageable(PageRequest.of(pageNum-1,size));


        //将BoolQueryBuilder填充给NativeSearchQueryBuilder
        builder.withQuery(boolQuery);
        return builder;
    }

    /**
     * 接收前端传入的分页参数
     * @param searchMap
     * @return
     */
    public Integer coverterPage(Map<String,String> searchMap){
        if (searchMap != null){
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e){

            }
        }
        return 1;
    }

    /**
     * 结果集搜索
     * @param builder
     * @return
     */
    public HashMap<String, Object> searchList(NativeSearchQueryBuilder builder) {

        //高亮配置
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        //前缀
        field.preTags("<em style=\"color:red;\">");
        //后缀
        field.postTags("</em>");
        //碎片长度
        field.fragmentSize(100);
        //添加高亮
        builder.withHighlightFields();


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
     * 规格分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public HashMap<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder){
        /**
         * 分组查询规格集合
         * .addAggregation 添加一个聚合操作
         * field 根据某个域分组
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        /**
         * 获取分组数据
         * .get("skuSpec"); 获取指定域的集合 [{},{}]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuSpec");
        ArrayList<String> specList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String specName = bucket.getKeyAsString();//其中一个规格的名字
            specList.add(specName);
        }

        //规格汇总合并
        HashMap<String, Set<String>> allSpec = putAllSpec(specList);

        return allSpec;
    }

    /**
     * 规格汇总合并
     * @param specList
     * @return
     */
    public HashMap<String, Set<String>> putAllSpec(ArrayList<String> specList) {
        //合并后的
        HashMap<String, Set<String>> allSpec = new HashMap<>();
        //循环specList
        for (String spec : specList) {
            //将每个JSON串转成Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);
            //合并，循环所有Map
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                //取出当前Map，并获取对应的Key以及value
                String key = entry.getKey();
                String value = entry.getValue();
                //将当前循环数据合并到Map<String,Set<String>>中
                //从allSpec获取当前规格对应的set集合数据
                Set<String> specSet = allSpec.get(key);
                if (specSet==null){
                    //之前allSpec没有该规格
                    specSet = new HashSet<>();
                }
                specSet.add(value);
                allSpec.put(key,specSet);
            }
        }
        return allSpec;
    }

    /**
     * 品牌分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder){
        /**
         * 分组查询品牌集合
         * .addAggregation 添加一个聚合操作
         * field 根据某个域分组
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        /**
         * 获取分组数据
         * .get("skuBrand"); 获取指定域的集合 [华为，小米，中兴]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuBrand");
        ArrayList<String> brandList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String brandName = bucket.getKeyAsString();//其中一个品牌的名字
            brandList.add(brandName);
        }

        return brandList;
    }

    /**
     * 分类分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder){
        /**
         * 分组查询分类集合
         * .addAggregation 添加一个聚合操作
         * field 根据某个域分组
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        /**
         * 获取分组数据
         * .get("skuCategory"); 获取指定域的集合
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");
        ArrayList<String> categoryList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String categoryName = bucket.getKeyAsString();//其中一个分类的名字
            categoryList.add(categoryName);
        }

        return categoryList;
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
