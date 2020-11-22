package actions;

import java.io.IOException;

import baseWeb.WebElementBase;
import pageObject.BaiduPage;
import pageObject.LoginPage;

public class LoginAction {
	WebElementBase action = new WebElementBase();
	
	//打开网页
	public void gopage(String url) {
		LoginPage login = new LoginPage();
		login.getPage(url);
	}
	
	//登录页面测试
	public void testLogin(String userName,String passWord) throws InterruptedException, IOException {
		LoginPage login = new LoginPage();
		action.type(login.username(), userName);//输入用户名
		action.type(login.password(),passWord);//输入密码
		action.click(login.submit());//点击登录按钮
	}
}
