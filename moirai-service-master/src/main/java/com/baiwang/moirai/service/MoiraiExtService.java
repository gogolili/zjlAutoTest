package com.baiwang.moirai.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.SyspageBasicQuery;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public interface MoiraiExtService {

    public Map buildOrgExcelWorkbook(JSONObject jsonObject,String excelName);

    public void createExcelTemplate( @RequestBody SyspageBasicQuery queryParam);

    public Map<String,Map<String,String>> calExtMap(String tenantId,String orgId, String userId,String pageUniqueName,String tag);

    public void buildExcelData(JSONArray jsonArray, List<Map<String,Object>> templateMapList, Object preInvoice, Map<String,Object> extMap) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    public JSONObject getTemplateDate(String tenantId, String orgId, String userId, String pageUniqueName);
}
