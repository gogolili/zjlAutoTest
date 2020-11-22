package baseWeb;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

public class WebCaseBase {
	public static WebDriver driver;
	
	//方法描述
	public static String description;
	private static Logger log = Logger.getLogger(WebCaseBase.class);
	public static String downloadsPath = System.getProperty("user.dir") + File.separator + "download" + File.separator;
	@BeforeClass
	@Parameters({"browerType",})
	public void setup(String browerType) throws IOException {
		log.info("----------开始执行测试-----------");
		String driverdir = System.getProperty("user.dir") + File.separator + "driver" + File.separator;	
		WebElementBase action = new WebElementBase();
		log.info("启动浏览器");
		switch (browerType) {
		case "chrome":
			String driverPath = driverdir + "chromedriver.exe";
			ChromeDriverService service;
			service = new ChromeDriverService.Builder()
					.usingDriverExecutable(new File(driverPath))//chromedriver的目录
					.usingAnyFreePort().build();
			service.start();
//		    System.setProperty("webdriver.chrome.driver", driverPath);//设置驱动的路径   
		    DesiredCapabilities caps = setDownloadsPath();//更改默认下载路径        
		    this.driver = new ChromeDriver(caps);
			log.info("启动Chrome浏览器");
			break;
		case "ff":
	        this.driver = new FirefoxDriver();
	        log.info("启动Firefox浏览器");
	        break;
		case "IE":
			this.driver = new InternetExplorerDriver();
			log.info("启动IE浏览器");
			break;
		default:
			this.driver = new ChromeDriver();
	        log.info("启动默认浏览器（Chrome）");
	        break;
		}
	}
	
	//单独重构成一个方法，然后调用
	public DesiredCapabilities setDownloadsPath() {
		
	    HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
	    chromePrefs.put("download.default_directory", downloadsPath);
	    chromePrefs.put("profile.default_content_settings.popups", 1);
	    ChromeOptions options = new ChromeOptions();
	    options.setExperimentalOption("prefs", chromePrefs);
	    DesiredCapabilities caps = new DesiredCapabilities();
	    caps.setCapability(ChromeOptions.CAPABILITY, options);
	    return caps;
	}
	@AfterClass
	public void tearmDown() {
		this.driver.quit();
		WebElementBase action = new WebElementBase();//我觉得没用
		log.info("关闭浏览器");
	    log.info("-------------结束测试，并关闭退出driver及appium server-------------");
	}
}
