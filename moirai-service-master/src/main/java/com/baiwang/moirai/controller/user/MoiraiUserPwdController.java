package com.baiwang.moirai.controller.user;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.api.MoiraiUserPwdSvc;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiUserPwdService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.RegularExpUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@SuppressWarnings("all")
public class MoiraiUserPwdController implements MoiraiUserPwdSvc {

    private static Logger logger = LoggerFactory.getLogger(MoiraiUserPwdController.class);

    @Autowired
    private MoiraiUserPwdService moiraiUserPwdService;

    @Autowired
    private PasswordService passwordService;

    /**
     * 短信验证通过之后修改用户登录密码
     */
    @Override
    public BWJsonResult modifyPwd(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        String nonceStr = moiraiUserCondition.getNonceStr();
        String newPwd = moiraiUserCondition.getNewPassword();
        String userType = moiraiUserCondition.getUserType();
        if ((StringUtils.isBlank(userType) || StringUtils.isBlank(newPwd) || StringUtils.isBlank(nonceStr)) ||
            ("C".equals(userType) && StringUtils.isBlank(moiraiUserCondition.getTelephone())) ||
            ("B".equals(userType) && StringUtils.isBlank(moiraiUserCondition.getUserAccount()))) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        moiraiUserPwdService.modifyPwd(moiraiUserCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("密码更改成功");
        return bwJsonResult;
    }

    /**
     * 通过调用别人的接口校验短信验证码
     *
     * @throws IOException
     */
    @Override
    public BWJsonResult checkPwd(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        String smsCode = moiraiUserCondition.getSmsCode();
        String telephone = moiraiUserCondition.getTelephone();
        String userEmail = moiraiUserCondition.getUserEmail();
        logger.info("校验手机/邮箱验证码:telephone={} userEmail={}, smsCode={}", telephone, userEmail, smsCode);
        if (null == moiraiUserCondition || StringUtils.isBlank(smsCode)
                || (StringUtils.isBlank(telephone) && StringUtils.isBlank(userEmail))) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        moiraiUserPwdService.checkSMSCode(moiraiUserCondition);
        String key = "";
        if (StringUtils.isNotBlank(telephone)){
            key = telephone;
        } else if (StringUtils.isNotBlank(userEmail)) {
            key = userEmail;
        }
        Map<String, Object> rtnMap = moiraiUserPwdService.createUuid(key);
        return new BWJsonResult(rtnMap);
    }

    /**
     * 校验手机号是否可用 并发送验证码
     *
     * @param 参数:userType,userAccount,telephone,cert
     * @return
     */
    @Override
    public BWJsonResult sendMessage(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        logger.info("*****修改密码请求信息:{}*****", moiraiUserCondition);
        if (null == moiraiUserCondition || StringUtils.isBlank(moiraiUserCondition.getUserType())
            || StringUtils.isBlank(moiraiUserCondition.getCert())) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*************忘记密码，发送短信***************");
        moiraiUserPwdService.smsForPwd(moiraiUserCondition);
        return new BWJsonResult();
    }

    /**
     * 返回默认密码
     */
    @RequestMapping("/getDefaultPwd")
    public BWJsonResult<String> getDefaultPwd(){
        return BWJsonResult.success(Constants.INIT_PWD);
    }

    /**
     * 发送短信
     *
     * @param moiraiUserCondition 参数:telephone,userId
     * @return
     */
    @Override
    @Deprecated
    public BWJsonResult sendPhoneCode(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        String telephone = moiraiUserCondition.getTelephone();
        Long userId = moiraiUserCondition.getUserId();
        if (null == userId || StringUtils.isEmpty(telephone)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("请求信息:userId={}, telephone={}", userId, telephone);

        BWJsonResult bwJsonResult = moiraiUserPwdService.sendPhoneCode(moiraiUserCondition);
        return bwJsonResult;
    }

    /**
     * 绑定手机号
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @UserCenterOperationLog(moduleName = "用户管理", action = "绑定手机号", description = "用户绑定手机号")
    @Deprecated
    public BWJsonResult bindingPhone(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        Long userId = moiraiUserCondition.getUserId();
        String telephone = moiraiUserCondition.getTelephone();
        String smsCode = moiraiUserCondition.getSmsCode();
        if (StringUtils.isEmpty(telephone) || StringUtils.isEmpty(smsCode) || userId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("*****绑定手机号请求参数*****", moiraiUserCondition.toString());
        moiraiUserPwdService.bindingPhone(moiraiUserCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("手机号绑定成功");
        return bwJsonResult;
    }

    /**
     * 解除绑定手机号
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @UserCenterOperationLog(moduleName = "用户管理", action = "解绑手机号", description = "用户解绑手机号")
    @Deprecated
    public BWJsonResult unbindPhone(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition.getUserId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("*****要解绑的用户ID：{}*****", moiraiUserCondition.getUserId());
        moiraiUserPwdService.unbindPhone(moiraiUserCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("手机号解绑成功");
        return bwJsonResult;
    }

    /**
     * 解除账号锁定(用于接口登录解锁用户)
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @UserCenterOperationLog(moduleName = "用户管理", action = "解除账号", description = "用户解除账号")
    public BWJsonResult unlockAccount(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (StringUtils.isEmpty(moiraiUserCondition.getUserAccount())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("*****要解锁的用户账号：{}*****", moiraiUserCondition.getUserAccount());
        moiraiUserPwdService.unlockAccount(moiraiUserCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("解锁成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>邮件发送<BR>
     * <B>概要说明：</B>双因子验证发送邮件<BR>
     *
     * @return
     * @since 2019年11月27日
     */
    public BWJsonResult sendEmail(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || StringUtils.isBlank(moiraiUserCondition.getUserType())) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        if (!RegularExpUtils.checkEmail(moiraiUserCondition.getUserEmail())) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_BAN_EDIT_FORMAT));
        }
        moiraiUserPwdService.sendEmail(moiraiUserCondition);
        return new BWJsonResult();
    }

    /**
     * <B>方法名称：</B>邮件校验<BR>
     * <B>概要说明：</B>双因子验证码<BR>
     *
     * @return
     * @since 2019年11月27日
     */
    public BWJsonResult checkEmail(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        String smsCode = moiraiUserCondition.getSmsCode();
        String userEmail = moiraiUserCondition.getUserEmail();
        logger.info("校验邮箱验证码:userEmail={}, smsCode={}", userEmail, smsCode);
        if (null == moiraiUserCondition || StringUtils.isBlank(smsCode) || StringUtils.isBlank(userEmail)) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        moiraiUserPwdService.checkEmail(smsCode, userEmail);
        Map<String, Object> rtnMap = moiraiUserPwdService.createUuid(userEmail);
        return new BWJsonResult(rtnMap);
    }

    /**
     * 获取公钥
     *
     * @return
     */
    public BWJsonResult getPublicKey() {
        logger.info("*****获取公钥：start*****");
        BWJsonResult bwJsonResult = moiraiUserPwdService.getPublicKey();
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>获取密码策略<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2020年06月30日
     */
    @RequestMapping(value = {"/getPasswordMark"}, method = RequestMethod.POST)
    public BWJsonResult getPasswordMark() {
        boolean mark = passwordService.calculatePasswordMark();
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setSuccess(mark);
        return bwJsonResult;
    }
}
