package com.aaron.service;

import com.aaron.pojo.Content;
import com.aaron.utils.HtmlParseUtils;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Aaron
 * @Date: 2020-05-17 14:42
 * @Description:
 */
@Service
public class ContentService {

    @Resource
    private RestHighLevelClient client;

    /**
     * @Author: Aaron
     * @Description: 输入搜索条件后将数据解析存储到ES中，并返回插入是否成功
     * @Date: 2020-05-17 15:01
     * @param: keyword
     * @return: java.lang.Boolean
     **/
    public Boolean parseContent(String keyword) throws IOException {
        //1.使用JSoup解析网页获取数据
        List<Content> contentList = new HtmlParseUtils().parseJD(keyword);
        //2.判断索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest("jd_goods");
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        //3.如果索引不存在则创建jd_goods索引
        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("jd_goods");
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        }
        //4.如果成功创建BulkRequest对象，并设置超时时间
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        for (int i = 0; i < contentList.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                            .source(JSON.toJSONString(contentList.get(i)), XContentType.JSON)
            );
        }
        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    /**
     * @Author: Aaron
     * @Description: 获取数据实现搜索功能
     * @Date: 2020-05-17 15:24
     * @param: keyword
     * @param: pageNo
     * @param: pageSize
     * @return: List<Map < String, Object>>
     **/
    public List<Map<String, Object>> searchPage(String keyword, Integer pageNo, Integer pageSize) throws IOException {
        //1.分页合理化
        if (pageNo < 1) {
            pageNo = 1;
        }
        //2.创建搜索请求
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        //3.构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //4.开启分页
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);
        //5.精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQuery);
        //6.关键字高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");//要高亮的属性
        highlightBuilder.requireFieldMatch(false);//关闭多个高亮
        highlightBuilder.preTags("<span style='color:red'>");//高亮的前缀
        highlightBuilder.postTags("</span>");//高亮的后缀
        searchSourceBuilder.highlighter(highlightBuilder);
        //7.设置超时间60s
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //8.将我们的搜素条件放到请求当中
        searchRequest.source(searchSourceBuilder);
        //9.客户端执行搜索请求,返回搜索结果
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        //10.将返回结果封装
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            //解析高亮的字段(将原来的字段换成我们新的高亮字段)
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if (null != title) {
                Text[] fragments = title.fragments();
                String newTitle = "";
                for (Text fragment : fragments) {
                    newTitle += fragment;
                }
                sourceAsMap.put("title", newTitle);
            }
            mapList.add(sourceAsMap);
        }
        return mapList;
    }
}
