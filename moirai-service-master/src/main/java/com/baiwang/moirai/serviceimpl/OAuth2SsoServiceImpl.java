package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.MoiraiModuleMapper;
import com.baiwang.moirai.mapper.MoiraiOauth2ClientMapper;
import com.baiwang.moirai.mapper.MoiraiResourceMapper;
import com.baiwang.moirai.mapper.MoiraiSecurityControlMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.oauth.MoiraiOauth2Client;
import com.baiwang.moirai.model.oauth.Oauth2CerdCode;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.role.MoiraiModuleService;
import com.baiwang.moirai.model.role.MoiraiModuleServiceCondition;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiResourceSecurityCondition;
import com.baiwang.moirai.model.role.MoiraiSecurityControl;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserLoginHistory;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.service.OAuth2SsoService;
import com.baiwang.moirai.utils.BwBeanUtils;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.Signature;
import com.baiwang.moirai.utils.StrUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
public class OAuth2SsoServiceImpl implements OAuth2SsoService {

    private Logger logger = LoggerFactory.getLogger(OAuth2SsoServiceImpl.class);

    @Autowired
    private MoiraiOauth2ClientMapper moiraiOauth2ClientMapper;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiResourceMapper moiraiResourceMapper;

    @Autowired
    private MoiraiSecurityControlMapper moiraiSecurityControlMapper;

    @Autowired
    private MoiraiModuleMapper moiraiModuleMapper;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    public BWJsonResult<Oauth2CerdCode> authorize4CerdCodeService(Oauth2CerdCode oauth2CerdCode) {

        String client_id = oauth2CerdCode.getClient_id();
        String loginSign = oauth2CerdCode.getLoginSign();
        String systemCode = oauth2CerdCode.getSystemCode();
        String username = oauth2CerdCode.getUsername();
        String userType = oauth2CerdCode.getUserType();

        MoiraiOauth2Client oAuth2SsoInfo = this.getOAuth2SsoInfo(client_id);
        if (oAuth2SsoInfo == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        String tokenKey = oAuth2SsoInfo.getTokenKey();
        logger.info("tokenKey:" + tokenKey);
        Map map = new HashMap();
        Map<String, Object> transBean2Map = BwBeanUtils.transBean2Map(oauth2CerdCode, true);
        Iterator<String> iterator = transBean2Map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = transBean2Map.get(key);
            if (!key.equals("loginSign") && !key.equals("requestId") && !ObjectUtils.isEmpty(value)) {
                map.put(key, value);
            }
        }
        String md5Str = Signature.getSign(map, "tokenKey", tokenKey);
        logger.info("解密形成的结果md5Str={}" + md5Str);
        if (!md5Str.equals(loginSign)) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_SIGN_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
            return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_SIGN_ERROR));
        }

        MoiraiOauth2Client record = new MoiraiOauth2Client();
        record.setSystemCode(systemCode);
        List<MoiraiOauth2Client> selectByClient = moiraiOauth2ClientMapper.selectByClient(record);
        if (!StrUtils.isEmptyList(selectByClient)) {

            MoiraiOauth2Client moiraiOauth2Client = selectByClient.get(0);
            String clientId = moiraiOauth2Client.getClientId();
            oauth2CerdCode.setClient_id(clientId);

            //获取用户信息
            String shortName = oauth2CerdCode.getShortName();
            if (!StrUtils.isEmpty(shortName)) {
                if (!RegularExpUtils.checkMobile(username)) {
                    username = shortName + "_" + username;
                }
            }
            MoiraiUser moiraiUser;
            if (Constants.USER_TYPE_B.equals(userType)) {
                moiraiUser = moiraiUserMapper.getUserByUserAccount(username);
                if (moiraiUser != null) {
                    //租户是否锁定
                    MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(moiraiUser.getTenantId());
                    if (moiraiTenant == null || Constants.flag_N.equals(moiraiTenant.getUseFlag())) {
                        return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_LOCK));
                    }
                    //暂时只判断所在机构是否锁定
                    MoiraiOrg moiraiOrg = moiraiOrgService.selectByOrgId(moiraiUser.getOrgId());
                    if (moiraiOrg == null || Constants.flag_N.equals(moiraiOrg.getUseFlag())) {
                        return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_LOCK));
                    }
                }
            } else {
                moiraiUser = moiraiUserMapper.getCUserByTelephone(username);
            }
            if (moiraiUser == null) {
                return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR));
            }
            if (Constants.flag_N.equals(moiraiUser.getUseFlag())) {
                return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_FROZEN_ERROR));
            }
            if (Constants.flag_L.equals(moiraiUser.getUseFlag())) {
                return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_LOCK_ERROR));
            }

            MoiraiUserLoginHistory loginHistory = new MoiraiUserLoginHistory();
            loginHistory.setUserId(moiraiUser.getUserId());
            loginHistory.setLoginType(Constants.DEFAULT_ONE);
            loginHistory.setTenantId(moiraiUser.getTenantId());
            loginHistory.setOrgId(moiraiUser.getOrgId());
            loginHistory.setCreater(username);
            moiraiUserService.updateLastLoginHistory(loginHistory);
            oauth2CerdCode.setMoiraiUser(moiraiUser);
            return new BWJsonResult<>(oauth2CerdCode);
        }
        return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_SYSTEMCODE_ERROR));
    }

    public MoiraiOauth2Client getOAuth2SsoInfo(String clientId) {
        return moiraiOauth2ClientMapper.selectByClientId(clientId);
    }

    public List<MoiraiOauth2Client> initAllClientService(MoiraiOauth2Client moiraiOauth2Client) {
        return moiraiOauth2ClientMapper.selectAllOrCondition(moiraiOauth2Client);
    }

    @Override
    public void initPermissionResource() {
        // 资源权限
        List<MoiraiResource> resourceList = moiraiResourceMapper.selectAllPermissionResource();
        logger.info("需要加载权限菜单:{}", resourceList);
        for (MoiraiResource moiraiResource : resourceList) {
            MoiraiResourceSecurityCondition security = new MoiraiResourceSecurityCondition();
            security.setResourceId(moiraiResource.getResourceId());
            security.setResourceType("1");
            List<MoiraiSecurityControl> securityList = moiraiSecurityControlMapper.getResourceSecurityList(security);
            redisTemplate.opsForValue().set(Constants.JUDGE_PERMISSION + moiraiResource.getJudgeAuthc(), securityList.toString());
        }

        // 通用权限
        MoiraiModuleServiceCondition condition = new MoiraiModuleServiceCondition();
        List<MoiraiModuleService> services = moiraiModuleMapper.selectListByCondition(condition);
        for (MoiraiModuleService moduleService : services) {
            MoiraiResourceSecurityCondition security = new MoiraiResourceSecurityCondition();
            security.setResourceId(moduleService.getId());
            security.setResourceType("0");
            List<MoiraiSecurityControl> securityList = moiraiSecurityControlMapper.getResourceSecurityList(security);
            redisTemplate.opsForValue().set(Constants.JUDGE_PERMISSION + moduleService.getUrlPrefix(), securityList.toString());
        }
    }
}
