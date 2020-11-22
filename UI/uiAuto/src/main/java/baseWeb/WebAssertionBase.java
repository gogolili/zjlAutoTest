package baseWeb;

import java.sql.Driver;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import base.LocatorBase;
import utils.ScreenShotUtil;



public class WebAssertionBase extends WebCaseBase{
	private static Logger log = Logger.getLogger(WebAssertionBase.class);
	//收集断言异常用于报表日志展示
	public static List<Error> errors = new ArrayList<Error>();
	//收集断言信息文本，用于报表展示
	public static List<String> assertInfoList = new ArrayList<String>();
	//收集信息文本用于报表展示
	public static List<String> messageList = new ArrayList<String>();
	//记录错误数量
	public static Integer errorIndex = 0;
	
	//格式化时间
	public static String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmssSSS");
		return formatter.format(date).toString();		
	}
	
	private static void snapshotInfo() {
	    WebDriver driver = WebCaseBase.driver;
	    ScreenShotUtil screenShot = new ScreenShotUtil(driver);
	    // 设置截图名字
	    Date nowDate = new Date();
	    screenShot.setScreenName(WebAssertionBase.formatDate(nowDate));
	    screenShot.takeScreenshot();
	    // Assertion.assertInfolList.add("&lt;a href=\"snapshot/"+Assertion.formatDate(nowDate)+".jpg\"
	    // &gt;&lt;img height=\"100\" width=\"100\"
	    // src=\"snapshot\\"+Assertion.formatDate(nowDate)+".jpg\"&gt;&lt;/img&gt;&lt;/a&gt;&lt;br/&gt;"+"&lt;a
	    // href=\"snapshot\\"+Assertion.formatDate(nowDate)+".jpg\" &gt;点击查看大图&lt;/a&gt;\n");
	    WebAssertionBase.messageList.add("&lt;a class=\"clickbox\" href=\"#url\"&gt;\n"
	        + "&lt;img src=\"snapshot/" + WebAssertionBase.formatDate(nowDate)
	        + ".jpg\" height=\"100\" width=\"100\" alt=\"\" /&gt;\n" + "&lt;b class=\"lightbox\"&gt;\n"
	        + "&lt;b class=\"light\"&gt;&lt;/b&gt;\n" + "&lt;b class=\"box\"&gt;\n"
	        + "&lt;img src=\"snapshot/" + WebAssertionBase.formatDate(nowDate)
	        + ".jpg\" height=\"530\" width=\"1024\" onmousewheel=\"return bigimg(this)\" alt=\"\" /&gt;\n"
	        + "&lt;span&gt;滚动鼠标缩放大小,点击X关闭当前图片，返回报表界面.&lt;br /&gt;&lt;i&gt;&lt;/i&gt;&lt;/span&gt;\n"
	        + "&lt;/b&gt;\n" + "&lt;/b&gt;\n" + "&lt;/a&gt;\n" + "&lt;br class=\"clear\" /&gt;\n"
	        + "&lt;a class=\"clickbox\" href=\"#url\"&gt;" + "点击查看大图" + "&lt;b class=\"lightbox\"&gt;"
	        + "&lt;b class=\"light\"&gt;&lt;/b&gt;" + "&lt;b class=\"box\"&gt;&lt;img src=\"snapshot/"
	        + WebAssertionBase.formatDate(nowDate)
	        + ".jpg\" height=\"530\" width=\"1024\" onmousewheel=\"return bigimg(this)\" alt=\"\" /&gt;"
	        + "&lt;span&gt;滚动鼠标缩放大小,点击X关闭当前图片，返回报表界面." + "&lt;br /&gt;&lt;i&gt;&lt;/i&gt;&lt;/span&gt;"
	        + "&lt;/b&gt;" + "&lt;/b&gt;" + " &lt;/a&gt;\n&lt;/br&gt;"
	        + "&lt;div id=\"close\"&gt;&lt;/div&gt;\n");
	  }
	
	//验证实际值是否包含预期值exceptstr
	public static void VerityCationString(String actual,String exceptStr) {
		String verityStr = "Assert验证：{"+"实际值："+actual+","+"预期值：" + exceptStr + "} 实际值是否包含预期值";
		Boolean flagBoolean = actual.contains(exceptStr);
		log.info(flagBoolean.toString());
		try {
			Assert.assertTrue(flagBoolean);
			AssertPassLog();
			assertInfoList.add(verityStr + "pass");
		}catch (Error e) {
			errors.add(e);
			AssertFailedLog();
			errorIndex++;
			assertInfoList.add(verityStr + ":failed");
			WebAssertionBase.snapshotInfo();
		}
	}
	
	//验证实际值是否包含预期值exceptstr,加了验证中文描述
	public static void VerityCationString(String actual,String exceptStr,String message) {
		String verityStr = "Assert验证：{"+"实际值："+actual+","+"预期值：" + exceptStr + "} 实际值是否包含预期值";
		Boolean flagBoolean = actual.contains(exceptStr);
		log.info(message + ":" + verityStr);
		try {
			Assert.assertTrue(flagBoolean);
			AssertPassLog();
			assertInfoList.add(message+verityStr + ":pass");
			messageList.add(message + ":pass");
		}catch (Error e) {
			errors.add(e);
			AssertFailedLog();
			errorIndex++;
			assertInfoList.add(message + verityStr + ":failed");
			messageList.add(message + ":failed");
			WebAssertionBase.snapshotInfo();
		}
	}
	
	//验证实际值actual与预期值exceptStr是否相等
	public static void VerityString(String actual,String exceptStr) {
		String verityStr = "Assert验证：{"+"实际值："+actual+","+"预期值：" + exceptStr + "} 实际值是否包含预期值";
		log.info(verityStr);
		try {
			Assert.assertEquals(actual, exceptStr);
			AssertPassLog();
			assertInfoList.add(verityStr + "pass");
		}catch (Error e) {
			errors.add(e);
			AssertFailedLog();
			errorIndex++;
			assertInfoList.add(verityStr + ":failed");
			WebAssertionBase.snapshotInfo();
		}
	}
	
	//验证实际值actual与预期值exceptStr是否相等,加了验证中文描述
	public static void VerityString(String actual, String exceptStr, String Message) {
	    String verityStr = "Assert验证：{" + "实际值" + actual + "," + "预期值" + exceptStr + "} 实际值与预期值是否一致";
	    log.info(Message + ":" + verityStr);
	    try {
	      Assert.assertEquals(actual, exceptStr);
	      AssertPassLog();
	      assertInfoList.add(Message + verityStr + ":pass");
	      messageList.add(Message + ":pass");
	    } catch (Error e) {
	      // TODO: handle exception
	      AssertFailedLog();
	      errorIndex++;
	      errors.add(e);
	      assertInfoList.add(Message + verityStr + ":failed");
	      messageList.add(Message + ":failed");
	      WebAssertionBase.snapshotInfo();
	    }
	  }
	
	//验证实际值actual与预期值exceptStr是否不相等
	public static void VerityNotString(String actual, String exceptStr, String Message) {
	    String verityStr = "Assert验证：{" + "实际值" + actual + "," + "预期值" + exceptStr + "} 实际值与预期值是否不相等";
	    log.info(Message + ":" + verityStr);
	    try {
	      Assert.assertNotEquals(actual, exceptStr);
	      AssertPassLog();
	      assertInfoList.add(Message + verityStr + ":pass");
	      messageList.add(Message + ":pass");
	    } catch (Error e) {
	      // TODO: handle exception
	      AssertFailedLog();
	      errorIndex++;
	      errors.add(e);
	      assertInfoList.add(Message + verityStr + ":failed");
	      messageList.add(Message + ":failed");
	      WebAssertionBase.snapshotInfo();

	    }
	  }
	
	//验证布尔值是否与预期一致
	public static void VerityBoolean(Boolean actual, Boolean except, String message) {

	    String verityStr = "Assert验证：{" + "实际值：" + actual + "," + "预期值：" + except + "} 实际值与预期值是否一致";
	    log.info(message + ":" + verityStr);
	    try {
	      Assert.assertEquals(actual, except);
	      AssertPassLog();
	      assertInfoList.add(message + verityStr + ":pass");
	      messageList.add(message + ":pass");
	    } catch (Error e) {
	      // TODO: handle exception
	      AssertFailedLog();
	      errorIndex++;
	      errors.add(e);
	      assertInfoList.add(message + verityStr + ":failed");
	      messageList.add(message + ":failed");
	      WebAssertionBase.snapshotInfo();
	      // e.printStackTrace();
	    }
	  }
	
	//验证页面是否出现某文本excepStr
	public static void VerityTextPresent(String exceptStr) {
		String verityStr = "【Assert验证】："+"页面是否出现" + "【" + "预期值：" + exceptStr + "】" + "字符串";
		Boolean flag = false;
		log.info(verityStr);
		try {
			// html网页有限制，有时候通过getText()获取不到元素之间的文本，比如元素的display属性如果设置为false，此时是无法获取到元素的，重写校验方法
		    // exceptStr = "//*[contains(@text,'" + exceptStr + "')]";
		    // log.info("定位信息：" + exceptStr);
		    // driver.getPageSource().contains(exceptStr);
		    // driver.findElements(By.xpath(exceptStr));
		    // if (driver.findElements(By.xpath(exceptStr)).size() > 0)
			System.out.println("----------------------");
			System.out.println(driver.getPageSource());
			System.out.println("----------------------");
			if (driver.getPageSource().contains(exceptStr)) {
				flag = true;
			}else {
				flag = false;
			}	
		}catch (NoSuchElementException e) {
			flag = false;
			WebElementBase.noSuchElementExceptions.add(e);
			e.printStackTrace();
		}
		try {
			Assert.assertTrue(flag);
			AssertPassLog();
			assertInfoList.add(verityStr+":pass");
		}catch (Error e) {
			AssertFailedLog();
			errors.add(e);
		    errorIndex++;
		    assertInfoList.add(verityStr + ":failed");
		    WebAssertionBase.snapshotInfo();
		    Assert.fail("【Assert验证】:" + "页面未出现" + "【" + "预期值：" + exceptStr + "】" + "字符串");
		}
	}
	
	//验证页面是否出现某文本exceptStr
	  public static void VerityTextPresent(String exceptStr, String Message) {
		    String verityStr = "【Assert验证】:" + "页面是否出现" + "【" + "预期值：" + exceptStr + "】" + "字符串";
		    Boolean flag = false;
		    log.info(Message + ":" + verityStr);
		    try {
		      // app查找是否出现某文本用@text属性，如果是web需是text()
		      exceptStr = "//*[contains(text(),'" + exceptStr + "')]";
		      System.out.println(exceptStr);
		      List<WebElement> webElements = driver.findElements(By.xpath(exceptStr));
		      if (webElements.size() > 0) {
		        flag = true;
		      } else {
		        flag = false;
		      }
		    } catch (NoSuchElementException e) {
		      // TODO: handle exception
		      flag = false;
		      WebElementBase.noSuchElementExceptions.add(e);
		      e.printStackTrace();
		    }
		    try {
		      Assert.assertTrue(flag);
		      AssertPassLog();
		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error f) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(f);
		      errorIndex++;
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		    }
		  }	
	  
	  //验证页面是否没有出现某文本
	  public static void VerityNotTextPresent(String exceptStr, String Message) {
		    String verityStr = "【Assert验证】:" + "页面是否没有出现" + "【" + "预期值：" + exceptStr + "】" + "字符串";
		    Boolean flag = false;
		    log.info(Message + ":" + verityStr);
		    try {
		      exceptStr = "//*[contains(.,'" + exceptStr + "')]";
		      driver.findElement(By.xpath(exceptStr));
		      flag = false;
		      System.out.println(flag);
		    } catch (NoSuchElementException e) {
		      // TODO: handle exception
		      flag = true;
		      System.out.println(flag);
		    }
		    try {
		      Assert.assertTrue(flag);
		      AssertPassLog();
		      System.out.println(flag);
		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error f) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(f);
		      errorIndex++;
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      System.out.println(flag);
		      WebAssertionBase.snapshotInfo();
		    }
		  }
	  
	  //验证页面是否出现某文本---精确匹配
	  public static void VerityTextPresentPrecision(String exceptStr) {
		    String verityStr = "【Assert验证】:" + "页面是否出现" + "【" + "预期值：" + exceptStr + "】" + "字符串";
		    Boolean flag = false;
		    log.info(verityStr);
		    try {
		      // app查找是否出现某文本用@text属性，如果是web需是text()
		      exceptStr = "//*[text=\"" + exceptStr + "\"]";
		      System.out.println(exceptStr);
		      driver.findElement(By.xpath(exceptStr));
		      flag = true;
		    } catch (NoSuchElementException e) {
		      // TODO: handle exception
		      flag = false;
		      WebElementBase.noSuchElementExceptions.add(e);
		      e.printStackTrace();
		      /// AssertFailedLog();
		    }
		    System.out.println(false);
		    try {
		      Assert.assertTrue(flag);
		      AssertPassLog();
		      assertInfoList.add(verityStr + ":pass");
		    } catch (Error f) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(f);
		      errorIndex++;
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw f;
		    }


		  }
	  
	  //验证页面是否出现某文本---精确匹配（预期值、验证中文描述）
	  public static void VerityTextPresentPrecision(String exceptStr, String Message) {
		    String verityStr = "【Assert验证】:" + "页面是否出现" + "【" + "预期值：" + exceptStr + "】" + "字符串";
		    Boolean flag = false;
		    log.info(Message + ":" + verityStr);
		    try {
		      // app查找是否出现某文本用@text属性，如果是web需是text()
		      exceptStr = "//*[text=\"" + exceptStr + "\"]";
		      WebElement webElement = driver.findElement(By.xpath(exceptStr));
		      System.out.println(exceptStr);
		      flag = true;
		    } catch (NoSuchElementException e) {
		      // TODO: handle exception
		      flag = false;
		    }
		    System.out.println(flag);
		    try {
		      Assert.assertTrue(flag);
		      AssertPassLog();
		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error f) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(f);
		      errorIndex++;
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();

		      // throw f;
		    }

		  }	  
	  
	  //验证页面是否没有出现某文本----精确匹配
	  public static void VerityNotTextPresentPrecision(String exceptStr) {
		    String verityStr = "【Assert验证】:" + "页面是否没有出现" + "【" + "预期值：" + exceptStr + "】" + "字符串";
		    Boolean flag = false;
		    log.info(verityStr);
		    try {
		      // app查找是否出现某文本用@text属性，如果是web需是text()
		      exceptStr = "//*[@text=\"" + exceptStr + "\"]";
		      System.out.println(exceptStr);
		      driver.findElement(By.xpath(exceptStr));
		      flag = false;
		    } catch (NoSuchElementException e) {
		      // TODO: handle exception
		      flag = true;
		      WebElementBase.noSuchElementExceptions.add(e);
		      e.printStackTrace();
		      /// AssertFailedLog();
		    }
		    System.out.println(false);
		    try {
		      Assert.assertTrue(flag);
		      AssertPassLog();
		      assertInfoList.add(verityStr + ":pass");
		    } catch (Error f) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(f);
		      errorIndex++;
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		    }


		  }
	  
	  //验证页面是否没有出现某文本----精确匹配
	  public static void VerityNotTextPresentPrecision(String exceptStr, String Message) {
		    String verityStr = "【Assert验证】:" + "页面是否没有出现" + "【" + "预期值：" + exceptStr + "】" + "字符串";
		    Boolean flag = false;
		    log.info(Message + ":" + verityStr);
		    try {
		      // app查找是否出现某文本用@text属性，如果是web需是text()
		      exceptStr = "//*[@text=\"" + exceptStr + "\"]";
		      System.out.println(exceptStr);
		      driver.findElement(By.xpath(exceptStr));
		      flag = false;
		    } catch (NoSuchElementException e) {
		      // TODO: handle exception
		      flag = true;
		    }
		    System.out.println(flag);
		    try {
		      Assert.assertTrue(flag);
		      AssertPassLog();
		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error f) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(f);
		      errorIndex++;
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();

		      // throw f;
		    }
		  } 
	  
	  //验证浏览器标题是否与预期值exceptTitle相同
	  public static void VerityTitle(String exceptTitle) {
		  String title = driver.getTitle();
		  String verityStr = "Assert验证:页面title是否与预期值一致{" + "实际网页标题：" + title + "," + "预期网页标题：" + exceptTitle + "}";
		  log.info(verityStr);
		  try {
			Assert.assertEquals(title, exceptTitle);
			AssertPassLog();
			assertInfoList.add(verityStr + ":pass");
		} catch (Error e) {
			AssertFailedLog();
			errors.add(e);
			errorIndex++;
		    assertInfoList.add(verityStr + ":failed");
		    WebAssertionBase.snapshotInfo();
		    e.printStackTrace();
		    Assert.fail("【Assert验证】:" + "Title" + "【" + "预期值：" + exceptTitle + "】" + "字符串");
		}
	  }
	  
	  //验证浏览器标题是否与预期值exceptTitle相同
	  public static void VerityTitle(String exceptTitle, String Message) {

		    String title = driver.getTitle();
		    String verityStr =
		        "Assert验证:页面title是否与预期值一致{" + "实际网页标题：" + title + "," + "预期网页标题：" + exceptTitle + "}";
		    log.info(Message + ":" + verityStr);
		    try {
		      Assert.assertEquals(title, exceptTitle);
		      AssertPassLog();
		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(e);
		      errorIndex++;
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		      e.printStackTrace();
		      Assert.fail("【Assert验证】:" + "Title" + "【" + "预期值：" + exceptTitle + "】" + "字符串");
		      // throw e;
		    }
		  }
	  
	  //验证某元素文本值是否与预期值exceptText一样
	  public static void VerityText(LocatorBase locator, String exceptText) {
		    WebElementBase action = new WebElementBase();
		    WebElement webElement = action.findElement(locator);
		    String text = webElement.getText();
		    String verityStr = "Assert验证：某文本值是否与预期值一致{" + "实际值：" + text + "," + "预期值：" + exceptText + "}";
		    log.info(verityStr);
		    try {
		      Assert.assertEquals(text, exceptText);
		      AssertPassLog();
		      assertInfoList.add(verityStr + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertPassLog();
		      errorIndex++;
		      errors.add(e);
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }
		  } 
	//验证某元素文本值是否与预期值exceptText一致
	  public static void VerityText(LocatorBase locator, String exceptText, String Message) {
		    WebElementBase action = new WebElementBase();
		    WebElement webElement = action.findElement(locator);
		    String text = webElement.getText();
		    String verityStr = "Assert验证：某文本值是否与预期值一致{" + "实际值：" + text + "," + "预期值:" + exceptText + "}";
		    log.info(Message + ":" + verityStr);
		    try {
		      Assert.assertEquals(text, exceptText);
		      AssertPassLog();
		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertPassLog();
		      errorIndex++;
		      errors.add(e);
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }
		  }
	 
	  
	  //验证某元素某个属性值与预期值exceptAttributeValue 一样
	  public static void VerityAttribute(LocatorBase locator, String AttributeName,String exceptAttributeValue) {
		    WebElementBase action = new WebElementBase();
		    WebElement webElement = action.findElement(locator);
		    String attribute = webElement.getAttribute(AttributeName);
		    String verityStr = "Assert验证：某属性值是否与预期值一致{" + "实际属性值：" + attribute + "," + "预期属性值："
		        + exceptAttributeValue + "}";
		    try {
		      Assert.assertEquals(attribute, exceptAttributeValue);
		      log.info(verityStr);
		      AssertPassLog();
		      assertInfoList.add(verityStr + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errorIndex++;
		      errors.add(e);
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }
		  }
	  
	//验证某元素某个属性值与预期值exceptAttributeValue 一样
	  public static void VerityAttribute(LocatorBase locator, String AttributeName,String exceptAttributeValue, String Message) {
		    WebElementBase action = new WebElementBase();
		    WebElement webElement = action.findElement(locator);
		    String attribute = webElement.getAttribute(AttributeName);
		    String verityStr = "Assert验证：某属性值是否与预期值一致{" + "实际属性值：" + attribute + "," + "预期属性值："
		        + exceptAttributeValue + "}";
		    log.info(Message + ":" + verityStr);
		    try {
		      Assert.assertEquals(attribute, exceptAttributeValue);
		      log.info(verityStr);
		      AssertPassLog();
		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errorIndex++;
		      errors.add(e);

		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }
		  }
	  
	  //验证某输入框是否不可编辑
	  public static void VertityNoEdit(LocatorBase locator) {
		    Boolean status = false;
		    WebElementBase action = new WebElementBase();
		    WebElement webElement = action.findElement(locator);
		    String value1 = webElement.getAttribute("disabled");
		    String value2 = webElement.getAttribute("readOnly");
		    String verityStr =
		        "【Assert验证】:文本框是否不可编辑{" + "实际值：" + status.toString() + "，" + "预期值：false" + "}";
		    if (value1.equals("true")) {
		      status = true;
		    } else if (value2.equals("true")) {
		      status = true;
		    } else {
		      status = false;
		    }
		    log.info(verityStr);
		    try {
		      Assert.assertTrue(status);
		      AssertPassLog();
		      assertInfoList.add(verityStr + ":pass");

		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errorIndex++;
		      errors.add(e);
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }

		  }
	//验证某输入框是否不可编辑
	  public static void VertityNoEdit(LocatorBase locator,String Message) {
		  Boolean status = false;
		  WebElementBase action = new WebElementBase();
		  WebElement webElement = action.findElement(locator);
		  String value1 = webElement.getAttribute("disabled");
		  String value2 = webElement.getAttribute("readOnly");
		  if (value1.equals("true")) {
			status = true;
		}else if (value2.equals("true")) {
			status = true;
		}else {
			status = false;
		}
	  String verityStr =
		        "【Assert验证】:文本框是否不可编辑{" + "实际值：" + status.toString() + "，" + "预期值：false" + "}";
		    log.info(Message + ":" + verityStr);
		    try {
		      Assert.assertTrue(status);
		      AssertPassLog();

		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errorIndex++;
		      errors.add(e);
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		    }
	  }
	  
	  //验证某元素可编辑
	  public static void VertityEdit(LocatorBase locator) {
		    Boolean status = false;
		    WebElementBase action = new WebElementBase();
		    WebElement webElement = action.findElement(locator);
		    String value1 = webElement.getAttribute("disabled");
		    String value2 = webElement.getAttribute("readOnly");
		    String verityStr = "【Assert验证】:文本框是否可编辑{" + "实际值：" + status.toString() + "，" + "预期值：true" + "}";
		    if (value1.equals("false")) {
		      status = true;
		    } else if (value2.equals("false")) {
		      status = true;
		    } else {
		      status = false;
		    }
		    // AssertLog("true", status.toString());
		    log.info(verityStr);
		    try {
		      Assert.assertTrue(status);
		      AssertPassLog();
		      assertInfoList.add(verityStr + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(e);
		      errorIndex++;
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }
		  }
	 //验证某元素可编辑 
	  public static void VertityEdit(LocatorBase locator, String Message) {
		    Boolean status = false;
		    WebElementBase action = new WebElementBase();
		    WebElement webElement = action.findElement(locator);
		    String value1 = webElement.getAttribute("disabled");
		    String value2 = webElement.getAttribute("readOnly");
		    String verityStr = "【Assert验证】:文本框是否可编辑{" + "实际值：" + status.toString() + "，" + "预期值：true" + "}";
		    if (value1.equals("false")) {
		      status = true;
		    } else if (value2.equals("false")) {
		      status = true;
		    } else {
		      status = false;
		    }
		    // AssertLog("true", status.toString());
		    log.info(Message + ":" + verityStr);
		    try {
		      Assert.assertTrue(status);
		      AssertPassLog();

		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");

		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(e);
		      errorIndex++;

		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }
		  }
	  //验证alert对话框提示信息是否与预期值一致
	  public static void VerityAlertText(String expectAlertText) {
		  Alert alert = driver.switchTo().alert();
		  String alertText = alert.getText();
		  String verityStr = "【Assert验证】:弹出的对话框的文本内容是否一致{" + alertText + "," + expectAlertText + "}";
		  log.info("【Assert验证】:弹出的对话框的文本内容是否一致{" + "实际值：" + alertText + "," + "预期值" + expectAlertText + "}");
		  try {
		      Assert.assertEquals(alertText, expectAlertText);
		      AssertPassLog();
		      assertInfoList.add(verityStr + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(e);
		      errorIndex++;
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		      // throw e;
		    }
	  }
	  
	  //验证alert对话框提示信息是否与预期值一致
	  public static void VerityAlertText(String expectAlertText, String Message) {
		    Alert alert = driver.switchTo().alert();
		    String alertText = alert.getText();
		    String verityStr =
		        "【Assert验证】:弹出的对话框的文本内容是否一致{" + "实际值：" + alertText + "," + "预期值：" + expectAlertText + "}";
		    log.info(Message + ":" + verityStr);
		    try {
		      Assert.assertEquals(alertText, expectAlertText);
		      AssertPassLog();

		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(e);
		      errorIndex++;
		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		    }
		  }
	  
	  //验证当前URL是否与预期值Url一致
	  public static void VerityURL(String expectURL) {
		    String url = driver.getCurrentUrl();
		    String verityStr = "【Assert验证】:URL是否与预期的一致{" + "实际值：" + url + "," + "预期值：" + expectURL + "}";
		    log.info(verityStr);
		    try {
		      Assert.assertEquals(url, expectURL);
		      AssertPassLog();
		      //
		      assertInfoList.add(verityStr + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(e);
		      // throw e;
		      errorIndex++;
		      assertInfoList.add(verityStr + ":failed");
		      WebAssertionBase.snapshotInfo();
		    }
		  }
	//验证当前URL是否与预期值Url一致
	  public static void VerityURL(String expectURL, String Message) {
		    String url = driver.getCurrentUrl();
		    String verityStr = "【Assert验证】:URL是否与预期的一致{" + "实际值：" + url + "," + "预期值：" + expectURL + "}";
		    log.info(Message + ":" + verityStr);
		    try {
		      Assert.assertEquals(url, expectURL);
		      AssertPassLog();
		      //

		      assertInfoList.add(Message + verityStr + ":pass");
		      messageList.add(Message + ":pass");
		    } catch (Error e) {
		      // TODO: handle exception
		      AssertFailedLog();
		      errors.add(e);
		      errorIndex++;

		      assertInfoList.add(Message + verityStr + ":failed");
		      messageList.add(Message + ":failed");
		      WebAssertionBase.snapshotInfo();
		    }
		  }
	  
	//断言成功日志内容
	private static void AssertPassLog() {
		log.info("【Assert验证  pass!】");
	}
	
	//断言失败日志内容
	private static void AssertFailedLog() {
		log.info("【Assert验证  failed!】");
	}
	
	//断言日志内容
	private static void AssertLog(String str1,String str2) {
		log.info("【Assert验证】:" + "判断[比较]" + "{" + str1 + "," + str2 + "}" + "是否一致[相等]");
	}
	
	//判断用例是否含有验证失败的断言，如果有此方法会抛出异常，让testng监听器检测到，如果没有不会抛出异常，testng监听器会认为用例成功
	public static void VerityError() {
		Assert.assertEquals(errors.size(), 0);
		//有找不到元素的异常也认为用例失败
		Assert.assertEquals(WebElementBase.noSuchElementExceptions.size(), 0);
	}
	
	

}
