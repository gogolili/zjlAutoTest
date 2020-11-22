package com.baiwang.moirai.controller.org.api;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.utils.StrUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对开放平台接口，禁止其他地方调用
 */
@RestController
@RequestMapping("/orgApi")
public class MoiraiOrgApiController {

    private static Logger logger = LoggerFactory.getLogger(MoiraiOrgApiController.class);

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    /**
     * 添加组织机构
     *
     * @param moiraiOrg
     * @return
     */
    @UserCenterOperationLog(moduleName = "机构管理", action = "添加机构", description = "添加机构")
    @RequestMapping(value = "/addOrg")
    public BWJsonResult<MoiraiUser> addOrg(@RequestBody MoiraiOrg moiraiOrg) {
        String orgCode = moiraiOrg.getOrgCode();
        String parentOrgCode = moiraiOrg.getParentOrgCode();
        Integer orgType = moiraiOrg.getOrgType();
        String creater = moiraiOrg.getCreater();
        Long tenantId = moiraiOrg.getTenantId();
        /**插入数据库必填字段**/
        if (StringUtils.isBlank(orgCode) || StringUtils.isBlank(parentOrgCode) || orgType == null || StrUtils.isEmpty(creater) || tenantId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开放平台addOrg接口入参:{}", moiraiOrg);
        MoiraiOrg org = new MoiraiOrg();
        org.setTenantId(tenantId);
        org.setOrgCode(parentOrgCode);
        MoiraiOrg oneOrg = moiraiOrgService.selectOneOrg(org);
        if (oneOrg == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_PARENTNOTEXT);
        }
        moiraiOrg.setParentOrg(oneOrg.getOrgId());
        MoiraiUser moiraiUser = moiraiOrgService.addorg(moiraiOrg);
        BWJsonResult bWJsonResult = new BWJsonResult(moiraiUser);
        bWJsonResult.setMessage("添加组织机构成功");
        return bWJsonResult;
    }

    @UserCenterOperationLog(moduleName = "机构管理", action = "编辑机构", description = "编辑机构")
    @RequestMapping(value = "/updateOrg")
    public BWJsonResult<MoiraiOrg> updateOrg(@RequestBody MoiraiOrg moiraiOrg) {
        Long tenantId = moiraiOrg.getTenantId();
        String orgCode = moiraiOrg.getOrgCode();
        if (StringUtils.isBlank(orgCode) || tenantId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开放平台updateOrg接口入参:{}", moiraiOrg);
        MoiraiOrg org = new MoiraiOrg();
        org.setTenantId(tenantId);
        org.setOrgCode(orgCode);
        MoiraiOrg oneOrg = moiraiOrgService.selectOneOrg(org);
        if (oneOrg != null) {
            moiraiOrg.setOrgId(oneOrg.getOrgId());
            int updateResult = moiraiOrgService.updateOrg(moiraiOrg);
            if (updateResult == 0) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_UPDATEERROR);
            }
        } else {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_UPDATEERROR);
        }
        BWJsonResult BWJsonResult = new BWJsonResult();
        BWJsonResult.setMessage("更新组织机构成功");
        return BWJsonResult;
    }

    @RequestMapping(value = "/updateOrgEntity")
    public BWJsonResult<MoiraiOrg> updateOrgEntity(@RequestBody MoiraiOrg moiraiOrg) {
        Long tenantId = moiraiOrg.getTenantId();
        Long orgId = moiraiOrg.getOrgId();
        if (orgId == null || tenantId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开放平台updateOrgEntity接口入参:{}", moiraiOrg);
        moiraiOrgService.updateOrgEntity(moiraiOrg);
        BWJsonResult BWJsonResult = new BWJsonResult();
        BWJsonResult.setMessage("更新组织机构成功");
        return BWJsonResult;
    }

    @RequestMapping(value = "/getOrgByCondition")
    public BWJsonResult<MoiraiOrg> getOrgByCondition(@RequestBody MoiraiOrgCondition moiraiOrg) {
        Long tenantId = moiraiOrg.getTenantId();
        if (tenantId == null && StringUtils.isBlank(moiraiOrg.getTaxCode())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开放平台getOrgByCondition接口入参:{}", moiraiOrg);
        return moiraiOrgService.getOrgByCondition(moiraiOrg);
    }

}
