package com.baiwang.moirai.service;

import java.util.Map;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgConfig;

public interface MoiraiOrgAnotherService {

	public BWJsonResult<MoiraiOrgConfig> orgConfigFindService(MoiraiOrgCondition moiraiOrgCondition);
	
	public int getOrgConfigCount(MoiraiOrgCondition moiraiOrgCondition);

	public Map<String,Object> getOrgBySwitchState(MoiraiOrgCondition moiraiOrgCondition);

    void deleteOrgConfig(MoiraiOrgConfig config);
}
