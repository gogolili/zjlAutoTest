package pageObject;

import java.io.IOException;

import org.omg.CORBA.PUBLIC_MEMBER;

import base.LocatorBase;
import baseWeb.WebActionBase;

public class MenuPage extends WebActionBase{
	//用于eclipse工程内部运行查找对象库文件路径
	private String path="src/main/resources/pageObjectFiles/yml/";
	//工程内读取对象库文件
	public MenuPage() {
		//工程内读取对象库文件
		setXmlObjectPath(path + "/Menu.yml");
		getLocatorMap();
	}
	
	/**
	 * 顶部导航元素定位
	 * @return 顶部元素定位
	 * @throws IOException
	 */
	public LocatorBase menuTop() throws IOException{
		LocatorBase locator = getLocator("menuTop");
		return locator;
	}
	
	public LocatorBase menuLeftFirst() {
		LocatorBase locator = getLocator("menuLeftFirst");
		return locator;
	}
	public LocatorBase menuLeftMiddle() {
		LocatorBase locator = getLocator("menuLeftMiddle");
		return locator;
	}
	public LocatorBase menuLeftLast() {
		LocatorBase locator = getLocator("menuLeftLast");
		return locator;
	}
	
	
	

}
