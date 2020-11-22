package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiResourceCondition;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzCondition;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.vo.MoiraiProductVo;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public interface MoiraiUserExtraService {

    void checkUserInfo(MoiraiUserCondition condition);

    Map<String, Object> getUserList(MoiraiUserCondition condition);

    List<MoiraiOrg> getAuthOrgByResourceId(MoiraiUserAuthzCondition auth);

    List<MoiraiUserDataScope> getUserDataScope(MoiraiUserCondition condition);

    MoiraiOrg getUserAuthOrgTree(MoiraiUserCondition moiraiUserCondition);

    /**
     * 获取用户所有资源
     *
     * @param moiraiUserCondition
     * @return
     */
    List<MoiraiOrgProduct> getResourceService(MoiraiUserCondition moiraiUserCondition, boolean treeFlag);

    /**
     * 获取用户资源
     */
    List<MoiraiProductVo> getResourceServiceExt(MoiraiUserCondition moiraiUserCondition, boolean b);

    List<String> getResourceAuthcService(MoiraiUserCondition moiraiUserCondition);

    BWJsonResult userEmpowerment(MoiraiUserCondition moiraiUserCondition);

    BWJsonResult userEPower(MoiraiUserCondition moiraiUserCondition);

    BWJsonResult<Map<String, List<MoiraiRole>>> getRoleListByAuth(MoiraiUserCondition condition);

    List<Long> getAuthOrgIdsByRole(MoiraiUserAuthz authz);

    BWJsonResult saveUserAuth(MoiraiUserAuthz authz);

    BWJsonResult delUserAuth(MoiraiUserAuthz moiraiUserAuthz);

    List<MoiraiRole> getRoleListByUserAuth(MoiraiUserCondition condition);

    List<MoiraiOrg> getAuthOrgByRole(MoiraiUserAuthz moiraiUserAuthz);
    void downloadUserAuthTemplate(Long userId, HttpServletResponse response);

    BWJsonResult batchAuthResource(MoiraiResourceCondition resourceCondition);

    BWJsonResult<MoiraiUserCondition> findBUserListByCondition(MoiraiUserCondition condition);
}
