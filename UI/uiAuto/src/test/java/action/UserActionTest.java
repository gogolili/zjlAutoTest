package action;

import org.testng.annotations.Test;
import org.apache.tools.ant.types.FileList.FileName;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import actions.LoginAction;
import actions.MenuAction;
import actions.UserAction;
import baseWeb.WebActionBase;
import baseWeb.WebAssertionBase;
import baseWeb.WebElementBase;
import utils.ExcelReadUtil;
import utils.FileMangerUtil;

public class UserActionTest extends WebActionBase {
	WebElementBase action = new WebElementBase();
	
	@Test(description="登录页面",dataProvider="loginData",priority=0)
	public void testLogin(String URL,String userName,String passWord,String expect) throws InterruptedException, IOException {
		LoginAction loginAction = new LoginAction();
		loginAction.gopage(URL);
		loginAction.testLogin(userName, passWord);
		action.sleep(5);
		WebAssertionBase.VerityTextPresent(expect, "测试账号是否出现");
	}
	
	@Test(description="进入菜单",priority=1)
	public void testMenu() throws InterruptedException, IOException {
		MenuAction menuAction = new MenuAction();
		menuAction.testMenuTop("用户中心");
		menuAction.testMenuLeft("用户管理");
		WebAssertionBase.VerityTextPresent("用户管理", "用户管理是否出现");
	}
	
//	@Test(description="添加用户",dataProvider="userData",priority=2)
	public void testAddUser(String userAccount,String userName,String departMent,String telephone,String describe,String email,String exceptStr) throws InterruptedException, IOException {
		UserAction userAction = new UserAction();
		userAction.getButton("添加用户");
		userAction.setMustInput("用户账号", userAccount);
		userAction.setMustInput("用户姓名", userName);
		userAction.setNotInput("所属部门", departMent);
		userAction.setNotInput("手机号码", telephone);
		userAction.setdesInput("用户描述", describe);
		userAction.setNotInput("邮箱地址", email);
		userAction.submit("保存");
		WebAssertionBase.VerityString(userAction.getFloatMessage(), exceptStr, "验证用户是否添加成功");
	}
	
//	@Test(description="批量解除锁定",priority=3)
	public void testBatchRelease() throws IOException, InterruptedException {
		UserAction userAction = new UserAction();
		userAction.slectCheckBox(0, 0);
		userAction.getButton("批量解除锁定");
		userAction.ivuModalButton("确定");
		WebAssertionBase.VerityString(userAction.getFloatMessage(), "解锁成功", "验证批量解除是否成功");
	}
	
//	@Test(description="下载模版文件",priority=3)
	public void testDownLoad() throws IOException, InterruptedException {
		String FilePath = downloadsPath + "用户导入模版.xls";
		File file = new File(FilePath);
		FileMangerUtil.clearFile(FilePath);
		UserAction userAction = new UserAction();
		userAction.getButton("下载模版文件");
		action.sleep(5);
		WebAssertionBase.VerityBoolean(file.exists(), true, "验证用户模板下载是否成功");		
	}
	
	@Test(description="批量导入用户",priority=4)
	public void testImportUser() throws IOException, InterruptedException {
		String uploadPath = "E:\\Testfan\\workspace\\uiAuto\\upload\\upload.exe";
		UserAction userAction = new UserAction();
		userAction.getButton("批量导入用户");
		userAction.ivuModalButton("点击选择文件");
		Runtime.getRuntime().exec(uploadPath);
		action.sleep(2);
		userAction.importIvuModalButton("确定");
		action.sleep(5);
	}
	
//	@Test(description="批量删除用户",priority=3)
	public void testDelUser() throws IOException, InterruptedException {
		UserAction userAction = new UserAction();
		userAction.slectCheckBox(0, 0);
		userAction.getButton("批量删除用户");
		userAction.ivuModalButton("确定");
		WebAssertionBase.VerityString(userAction.getFloatMessage(), "删除成功", "验证批量删除是否成功");
	}
//	@Test(description="查看详情",priority=3)
	public void testDetail() throws InterruptedException{
		UserAction userAction = new UserAction();
		userAction.getLineButton(0, "查看详情");
		userAction.getTab("授权信息");
		action.sleep(2);
		userAction.ivuModalButton("关闭");
		action.sleep(3);
	}
	
//	@Test(description="重置密码",priority=3)
	public void testResetPasswd() throws InterruptedException{
		UserAction userAction = new UserAction();
		userAction.getLineButton(0, "重置密码");
		action.sleep(2);
		userAction.ivuModalButton("确定");
		action.sleep(3);
		WebAssertionBase.VerityString(userAction.getFloatMessage(), "重置密码成功", "验证重置密码是否成功");
		
	}
	
//	@Test(description="禁用/启用",priority=3)
	public void testAccountStatus() throws InterruptedException{
		UserAction userAction = new UserAction();
		userAction.getLineButton(0, "禁用");
		WebAssertionBase.VerityString(userAction.ivuModalBody(), "确定禁用 ？", "验证禁用账号成功");
		action.sleep(2);
		userAction.ivuModalButton("确定");
		action.sleep(3);
	}
	
//	@Test(description="编辑用户",priority=3)
	public void testUpdateUser() throws InterruptedException, IOException {
		Boolean flag = false;
		UserAction userAction = new UserAction();
		userAction.getLineButton(0, "编辑用户");
//		flag = userAction.getMustInput("用户账号").getAttribute("class").contains("disabled");
//		System.out.println(flag);
//		WebAssertionBase.VerityBoolean(flag, true, "验证用户账号的输入框不可编辑");
		userAction.setMustInput("用户姓名", "zhangjingli");
		userAction.setNotInput("所属部门", "wrwrr");
		userAction.setNotInput("手机号码", "18210403002");
		userAction.setdesInput("用户描述", "描述");
		userAction.setNotInput("邮箱地址", "454444444@qq.com");
		userAction.submit("确定");
		WebAssertionBase.VerityString(userAction.getFloatMessage(), "编辑用户成功", "验证用户是否编辑成功");
	}
	
//	@Test(description="编辑授权",priority=3)
	public void testUpdateAuth() throws InterruptedException{
		UserAction userAction = new UserAction();
		userAction.getLineButton(0, "编辑授权");
		action.sleep(3);
		userAction.getAuthRole(0);
		action.sleep(7);
		userAction.getauthOrgTree(2);
		action.sleep(3);
		userAction.ivuModalButton("保存");
		userAction.ivuModalButton("否");
		action.sleep(3);
	}
	

	
	
	//数据驱动案例----start
	@DataProvider(name="userData")
	public Object[][] userData(){
		//读取登陆用例测试数据
		String filePath = "src/main/resources/data/UserTest.xls";
		//读取第一个sheet，第2行到第5列---第2行到第4列之间的数据
		return ExcelReadUtil.case_data_excel(0, 1, 1,0, 6, filePath);
	}
	
	@DataProvider(name="loginData")
	public Object[][] loginData(){
		//读取登陆用例测试数据
		String filePath = "src/main/resources/data/LoginTest.xls";
		//读取第一个sheet，第2行到第5列---第2行到第4列之间的数据
		return ExcelReadUtil.case_data_excel(0, 1, 1,0, 3, filePath);
	}
}
