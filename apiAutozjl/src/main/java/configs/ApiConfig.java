package configs;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiConfig {


    private String rootUrl;//环境路径
    private Map<String,String> headers = new HashMap<String, String>();//header中存放了账号信息
    private Map<String,String> params = new HashMap<String, String>();//params中存放了接口参数

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public ApiConfig(String configFilePath) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(configFilePath);
        Element rootElement = document.getRootElement();

        rootUrl = rootElement.element("rootUrl").getTextTrim();

        //取出xml文件中的params放入map中
        @SuppressWarnings("unchecked")
        List<Element> paramElements = rootElement.element("params").elements("param");
        for (Element ele:paramElements){
            params.put(ele.attributeValue("name").trim(),ele.attributeValue("value").trim());
        }

        //取出xml文件中的headers放入map中
        @SuppressWarnings("unchecked")
        List<Element> headers = rootElement.element("headers").elements("header");
        for (Element ele:headers){
            params.put(ele.attributeValue("name").trim(),ele.attributeValue("value").trim());
        }

        //项目名称
        Element projectEle = rootElement.element("project_name");
        if (projectEle!=null){//如果项目名称不为空，赋值报告名称
//            ReportUtil.setReportName(projectEle.getTextTrim());
        }


    }
}
