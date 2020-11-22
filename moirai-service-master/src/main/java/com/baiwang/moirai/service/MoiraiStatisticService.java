package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.user.MoiraiUserLoginHistoryCondition;

import java.util.List;
import java.util.Map;

/**
 * @author LC
 * @date 2020/8/7 16:23
 */
public interface MoiraiStatisticService {

    /**
     * 查询用户登陆统计数据
     */
    List<Map> queryLogin(MoiraiUserLoginHistoryCondition query);

    /**
     * 查询用户登陆列表
     * @param query
     * @return
     */
    BWJsonResult<MoiraiUserLoginHistoryCondition> queryLoginList(MoiraiUserLoginHistoryCondition query);
}
