package pageObject;

import java.io.IOException;
import java.util.HashMap;

import org.omg.CORBA.PUBLIC_MEMBER;

import base.LocatorBase;
import baseWeb.WebActionBase;

public class UserPage extends WebActionBase{
	//用于eclipse工程内部运行查找对象库文件路径
	private String path="src/main/resources/pageObjectFiles/yml/";
	//工程内读取对象库文件
	public UserPage() {
		//工程内读取对象库文件
		setXmlObjectPath(path + "/User.yml");
		getLocatorMap();
	}
	
	public LocatorBase buttonList() throws IOException{
		LocatorBase locator = getLocator("buttonList");
		return locator;
	}
	
	public LocatorBase mustInput() {
		LocatorBase locator = getLocator("mustInput");
		return locator;
	}
	public LocatorBase notInput() {
		LocatorBase locator = getLocator("notInput");
		return locator;
	}
	public LocatorBase submit() {
		LocatorBase locator = getLocator("submit");
		return locator;
	}
	public LocatorBase floatMessage() {
		LocatorBase locator = getLocator("floatMessage");
		return locator;
	}
	
	public LocatorBase table() {
		LocatorBase locator = getLocator("table");
		return locator;
	}
	
	public LocatorBase ivuModalButton() {
		LocatorBase locator = getLocator("ivuModalButton");
		return locator;
	}
	
	public LocatorBase ivuModalBody() {
		LocatorBase locator = getLocator("ivuModalBody");
		return locator;
	}
	
	public LocatorBase lineButton() {
		LocatorBase locator = getLocator("lineButton");
		return locator;
	}
	
	public LocatorBase tabs() {
		LocatorBase locator = getLocator("tabs");
		return locator;
	}
	
	public LocatorBase authRole() {
		LocatorBase locator = getLocator("authRole");
		return locator;
	}
		
	public LocatorBase authOrgTree() {
		LocatorBase locator = getLocator("authOrgTree");
		return locator;
	}
	

	
	
	
	

}
