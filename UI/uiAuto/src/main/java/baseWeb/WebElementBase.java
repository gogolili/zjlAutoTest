package baseWeb;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By.ByTagName;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.io.Files;

import base.LocatorBase;
import utils.ScreenShotUtil;

public class WebElementBase extends WebCaseBase {
	private static Logger log = Logger.getLogger(WebElementBase.class);
	public static ArrayList<Exception> noSuchElementExceptions = new ArrayList<Exception>();

	// 新添加---表格的操作,返回行
	public int countRows(LocatorBase locator) {
		// 查找table元素
		WebElement table = findElement(locator);
		// 遍历table元素下的tr元素
		List<WebElement> trList = table.findElements(By.tagName("tr"));
		int rows = trList.size();
		return rows;
	}

	// 新添加---表格的操作,返回列
	public int countColumns(LocatorBase locator) {
		// 查找table元素
		WebElement table = findElement(locator);
		// 遍历table元素下的tr元素
		List<WebElement> trList = table.findElements(By.tagName("tr"));
		// 根据指定行获取所有td元素
		List<WebElement> tdList = trList.get(0).findElements(By.tagName("td"));
		int columns = tdList.size();
		return columns;
	}

	// 新添加---表格的操作,返回单元格的元素
	public WebElement getCell(LocatorBase locator, int row, int column) {
		// 查找table元素
		WebElement table = findElement(locator);
		// 遍历table元素下的tr元素
		List<WebElement> trList = table.findElements(By.tagName("tr"));
		// 根据指定行获取所有td元素
		List<WebElement> tdList = trList.get(row).findElements(By.tagName("td"));
		return tdList.get(column);
	}

	// 返回操作
	public void clickBackButton() {
		driver.navigate().back();
	}

