package com.baiwang.moirai.controller.org;

import com.baiwang.moirai.api.MoiraiOrgConfigSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgConfig;
import com.baiwang.moirai.service.MoiraiOrgAnotherService;
import com.baiwang.moirai.utils.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MoiraiOrgAnotherController implements MoiraiOrgConfigSvc {

    @Autowired
    private MoiraiOrgAnotherService moiraiOrgAnotherService;

    /**
     * 组织机构配置表查询接口
     *
     * @param moiraiOrgCondition
     * @return
     */
    public BWJsonResult<MoiraiOrgConfig> orgConfigFind(@RequestBody MoiraiOrgCondition moiraiOrgCondition) {

        if (0 >= moiraiOrgCondition.getPageNo()) {
            moiraiOrgCondition.setPageNo(1);
        }
        if (0 >= moiraiOrgCondition.getPageSize()) {
            moiraiOrgCondition.setPageSize(20);
        }

        if (StrUtils.isEmpty(moiraiOrgCondition.getItem()) && StrUtils.isEmpty(moiraiOrgCondition.getValue())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult<MoiraiOrgConfig> orgConfigFindService = moiraiOrgAnotherService.orgConfigFindService(moiraiOrgCondition);

        return orgConfigFindService;
    }

    public BWJsonResult orgConfigCount(@RequestBody MoiraiOrgCondition moiraiOrgCondition) {

        if (StrUtils.isEmpty(moiraiOrgCondition.getItem()) && StrUtils.isEmpty(moiraiOrgCondition.getValue())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        int total = moiraiOrgAnotherService.getOrgConfigCount(moiraiOrgCondition);

        return new BWJsonResult(total);
    }

    @Override
    public BWJsonResult deleteOrgConfig(@RequestBody MoiraiOrgConfig moiraiOrgConfig) {
        moiraiOrgAnotherService.deleteOrgConfig(moiraiOrgConfig);
        return new BWJsonResult();
    }


    /**
     * 根据开关状态获取机构信息:其中取数获取的是纳税主体
     *
     * @param moiraiOrgCondition
     * @return
     */
    public BWJsonResult getOrgBySwitchState(@RequestBody MoiraiOrgCondition moiraiOrgCondition) {

        String isFetch = moiraiOrgCondition.getIsFetch();
        String isProof = moiraiOrgCondition.getIsProof();
        String isAuthe = moiraiOrgCondition.getIsAuthe();
        if (StrUtils.isEmpty(isAuthe) && StrUtils.isEmpty(isProof) && StrUtils.isEmpty(isFetch)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        int pageNum = moiraiOrgCondition.getPageNo();
        int pageSize = moiraiOrgCondition.getPageSize();
        if (pageNum == 0) {
            moiraiOrgCondition.setPageNo(1);
        }
        if (pageSize == 0) {
            moiraiOrgCondition.setPageSize(5000);
        }

        Map<String, Object> orgBySwitchState = moiraiOrgAnotherService.getOrgBySwitchState(moiraiOrgCondition);
        Long total = (Long) orgBySwitchState.get("total");
        List<MoiraiOrg> orgList = (List<MoiraiOrg>) orgBySwitchState.get("orgList");
        BWJsonResult<MoiraiOrg> bwJsonResult = new BWJsonResult<>(orgList);
        bwJsonResult.setTotal(total.intValue());
        return bwJsonResult;
    }
}
