package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.oauth.MoiraiOauth2Client;
import com.baiwang.moirai.model.oauth.Oauth2CerdCode;
import java.util.List;

public interface OAuth2SsoService {
	
	BWJsonResult<Oauth2CerdCode> authorize4CerdCodeService(Oauth2CerdCode oauth2CerdCode);
	
	MoiraiOauth2Client getOAuth2SsoInfo(String clientId);
	
	List<MoiraiOauth2Client> initAllClientService(MoiraiOauth2Client moiraiOauth2Client);

    void initPermissionResource();
}
