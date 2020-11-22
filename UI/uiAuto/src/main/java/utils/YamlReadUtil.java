package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import org.testng.internal.Yaml;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import base.LocatorBase;
import base.LocatorBase.ByType;

public class YamlReadUtil {

	/**
	 * 
	 * @param path  yml文件的路径
	 * @param pageName yml文件中的pagename，即pageObject包中的文件路径地址
	 * @return
	 * @throws FileNotFoundException 
	 * @throws YamlException 
	 */
	
	public static HashMap<String, LocatorBase> getLocatorMap(String path,String pageName) throws FileNotFoundException, YamlException{
		HashMap<String, LocatorBase> locatorHashMap = new HashMap();
		YamlReader yamlReader = new YamlReader(new FileReader(path));
		Object yamlObject = yamlReader.read();
		Map yamlMap = (Map) yamlObject;		
		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, Object>> pages = (ArrayList<HashMap<String, Object>>) yamlMap.get("pages");
		for(int i=0;i<pages.size();i++) {//遍历page节点
			HashMap<String, Object> pageNode = pages.get(i);//获取page节点
			System.out.println(pageNode);
			HashMap<String, Object> pageElement = (HashMap<String, Object>) pageNode.get("page");
			if (pageElement.get("pageName").toString().equalsIgnoreCase(pageName)) {//判断是否需要获取的page节点
//				System.out.println(pageElement.get("desc"));
				List<HashMap<String,Object>> locators = (List<HashMap<String,Object>>)pageElement.get("locators");//获取locators列表
				for (int j = 0; j < locators.size(); j++) {//遍历locators列表
					HashMap<String,Object> locatorNode = locators.get(j);
					LocatorBase locator = new LocatorBase();
					locator.setType(getByType(locatorNode.get("type").toString()));
					locator.setValue(locatorNode.get("value").toString());
					locator.setTimeout(Integer.parseInt(locatorNode.get("timeout").toString()));
                    locator.setLocatorName(locatorNode.get("name").toString());
                    locatorHashMap.put(locatorNode.get("name").toString(),locator);		
				}
			}else {
				continue;
			}
			
		}
		
		
		return locatorHashMap;
	}
	
	/**
	 * 获取yml文件的url
	 * @param path
	 * @param pageName
	 * @return
	 * @throws FileNotFoundException
	 * @throws YamlException
	 */
	public static String getYamlPageUrl(String path,String pageName) throws FileNotFoundException, YamlException {
		Map<String,LocatorBase> locatorHashMap=new HashMap<>();
        YamlReader yamlReader=new YamlReader(new FileReader(path));
        Object yamlObject=yamlReader.read();
        Map yamlMap=(Map) yamlObject;
        ArrayList<HashMap<String,Object>> pages=(ArrayList<HashMap<String,Object>>)yamlMap.get("pages");
        String url="";
        for (int i=0;i<pages.size();i++)//遍历Page节点
        {
            HashMap<String,Object> pageNode=pages.get(i);//获取page节点
            HashMap<String,Object> pageElement=(HashMap<String,Object>)pageNode.get("page");
            if (pageElement.get("pageName").toString().equalsIgnoreCase(pageName))//判断是否需要获取的Page节点
            {
                url=pageElement.get("value").toString();
            }else {continue;}
        }
        return url;
	}
	
	
	public static ByType getByType(String type) {
		LocatorBase.ByType byType =LocatorBase.ByType.xpath;
		if (type == null || type.equalsIgnoreCase("xpath")) {
            byType = LocatorBase.ByType.xpath;
        } else if (type.equalsIgnoreCase("id")) {
            byType = LocatorBase.ByType.id;
        } else if (type.equalsIgnoreCase("linkText")) {
            byType = LocatorBase.ByType.linktext;
        } else if (type.equalsIgnoreCase("name")) {
            byType = LocatorBase.ByType.name;
        } else if (type.equalsIgnoreCase("className")) {
            byType = LocatorBase.ByType.className;
        } else if (type.equalsIgnoreCase("cssSelector")) {
            byType = LocatorBase.ByType.cssSelector;
        } else if (type.equalsIgnoreCase("partialLinkText")) {
            byType = LocatorBase.ByType.partialLinkText;
        } else if (type.equalsIgnoreCase("tagName")) {
            byType = LocatorBase.ByType.tagName;
        }
		return byType;
	}
}
