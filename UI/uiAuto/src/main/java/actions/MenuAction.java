package actions;

import java.io.IOException;
import java.util.List;

import org.dom4j.Element;
import org.openqa.selenium.WebElement;

import baseWeb.WebElementBase;
import pageObject.BaiduPage;
import pageObject.MenuPage;

public class MenuAction {
	WebElementBase action = new WebElementBase();

	// 获取导航栏的定位
	// 导航栏测试
	public void testMenuTop(String menuTopName) throws InterruptedException, IOException {
		MenuPage menuPage = new MenuPage();
		Thread.sleep(5000);
		List<WebElement> menuTopList = action.findElements(menuPage.menuTop());
		for (WebElement menuTop : menuTopList) {
			if (menuTop.isDisplayed() && action.getinnerText(menuTop).contains(menuTopName)) {
				if (!menuTop.getAttribute("class").contains("active")) {
					menuTop.click();
					break;
				}
			}
		}
	}

	// 获取左侧树菜单的定位
	public void testMenuLeft(String... menuLeftName) throws InterruptedException, IOException {
		MenuPage menuPage = new MenuPage();
		Thread.sleep(5000);
		if (menuLeftName.equals(null)) {
			System.out.println("左侧树菜单不能为空");
		} else if (menuLeftName.length == 1) {
			List<WebElement> menuLeftList = action.findElements(menuPage.menuLeftLast());
			for (WebElement menuLeft : menuLeftList) {
				if (menuLeft.isDisplayed() && action.getinnerText(menuLeft).contains(menuLeftName[0])) {
					if (!menuLeft.getAttribute("class").contains("selected")) {
						menuLeft.click();
						break;
					}
				}
			}
		} else if (menuLeftName.length == 2) {
			List<WebElement> menuLeftFirstList = action.findElements(menuPage.menuLeftFirst());
			List<WebElement> menuLeftLastList = action.findElements(menuPage.menuLeftLast());
			for (WebElement menuLeftFirst : menuLeftFirstList) {
				System.out.println(menuLeftFirst);
				if (menuLeftFirst.isDisplayed() && action.getinnerText(menuLeftFirst).contains(menuLeftName[0])) {
					if (!menuLeftFirst.getAttribute("class").contains("opened")) {
						menuLeftFirst.click();
						break;
					}
				}
			}
			for (WebElement menuLeftLast : menuLeftLastList) {
				System.out.println(menuLeftLast);
				if (menuLeftLast.isDisplayed() && action.getinnerText(menuLeftLast).contains(menuLeftName[1])) {
					if (!menuLeftLast.getAttribute("class").contains("selected")) {
						menuLeftLast.click();
						break;
					}
				}
			}
		} else if (menuLeftName.length > 2) {
			List<WebElement> menuLeftFirstList = action.findElements(menuPage.menuLeftFirst());			
			for (WebElement menuLeftFirst : menuLeftFirstList) {
				System.out.println(menuLeftFirst);
				if (menuLeftFirst.isDisplayed() && action.getinnerText(menuLeftFirst).contains(menuLeftName[0])) {
					if (!menuLeftFirst.getAttribute("class").contains("opened")) {
						menuLeftFirst.click();
						break;
					}
				}
			}
			List<WebElement> menuLeftMiddleList = action.findElements(menuPage.menuLeftMiddle());
			for (int i = 1; i < menuLeftName.length-1; i++) {
				for (WebElement menuLeftMiddle : menuLeftMiddleList) {
					System.out.println(menuLeftMiddle);
					if (menuLeftMiddle.isDisplayed() && action.getinnerText(menuLeftMiddle).contains(menuLeftName[i])) {
						if (!menuLeftMiddle.getAttribute("class").contains("opened")) {
							menuLeftMiddle.click();
							break;
						}
					}
				}
			}
			
			List<WebElement> menuLeftLastList = action.findElements(menuPage.menuLeftLast());
			for (WebElement menuLeftLast : menuLeftLastList) {
				System.out.println(menuLeftLast);
				if (menuLeftLast.isDisplayed() && action.getinnerText(menuLeftLast).contains(menuLeftName[menuLeftName.length-1])) {
					if (!menuLeftLast.getAttribute("class").contains("selected")) {
						menuLeftLast.click();
						break;
					}
				}
			}
		}

	}

	





}
