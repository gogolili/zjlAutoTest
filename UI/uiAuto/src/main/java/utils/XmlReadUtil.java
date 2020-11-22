package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import base.LocatorBase;
import base.LocatorBase.ByType;

public class XmlReadUtil {
	private static Logger log = Logger.getLogger(XmlReadUtil.class);
	
	/**
	 * 
	 * @param path  xml文件的路径
	 * @param pageName  xml文件中page层标签的pagename名字
	 * @return locatorMap key值为map里面包含了元素的文本，value值为这个元素的元素定位
	 */
	public HashMap<String, LocatorBase> readXMLDocument(String path,String pageName){
//		log.info("---------开始解析UILibrary.xml对象库----------");
//		log.info("开始读取：" + pageName + "页面信息");
		HashMap<String, LocatorBase> locatorMap = new HashMap<String,LocatorBase>();
		locatorMap.clear();
		try {
			File file = new File(path);
			if (!file.exists()) {
				throw new IOException("Can't find" + path);
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for (Iterator<?> i = root.elementIterator(); i.hasNext();) {
				Element page = (Element) i.next();
				//判断page节点的第一个属性pagename是否跟输入的pageName一致
				if (page.attribute(0).getValue().equalsIgnoreCase(pageName)) {
//					log.info("成功读取页面：" + pageName);
					//遍历page节点下的元素
					for(Iterator<?> l = page.elementIterator();l.hasNext();) {
						String type = null;
						String timeOut = "3";
						String value = null;
						String locatorName = null;
						Element locator = (Element) l.next();
						//获取元素名
						locatorName = locator.getText();
//						log.info("开始读取" + locatorName + "定位信息");
						for(Iterator<?> j = locator.attributeIterator();j.hasNext();) {
							Attribute attribute = (Attribute) j.next();
							if (attribute.getName().equals("type")) {
								type = attribute.getValue();
								log.info("读取定位方式： " + type);
							}else if (attribute.getName().equals("timeout")) {
								timeOut = attribute.getValue();
								log.info("读取元素等待时间： " + timeOut);
							}else if (attribute.getName().equals("value")) {
								value = attribute.getValue();
								log.info("读取定位内容 " + value);
							}
						}
						LocatorBase temp = new LocatorBase(value.trim(), Integer.parseInt(timeOut), getByType(type),locatorName.trim());
//						log.info("成功读取 " + locatorName+"元素信息！");
						locatorMap.put(locatorName.trim(), temp);			
					}
					continue;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return locatorMap;	
	}
	
	/**
	 * 
	 * @param path  xml文件得路径
	 * @param pageName page得文件路径名称
	 * @return
	 */
	public  HashMap<String, LocatorBase> readXMLDocument(InputStream path,String pageName) {
        //log.info("----------开始解析UILibrary.xml对象库-----------");
        //log.info("开始读取："+pageName+"页信息");
        HashMap<String, LocatorBase> locatorMap = new HashMap<String, LocatorBase>();
        locatorMap.clear();
        try {
            InputStreamReader inputStreamReader=new InputStreamReader(path,"UTF-8");
            SAXReader reader = new SAXReader();
            Document document=reader.read(inputStreamReader);
            Element root = document.getRootElement();
            for (Iterator<?> i = root.elementIterator(); i.hasNext();)
            {
                Element page = (Element) i.next();
                if (page.attribute(0).getValue().equalsIgnoreCase(pageName))
                {
                    //log.info("成功读取页名:" + pageName);
                    for (Iterator<?> l = page.elementIterator(); l.hasNext();)
                    {
                        String type = null;
                        String timeOut = "3";
                        String value = null;
                        String locatorName = null;
                        Element locator = (Element) l.next();
                        //获取元素名
                        locatorName = locator.getText();
                        //log.info("开始读取"+locatorName+"定位信息");
                        for (Iterator<?> j = locator.attributeIterator(); j.hasNext();)
                        {
                            Attribute attribute = (Attribute) j.next();
                            if (attribute.getName().equals("type"))
                            {
                                type = attribute.getValue();
                                //log.info("读取定位方式： " + type);
                            } else if (attribute.getName().equals("timeout"))
                            {
                                timeOut = attribute.getValue();
                                //log.info("读取元素等待时间 ：" + timeOut);
                            } else if (attribute.getName().equals("value"))
                            {
                                value = attribute.getValue();
                                //log.info("读取定位内容：" + value);
                            }
                        }
                        //trim()去除字符串前后空格
                        LocatorBase temp = new LocatorBase(value.trim(),Integer.parseInt(timeOut), getByType(type),locatorName.trim());
                        //log.info("成功读取 " + locatorName+"元素信息！");
                        locatorMap.put(locatorName.trim(), temp);
                    }
                    continue;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        //log.info("----------解析UILibrary.xml对象库完毕-----------\n");
        return locatorMap;
    }
	
	public static ByType getByType(String type) {
		ByType byType = ByType.xpath;//默认xpath
		if (type == null || type.equalsIgnoreCase("xpath")) {
			byType = ByType.xpath;
		}else if (type.equalsIgnoreCase("id")) {
			byType = ByType.id;
		}else if (type.equalsIgnoreCase("linkText")) {
			byType = ByType.linktext;
		}else if (type.equalsIgnoreCase("name")) {
			byType = ByType.name;
		}else if (type.equalsIgnoreCase("className")) {
			byType = ByType.className;
		}else if (type.equalsIgnoreCase("cssSelector")) {
			byType = ByType.cssSelector;
		}else if (type.equalsIgnoreCase("partiaLinkText")) {
			byType = ByType.partialLinkText;
		}else if (type.equalsIgnoreCase("tagName")) {
			byType = ByType.tagName;
		}
		
		return byType;
		
	}
	
	//返回xml的页面的page标签中的value值url
	public static String getXmlPageURL(InputStream path,String pageName) {
		String URL = null;
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(path,"UTF-8");
			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStreamReader);
			System.out.println("文档内容"+ document.asXML());
			//获取xml文档的根节点
			Element rootElement = document.getRootElement();
			//遍历xml根节点下的page节点
			for (Iterator<?> i = rootElement.elementIterator();i.hasNext();) {
				Element page = (Element) i.next();
				if (page.attribute(0).getValue().equals(pageName)) {
                    //System.out.println("page Info is:" + pageName);
                    //遍历page节点下的元素	
					for (Iterator<?> n = page.attributeIterator(); n.hasNext();) {
						Attribute attribute = (Attribute) n.next();
						if (attribute.getName().equals("value")) {
							URL=attribute.getValue().trim();
						}
						
					}
					continue;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return URL;
		
	}
	
	public static String getXmlPageURL(String path,String pageName) {
		String URL = null;
		try {
			File file = new File(path);
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			System.out.println("文档内容"+ document.asXML());
			//获取xml文档的根节点
			Element rootElement = document.getRootElement();
			//遍历xml根节点下的page节点
			for (Iterator<?> i = rootElement.elementIterator();i.hasNext();) {
				Element page = (Element) i.next();
				if (page.attribute(0).getValue().equals(pageName)) {
                    //System.out.println("page Info is:" + pageName);
                    //遍历page节点下的元素	
					for (Iterator<?> n = page.attributeIterator(); n.hasNext();) {
						Attribute attribute = (Attribute) n.next();
						if (attribute.getName().equals("value")) {
							URL=attribute.getValue().trim();
						}
						
					}
					continue;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return URL;
		
	}

	//获取testng用例中的参数
	public static String getTestngParametersValue(String path,String ParametersName) throws IOException, DocumentException {
		File file = new File(path);
		if (!file.exists()) {
			throw new IOException("Can't find " + path);
		}
		String value = null;
		SAXReader reader = new SAXReader();
        Document  document = reader.read(file);
        Element root = document.getRootElement();
        for (Iterator<?> i = root.elementIterator(); i.hasNext();) {
        	Element page = (Element) i.next();
        	if (page.attributeCount()>0) {
				if (page.attribute(0).getValue().equalsIgnoreCase(ParametersName)) {
					value = page.attribute(1).getValue();
				}
				continue;
			}	
        }
		return value;
	}
}
