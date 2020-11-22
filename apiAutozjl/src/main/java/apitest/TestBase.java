package apitest;

import beans.BaseBean;
import com.alibaba.fastjson.JSONPath;
import org.testng.Assert;
import utils.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestBase {

    //公共参数数据池（全局可用）
    public static Map<String,String> saveDatas = new HashMap<String, String>();

    //替换符，如果数据中包含${}则会被替换成公共参数中存储的数据
    protected Pattern replaceParamPattern = Pattern.compile("\\$\\{(.*?)\\}");

    //截取自定义方法正则表达式：__XXX(ooo)
    protected Pattern funPattern = Pattern.compile("__(\\w*?)\\((([\\w\\\\\\/:\\.\\$]*,?)*)\\)");

    protected void setSaveDates(Map<String,String> map){
        saveDatas.putAll(map);
    }


    /**
     *验证结果：$.status=0;$.result.name=${first_name}
     * @param sourchData  接口返回的json
     * @param verifyStr $.status=0;$.result.name=${first_name}
     * @param contains
     */
    protected void verifyResult(String sourchData, String verifyStr,
                                boolean contains) {
        if (StringUtil.isEmpty(verifyStr)) {
            return;
        }
        String allVerify = getCommonParam(verifyStr);
        ReportUtil.log("验证数据：" + allVerify);
        if (contains) {
            // 验证结果包含
            AssertUtil.contains(sourchData, allVerify);
        } else {
            // 通过';'分隔，通过jsonPath进行一一校验
            Pattern pattern = Pattern.compile("([^;]*)=([^;]*)");
            Matcher m = pattern.matcher(allVerify.trim());
            while (m.find()) {
                String actualValue = JSONPath.read(sourchData, m.group(1)).toString();
                String exceptValue = m.group(2);
                ReportUtil.log(String.format("验证转换后的值%s=%s", actualValue,
                        exceptValue));
                Assert.assertEquals(actualValue, exceptValue, "验证预期结果失败。");
            }
        }
    }


    /**
     * 组件预参数（处理__fucn()以及${xxxx}）
     *__fucn()中将（）内的参数返回
     * @param param
     * @return
     */
    protected String buildParam(String param) {
        // 处理${}
        param = getCommonParam(param);
        // Pattern pattern = Pattern.compile("__(.*?)\\(.*\\)");// 取__开头的函数正则表达式
        // Pattern pattern =
        // Pattern.compile("__(\\w*?)\\((\\w*,)*(\\w*)*\\)");// 取__开头的函数正则表达式
        Matcher m = funPattern.matcher(param);
        while (m.find()) {
            String funcName = m.group(1);
            String args = m.group(2);//拿到的值为aaa,bbb,ccc
            String value;
            // bodyfile属于特殊情况，不进行匹配，在post请求的时候进行处理
            if (FunctionUtil.isFunction(funcName) && !funcName.equals("bodyfile")) {
                // 属于函数助手，调用那个函数助手获取。
                value = FunctionUtil.getValue(funcName, args.split(","));
                // 解析对应的函数失败
                Assert.assertNotNull(value,String.format("解析函数失败：%s。", funcName));
                param = StringUtil.replaceFirst(param, m.group(), value);
            }
        }
        return param;
    }
    /**
     * 对应excel的save列，将取出来的数据存入全局map，excel编写时将多个要存储的字段以；号形式分隔
     * @param preParam
     */
    protected void savePreParam(String preParam) {
        // 通过';'分隔，将参数加入公共参数map中
        if (StringUtil.isEmpty(preParam)) {
            return;
        }
        String[] preParamArr = preParam.split(";");
        String key, value;
        for (String prepar : preParamArr) {
            if (StringUtil.isEmpty(prepar)) {
                continue;
            }
            key = prepar.split("=")[0];
            value = prepar.split("=")[1];
            ReportUtil.log(String.format("存储%s参数，值为：%s。", key, value));
            saveDatas.put(key, value);
        }
    }
    /**
     * 取公共参数 并替换参数
     *
     * @param param
     * @return
     */
    protected String getCommonParam(String param) {
        if (StringUtil.isEmpty(param)) {
            return "";
        }
        Matcher m = replaceParamPattern.matcher(param);// 取公共参数正则
        while (m.find()) {
            String replaceKey = m.group(1);
            String value;
            // 从公共参数池中获取值
            value = getSaveData(replaceKey);
            // 如果公共参数池中未能找到对应的值，该用例失败。
            Assert.assertNotNull(value,
                    String.format("格式化参数失败，公共参数中找不到%s。", replaceKey));
            param = param.replace(m.group(), value);
        }
        return param;
    }

    /**
     * 获取公共数据池中的数据
     *
     * @param key 公共数据的key
     * @return 对应的value
     */
    protected String getSaveData(String key){
        if ("".equals(key) || !saveDatas.containsKey(key)){
            return null;
        }else {
            return saveDatas.get(key);
        }
    }

    /**
     * 根据配置读取测试用例
     * @param clz 需要转换的类，即与excel对应的实例类
     * @param excelPathArr 所有excel的路径配置
     * @param sheetNameArr  本次需要过滤的excel文件名
     * @param <T>
     * @return
     */
    protected <T extends BaseBean> List<T> readExcelData(Class<T> clz,String[] excelPathArr,String[] sheetNameArr){
        List<T> allExcelData = new ArrayList<T>();//excel文件数组
        List<T> temArrayList = new ArrayList<T>();
        for (String excelPath:excelPathArr){
            File file = Paths.get(System.getProperty("user.dir"),excelPath).toFile();
            temArrayList.clear();
            if (sheetNameArr.length == 0 || sheetNameArr[0] == ""){
                temArrayList = ExcelUtil.readExcel(clz,file.getAbsolutePath());
            }else {
                for (String sheetName:sheetNameArr){
                    temArrayList.addAll(ExcelUtil.readExcel(clz,file.getAbsolutePath(),sheetName));
                }
            }
            temArrayList.forEach((bean) -> {
                bean.setExcelName(file.getName());
            });
            allExcelData.addAll(temArrayList);

        }
            return allExcelData;
    }


    /**
     * 对应excel的save列，将取出来的数据存入全局map，excel编写时将多个要存储的字段以；号形式分隔
     * @param preHeader
     */
    protected Map<String,String> savePreheader(String preHeader) {
        Map<String,String> headers = new HashMap<String, String>();
        // 通过';'分隔，将参数加入公共参数map中
        if (StringUtil.isEmpty(preHeader)) {
            return null;
        }
        String[] preHeaderArr = preHeader.split(";");
        String key, value;
        for (String prehead : preHeaderArr) {
            if (StringUtil.isEmpty(prehead)) {
                continue;
            }
            key = prehead.split("=")[0];
            value = prehead.split("=")[1];
            headers.put(key, value);
        }
        return headers;
    }


}
