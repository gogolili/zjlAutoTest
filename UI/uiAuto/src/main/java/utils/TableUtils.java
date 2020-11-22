package utils;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TableUtils {
	/**
	 * 封装的表格方法
	 */
	public static int countRows(WebDriver driver,By tableBy) {
		//查找table元素
		WebElement table = driver.findElement(tableBy);
		//遍历table元素下的tr元素
		List<WebElement> trList = table.findElements(By.tagName("tr"));
		//javashop的表头和内容是两个独立的表格，此处无需size-1获取表格提的内容
		int rows = trList.size();
		return rows;
	}
	
	public static WebElement getCell(WebDriver driver,By tableBy,int row,int column) {
		//查找table元素
		WebElement table = driver.findElement(tableBy);
		//遍历table元素下的tr元素
		List<WebElement> trList = table.findElements(By.tagName("tr"));
		//根据指定行获取所有td元素
		List<WebElement> tdList = trList.get(row).findElements(By.tagName("td"));
		return tdList.get(column);
		
	}

}
