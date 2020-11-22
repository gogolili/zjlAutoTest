package action;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import actions.BaiduAction;
import baseWeb.WebActionBase;
import baseWeb.WebAssertionBase;
import baseWeb.WebElementBase;
import utils.ExcelReadUtil;

public class BaiduActionTest extends WebActionBase{
	WebElementBase action = new WebElementBase();
	
	@Test(description="百度界面",dataProvider="loginData")
	public void testBaidu(String URL,String data1,String data2,String expect) throws InterruptedException, IOException {
		BaiduAction baiduAction = new BaiduAction();
		baiduAction.gopage(URL);
		action.sleep(5);
		baiduAction.testBaidu(data1, data2);
		WebAssertionBase.VerityTextPresent(expect, "测试百度是否出现");
	}
	
	//数据驱动案例----start
	@DataProvider(name="loginData")
	public Object[][] loginData(){
		//读取登陆用例测试数据
		String filePath = "src/main/resources/data/BaiduTest.xls";
		//读取第一个sheet，第2行到第5列---第2行到第4列之间的数据
		return ExcelReadUtil.case_data_excel(0, 1, 1,0, 3, filePath);
	}
	

}
