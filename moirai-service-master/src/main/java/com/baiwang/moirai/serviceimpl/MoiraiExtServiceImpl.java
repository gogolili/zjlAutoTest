package com.baiwang.moirai.serviceimpl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.baiwang.cloud.common.exception.SystemException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.SyspageBasicQuery;
import com.baiwang.cloud.common.util.StringUtil;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.RequestContext;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.ExtTableClient;
import com.baiwang.moirai.service.MoiraiExtService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.ExcelExportStatisticSelfStyler;
import com.baiwang.moirai.utils.ExcelMultiSheetExportUtil;
import com.baiwang.moirai.utils.StrUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public class MoiraiExtServiceImpl implements MoiraiExtService {

    private final Logger logger = LoggerFactory.getLogger(MoiraiExtServiceImpl.class);

    @Autowired
    ExtTableClient extTableClient;

    @Autowired
    private PasswordService passwordService;

    public void createExcelTemplate(@RequestBody SyspageBasicQuery queryParam) {
        logger.info("生成导出模板数据参数:{}" + queryParam);

        String excelName = queryParam.getExcelName();

        BWJsonResult result = extTableClient.listPageBasicExcel(queryParam);
        List<String> datas = result.getData();
        JSONObject jsonObject = JSON.parseObject(datas.get(0), Feature.OrderedField);
        OutputStream out = null;
        Map<String, Object> map = null;
        HttpServletResponse response = RequestContext.getResponse();
        HttpServletRequest request = RequestContext.getRequest();
        try {
            if (Constants.MOIRAI_ORG_IMPORT_TAG.equals(excelName)) {
                map = buildOrgExcelWorkbook(jsonObject, excelName);
            } else {
                map = buildExcelWorkbook(jsonObject, excelName);
            }
            Workbook workbook = (Workbook) map.get("workbook");

            String excelFileName = String.valueOf(map.get("name")) + ".xls";
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && -1 != agent.indexOf("MSIE")) {
                response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + java.net.URLEncoder.encode(excelFileName, "UTF-8").replace("+", " ") + "\"");
            } else if (null != agent && -1 != agent.indexOf("Firefox")) {
                response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(excelFileName.getBytes(), "iso8859-1") + "\"");
            } else {
                response.setHeader("Content-Disposition", "attachment;filename="
                    + new String(java.net.URLEncoder.encode(excelFileName, "utf-8").getBytes(), "iso8859-1"));
            }
            response.setContentType("application/vnd.ms-excel");
            out = response.getOutputStream();
            workbook.write(out);
        } catch (Exception ec) {
            logger.info("模版下载失败", ec);
            throw new SystemException("-2", "模版下载失败");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        }
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     * 机构模板还是通过模板下载，方便三级联动、以及下拉实现
     *
     * @return
     * @since 2020/3/10
     */
    public Map buildOrgExcelWorkbook(JSONObject jsonObject, String excelName) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject excelObj = (JSONObject) jsonObject.get(excelName);
        logger.info("buildExcelWorkbook JSONObject = 【{}】", excelObj);
        //Excel主模板数据
        JSONArray jsonArray = (JSONArray) excelObj.get("data");
        //Excel 存在的参考工作表模板数据
        String excelFileName = String.valueOf(excelObj.get("name"));
        List<Map<String, Object>> remarkList = new ArrayList();
        List<Map<String, Object>> colMapList = new ArrayList();
        for (int i = 0; i < jsonArray.size(); i++) {
            Map<String, Object> colMap = new HashMap<>();
            Map<String, Object> remarkMap = new HashMap<>();
            JSONObject jobj = jsonArray.getJSONObject(i);
            if ("adminUserEmail".equals(jobj.getString("key"))){
                // 百望云邮箱必填
                jobj.put("must", passwordService.calculatePasswordMark() ? "1" : "0");
            }
            //列名
            String name = jobj.getString("name");
            //字段值
            colMap.put("headCN", name);
            String value = jobj.getString("value");
            colMap.put("exampleVal", value);
            String must = jobj.getString("must");
            String note = jobj.getString("note");
            String ext = jobj.getString("ext");
            String type = jobj.getString("type");
            //字典类型
            if (ext.equals("1") && type.equals("select")) {
                JSONArray dicJSONArry = jobj.getJSONArray("selectList");
                if (null != dicJSONArry && dicJSONArry.size() > 0) {
                    note = calExtSelectNote(dicJSONArry);
                }
            }
            remarkMap.put("cnName", name);
            remarkMap.put("must", "1".equals(must) ? "是" : "否");
            remarkMap.put("bz", note);
            colMapList.add(colMap);
            remarkList.add(remarkMap);
        }
        map.put("remarkList", remarkList);
        map.put("colList", colMapList);

        // 执行方法
        TemplateExportParams params = new TemplateExportParams("templates/orgTemplate.xls", true);
        params.setColForEach(true);
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        HSSFCellStyle cellStyle = (HSSFCellStyle) workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 填表说明内容左对齐
        Sheet sheet1 = workbook.getSheetAt(1);
        for (int i = 2; i <= sheet1.getLastRowNum(); i++){
            Row row1 = sheet1.getRow(i);
            Cell cell = row1.getCell(3);
            int valueLength = cell.getStringCellValue().getBytes().length;
            int width = cell.getSheet().getColumnWidth(cell.getColumnIndex()) / 256;
            int h = valueLength / width;
            h = h < 1 ? 1 : h;
            row1.setHeight(Short.valueOf(row1.getHeight() * h + ""));
        }
        map.put("workbook", workbook);
        map.put("name", excelFileName);
        return map;
    }

    /**
     * <B>方法名称：</B>根据模板信息组装工作簿<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/18
     */
    private Map buildExcelWorkbook(JSONObject jsonObject, String excelName) {
        Map<String, Object> map = new HashMap<>();
        List<ExcelExportEntity> templateEntity = new ArrayList<ExcelExportEntity>();
        List<Map<String, String>> templateMapList = new ArrayList<Map<String, String>>();
        Map<String, String> templateMap = new HashMap<>();
        Map<String, String> templateDescMap = new HashMap<>();

        templateMapList.add(templateMap);
        List<Map<String, String>> noteMapList = new ArrayList<Map<String, String>>();

        JSONObject excelObj = (JSONObject) jsonObject.get(excelName);
        logger.info("buildExcelWorkbook JSONObject = 【{}】", excelObj);
        //Excel主模板数据
        JSONArray jsonArray = (JSONArray) excelObj.get("data");
        //Excel 存在的参考工作表模板数据
        String excelFileName = String.valueOf(excelObj.get("name"));
        int sexIndex = -1;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            //列名
            String name = (String) jobj.get("name");
            //字段值
            String key = (String) jobj.get("key");
            if ("sex".equals(key)){
                sexIndex = i;
            }
            if ("userEmail".equals(key)) {
                // 百望云邮箱必填
                jobj.put("must", passwordService.calculatePasswordMark() ? "1" : "0");
            }
            String value = jobj.getString("value");
            String length = jobj.getString("length");
            String must = jobj.getString("must");
            String note = jobj.getString("note");
            String desc = jobj.getString("desc");
            String ext = jobj.getString("ext");
            String type = jobj.getString("type");
            //字典类型
            if (ext.equals("1")) {
                if (type.equals("select")) {
                    JSONArray dicJSONArry = (JSONArray) jobj.get("selectList");
                    if (null != dicJSONArry && dicJSONArry.size() > 0) {
                        note = calExtSelectNote(dicJSONArry);
                    }
                } else {
                    note = "";
                }
            }

            if (!StringUtil.isEmpty(desc)) {
                templateDescMap.put(key, desc);
            }

            //模板注释数据
            templateMap.put(key, value);

            ExcelExportEntity excelExportEntity = new ExcelExportEntity(name, key);
            excelExportEntity.setWidth(20);
//            if("382EB3CCAAB3FB48695CA1F8FB4F54FC".equals(name)){
//                excelExportEntity.setWidth(0);
//            }
            templateEntity.add(excelExportEntity);
            Map<String, String> noteMap = new HashMap<>();
            noteMap.put("bw_tbsmxh", String.valueOf(i + 1));
            noteMap.put("key", key);
            noteMap.put("name", name);
            noteMap.put("length", length);
            noteMap.put("must", must);
            noteMap.put("note", note);
            noteMapList.add(noteMap);
        }
        if (templateDescMap.size() > 0) {
            templateMapList.add(templateDescMap);
        }

        String sheetName = String.valueOf(excelObj.get("sheetname"));
        Map<String, Object> sheetsTemplateMap = buildUserTemplateSheet(sheetName, templateEntity, templateMapList);
        Map<String, Object> noteTemplateMap = buildNoteSheet(noteMapList);
        List<Map<String, Object>> sheetsList = new ArrayList<>();
        sheetsList.add(sheetsTemplateMap);
        sheetsList.add(noteTemplateMap);
        // 执行方法
        Workbook workbook = ExcelMultiSheetExportUtil.exportExcel(sheetsList, ExcelType.HSSF);
        Sheet sheetAt = workbook.getSheetAt(0);
        //  生成性别下拉列表
        if (sexIndex != -1) {
            //  只对(x，x)单元格有效
            CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(2, 65535, sexIndex, sexIndex);
            //  生成下拉框内容
            DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(new String[]{"男", "女"});
            HSSFDataValidation dataValidation = new HSSFDataValidation(cellRangeAddressList, dvConstraint);
            //  对sheet页生效
            sheetAt.addValidationData(dataValidation);
        }
        // 填表说明内容左对齐
        Sheet sheet1 = workbook.getSheetAt(1);
        for (int i = 2; i <= sheet1.getLastRowNum(); i++){
            Row row1 = sheet1.getRow(i);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            row1.getCell(1).setCellStyle(cellStyle);
            row1.getCell(3).setCellStyle(cellStyle);
        }
        map.put("workbook", workbook);
        map.put("name", excelFileName);

        return map;
    }

    /**
     * <B>方法名称：</B>获取模板数据<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/12
     */
    public JSONObject getTemplateDate(String tenantId, String orgId, String userId, String pageUniqueName) {
        String type = "2";
        SyspageBasicQuery query = new SyspageBasicQuery();
        query.setTenantId(tenantId);
        query.setOrgId(orgId);
        query.setPageUniqueName(pageUniqueName);
        String excelType = Constants.MOIRAI_EXCEL_TYPE_EXPORT;
        query.setExcelType(excelType);
        query.setType(type);
        query.setUserId(userId);
        logger.info("pageUniqueName = 【{}】 tenantId = 【{}】 orgId = 【{}】 ExcelType = 【{}】", pageUniqueName, tenantId, orgId, excelType);
        BWJsonResult result = extTableClient.listPageBasicExcel(query);
        logger.info("导入模板数据 extTableClient ：【{}】", JSONObject.toJSONString(result));
        List<String> datas = result.getData();
        JSONObject jsonObject = JSON.parseObject(datas.get(0), Feature.OrderedField);
        return jsonObject;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     * jsonArray：模板集 1、实体类对象与Excel 列是一对一关系  实体类与map集合一对一关系 2、templateMapList：实体类属性名、值以Map形式存储的集合列表，
     *
     * @return
     * @since 2019/12/12
     */
    public void buildExcelData(JSONArray jsonArray, List<Map<String, Object>> templateMapList, Object moiraiorg,
        Map<String, Object> extMap) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        //filedName(key)<-->filedValue(value)
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            String filedName = (String) jobj.get("key");
            String value = (String) jobj.get("value");
            String ext = String.valueOf(jobj.get("ext"));
            if (ext.equals("1")) {
                String filedVal = BeanUtils.getProperty(extMap, filedName);
                map.put(filedName, filedVal);
            } else {
                if (!StrUtils.isEmpty(value)) {
                    map.put(filedName, value);
                } else {
                    String filedVal = BeanUtils.getProperty(moiraiorg, filedName);
                    map.put(filedName, filedVal);
                }
            }
        }
        templateMapList.add(map);
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/12
     */
    public Map<String, Map<String, String>> calExtMap(String tenantId, String orgId, String userId,
        String pageUniqueName, String tag) {
        Map<String, Map<String, String>> map = new HashMap<>();
        Map<String, String> extMap = new HashMap<>();
        Map<String, String> nomalMap = new HashMap<>();
        map.put("extMap", extMap);
        map.put("nomalMap", nomalMap);

        SyspageBasicQuery query = new SyspageBasicQuery();
        String excelType = "1";
        query.setTenantId(tenantId);
        query.setOrgId(orgId);
        query.setPageUniqueName(pageUniqueName);
        query.setExcelType(excelType);
        query.setUserId(userId);
        query.setType("2"); //  查询用户模版
        if (StringUtil.isBlank(userId)) {
            query.setType("1"); // 查询租户模版
            query.setUserId("0");
        }
        logger.info("获取excel模板参数：【{}】", query);
        BWJsonResult result = extTableClient.listPageBasicExcel(query);
        logger.info("获取excel模板结果：【{}】", result);
        if (result.getData() == null || result.getData().isEmpty()){
            throw new MoiraiException("", "获取扩展字段信息异常！");
        }
        List<String> datas = result.getData();
        JSONObject jsonObject = JSON.parseObject(datas.get(0), Feature.OrderedField);
        JSONObject excelObj = jsonObject.getJSONObject(tag);
        JSONArray jsonArray = excelObj.getJSONArray("data");

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            String ext = jsonObject1.getString("ext");
            // 字段名称
            String name = jsonObject1.getString("name");
            // 字段键值
            String key = jsonObject1.getString("key");
            if (ext.equals("0")) {
                nomalMap.put(name, key);
            } else {
                extMap.put(name, key);
                // 字段类型
                String fileType = jsonObject1.getString("type");
                if ("select".equals(fileType)){
                    JSONArray selectList = jsonObject1.getJSONArray("selectList");
                    JSONArray handelList = new JSONArray();
                    for (int j = 0; j < selectList.size(); j++){
                        JSONObject item = selectList.getJSONObject(j);
                        if ("1".equals(item.getString("useFlag"))){
                            handelList.add(item);
                        }
                    }
                    extMap.put(name + "SelectList", handelList.toJSONString());
                }
            }
        }
        return map;
    }

    private Map<String, Object> buildUserTemplateSheet(String sheetName, List<ExcelExportEntity> templateEntitys,
        List<Map<String, String>> datas) {
        ExportParams exportParams1 = new ExportParams();
        exportParams1.setStyle(ExcelExportStatisticSelfStyler.class);
        exportParams1.setSheetName(sheetName);
        exportParams1.setTitle("2.0用户导入模版");
        Map<String, Object> deptDataMap = new HashMap<>();
        // title的参数为ExportParams类型
        deptDataMap.put("title", exportParams1);
        deptDataMap.put("entity", templateEntitys);
        // sheet中要填充得数据
        deptDataMap.put("data", datas);
        return deptDataMap;
    }

    /**
     * <B>方法名称：</B>Excel中字段输入的说明信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/18
     */
    private Map<String, Object> buildNoteSheet(List<Map<String, String>> datas) {
        ExportParams noteParams = new ExportParams();
        noteParams.setStyle(ExcelExportStatisticSelfStyler.class);
        noteParams.setSheetName("填表说明");
        noteParams.setTitle("填表说明");

        Map<String, Object> userDataMap = new HashMap<>();
        userDataMap.put("title", noteParams);
        List<ExcelExportEntity> noteEntitys = new ArrayList<ExcelExportEntity>();
        noteEntitys.add(new ExcelExportEntity("序号", "bw_tbsmxh"));
//        noteEntitys.add(new ExcelExportEntity("英文字段", "key"));
        noteEntitys.add(new ExcelExportEntity("中文字段", "name"));
//        noteEntitys.add(new ExcelExportEntity("长度", "length"));
        ExcelExportEntity mustExcelExportEntity = new ExcelExportEntity("必填", "must");
        String[] mustReplace = {"是_1", "否_0"};
        mustExcelExportEntity.setReplace(mustReplace);
        noteEntitys.add(mustExcelExportEntity);
        noteEntitys.add(new ExcelExportEntity("备注", "note"));
        userDataMap.put("entity", noteEntitys);

        userDataMap.put("data", datas);
        return userDataMap;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     * 字典信息
     *
     * @return
     * @since 2019/12/18
     */
    private String calExtSelectNote(JSONArray dicJSONArry) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < dicJSONArry.size(); i++) {
            JSONObject jsonObject = (JSONObject) dicJSONArry.get(i);
            if ("1".equals(jsonObject.getString("useFlag"))){
                String dictCode = String.valueOf(jsonObject.get("dictCode"));
                String dictName = String.valueOf(jsonObject.get("dictName"));
                stringBuilder.append(dictCode + "-" + dictName + ", ");
            }
        }
        return stringBuilder.toString();
    }

}
