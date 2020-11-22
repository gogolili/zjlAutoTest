/*
 * @项目名称: Moirai
 * @文件名称: IndexController.java
 * @Date: 17-11-28 下午5:24
 * @author Lance cui
 *
 */

package com.baiwang.moirai.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 */
@RestController
public class IndexController
{
    @RequestMapping(value = "/bwhealthy")
    public String bwhealthy()
    {
        return "Moirai service success";
    }
    
}
