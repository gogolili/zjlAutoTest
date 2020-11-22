package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.user.MoiraiSysUsers;
import com.baiwang.moirai.model.user.MoiraiSysUsersExtra;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;

/**
 *
 */
public interface MoiraiSysUsersService {

    MoiraiUser getBwUser(MoiraiSysUsers moiraiSysUsers);

    BWJsonResult bindBwUser(MoiraiSysUsersExtra moiraiSysUsersExtra);

}
