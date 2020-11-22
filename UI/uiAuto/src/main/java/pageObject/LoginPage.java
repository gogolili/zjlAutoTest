package pageObject;

import java.io.IOException;

import org.omg.CORBA.PUBLIC_MEMBER;

import base.LocatorBase;
import baseWeb.WebActionBase;

public class LoginPage extends WebActionBase{
	//用于eclipse工程内部运行查找对象库文件路径
	private String path="src/main/resources/pageObjectFiles/yml/";
	//工程内读取对象库文件
	public LoginPage() {
		//工程内读取对象库文件
		setXmlObjectPath(path + "/Login.yml");
		getLocatorMap();
	}
	
	public LocatorBase username() throws IOException{
		LocatorBase locator = getLocator("username");
		return locator;
	}
	
	public LocatorBase password() throws IOException{
		LocatorBase locator = getLocator("password");
		return locator;
	}
	
	public LocatorBase submit() throws IOException{
		LocatorBase locator = getLocator("submit");
		return locator;
	}
	
	

}
