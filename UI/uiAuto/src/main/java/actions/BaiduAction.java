package actions;

import java.io.IOException;

import baseWeb.WebElementBase;
import pageObject.BaiduPage;

public class BaiduAction {
	WebElementBase action = new WebElementBase();
	
	//打开网页
	public void gopage(String url) {
		BaiduPage baidu = new BaiduPage();
		baidu.getPage(url);
	}
	
	//百度页面测试
	public void testBaidu(String string1,String string2) throws InterruptedException, IOException {
		BaiduPage baidu = new BaiduPage();
		Thread.sleep(5000);
		action.click(baidu.keyword());//单击输入关键字的框
		Thread.sleep(5000);
		action.type(baidu.keyword(), string1+string2);//输入内容
		Thread.sleep(5000);
		action.click(baidu.submit());//点击查询按钮
	}

}