	// 文本框输入操作
	public void type(LocatorBase locator, String value) {
		try {
			WebElement webElement = findElement(locator);
			webElement.sendKeys(value);
			log.info("input输入：" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "  value:" + value + "]");
		} catch (NoSuchElementException e) {
			log.error("找不到元素，input输入失败:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
			e.printStackTrace();
		}
	}

	// 模拟鼠标键盘操作
	public void type_actio(LocatorBase locator, String value) {
		Actions actions = new Actions(driver);
		WebElement webElement = findElement(locator);
		actions.sendKeys(webElement, value);
	}

	// 普通单击操作
	public void click(LocatorBase locator) {
		try {
			WebElement webElement = findElement(locator);
			webElement.click();
			log.info("click元素：" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]成功！");
		} catch (NoSuchElementException e) {
			log.error("找不到元素，click失败:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
			e.printStackTrace();
			throw e;
		}
	}

	// 选择下拉框操作
	public void selectByText(LocatorBase locator, String text) {
		try {
			WebElement webElement = findElement(locator);
			Select select = new Select(webElement);
			log.info("选择select标签：" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
			try {
				select.selectByVisibleText(text);
				log.info("选择下拉列表项：" + text);
			} catch (NoSuchElementException notByValue) {
				log.info("找不到下拉值，选择下拉列表项失败 " + text);
				throw notByValue;
			}

		} catch (NoSuchElementException e) {
			log.error("找不到元素，选择select标签失败:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
			throw e;
		}
	}

	// 选择下拉框操作，选择下拉值
	public void selectByValue(LocatorBase locator, String value) {
		Select select;
		try {
			WebElement webElement = findElement(locator);
			select = new Select(webElement);
			log.info("选择select标签:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
		} catch (NoSuchElementException e) {
			log.error("找不到元素，选择select标签失败:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
			throw e;
		}
		try {
			select.selectByValue(value);
			log.info("选择下拉列表项：" + value);
		} catch (NoSuchElementException notByValue) {
			log.info("找不到下拉值，选择下拉列表项失败 " + value);
			throw notByValue;
		}
	}

	// 选择下拉框操作，按index
	public void selectByIndex(LocatorBase locator, int index) {
		// TODO 自动生成的方法存根
		Select select;
		try {
			WebElement webElement = findElement(locator);
			select = new Select(webElement);
			log.info("选择select标签:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
		} catch (NoSuchElementException e) {
			// TODO: handle exception
			log.error("找不到元素，选择select标签失败" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
			throw e;
		}
		try {
			// select.selectByValue(value);
			select.selectByIndex(index);
			log.info("选择下拉列表项：" + index);

		} catch (NoSuchElementException notByindex) {
			// TODO: handle exception
			log.info("找不到下拉值，选择下拉列表项失败 " + index);
			throw notByindex;
		}
	}

	// 获取下拉列表的value属性
	public String getSelectOptionValue(LocatorBase selectLocator, String optionText) {
		WebElement webElement = driver
				.findElement(By.xpath(selectLocator.getElement() + "//option[text()='" + optionText + "']"));
		return webElement.getAttribute("value");
	}

	// 获取下拉列表的文本
	public String getSelectOptionText(LocatorBase selectLocator, String optinValue) {
		WebElement webElement = driver
				.findElement(By.xpath(selectLocator.getElement() + "//option[text()='" + optinValue + "']"));
		return webElement.getText();
	}

	// 单击确认按钮
	public void alertConfirm() {
		Alert alert = driver.switchTo().alert();
		try {
			alert.accept();
			log.info("点击确认按钮");
		} catch (NoAlertPresentException notFindAlert) {
			log.error("找不到确认按钮");
			throw notFindAlert;
		}
	}

	// 点击取消按钮
	public void alertDismiss() {
		Alert alert = driver.switchTo().alert();
		try {
			alert.dismiss();
			log.info("点击取消按钮");
		} catch (NoAlertPresentException notFindAlert) {
			// TODO: handle exception
			// throw notFindAlert;
			log.error("找不到取消按钮");
			throw notFindAlert;
		}
	}

	// 获取对话框文本
	public String getAlertText() {
		Alert alert = driver.switchTo().alert();
		try {
			String text = alert.getText().toString();
			log.info("获取对话框文本：" + text);
			return text;
		} catch (NoAlertPresentException notFindAlert) {
			// TODO: handle exception
			log.error("找不到对话框");
			// return "找不到对话框";
			throw notFindAlert;

		}
	}

	// 双击操作
	public void click_double(LocatorBase locator) {
		WebElement webElement = findElement(locator);
		Actions actions = new Actions(driver);
		actions.doubleClick(webElement).perform();
	}

	// 清除文本框内容
	public void clear(LocatorBase locator) {
		try {
			WebElement webElement = findElement(locator);
			webElement.clear();
			log.info("清除input值:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");
		} catch (NoSuchElementException e) {
			log.error("清除input值失败:" + locator.getLocatorName() + "[" + "By." + locator.getType() + ":"
					+ locator.getElement() + "]");

		}
	}

	// 切换frame/irame框架
	// 定位frame位置，并选取frame
	public void switchToFrame(LocatorBase locator) {
		WebElement frameElement = findElement(locator);
		driver.switchTo().frame(frameElement);
	}

	// iFrame有ID 或者 name的情况,直接赋值
	public void switchToFrame(String value) {
		driver.switchTo().frame(value);
	}

	// frame的序号,第一个为0
	public void switchToFrame(int index) {
		driver.switchTo().frame(index);
	}

	// 跳出frame
	public void switchToDefaultFrame() {
		driver.switchTo().defaultContent();
	}

	// 多窗口切换
	public void switchToWindow(int i) {
		String[] handls = new String[driver.getWindowHandles().size()];
		driver.getWindowHandles().toArray(handls);
		driver.switchTo().window(handls[i]);
	}

	// 隐士等待，t最大等待时间，秒为单位
	public void Waitformax(int t) {
		driver.manage().timeouts().implicitlyWait(t, TimeUnit.SECONDS);
	}

	// 获取元素文本
	public String getText(LocatorBase locator) {
		WebElement webElement = findElement(locator);
		String text = webElement.getText();
		return text;
	}

	// 按元素获取元素的文本信息
	public String getinnerText(WebElement webElement) {
		String text = webElement.getAttribute("innerText");
		return text;
	}

	// 获取元素属性的值
	public String getAttribute(LocatorBase locator, String attributeName) {
		WebElement webElement = findElement(locator);
		String value = webElement.getAttribute(attributeName);
		return value;
	}

	// 获取当前URL
	public String getUrl() {
		String url = driver.getCurrentUrl();
		return url;
	}

	// 获取当前网页标题
	public String getTitle() {
		String title = driver.getTitle();
		return title;
	}

	// 截屏方法，参数：文件保存路径、文件名
	public void Snapshot(String FileDriver, String Filename) throws Exception {

		try {
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			Files.copy(scrFile, new File(FileDriver + Filename));
			System.out.println("错误截图：" + FileDriver + Filename);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// 显示等待，程序休眠暂停
	public void sleep(long time) {
		try {
			log.info("等待" + time + "秒");
			Thread.sleep(time * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 显示等待，判断页面是否完全加载完成
	public void pagefoload(long time) {
		ExpectedCondition<Boolean> pageLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver input) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, time * 1000);
		wait.until(pageLoad);
	}

	// 执行js脚本
	public void executeJS(String js) {
		((JavascriptExecutor) driver).executeScript(js);
		System.out.println("执行JS脚本：" + js);
	}

	// 判断一组元素是否存在
	public boolean isElementsPresent(LocatorBase locator, int timeOut) throws InterruptedException {
		log.info("等待" + timeOut + "秒判断元素：" + locator.getElement() + "是否存在");
		boolean isPresent = false;
		Thread.sleep(timeOut * 1000);
		List<WebElement> we = findElements(locator);
		if (we.size() != 0) {
			isPresent = true;
		}
		log.info("判断结果为：" + isPresent);
		return isPresent;
	}

	// 执行cmd命令,cmd为命令
	public void executeCmd(String cmd) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		runtime.exec("cmd /c start" + cmd);
	}

	// 判断元素是否显示,true显示，false隐藏
	public boolean isElementDisplayed(LocatorBase locator) {
		WebElementBase action = new WebElementBase();
		WebElement webElement = action.findElement(locator);
		// webElement.isEnabled();//估计是多余的,可编辑状态
		log.info("元素显示状态为：" + webElement.isDisplayed());
		return webElement.isDisplayed();// 显示状态
	}

	// 等待30秒让元素可见
	public void DisplayElement(LocatorBase locator) {
		WebElementBase action = new WebElementBase();
		WebDriverWait webDriverWait = new WebDriverWait(driver, 30);
		webDriverWait.until(ExpectedConditions.visibilityOf(action.findElement(locator))).isDisplayed();
	}

	//

	// 查找某个元素,及其异常处理
	public WebElement findElement(final LocatorBase locator) {
		WebElement webElement = null;
		try {
			webElement = new WebDriverWait(driver, 20).until(new ExpectedCondition<WebElement>() {
				@Override
				public WebElement apply(WebDriver input) {
					WebElement element = null;
					element = getElement(locator);
					return element;
				}
			});
			return webElement;
		} catch (NoSuchElementException e) {
			log.info("无法定位页面元素");
			e.printStackTrace();
			WebAssertionBase.assertInfoList.add(
					"failed,找不到元素：[" + locator.getType() + ":" + locator.getElement() + "等待:" + locator.getWaitSec());
			noSuchElementExceptions.add(e);
			WebAssertionBase.messageList.add("找不到所需页面元素[" + locator.getElement() + "]:failed");
			ScreenShotUtil screenShot = new ScreenShotUtil(driver);
			// 设置截图名字
			Date nowDate = new Date();
			screenShot.setScreenName(this.formatDate(nowDate));
			screenShot.takeScreenshot();
			// 报表展示截图
			this.showscreenShot(nowDate);
			log.info(this.formatDate(nowDate));
			return webElement;
		} catch (TimeoutException e) {
			log.info("超时无法定位页面元素");
			e.printStackTrace();
			WebAssertionBase.assertInfoList.add(
					"failed,超时找不到元素：[" + locator.getType() + ":" + locator.getElement() + "等待:" + locator.getWaitSec());
			noSuchElementExceptions.add(e);
			WebAssertionBase.messageList.add("超时找不到所需页面元素[" + locator.getElement() + "]:failed");
			ScreenShotUtil screenShot = new ScreenShotUtil(driver);
			// 设置截图名字
			Date nowDate = new Date();
			screenShot.setScreenName(this.formatDate(nowDate));
			screenShot.takeScreenshot();
			// 展示报表截图
			this.showscreenShot(nowDate);
			log.info(this.formatDate(nowDate));
			return webElement;
		} catch (ElementNotVisibleException e) {
			log.info("超时无法定位页面元素");
			e.printStackTrace();
			WebAssertionBase.assertInfoList.add(
					"failed,超时找不到元素：[" + locator.getType() + ":" + locator.getElement() + "等待:" + locator.getWaitSec());
			noSuchElementExceptions.add(e);
			WebAssertionBase.messageList.add("超时页面元素不可视[" + locator.getElement() + "]:failed");
			ScreenShotUtil screenShot = new ScreenShotUtil(driver);
			// 设置截图名字
			Date nowDate = new Date();
			screenShot.setScreenName(this.formatDate(nowDate));
			screenShot.takeScreenshot();
			// 展示报表截图
			this.showscreenShot(nowDate);
			log.info(this.formatDate(nowDate));
			return webElement;
		}
	}

	// 查找一组元素
	public List<WebElement> findElements(final LocatorBase locator) {
		List<WebElement> webElements = null;
		try {
			webElements = (new WebDriverWait(driver, 20)).until(new ExpectedCondition<List<WebElement>>() {

				@Override
				public List<WebElement> apply(WebDriver driver) {
					// TODO 自动生成的方法存根
					List<WebElement> element = null;
					element = getElements(locator);
					return element;
				}
			});
			return webElements;
		} catch (NoSuchElementException e) {
			// TODO: handle exception
			log.info("无法定位页面元素");
			e.printStackTrace();
			WebAssertionBase.assertInfoList.add(
					"failed,找不到元素：[" + locator.getType() + ":" + locator.getElement() + "等待:" + locator.getWaitSec());
			noSuchElementExceptions.add(e);
			WebAssertionBase.messageList.add("找不到所需页面元素[" + locator.getElement() + "]:failed");
			ScreenShotUtil screenShot = new ScreenShotUtil(driver);
			// 设置截图名字
			Date nowDate = new Date();
			screenShot.setScreenName(this.formatDate(nowDate));
			screenShot.takeScreenshot();
			// 报表展示截图
			this.showscreenShot(nowDate);
			log.info(this.formatDate(nowDate));
			return webElements;
		} catch (TimeoutException e) {
			// TODO: handle exception
			log.info("查找页面元素超时");
			e.printStackTrace();
			WebAssertionBase.assertInfoList.add(
					"failed,超时找不到元素：[" + locator.getType() + ":" + locator.getElement() + "等待:" + locator.getWaitSec());
			noSuchElementExceptions.add(e);
			WebAssertionBase.messageList.add("超时找不到所需页面元素[" + locator.getElement() + "]:failed");
			ScreenShotUtil screenShot = new ScreenShotUtil(driver);
			// 设置截图名字
			Date nowDate = new Date();
			screenShot.setScreenName(this.formatDate(nowDate));
			screenShot.takeScreenshot();
			// 报表展示截图
			this.showscreenShot(nowDate);
			log.info(this.formatDate(nowDate));
			// Assertion.assertInfolList.add(arg0)
			return webElements;
		} catch (ElementNotVisibleException e) {
			// TODO: handle exception
			log.info("查找页面元素超时");
			e.printStackTrace();
			WebAssertionBase.assertInfoList.add(
					"failed,页面元素不可视：[" + locator.getType() + ":" + locator.getElement() + "等待:" + locator.getWaitSec());
			noSuchElementExceptions.add(e);
			WebAssertionBase.messageList.add("超时页面元素不可视[" + locator.getElement() + "]:failed");
			ScreenShotUtil screenShot = new ScreenShotUtil(driver);
			// 设置截图名字
			Date nowDate = new Date();
			screenShot.setScreenName(this.formatDate(nowDate));
			screenShot.takeScreenshot();
			// 展示报表截图
			this.showscreenShot(nowDate);
			log.info(this.formatDate(nowDate));
			// Assertion.assertInfolList.add(arg0)
			return webElements;
		}
	}

	public WebElement getElement(LocatorBase locator) {
		// locator.getElement(),获取对象库对象定位信息
		log.info("查找元素" + locator.getLocatorName() + "方式" + "[" + "By." + locator.getType() + ":" + locator.getElement()
				+ "]");
		WebElement webElement;
		switch (locator.getType()) {
		case xpath:
			webElement = driver.findElement(By.xpath(locator.getElement()));
			break;
		case id:
			webElement = driver.findElement(By.id(locator.getElement()));
			break;
		case cssSelector:
			webElement = driver.findElement(By.cssSelector(locator.getElement()));
			break;
		case name:
			webElement = driver.findElement(By.name(locator.getElement()));
			break;
		case className:
			webElement = driver.findElement(By.className(locator.getElement()));
			break;
		case linktext:
			webElement = driver.findElement(By.linkText(locator.getElement()));
			break;
		case partialLinkText:
			webElement = driver.findElement(By.partialLinkText(locator.getElement()));
			break;
		case tagName:
			webElement = driver.findElement(By.tagName(locator.getElement()));
			break;
		default:
			webElement = driver.findElement(By.xpath(locator.getElement()));
			break;
		}
		return webElement;
	}

	// 通过定位信息获取一组数据
	public List<WebElement> getElements(LocatorBase locator) {
		log.info("查找一组元素：" + locator.getLocatorName() + " 方式" + "[" + "By." + locator.getType() + ":"
				+ locator.getElement() + "]");
		List<WebElement> webElements;
		switch (locator.getType()) {
		case xpath:
			webElements = driver.findElements(By.xpath(locator.getElement()));
			break;
		case id:
			webElements = driver.findElements(By.id(locator.getElement()));
			break;
		case cssSelector:
			webElements = driver.findElements(By.cssSelector(locator.getElement()));
			break;
		case name:
			webElements = driver.findElements(By.name(locator.getElement()));
			break;
		case className:
			webElements = driver.findElements(By.className(locator.getElement()));
			break;
		case linktext:
			webElements = driver.findElements(By.linkText(locator.getElement()));
			break;
		case partialLinkText:
			webElements = driver.findElements(By.partialLinkText(locator.getElement()));
			break;
		case tagName:
			webElements = driver.findElements(By.tagName(locator.getElement()));
			break;
		default:
			webElements = driver.findElements(By.xpath(locator.getElement()));
			break;
		}
		return webElements;
	}

	// 将时间格式转换为字符串类型
	private String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmssSSS");
		return formatter.format(date).toString();
	}

	// 报表展示截图
	private void showscreenShot(Date nowDate) {
		WebAssertionBase.messageList.add("&lt;a class=\"clickbox\" href=\"#url\"&gt;\n" + "&lt;img src=\"snapshot/"
				+ this.formatDate(nowDate) + ".jpg\" height=\"100\" width=\"100\" alt=\"\" /&gt;\n"
				+ "&lt;b class=\"lightbox\"&gt;\n" + "&lt;b class=\"light\"&gt;&lt;/b&gt;\n"
				+ "&lt;b class=\"box\"&gt;\n" + "&lt;img src=\"snapshot/" + this.formatDate(nowDate)
				+ ".jpg\" height=\"530\" width=\"1024\" onmousewheel=\"return bigimg(this)\" alt=\"\" /&gt;\n"
				+ "&lt;span&gt;滚动鼠标缩放大小,点击X关闭当前图片，返回报表界面.&lt;br /&gt;&lt;i&gt;&lt;/i&gt;&lt;/span&gt;\n"
				+ "&lt;/b&gt;\n" + "&lt;/b&gt;\n" + "&lt;/a&gt;\n" + "&lt;br class=\"clear\" /&gt;\n"
				+ "&lt;a class=\"clickbox\" href=\"#url\"&gt;" + "点击查看大图" + "&lt;b class=\"lightbox\"&gt;"
				+ "&lt;b class=\"light\"&gt;&lt;/b&gt;" + "&lt;b class=\"box\"&gt;&lt;img src=\"snapshot/"
				+ this.formatDate(nowDate)
				+ ".jpg\" height=\"530\" width=\"1024\" onmousewheel=\"return bigimg(this)\" alt=\"\" /&gt;"
				+ "&lt;span&gt;滚动鼠标缩放大小,点击X关闭当前图片，返回报表界面." + "&lt;br /&gt;&lt;i&gt;&lt;/i&gt;&lt;/span&gt;"
				+ "&lt;/b&gt;" + "&lt;/b&gt;" + " &lt;/a&gt;\n&lt;/br&gt;" + "&lt;div id=\"close\"&gt;&lt;/div&gt;\n");
	}

}
