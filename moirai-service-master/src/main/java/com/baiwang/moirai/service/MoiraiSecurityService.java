package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.role.MoiraiModuleService;
import com.baiwang.moirai.model.role.MoiraiModuleServiceCondition;
import com.baiwang.moirai.model.role.MoiraiResourceSecurity;
import com.baiwang.moirai.model.role.MoiraiResourceSecurityCondition;
import com.baiwang.moirai.model.role.MoiraiSecurityControl;
import com.baiwang.moirai.model.role.MoiraiSecurityControlCondition;
import java.util.List;

/**
 * @author LC
 * @date 2020/3/11 15:34
 */
public interface MoiraiSecurityService {

    /**
     * 查询规则列表
     */
    BWJsonResult<MoiraiSecurityControl> getSecurityControlList(MoiraiSecurityControlCondition query);

    /**
     * 查询已绑定资源规则列表
     */
    BWJsonResult<MoiraiSecurityControl> getResourceSecurityList(MoiraiResourceSecurityCondition query);

    /**
     * 添加规则
     */
    void addSecurityControl(MoiraiSecurityControl control);

    /**
     * 更新规则
     */
    void updateSecurityControl(MoiraiSecurityControl control);
    /**
     * 删除规则
     */
    void delSecurityControl(MoiraiSecurityControl control);

    /**
     * 绑定资源规则
     *
     * @param security
     */
    void insertResourceSecurity(List<MoiraiResourceSecurity> security);

    /**
     * 解绑资源规则
     *
     * @param security
     */
    void deleteResourceSecurity(MoiraiResourceSecurity security);

    BWJsonResult<MoiraiSecurityControl> queryUnbindSecurity(MoiraiResourceSecurityCondition securityCondition);

    BWJsonResult<MoiraiModuleService> queryGeneralInfo(MoiraiModuleServiceCondition serviceCondition);

    int addGeneralInfo(MoiraiModuleService generalInfo);

    int editGeneralInfo(MoiraiModuleService queryGeneralInfo);

    int deleteGeneralInfo(MoiraiModuleService moiraiModuleService);
}
