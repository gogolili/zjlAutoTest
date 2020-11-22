package com.baiwang.moirai.controller.org;

import com.baiwang.moirai.api.MoiraiOrgOtherSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.service.MoiraiOrgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-20 13:50
 * @Description:
 */
@RestController
public class MoiraiOrgOtherController implements MoiraiOrgOtherSvc {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private MoiraiOrgService moiraiOrgService;

    /**
     * 同步租户下所有开通CP功能的机构
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult syncUsers(@RequestBody MoiraiOrg moiraiOrg) {
        if( moiraiOrg.getTenantId()==null){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开始同步租户id：【{}】下开通CP功能的机构",moiraiOrg.getTenantId());
        moiraiOrgService.syncOrgs(moiraiOrg.getTenantId());
        return new BWJsonResult();
    }
}
