package actions;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.bytedeco.javacpp.opencv_highgui.ButtonCallback;
import org.bytedeco.javacpp.videoInputLib.videoDevice;
import org.dom4j.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.jmx.export.annotation.ManagedMetric;

import base.LocatorBase;
import baseWeb.WebAssertionBase;
import baseWeb.WebElementBase;
import jxl.common.AssertionFailed;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.Listener.ErrorEscalating;
import pageObject.UserPage;

public class UserAction {
	WebElementBase action = new WebElementBase();
	

	/**
	 * 获取按钮行的按钮
	 * @param buttonName 按钮的名字
	 * @throws IOException 
	 */
	public void getButton(String buttonName) throws IOException{
		UserPage userPage = new UserPage();
		List<WebElement> buttonList = action.findElements(userPage.buttonList());
		for (WebElement button : buttonList) {
			if (button.isDisplayed()&&action.getinnerText(button).contains(buttonName)) {
				button.click();
				break;
			}
		}
	}
	
	/**
	 * 根据元素定位到一组数据，再根据标签名找到相对应的输入框整体，再根据输入框的tag标签找到对应的输入框位置，适用于大部分的输入框输入
	 * @param locator
	 * @param inputTitle
	 * @param content
	 * @param tagName
	 * @throws InterruptedException
	 * @throws IOException
	 */
	
	
	/**
	 * 获取必填项的输入框，根据输入框前的标签名称，输入相应的测试数据
	 * @param inputTitle 输入框前的标签
	 * @param content 输入的测试数据
	 * @throws InterruptedException 
	 * @throws IOException
	 */
	public void setMustInput(String inputTitle,String content) throws InterruptedException, IOException {
		UserPage userPage = new UserPage();
		List<WebElement> mustInputList = action.findElements(userPage.mustInput());
		for (WebElement mustInput : mustInputList) {
			if (mustInput.isDisplayed()&&action.getinnerText(mustInput).contains(inputTitle)) {
				WebElement mustInputForm = mustInput.findElement(By.tagName("input"));
				mustInputForm.clear();
				mustInputForm.sendKeys(content);
			}
		}
	}
	
	/**
	 * 返回必填输入框的元素
	 * @param inputTitle
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public WebElement getMustInput(String inputTitle) throws InterruptedException, IOException {
		UserPage userPage = new UserPage();
		List<WebElement> mustInputList = action.findElements(userPage.mustInput());
		WebElement mustInputForm=null;
		for (WebElement mustInput : mustInputList) {
			if (mustInput.isDisplayed()&&action.getinnerText(mustInput).contains(inputTitle)) {
				mustInputForm = mustInput.findElement(By.tagName("input"));
			}
		}
		return mustInputForm;
	}
	
	
	/**
	 * 获取非必填项的输入框，根据输入框前的标签名称，输入相应的测试数据
	 * @param inputTitle 输入框前的标签
	 * @param content 输入的测试数据
	 * @throws InterruptedException 
	 * @throws IOException
	 */
	
	
	public void setNotInput(String inputTitle,String content) throws InterruptedException, IOException {
		UserPage userPage = new UserPage();
		List<WebElement> notInputList = action.findElements(userPage.notInput());
		for (WebElement notInput : notInputList) {
			if (notInput.isDisplayed()&&action.getinnerText(notInput).contains(inputTitle)) {
				WebElement notInputForm = notInput.findElement(By.tagName("input"));
				notInputForm.clear();
				notInputForm.sendKeys(content);
			}
		}
	}
	
	
	/**
	 * 描述框
	 * @param inputTitle
	 * @param content
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void setdesInput(String inputTitle,String content) throws InterruptedException, IOException {
		UserPage userPage = new UserPage();
		List<WebElement> notInputList = action.findElements(userPage.notInput());
		for (WebElement notInput : notInputList) {
			if (notInput.isDisplayed()&&action.getinnerText(notInput).contains(inputTitle)) {
				WebElement notInputForm = notInput.findElement(By.tagName("textarea"));
				notInputForm.clear();
				notInputForm.sendKeys(content);
			}
		}
	}
	
	
	/**
	 * 添加用户时点击保存按钮
	 */
	public void submit(String btnName) {
		UserPage userPage = new UserPage();
		List<WebElement> bnts = action.findElements(userPage.submit());
		for (WebElement bnt : bnts) {
			if (bnt.isDisplayed()&&action.getinnerText(bnt).contains(btnName)) {
				bnt.click();
			}
		}
		
	}
	
	/**
	 * 获取浮框信息
	 * @return 浮框的文本信息
	 * @throws InterruptedException 
	 */
	public String  getFloatMessage() throws InterruptedException {
		UserPage userPage = new UserPage();
		String fltText=null;
		WebElement floatMessage = null;
		for (int i = 0; i < 5; i++) {
			floatMessage = userPage.findElement(userPage.floatMessage());
			System.out.println("============="+floatMessage);
			if (!floatMessage.isDisplayed()) {
				Thread.sleep(1000);
				continue;
			}else {
				fltText = floatMessage.getText();	
			}
		}
		return fltText;
	}
	
