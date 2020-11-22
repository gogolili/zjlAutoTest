package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MoiraiOrgExtraService {

	BWJsonResult<MoiraiOrg> getOrgList(MoiraiOrgCondition moiraiOrgCondition);

    BWJsonResult<MoiraiUserAuthz> getOrgsOfUserService(MoiraiUserAuthzOrg authz);

	void batchReadOrgInfoService(HttpServletRequest request, HttpServletResponse response, MoiraiOrgCondition condition);

    BWJsonResult<MoiraiOrg> getOrgMoveTree(MoiraiOrgCondition condition);

    MoiraiOrg getAuthOrgTree(MoiraiOrgCondition moiraiOrgCondition);

    List<MoiraiOrg> getParentOrgTree(List<MoiraiOrgCondition> moiraiOrgCondition);
}
