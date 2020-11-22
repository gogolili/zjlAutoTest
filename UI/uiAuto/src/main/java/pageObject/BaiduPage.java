package pageObject;

import java.io.IOException;

import org.omg.CORBA.PUBLIC_MEMBER;

import base.LocatorBase;
import baseWeb.WebActionBase;

public class BaiduPage extends WebActionBase{
	//用于eclipse工程内部运行查找对象库文件路径
	private String path="src/main/resources/pageObjectFiles/yml/";
	//工程内读取对象库文件
	public BaiduPage() {
		//工程内读取对象库文件
		setXmlObjectPath(path + "/Baidu.yml");
		getLocatorMap();
	}
	
	public LocatorBase keyword() throws IOException{
		LocatorBase locator = getLocator("keyword");
		return locator;
	}
	
	public LocatorBase submit() throws IOException{
		LocatorBase locator = getLocator("submit");
		return locator;
	}
	
	

}
