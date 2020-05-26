package com.aaron.utils;

import com.aaron.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Aaron
 * @Date: 2020-05-17 14:12
 * @Description: 解析网页工具类
 */
@Component
public class HtmlParseUtils {

    public List<Content> parseJD(String keyword) throws IOException {
        String url = "https://search.jd.com/Search?keyword="+keyword;
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");
        List<Content> contentList = new ArrayList<>();
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("src");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            Content content=new Content();
            content.setTitle(title);
            content.setImg(img);
            content.setPrice(price);
            contentList.add(content);
        }
        return contentList;
    }

}
