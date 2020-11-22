package com.baiwang.moirai.controller.role;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.util.StringUtil;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.role.MoiraiModuleService;
import com.baiwang.moirai.model.role.MoiraiModuleServiceCondition;
import com.baiwang.moirai.model.role.MoiraiResourceSecurity;
import com.baiwang.moirai.model.role.MoiraiResourceSecurityCondition;
import com.baiwang.moirai.model.role.MoiraiSecurityControl;
import com.baiwang.moirai.model.role.MoiraiSecurityControlCondition;
import com.baiwang.moirai.service.MoiraiSecurityService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LC
 */
@RestController
@RequestMapping("/security")
public class MoiraiSecurityController {

    @Autowired
    private MoiraiSecurityService moiraiSecurityService;

    /**
     * 查询规则列表
     */
    @PostMapping("/getSecurityControlList")
    BWJsonResult<MoiraiSecurityControl> getSecurityControlList(@RequestBody MoiraiSecurityControlCondition query) {
        return moiraiSecurityService.getSecurityControlList(query);
    }

    /**
     * 查询已绑定资源规则列表
     */
    @PostMapping("/getResourceSercurityList")
    BWJsonResult<MoiraiSecurityControl> getResourceSecurityList(@RequestBody MoiraiResourceSecurityCondition query) {
        if (query.getResourceId() == null || query.getResourceType() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiSecurityService.getResourceSecurityList(query);
    }

    /**
     * 添加规则
     */
    @PostMapping("/addSecurityControl")
    BWJsonResult addSecurityControl(@RequestBody MoiraiSecurityControl control) {
        if (control == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        String name = control.getName();
        String field = control.getRealField();
        if (StringUtil.isBlank(name) || StringUtil.isBlank(field)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.addSecurityControl(control);
        return BWJsonResult.success();
    }

    /**
     * 更新规则
     */
    @PostMapping("/updateSecurityControl")
    BWJsonResult updateSecurityControl(@RequestBody MoiraiSecurityControl control) {
        if (control == null || control.getId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.updateSecurityControl(control);
        return BWJsonResult.success();
    }

    /**
     * 删除规则
     */
    @PostMapping("/delSecurityControl")
    BWJsonResult delSecurityControl(@RequestBody MoiraiSecurityControl control) {
        if (control == null || control.getId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.delSecurityControl(control);
        return BWJsonResult.success();
    }

    /**
     * 绑定资源规则
     *
     * @param security
     */
    @PostMapping("/insertResourceSecurity")
    BWJsonResult insertResourceSecurity(@RequestBody List<MoiraiResourceSecurity> security) {
        if (security == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.insertResourceSecurity(security);
        return BWJsonResult.success();
    }

    /**
     * 解绑资源规则
     *
     * @param security
     */
    @PostMapping("/deleteResourceSecurity")
    BWJsonResult deleteResourceSecurity(@RequestBody MoiraiResourceSecurity security) {
        if (security == null || security.getResourceId() == null || security.getSecurityControlId() == null || security.getResourceType() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.deleteResourceSecurity(security);
        return BWJsonResult.success();
    }

    /**
     * 查询资源还未绑定规则
     *
     * @param securityCondition
     */
    @PostMapping("/queryUnbindSecurity")
    BWJsonResult<MoiraiSecurityControl> queryUnbindSecurity(@RequestBody MoiraiResourceSecurityCondition securityCondition) {
        if (securityCondition == null || securityCondition.getResourceId() == null || securityCondition.getResourceType() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiSecurityService.queryUnbindSecurity(securityCondition);
    }

    //通用规则配置

    /**
     * 查询通用服务信息列表
     *
     * @param queryGeneralInfo
     */
    @PostMapping("/queryGeneralInfo")
    BWJsonResult<MoiraiModuleService> queryGeneralInfo(@RequestBody MoiraiModuleServiceCondition queryGeneralInfo) {
        return moiraiSecurityService.queryGeneralInfo(queryGeneralInfo);
    }

    /**
     * 添加通用服务信息
     *
     * @param info
     */
    @PostMapping("/addGeneralInfo")
    BWJsonResult addGeneralInfo(@RequestBody MoiraiModuleService info) {
        if (info == null || StringUtil.isBlank(info.getServiceName()) || StringUtil.isBlank(info.getUrlPrefix())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.addGeneralInfo(info);
        return new BWJsonResult();
    }

    /**
     * 编辑通用服务信息
     *
     * @param info
     */
    @PostMapping("/editGeneralInfo")
    BWJsonResult editGeneralInfo(@RequestBody MoiraiModuleService info) {
        if (info == null || StringUtil.isBlank(info.getServiceName()) || StringUtil.isBlank(info.getUrlPrefix()) || info.getId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.editGeneralInfo(info);
        return new BWJsonResult();
    }

    /**
     * 删除通用服务信息
     *
     * @param info
     */
    @PostMapping("/deleteGeneralInfo")
    BWJsonResult deleteGeneralInfo(@RequestBody MoiraiModuleService info) {
        if (info == null || info.getId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSecurityService.deleteGeneralInfo(info);
        return new BWJsonResult();
    }
}
