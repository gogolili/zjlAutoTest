package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.feignclient.DefenseServiceClient;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiUserPwdService;
import com.baiwang.moirai.service.PasswordPolicyService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.service.PushService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.Base64Utils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.RSAUtils;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.spush.api.model.consumer.email.vo.EmailDetail;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("all")
public class MoiraiUserPwdServiceImpl implements MoiraiUserPwdService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private DefenseServiceClient defenseService;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired(required = false)
    private PushService pushService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private PasswordService passwordService;

    /**
     * 用户信息的验证以及短信的发送
     */
    @Override
    public void smsForPwd(MoiraiUserCondition moiraiUserCondition) {
        //验证手机号
        String userType = moiraiUserCondition.getUserType();
        String telephone = moiraiUserCondition.getTelephone();
        String userEmail = moiraiUserCondition.getUserEmail();
        String cert = moiraiUserCondition.getCert();
        if (Constants.USER_TYPE_B.equals(userType)) { //企业用户
            MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(moiraiUserCondition.getUserAccount());
            if (moiraiUser == null) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            if (RegularExpUtils.checkMobile(telephone)) {
                String telephone2 = moiraiUser.getTelephone();
                if (!RegularExpUtils.checkMobile(telephone2)){
                    throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_TELE_NOT_REGI);
                }
                if (!telephone.equals(telephone2)){
                    throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_TELE_ERROR);
                }

                // 验证滑块验证码
                this.checkCert(cert);
                // 发短信验证码
                this.sendPhoneMsg(telephone);
            } else if (RegularExpUtils.checkEmail(userEmail)) {
                String email = moiraiUser.getUserEmail();
                if (!RegularExpUtils.checkEmail(email)){
                    throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_EMAIL_NOT_REGI);
                }
                if (!userEmail.equals(email)){
                    throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_EMAIL_ERROR);
                }

                // 验证滑块验证码
                this.checkCert(cert);
                //生成验证码
                String smsCode = AdminUtils.getSmsValidateCode();
                //调用推送中心发送邮箱
                List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
                List<String> emails = new ArrayList<>();
                emails.add(email);
                LazyDynaBean lazyDynaBean = new LazyDynaBean();
                lazyDynaBean.set("emails", emails);
                lazyDynaBean.set("context", "您的百望云找回密码验证码为:" + smsCode + "（15分钟有效），请及时输入验证码输入框。");
                lazyDynaBeans.add(lazyDynaBean);
                passwordService.sendMail(Constants.MOIRAI_VERSION_V2, "百望云邮件验证码", "YZM_HTML", "", lazyDynaBeans);
                //redis存储验证码
                redisTemplate.opsForValue().set(Constants.MOIRAI_PWD_SMSCODE + email, smsCode, 15, TimeUnit.MINUTES);
                logger.info("邮件方法执行完成 email = 【{}】 smsCode = 【{}】", email, smsCode);
            } else {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_TEL_OR_EMAIL_ERROR);
            }
        } else {
            if (!RegularExpUtils.checkMobile(telephone)) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_TELEPHONE_ERROR);
            }
            MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(telephone);
            if (!Constants.DEFAULT_ONE.equals(moiraiUserCondition.getComeFrom())) {
                //注册C验证重复
                if (userByTelephone != null) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TELEPHONE_ALREADY_REG);
                }
            } else {
                if (userByTelephone == null) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
                }
            }
            // 验证滑块验证码
            this.checkCert(cert);
            // 发短信验证码
            this.sendPhoneMsg(telephone);
        }
    }

    /**
     * 发找回密码短信验证码
     *
     * @param telephone
     */
    private void sendPhoneMsg(String telephone) {
        //生成验证码
        String smsCode = AdminUtils.getSmsValidateCode();
        //调用推送中心发送短信
        pushService.sendMsg(telephone, smsCode);
        //redis存储验证码
        redisTemplate.opsForValue().set(Constants.MOIRAI_PWD_SMSCODE + telephone, smsCode, 15, TimeUnit.MINUTES);
        logger.info("短信方法执行完成 telephone = 【{}】 smsCode = 【{}】", telephone, smsCode);
    }

    /**
     * 滑块验证
     *
     * @param telephone
     * @param cert
     * @return
     */
    private void checkCert(String cert) {
        logger.info("开始发送短信:cert={}", cert);
        try {
            String checkCert = defenseService.checkToken(cert);
            logger.info("调用defense-service校验Cert接口返回:{}", checkCert);
            JSONObject jsonObject = JSONObject.parseObject(checkCert);
            if (!jsonObject.getBoolean("success")) {
                throw new RuntimeException("滑块验证码证书不正确！");
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CALL_FEIGN_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_SEND_SMS_FAIL);
        }
    }

    /**
     * 校验短信验证码
     *
     * @param moiraiUserCondition
     * @return
     * @throws IOException
     */
    public void checkSMSCode(MoiraiUserCondition moiraiUserCondition) {
        String smsCode = moiraiUserCondition.getSmsCode();
        String telephone = moiraiUserCondition.getTelephone();
        String userEmail = moiraiUserCondition.getUserEmail();
        String key = "";
        if (redisTemplate.hasKey(Constants.MOIRAI_PWD_SMSCODE + telephone)){
            key = telephone;
        } else if (redisTemplate.hasKey(Constants.MOIRAI_PWD_SMSCODE + userEmail)) {
            key = userEmail;
        }
        String code = (String) redisTemplate.opsForValue().get(Constants.MOIRAI_PWD_SMSCODE + key);
        logger.info("校验短信验证码:smsCode={}, telephone={} , email={}, code = {}", smsCode, telephone, userEmail, code);
        if (StringUtils.isBlank(code)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_SMSCODE_EXPIRE);
        }
        if (!smsCode.equals(code)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_SMSCODE_ERROR);
        }
        if (!Constants.MOIRAI_VERSION_V2.equals(moiraiUserCondition.getVersion())){
            // 不为2时删除手机号/邮箱
            redisTemplate.delete(Constants.MOIRAI_PWD_SMSCODE + key);
        }

    }

    @Override
    public Map<String, Object> createUuid(String key) {
        Map<String, Object> rtnMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        logger.info("*****随机字符串,randomStr={}*****", uuid);
        redisTemplate.opsForValue().set(Constants.MOIRAI_PWD_KEY + key, uuid, 10 * 60, TimeUnit.SECONDS);
        rtnMap.put("nonceStr", uuid);
        return rtnMap;
    }

    public void checkNonceStr(String nonceStr, String telephone, String email) {
        Object loginRandom = redisTemplate.opsForValue().get(Constants.MOIRAI_PWD_KEY + telephone);
        if (loginRandom == null || StringUtils.isBlank(loginRandom.toString())) {
            loginRandom = redisTemplate.opsForValue().get(Constants.MOIRAI_PWD_KEY + email);
        }
        logger.info("Redis中获取到的随机字符串:loginRandom={}", loginRandom);
        if (!nonceStr.equals(loginRandom)) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_UPDATE_PWD_NONCESTR_ERROR);
        }
    }

    /**
     * 短信验证码通过之后，跳转至密码修改的页面修改密码
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPwd(MoiraiUserCondition moiraiUserCondition) {
        String nonceStr = moiraiUserCondition.getNonceStr();
        if (Constants.DEFAULT_ONE.equals(moiraiUserCondition.getPasswordTrans())) {
            moiraiUserCondition.setNewPassword(Base64Utils.decodeString(moiraiUserCondition.getNewPassword()));
        } else if (Constants.DEFAULT_TWO.equals(moiraiUserCondition.getPasswordTrans())) {
            try {
                moiraiUserCondition.setNewPassword(RSAUtils.decryptByPrivateKey(moiraiUserCondition.getNewPassword(), Constants.PRIVATE_KEY));
            } catch (Exception e) {
                logger.error("解码失败", e);
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_DECRYPT_FAIL);
            }
        }
        String userType = moiraiUserCondition.getUserType();
        Long nowTimeLong = DateTimeUtils.nowTimeLong();
        if ("C".equals(userType)) {
            checkNonceStr(nonceStr, moiraiUserCondition.getTelephone(), null);
            //验证手机号存在
            MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(moiraiUserCondition.getTelephone());
            if (userByTelephone == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            // 校验密码规则
            if (Constants.MOIRAI_VERSION_V2.equals(moiraiUserCondition.getVersion())) {
                passwordPolicyService.checkRegisterPolicy(null, moiraiUserCondition.getNewPassword(), moiraiUserCondition.getTelephone());
                String newyhMm = AdminUtils.getUuidPasswd(moiraiUserCondition.getNewPassword(), userByTelephone.getUuid());
                if (newyhMm.equals(userByTelephone.getUserPassword())) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_NEW_AND_OLD_PWD_EQUALS);
                }
                // 校验密码 是否和最近几次相同
                passwordPolicyService.checkUpdatePolicy(null, userByTelephone.getUserId(), moiraiUserCondition.getNewPassword());
            } else {
                boolean rule = RegularExpUtils.checkPasswordRule(moiraiUserCondition.getNewPassword(), null);
                if (!rule) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PASSWORD_ERROR);
                }
            }
            //密码
            String uuid = userByTelephone.getUuid();
            String passwd = AdminUtils.getUuidPasswd(moiraiUserCondition.getNewPassword(), uuid);
            userByTelephone.setUserPassword(passwd);
            userByTelephone.setModifyTime(nowTimeLong);
            userByTelephone.setFirstLogin(Constants.flag_N);

            // 存储旧密码到历史密码表中
            passwordPolicyService.addHistoryPassword(userByTelephone);

            int i = moiraiUserMapper.updateByPrimaryKeySelective(userByTelephone);
        }
        if ("B".equals(userType)) {
            String userAccount = moiraiUserCondition.getUserAccount();
            MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(userAccount);
            if (moiraiUser == null) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            checkNonceStr(nonceStr, moiraiUser.getTelephone(), moiraiUser.getUserEmail());
            // 校验密码规则
            if (Constants.MOIRAI_VERSION_V2.equals(moiraiUserCondition.getVersion())) {
                passwordPolicyService.checkRegisterPolicy(moiraiUser.getTenantId(), moiraiUserCondition.getNewPassword(), userAccount);
                String newyhMm = AdminUtils.getUuidPasswd(moiraiUserCondition.getNewPassword(), moiraiUser.getUuid());
                if (newyhMm.equals(moiraiUser.getUserPassword())) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_NEW_AND_OLD_PWD_EQUALS);
                }
                // 校验密码 是否和最近几次相同
                passwordPolicyService.checkUpdatePolicy(moiraiUser.getTenantId(), moiraiUser.getUserId(), moiraiUserCondition.getNewPassword());
            } else {
                boolean rule = RegularExpUtils.checkPasswordRule(moiraiUserCondition.getNewPassword(), userAccount);
                if (!rule) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PASSWORD_ERROR);
                }
            }
            // 存储旧密码到历史密码表中
            passwordPolicyService.addHistoryPassword(moiraiUser);

            String newyhMm = AdminUtils.getUuidPasswd(moiraiUserCondition.getNewPassword(), moiraiUser.getUuid());
            moiraiUser.setUserPassword(newyhMm);
            moiraiUser.setModifyTime(nowTimeLong);
            moiraiUser.setFirstLogin(Constants.flag_N);
            int updateRtn = moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);
        }
        redisTemplate.delete(Constants.MOIRAI_PWD_KEY + nonceStr);
        unlockAccount(moiraiUserCondition);
    }

    /**
     * 调用bwapi做短信相关操作
     *
     * @param jsonObject
     * @param url
     */
    /*private void smsMethod(JSONObject jsonObject, String url) {
        logger.info("开始发送短信:url={}, param:{}", url, jsonObject);
        String json = restTemplate.postForObject(url, jsonObject.toString(), String.class);
        logger.info("调用bwapi短信相关接口返回:{}", json);
        JSONObject bwapi = JSONObject.parseObject(json);
        if (bwapi != null && "9999".equals(bwapi.get("Code"))) {
            String str = (String) bwapi.get("Message");
            if (str.contains("验证码")) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_SMSCODE_ERROR);
            }
        }
        JSONObject result = (JSONObject) bwapi.get("BwSmsSendResponse");
        if (result == null) {
            result = (JSONObject) bwapi.get("BwSmsCheckResponse");
        }
        if (result == null || !result.getBoolean("Success")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR.getCode(), "调用bwapi短信相关接口失败");
        }
    }*/

    /**
     * 发送短信
     *
     * @param moiraiUserCondition
     */
    @Override
    public BWJsonResult sendPhoneCode(MoiraiUserCondition moiraiUserCondition) {
        //根据ID查询用户信息，手机号要是未验证
        MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(moiraiUserCondition.getUserId());
        if (null == moiraiUser) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        if (moiraiUser.getTelephone() != null && "Y".equals(moiraiUser.getPhoneValidate())) {
            if (moiraiUser.getTelephone().equals(moiraiUserCondition.getTelephone())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDING_REPEAT);
            }
        }
        //发送短信
        String phone = moiraiUserCondition.getTelephone();
        //生成验证码
        String smsCode = AdminUtils.getSmsValidateCode();
        //调用推送中心发送短信
        pushService.sendMsg(phone, smsCode);
        //redis存储验证码
        redisTemplate.opsForValue().set(Constants.MOIRAI_PWD_SMSCODE + phone, smsCode, 15, TimeUnit.MINUTES);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("短信发送成功");
        return bwJsonResult;
    }

    /**
     * 绑定手机号
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindingPhone(MoiraiUserCondition moiraiUserCondition) {
        //验证短信验证码
        checkSMSCode(moiraiUserCondition);
        //查询手机号是否已被使用，只查询新库
        MoiraiUser user = moiraiUserMapper.getUserByTelephone(moiraiUserCondition.getTelephone(), moiraiUserCondition.getTenantId());
        if (null != user) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TELEPHONE_DUPLICATION_ERROR);
        }
        MoiraiUser userById = moiraiUserMapper.selectByPrimaryKey(moiraiUserCondition.getUserId());
        if (userById == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        //绑定手机号
        MoiraiUser moiraiUser = new MoiraiUser();
        Long nowTimeLong = DateTimeUtils.nowTimeLong();
        moiraiUser.setUserId(moiraiUserCondition.getUserId());
        moiraiUser.setTelephone(moiraiUserCondition.getTelephone());
        moiraiUser.setModifyTime(nowTimeLong);
        moiraiUser.setModifyUser(moiraiUserCondition.getModifyUser());
        moiraiUser.setPhoneValidate("Y");
        moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);
        //清除所有相同的手机号
        moiraiUserMapper.deleteSimplePhone(moiraiUserCondition.getTelephone());
        MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(moiraiUserCondition.getTelephone());
        if (userByTelephone == null) {
            moiraiUser = new MoiraiUser();
            moiraiUser.setBindAccount(userById.getUserAccount());
            moiraiUser.setBindCompany("Y");
            moiraiUser.setTelephone(moiraiUserCondition.getTelephone());
            moiraiUser.setVersion(moiraiUserCondition.getVersion());
            completeCUser(moiraiUser);
        } else {
            userByTelephone.setBindAccount(userById.getUserAccount());
            userByTelephone.setBindCompany("Y");
            userByTelephone.setModifyTime(nowTimeLong);
            userByTelephone.setModifyUser(moiraiUserCondition.getModifyUser());
            moiraiUserMapper.updateByPrimaryKeySelective(userByTelephone);
        }
    }

    public void completeCUser(MoiraiUser user) {
        //组装数据
        String uuid = AdminUtils.getUuid();
        String userPassword = user.getUserPassword();
        if (StringUtils.isNotBlank(userPassword)) {
            if (Constants.DEFAULT_ONE.equals(user.getPasswordTrans())) {
                userPassword = Base64Utils.decodeString(userPassword);
            } else if (Constants.DEFAULT_TWO.equals(user.getPasswordTrans())) {
                try {
                    userPassword = RSAUtils.decryptByPrivateKey(userPassword, Constants.PRIVATE_KEY);
                } catch (Exception e) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_DECRYPT_FAIL);
                }
            }
            if (Constants.MOIRAI_VERSION_V2.equals(user.getVersion())) {
                // v2版本需要根据密码策略进行验证密码规则
                passwordPolicyService.checkRegisterPolicy(null, userPassword, user.getTelephone());
            } else {
                boolean rule = RegularExpUtils.checkPasswordRule(userPassword, null);
                if (!rule) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PASSWORD_ERROR);
                }
            }
            String password = AdminUtils.getUuidPasswd(userPassword, uuid);
            user.setUserPassword(password);
        }
        Long id = seqnumFeignClient.getNum(Constants.MOIRAI_USER);
        Long userInfoId = seqnumFeignClient.getNum(Constants.MOIRAI_USERINFO);
        user.setUserType(Constants.USER_TYPE_C);
        user.setUserId(id);
        user.setUserinfoId(userInfoId);
        user.setUuid(uuid);
        Long nowTime = DateTimeUtils.nowTimeLong();
        user.setCreateTime(nowTime);
        user.setModifyTime(nowTime);
        user.setDelFlag(Constants.flag_N);
        user.setUseFlag(Constants.flag_Y);
        user.setDefaultUser(Constants.USER_TYPE_C);
        user.setFirstLogin(Constants.flag_N);
        moiraiUserMapper.insertSelective(user);
    }

    /**
     * 解除手机号绑定
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindPhone(MoiraiUserCondition moiraiUserCondition) {
        Long userId = moiraiUserCondition.getUserId();
        MoiraiUser user = moiraiUserMapper.selectByPrimaryKey(userId);
        String telephone = user.getTelephone();
        Long nowTimeLong = DateTimeUtils.nowTimeLong();
        if (user != null) {
            MoiraiUser moiraiUser = new MoiraiUser();
            moiraiUser.setModifyUser(moiraiUserCondition.getModifyUser());
            moiraiUser.setUserId(userId);
            moiraiUser.setModifyTime(nowTimeLong);
            moiraiUser.setTelephone("");
            moiraiUser.setPhoneValidate(Constants.flag_N);
            moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);
            MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(telephone);
            if (userByTelephone != null) {
                userByTelephone.setBindCompany(Constants.flag_N);
                userByTelephone.setBindAccount("");
                userByTelephone.setModifyTime(nowTimeLong);
                moiraiUserMapper.updateByPrimaryKeySelective(userByTelephone);
            }
        }
    }

    /**
     * 解除账号锁定--后台调用登录时使用
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public void unlockAccount(MoiraiUser moiraiUser) {
        MoiraiUser userByUserAccount;
        if (Constants.USER_TYPE_C.equals(moiraiUser.getUserType())) {
            userByUserAccount = moiraiUserMapper.getCUserByTelephone(moiraiUser.getTelephone());
            if (null == userByUserAccount) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            redisTemplate.delete(Constants.LOGINRETRYCOUNT + moiraiUser.getTelephone());
        } else {
            userByUserAccount = moiraiUserMapper.getUserByUserAccount(moiraiUser.getUserAccount());
            if (null == userByUserAccount) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            //账号锁定
            redisTemplate.delete(Constants.LOGINRETRYCOUNT + moiraiUser.getUserAccount());
        }
    }

    @Override
    public void sendEmail(MoiraiUserCondition moiraiUserCondition) {
        this.checkCert(moiraiUserCondition.getCert());
        int num = (int) ((Math.random() * 9 + 1) * 100000);
        String userEmail = moiraiUserCondition.getUserEmail();

        List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
        List<String> emails = new ArrayList<>();
        emails.add(userEmail);
        LazyDynaBean lazyDynaBean = new LazyDynaBean();
        lazyDynaBean.set("emails", emails);
        lazyDynaBean.set("context", "您的百望云登录验证码为:" + num + "（15分钟有效），请及时输入验证码输入框。");
        lazyDynaBeans.add(lazyDynaBean);
        passwordService.sendMail(Constants.MOIRAI_VERSION_V2, "百望云双因子验证码", "YZM_HTML", "", lazyDynaBeans);
        redisTemplate.opsForValue().set(Constants.MOIRAI_PWD_SMSCODE + userEmail, String.valueOf(num), 15, TimeUnit.MINUTES);
    }

    @Override
    public void checkEmail(String code, String userEmail) {
        String num = (String) redisTemplate.opsForValue().get(Constants.MOIRAI_PWD_SMSCODE + userEmail);
        if (StringUtils.isBlank(num)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_SMSCODE_EXPIRE);
        }
        if (!code.equals(num)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_SMSCODE_ERROR);
        }
        redisTemplate.delete(Constants.MOIRAI_PWD_SMSCODE + userEmail);
    }

    /**
     * 前端获取公钥
     *
     * @return
     */
    @Override
    public BWJsonResult getPublicKey() {
        Map<String, String> map = redisTemplate.opsForHash().entries(Constants.MOIRAI_KEY_PAIR);
        String publicKey = null;
        if (null == map || map.isEmpty()) {
            publicKey = this.getKeyPair();
        } else {
            publicKey = (String) map.get("RSAPublicKey");
        }
        Map<String, String> resMap = new HashMap<>(1);
        resMap.put("RSAPublicKey", publicKey);
        BWJsonResult bwJsonResult = new BWJsonResult(resMap);
        bwJsonResult.setMessage("获取公钥成功");
        return bwJsonResult;
    }

    /**
     * 后台获取私钥
     *
     * @return
     */
    @Override
    public String getPrivateKey() {
        Map<String, String> map = redisTemplate.opsForHash().entries(Constants.MOIRAI_KEY_PAIR);
        return (String) map.get("RSAPrivateKey");
    }

    /**
     * 定时生成密钥对
     *
     * @return
     */
    private String getKeyPair() {
        Map<String, Object> keyPair = null;
        try {
            keyPair = RSAUtils.genKeyPair();
        } catch (Exception e) {
            logger.error("生成密钥对失败", e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_RSA_KEY_PAIR);
        }
        String publicKey = RSAUtils.getPublicKey(keyPair);
        String privateKey = RSAUtils.getPrivateKey(keyPair);
        Map<String, String> map = new HashMap<>(2);
        map.put("RSAPublicKey", publicKey);
        map.put("RSAPrivateKey", privateKey);
        redisTemplate.opsForHash().putAll(Constants.MOIRAI_KEY_PAIR, map);
        redisTemplate.expire(Constants.MOIRAI_KEY_PAIR, 24, TimeUnit.HOURS);
        logger.info("*****密钥对生成成功-存储完成*****");
        return publicKey;
    }
}
