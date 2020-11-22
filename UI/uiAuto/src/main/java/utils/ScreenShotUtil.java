package utils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;

import com.google.common.io.Files;



public class ScreenShotUtil {
	public WebDriver driver;
	private String screenName;
	private static Logger log = Logger.getLogger(ScreenShotUtil.class);
	
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public ScreenShotUtil(WebDriver driver) {
		super();
		this.driver = driver;
	}
	
	private void takeScreenshot(String screenPath) {
		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		
		try {
			Files.copy(scrFile, new File(screenPath));
			log.error("错误截图："+screenPath);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void takeScreenshot() {
		String screeName = this.screenName+".jpg";
		File dir = new File("test-output\\snapshot");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String screenPath=dir.getAbsolutePath() + File.separator + screeName;
		this.takeScreenshot(screenPath);
	}
	
	
	
	
	

}
