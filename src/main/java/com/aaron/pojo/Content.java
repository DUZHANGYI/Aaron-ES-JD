package com.aaron.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Aaron
 * @Date: 2020-05-17 14:33
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content {

    private String title;
    private String img;
    private String price;

}
