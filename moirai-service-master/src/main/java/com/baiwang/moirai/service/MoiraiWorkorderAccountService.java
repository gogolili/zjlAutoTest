package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccount;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccountCondition;

/**
 * @author LC
 */
public interface MoiraiWorkorderAccountService {

    /**
     * 找回密码
     */
    void backPassword(MoiraiWorkorderAccount workorderPwd);

    BWJsonResult<MoiraiWorkorderAccount> queryList(MoiraiWorkorderAccountCondition workorderPwd);

    int updateWorkorder(MoiraiWorkorderAccount workorder);
}
