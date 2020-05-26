package com.aaron.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: Aaron
 * @Date: 2020-05-17 14:12
 * @Description: 返回首页
 */
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(){
        return "index";
    }

}
