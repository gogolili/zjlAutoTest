package com.baiwang.cloud.web.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.baiwang.cloud.common.model.JxdkqkPageParam;
import com.baiwang.cloud.common.model.PageResult;
import com.baiwang.cloud.common.model.JxDkqk;
import com.baiwang.cloud.common.model.ServiceResponse;
import com.baiwang.cloud.common.utils.EasyExcelUtil;
import com.baiwang.cloud.service.IJxDkqkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.baiwang.cloud.common.model.ServiceResponse.success;

@RestController
public class JxdkqkController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JxdkqkController.class);

    @Autowired
    private IJxDkqkService jxDkqkService;


    /**
     * 查询dkqk表所有数据
     * @param jxDkqkPageParam
     * @return
     */
    @RequestMapping("/queryJxDkqkPage")
    @ResponseBody
    public ServiceResponse<PageResult<List<JxDkqk>>> queryJxDkqkByConditions(@RequestBody JxdkqkPageParam jxDkqkPageParam){
        ServiceResponse<PageResult<List<JxDkqk>>> response =null;
        try{
            PageResult<List<JxDkqk>> pageResult = jxDkqkService.queryJxDkqkPage(jxDkqkPageParam);
            response = success(pageResult,"查询成功");

        }catch (Exception e){
            response = ServiceResponse.error("查询失败",100);
        }
        return response;
    }

    //添加发票
    @RequestMapping("/inserJxDkqk")
    @ResponseBody
    public ServiceResponse<String> inserJxDkqk(@RequestBody @Validated JxDkqk jxDkqk){
        ServiceResponse<String> response = null;
        try{
            int result = this.jxDkqkService.inserJxDkqk(jxDkqk);
            response = success(result+"","添加成功");
        }catch (Exception e){
            LOGGER.error("添加失败",e);
            response = ServiceResponse.error(e.getMessage(),100);
        }
        return response;
    }

    //修改发票
    @RequestMapping("/updateJxDkqk")
    @ResponseBody
    public ServiceResponse<String> updateJxDkqk(@RequestBody @Validated JxDkqk jxDkqk){
        ServiceResponse<String> response = null;
        int result=1;
        try{
            result = this.jxDkqkService.updateJxDkqk(jxDkqk);
            response = success(result+"","更新成功");
        }catch (Exception e){
            response = ServiceResponse.error(e.getMessage(),200);
        }
        return response;
    }

    //删除
    @RequestMapping("/deleteJxDkqk")
    @ResponseBody
    public ServiceResponse<String> deleteJxDkqk(@RequestBody  JxDkqk jxDkqk){
        ServiceResponse<String> response = null;
        int result=1;
        try{
            result = this.jxDkqkService.deleteJxDkqk(jxDkqk);
            response = success(result+"","更新成功");
        }catch (Exception e){
            response = ServiceResponse.error(e.getMessage(),200);
        }
        return response;
    }

    //导入
    @RequestMapping("/impiortExcel")
    @ResponseBody
    public ServiceResponse<String> importExcel(@RequestParam("file") MultipartFile multipartFile){
        ServiceResponse<String> response = null;
        if (multipartFile.isEmpty()){
            response = ServiceResponse.error(response.getMessage(),300);
        }
        String fileName = multipartFile.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        try{
            jxDkqkService.importExcel(multipartFile);
            response = ServiceResponse.success("","导入成功");
        }catch (Exception e){
            response = ServiceResponse.error(e.getMessage(),400);
        }
        return response;
    }

    //导入2
    @RequestMapping("/impiortExcel2")
    @ResponseBody
    public ServiceResponse<String> importExcel2(@RequestParam("file") MultipartFile multipartFile){
        ServiceResponse<String> response = null;
        if (multipartFile.isEmpty()){
            response = ServiceResponse.error(response.getMessage(),300);
        }
        String fileName = multipartFile.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        try{
            jxDkqkService.importExcel(multipartFile);
            response = ServiceResponse.success("","导入成功");
        }catch (Exception e){
            response = ServiceResponse.error(e.getMessage(),400);
        }
        return response;
    }
}
