package com.aaron.controller;

import com.aaron.service.ContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: Aaron
 * @Date: 2020-05-17 14:42
 * @Description:
 */
@RestController
public class ContentController {

    @Resource
    private ContentService contentService;

    @GetMapping("/parseJD/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws IOException {
        return contentService.parseContent(keyword);
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> searchPage(@PathVariable("keyword") String keyword,
                                                @PathVariable(value = "pageNo") Integer pageNo,
                                                @PathVariable("pageSize") Integer pageSize) throws IOException {
        return contentService.searchPage(keyword, pageNo, pageSize);
    }

}
