package action;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import actions.BaiduAction;
import actions.LoginAction;
import actions.MenuAction;
import baseWeb.WebActionBase;
import baseWeb.WebAssertionBase;
import baseWeb.WebElementBase;
import utils.ExcelReadUtil;

public class MenuActionTest extends WebActionBase {
	WebElementBase action = new WebElementBase();

	public void testMenuTop(String menuTopName) throws InterruptedException, IOException {
		MenuAction menuAction = new MenuAction();
		menuAction.testMenuTop(menuTopName);
		action.sleep(5);
		WebAssertionBase.VerityTextPresent(menuTopName, "测试是否进入" + menuTopName + "产品");
	}

	public void testMenuLeft(String... menuLeftName) throws InterruptedException, IOException {
		MenuAction menuAction = new MenuAction();
		menuAction.testMenuLeft(menuLeftName);
		action.sleep(5);
		WebAssertionBase.VerityTextPresent(menuLeftName[menuLeftName.length - 1],
				"测试是否进入" + menuLeftName[menuLeftName.length - 1] + "页面");
	}
	
//	public void testMenu(String menuTopName,String... menuLeftName) throws InterruptedException, IOException {
//		testMenuTop(menuTopName);
//		testMenuLeft(menuLeftName);
//		
//	}
	
	
}
