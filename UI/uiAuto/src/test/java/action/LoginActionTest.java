package action;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import actions.BaiduAction;
import actions.LoginAction;
import baseWeb.WebActionBase;
import baseWeb.WebAssertionBase;
import baseWeb.WebElementBase;
import utils.ExcelReadUtil;

public class LoginActionTest extends WebActionBase{
	WebElementBase action = new WebElementBase();
	
	@Test(description="登录页面",dataProvider="loginData")
	public void testLogin(String URL,String userName,String passWord,String expect) throws InterruptedException, IOException {
		LoginAction loginAction = new LoginAction();
		loginAction.gopage(URL);
		loginAction.testLogin(userName, passWord);
		action.sleep(5);
		WebAssertionBase.VerityTextPresent(expect, "测试账号是否出现");
	}
	
	//数据驱动案例----start
	@DataProvider(name="loginData")
	public Object[][] loginData(){
		//读取登陆用例测试数据
		String filePath = "src/main/resources/data/LoginTest.xls";
		//读取第一个sheet，第2行到第5列---第2行到第4列之间的数据
		return ExcelReadUtil.case_data_excel(0, 1, 1,0, 3, filePath);
	}
	

}
