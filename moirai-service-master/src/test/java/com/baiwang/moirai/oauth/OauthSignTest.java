package com.baiwang.moirai.oauth;

import com.baiwang.moirai.controller.oauth.OAuth2SsoController;
import org.springframework.beans.factory.annotation.Autowired;

//@RunWith(SpringJUnit4ClassRunner.class)
public class OauthSignTest {

	@Autowired
	private OAuth2SsoController auth2SsoController;
	
//	@Test
	public void oauthSignTest(){
//		Oauth2CerdCode oauth2CerdCode = new Oauth2CerdCode();
//		oauth2CerdCode.setClient_id("bwe6d24ab9fd4c91b3bedb5389f16626");
//		oauth2CerdCode.setClient_secret("");
//		oauth2CerdCode.setLoginSign("3B3A74BB4F115AB4011D8AAD5FC89D7F");
//		oauth2CerdCode.setNonceStr("345ewghdfhjfti7qwertyuioplkjhgfd");
//		oauth2CerdCode.setRedirect_uri("http://123.56.92.221/bwcloudpage");
//		oauth2CerdCode.setResponse_type("cerdCode");
//		oauth2CerdCode.setState("1000");
//		oauth2CerdCode.setSystemCode("BWXX1001");
//		oauth2CerdCode.setTimeStamp("1530758269550");
//		oauth2CerdCode.setUsername("douxing");
//		oauth2CerdCode.setUserType("B");
//		BWJsonResult signObj = auth2SsoController.getSign(oauth2CerdCode);
//		List data = signObj.getData();
//		String sign = (String)data.get(0);
//		System.out.println(sign);
		System.out.println("9090");
	}
}
