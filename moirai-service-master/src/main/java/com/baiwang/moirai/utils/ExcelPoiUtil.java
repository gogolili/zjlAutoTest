package com.baiwang.moirai.utils;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelPoiUtil {

    /**
     * <B>方法名称：</B>templateEntity：构造所有列名与字段值的对应关系 <BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/12
     */
    public static void buildExcelExportEntity(JSONArray jsonArray, List<ExcelExportEntity> templateEntity){

        for(int i=0;i<jsonArray.size();i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            String cnName=(String)jobj.get("name");
            String filedName=(String)jobj.get("key");
            String length=String.valueOf(jobj.get("length"));
            String type=(String)jobj.get("type");

            JSONArray selectList=(JSONArray)jobj.get("selectList");
            //构造列名与字段值得对应关系 
            ExcelExportEntity excelExportEntity=new ExcelExportEntity(cnName, filedName);
            if(type.equals("select")){
                String[] replaces= parseDicReplace(selectList);
                excelExportEntity.setReplace(replaces);
            }


            excelExportEntity.setWidth(Double.parseDouble(length));

            templateEntity.add(excelExportEntity);
        }
    }

    public static void buildExcelExportEntityDetailList(JSONArray jsonArray, List<ExcelExportEntity> templateEntity){
        for(int i=0;i<jsonArray.size();i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            String name=(String)jobj.get("name");
            String key=(String)jobj.get("key");
            String value=(String)jobj.get("value");
            String length=String.valueOf(jobj.get("length"));
            String ext=String.valueOf(jobj.get("ext"));
            String type=(String)jobj.get("type");
            Object formatObj=jobj.get("format");
            Object excelObj=jobj.get("exceltype");
            ExcelExportEntity excelExportEntity=null;
            if(null!=excelObj){
                 excelExportEntity = new ExcelExportEntity(name, key);
                buildSonExcelExportEntity(jobj,excelExportEntity);
            }else {
                 excelExportEntity = new ExcelExportEntity(name, key);
                JSONArray selectList = (JSONArray) jobj.get("selectList");
                if (type.equals("select")) {
                    String[] replaces = parseDicReplace(selectList);
                    excelExportEntity.setReplace(replaces);
                }
                excelExportEntity.setWidth(Double.parseDouble(length));
            }
            templateEntity.add(excelExportEntity);
        }
    }

    private static void buildSonExcelExportEntity(JSONObject jobj, ExcelExportEntity exportEntity){
        List<ExcelExportEntity> sonEntity=new ArrayList<>();
        JSONArray jsonArray1=(JSONArray)jobj.get("son");
        for(int j=0;j<jsonArray1.size();j++){
            JSONObject sonJobj = jsonArray1.getJSONObject(j);
            String name=(String)sonJobj.get("name");
            String key=(String)sonJobj.get("key");
            String type=(String)sonJobj.get("type");
            JSONArray selectList=(JSONArray)sonJobj.get("selectList");
            ExcelExportEntity excelExportEntity = new ExcelExportEntity(name, key);
            if (type.equals("select")) {
                String[] replaces = parseDicReplace(selectList);
                excelExportEntity.setReplace(replaces);
            }
            sonEntity.add(excelExportEntity);
        }
        exportEntity.setList(sonEntity);
    }

    private static String[] parseDicReplace(JSONArray gridJsonObject){
        List<String> replaceList=new ArrayList<>();
        for(int i=0;i<gridJsonObject.size();i++){
            JSONObject jsonObject=(JSONObject)gridJsonObject.get(i);
            String dictName=String.valueOf(jsonObject.get("dictName"));
            String dictCode=String.valueOf(jsonObject.get("dictCode"));
            replaceList.add(dictName+"_"+dictCode);
        }
        replaceList.add("_null");
        String[] strings = new String[replaceList.size()];
        replaceList.toArray(strings);
        return strings;
    }

    public static void main(String[] args) {
        List<Map> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("dictName","待开");
        map.put("dictCode","11");
        Map<String,Object> map1 = new HashMap<>();
        map1.put("dictName","开具中");
        map1.put("dictCode","12");
        list.add(map);
        list.add(map1);
        JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(list));
        String[] strings = parseDicReplace(jsonArray);
        for(String str:strings){
            System.out.println(str);
        }
    }
}
