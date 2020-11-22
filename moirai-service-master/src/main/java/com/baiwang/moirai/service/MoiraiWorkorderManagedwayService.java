package com.baiwang.moirai.service;

import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedway;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedwayCondition;

import java.util.List;

/**
 * @author LC
 * @date 2020/6/24 10:14
 */
public interface MoiraiWorkorderManagedwayService {

    /**
     * 托管方式变更申请提交
     */
    void submit(MoiraiWorkorderManagedway managedway);

    /**
     * 查询列表
     */
    List<MoiraiWorkorderManagedway> list(MoiraiWorkorderManagedwayCondition managedwayCondition);

    /**
     * 审核工单
     */
    void update(MoiraiWorkorderManagedway managedway);
}
