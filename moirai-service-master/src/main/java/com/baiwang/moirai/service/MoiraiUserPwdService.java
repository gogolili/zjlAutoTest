package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import java.util.Map;

public interface MoiraiUserPwdService {

    void smsForPwd(MoiraiUserCondition moiraiUserCondition);

    void modifyPwd(MoiraiUserCondition moiraiUserCondition);

    void checkSMSCode(MoiraiUserCondition moiraiUserCondition);

    BWJsonResult sendPhoneCode(MoiraiUserCondition moiraiUserCondition);

    void bindingPhone(MoiraiUserCondition moiraiUserCondition);

    void unbindPhone(MoiraiUserCondition moiraiUserCondition);

    void unlockAccount(MoiraiUser moiraiUser);

    BWJsonResult getPublicKey();

    String getPrivateKey();

    Map<String, Object> createUuid(String telephone);

    void completeCUser(MoiraiUser user);

    void checkNonceStr(String nonceStr, String telephone, String email);

    void sendEmail(MoiraiUserCondition moiraiUserCondition);

    void checkEmail(String smsCode, String userEmail);
}
