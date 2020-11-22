package com.baiwang.moirai.controller.oauth;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.api.OAuth2SsoSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.oauth.MoiraiOauth2Client;
import com.baiwang.moirai.model.oauth.Oauth2CerdCode;
import com.baiwang.moirai.service.OAuth2SsoService;
import com.baiwang.moirai.utils.StrUtils;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("all")
public class OAuth2SsoController implements OAuth2SsoSvc {

	private Logger logger = LoggerFactory.getLogger(OAuth2SsoController.class);

	@Autowired
	private OAuth2SsoService oAuth2SsoService;


	@Override
	public BWJsonResult<Oauth2CerdCode> authorize4CerdCode(@RequestBody Oauth2CerdCode oauth2CerdCode) {

		String client_id = oauth2CerdCode.getClient_id();
		String loginSign = oauth2CerdCode.getLoginSign();
		logger.info("入参loginSign={}:"+loginSign);
		String nonceStr = oauth2CerdCode.getNonceStr();
		String redirect_uri = oauth2CerdCode.getRedirect_uri();
		String response_type = oauth2CerdCode.getResponse_type();
		String systemCode = oauth2CerdCode.getSystemCode();
		String timeStamp = oauth2CerdCode.getTimeStamp();
		String username = oauth2CerdCode.getUsername();
		String userType = oauth2CerdCode.getUserType();
		if(oauth2CerdCode==null ||StrUtils.isEmpty(client_id)||StrUtils.isEmpty(loginSign)||StrUtils.isEmpty(nonceStr)||StrUtils.isEmpty(redirect_uri)||
				StrUtils.isEmpty(response_type)||StrUtils.isEmpty(systemCode)||StrUtils.isEmpty(timeStamp)||StrUtils.isEmpty(username)||StrUtils.isEmpty(userType)){
			throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
		}
		BWJsonResult<Oauth2CerdCode> rtnMap = oAuth2SsoService.authorize4CerdCodeService(oauth2CerdCode);

		return rtnMap;
	}

	@Override
	public BWJsonResult<MoiraiOauth2Client> initAllClient(@RequestBody MoiraiOauth2Client moiraiOauth2Client) {

		List<MoiraiOauth2Client> initAllClientService = oAuth2SsoService.initAllClientService(moiraiOauth2Client);

		return new BWJsonResult<>(initAllClientService);
	}

	/**
	 * <B>方法名称：</B>查询所有设置权限的资源<BR>
	 * <B>概要说明：</B>程序启动时加载<BR>
	 *
	 * @return
	 * @since 2020年2月13日
	 */
	@Override
	public BWJsonResult initPermissionResource(){
		oAuth2SsoService.initPermissionResource();
		return new BWJsonResult<>();
	}
}
