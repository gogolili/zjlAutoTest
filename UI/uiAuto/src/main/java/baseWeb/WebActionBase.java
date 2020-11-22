package baseWeb;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Driver;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.apache.log4j.Logger;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.esotericsoftware.yamlbeans.YamlException;

import base.LocatorBase;
import base.LocatorBase.ByType;
import utils.XmlReadUtil;
import utils.YamlReadUtil;

public class WebActionBase extends WebCaseBase{
	protected HashMap<String, LocatorBase> locatorMap;
	public String path = null;
	public InputStream path_inputStream_1;
	public InputStream path_inputStream_2;
	private static Logger log = Logger.getLogger(WebActionBase.class);
	
	
	public void setXmlObjectPath(String path) {
		this.path = path;
	}
	public void setXmlObjectPathForLocator(InputStream path_inputStream) {
		this.path_inputStream_1 = path_inputStream;
	}
	public void setXmlObjectPathForPageURL(InputStream path_inputStream) {
		this.path_inputStream_2 = path_inputStream;
	}

	public WebActionBase() {
		
	}
	
	public void getLocatorMap() {
		XmlReadUtil xmlReadUtil = new XmlReadUtil();
		YamlReadUtil yamlReadUtil = new YamlReadUtil();
		try {
			//path为xml或者yml的路径
			if ((path==null||path.isEmpty())) {
				//this.getClass().getCanonicalName() 获取page类路径，也就是xml文档中的pageName
				locatorMap = xmlReadUtil.readXMLDocument(path_inputStream_1, this.getClass().getCanonicalName());
			}else {
				if (path.contains(".xml")) {
					locatorMap = xmlReadUtil.readXMLDocument(path, this.getClass().getCanonicalName());
				}else if (path.contains(".yml")) {
					locatorMap = yamlReadUtil.getLocatorMap(path, this.getClass().getCanonicalName());
				}
			}
		} catch (FileNotFoundException | YamlException e) {
			e.printStackTrace();
			}
	}
	
	static By getBy(ByType byType,LocatorBase locator) {
		switch (byType) {
		case id:
			return By.id(locator.getElement());
		case cssSelector:
			return By.cssSelector(locator.getElement());
		case name:
			return By.name(locator.getElement());
		case xpath:
			return By.xpath(locator.getElement());
		case className:
			return By.className(locator.getElement());
		case tagName:
			return By.tagName(locator.getElement());
		case linktext:
			return By.linkText(locator.getElement());
		case partialLinkText:
			return By.partialLinkText(locator.getElement());
		//return null也可以放到switch外面
		default:
			return null;
		}	
	}
	
	//从对象库获取定位信息
	public LocatorBase getLocator(String locatorName) {
		LocatorBase locator;
		//从对象库通过对象名字查找定位信息
		locator = locatorMap.get(locatorName);
		//加入对象库，找不到定位信息，就创建一个定位信息
		if (locator==null) {
			log.error("没有找到"+ locatorName+"页面元素");
		}
		return locator;	
	}
	
	//获取当前用例的执行操作页面的url
	public String getURL() {
		String pageURL =null;
		try {
			if (path==null||path.isEmpty()) {
			pageURL=XmlReadUtil.getXmlPageURL(path_inputStream_1, this.getClass().getCanonicalName());
			}else {
				if (path.contains(".xml")) {
					pageURL=XmlReadUtil.getXmlPageURL(path, this.getClass().getCanonicalName());
				}else if(path.contains(".yml")) {
					pageURL=YamlReadUtil.getYamlPageUrl(path, this.getClass().getCanonicalName());
				}
			
			}
		}catch (Exception e) {
			e.printStackTrace();
		}	
		return pageURL;
	}
	
	
	//打开浏览器
	public void open(String url) {
		driver.navigate().to(url);
		log.info("打开浏览器，访问"+url+"网址!");
	}
	
	//打开页面
	public void getPage(String url) {
		driver.get(url);
		log.info("打开页面");
	}
	
	//关闭浏览器
	public void close() {
		driver.close();
		log.info("关闭浏览器窗口");
	}
	
	//退出浏览器
	public void quit() {
		driver.quit();
		log.info("退出浏览器");
	}
	
	//浏览器前进
	public void forward() {
		driver.navigate().forward();
		log.info("浏览器前进");
	}
	
	//浏览器后退
	 public void back(){
        driver.navigate().back();
        log.info("浏览器后退");
	 }
	 
	 //刷新浏览器
	 public void refresh() {
		 driver.navigate().refresh();
		 log.info("浏览器刷新");
	 }
	 
	 //等待时间
	 public void Waitformax(int t) {
		 driver.manage().timeouts().implicitlyWait(t, TimeUnit.SECONDS);
	 }
	 
	 //查找元素
	 public WebElement findElement(final LocatorBase locator) {
		 Waitformax(Integer.valueOf(locator.getWaitSec()));///获取到xml或者yml文件中的等待时间，等待
		 WebElement webElement;
		 webElement = getElement(locator);
		 return webElement;
		 
	 }
	 
	 //通过定位信息获取元素
	 public WebElement getElement(LocatorBase locator) {
		 WebElement webElement;
		 switch (locator.getType()) {
		 case xpath :
             //log.info("find element By xpath");
             webElement=driver.findElement(By.xpath(locator.getElement()));
             break;
         case id:
             //log.info("find element By xpath");
             webElement=driver.findElement(By.id(locator.getElement()));
             break;
         case cssSelector:
             //log.info("find element By cssSelector");
             webElement=driver.findElement(By.cssSelector(locator.getElement()));
             break;
         case name:
             //log.info("find element By name");
             webElement=driver.findElement(By.name(locator.getElement()));
             break;
         case className:
             //log.info("find element By className");
             webElement=driver.findElement(By.className(locator.getElement()));
             break;
         case linktext:
             //log.info("find element By linkText");
             webElement=driver.findElement(By.linkText(locator.getElement()));
             break;
         case partialLinkText:
             //log.info("find element By partialLinkText");
             webElement=driver.findElement(By.partialLinkText(locator.getElement()));
             break;
         case tagName:
             //log.info("find element By tagName");
             webElement=driver.findElement(By.partialLinkText(locator.getElement()));
             break;
         default :
             //log.info("find element By xpath");
             webElement=driver.findElement(By.xpath(locator.getElement()));
             break;
		}
		 return webElement;
	 }

	
}