	/**
	 * 复选框
	 * @param row
	 * @param column
	 */
	public void slectCheckBox(int row,int column) {
		UserPage userPage = new UserPage();
		WebElement ckeckBoxCell = action.getCell(userPage.table(), row, column);
		WebElement ckeckBox = ckeckBoxCell.findElement(By.tagName("span"));
		if (!ckeckBox.getAttribute("class").contains("checked")) {
			ckeckBox.click();
		}
	
	}

	
	
	/**
	 * 点击确认框的按钮
	 * @param btnName 按钮的名称，确认or取消
	 * @throws InterruptedException 
	 */
	public void ivuModalButton(String btnName) throws InterruptedException {
		UserPage userPage = new UserPage();
		List<WebElement> ivuModalButton = action.findElements(userPage.ivuModalButton());
		for (WebElement button : ivuModalButton) {
			if (button.isDisplayed()&&action.getinnerText(button).contains(btnName)) {
				button.click();
				break;
			}else {
				System.out.println("=============又没找到，哎");
			}
		}	
	}
	
	/**
	 * 导入文件点击确认框的按钮
	 * @param btnName 按钮的名称，确认or取消
	 * @throws InterruptedException 
	 */
	public void importIvuModalButton(String btnName) throws InterruptedException {
		UserPage userPage = new UserPage();
		List<WebElement> lineButton = action.findElements(userPage.lineButton());
		for (WebElement button : lineButton) {
			if (button.isDisplayed()&&action.getinnerText(button).contains(btnName)) {
				button.click();
				break;
			}else {
				System.out.println("=============又没找到，哎");
			}
		}	
	}
	
	/**
	 * 点击行按钮，根据名称查找
	 * @param row
	 * @param btnName
	 */
	public void getLineButton(int row,String btnName) {
		UserPage userPage = new UserPage();
		WebElement lineButtonCell = action.getCell(userPage.table(),row,action.countColumns(userPage.table())-1);
		List<WebElement> lineButtons = lineButtonCell.findElements(By.xpath("//button[contains(@class,'bw-btn')]"));
		for (WebElement lineButton : lineButtons) {
			if (lineButton.isDisplayed()&&action.getinnerText(lineButton).contains(btnName)) {
				lineButton.click();
				break;
			}else {
				System.out.println("===========没找到");
			}
		}
		
	}
	
	
	/**
	 * 获取弹框文本
	 * @param btnName
	 * @throws InterruptedException
	 */
	public String  ivuModalBody() throws InterruptedException {
		UserPage userPage = new UserPage();
		List<WebElement> ivuModalBody = action.findElements(userPage.ivuModalBody());
		String ModalBody = null;
		for (WebElement bodyText : ivuModalBody) {
			if (bodyText.isDisplayed()&&"".equals(action.getinnerText(bodyText))) {
				ModalBody= action.getinnerText(bodyText);
			}
		}
		return ModalBody;	
	}
	/**
	 * 查看详情的tab页
	 * @return
	 * @throws InterruptedException
	 */
	public void getTab(String tabName) throws InterruptedException {
		UserPage userPage = new UserPage();
		List<WebElement> tabs = action.findElements(userPage.tabs());
		for (WebElement tab : tabs) {
			if (tab.isDisplayed()&&action.getinnerText(tab).contains(tabName)&&!tab.getAttribute("class").contains("active")) {
				tab.click();
			}
		}
	}
	
	
	/**
	 * 点击授权选择的角色
	 * @param index第几个，从0开始
	 */
	public void getAuthRole(int index) {
		UserPage userPage = new UserPage();
		List<WebElement> authRoles = action.findElements(userPage.authRole());
		try {
			WebElement authRole = authRoles.get(index);
			if (authRole.isDisplayed()) {
				authRole.click();
			}else {
				System.out.println("=====角色没找到=====");
			}
		}catch (IndexOutOfBoundsException e) {
			System.out.println("下标越界");
		}
		
	}
	/**
	 * 点击选择机构数的机构
	 * @param index
	 */
	public void getauthOrgTree(int index) {
		UserPage userPage = new UserPage();
		List<WebElement> authOrgTrees = action.findElements(userPage.authOrgTree());
		try {
			WebElement authOrgTree = authOrgTrees.get(index);
			authOrgTree.click();
//			if (authOrgTree.isDisplayed()) {
//				System.out.println("=============================看到了=====");
//				authOrgTree.click();
//			}else {
//				System.out.println("==============没找到机构");
//			}
		}catch (IndexOutOfBoundsException e) {
			System.out.println("下标越界");
		}
		
	}

	
	
	
	
	
	
	
	

	
	
	

	
	
	
	

	
	
	
}
