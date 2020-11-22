package com.baiwang.moirai.controller.workorder;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedway;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedwayCondition;
import com.baiwang.moirai.service.MoiraiWorkorderManagedwayService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LC
 * @date 10:07 2020/6/24
 **/
@RestController
@RequestMapping("/workorder/managedway")
public class WorkorderManagedwayController {

    @Autowired
    private MoiraiWorkorderManagedwayService managedwayService;

    /**
     * 托管方式变更申请
     */
    @PostMapping("/submit")
    public BWJsonResult backPassword(@RequestBody MoiraiWorkorderManagedway managedway) {
        if (StringUtils.isBlank(managedway.getOrgName()) || StringUtils.isBlank(managedway.getTaxCode())
                || StringUtils.isBlank(managedway.getDeviceCode()) || StringUtils.isBlank(managedway.getDeviceType())
                || StringUtils.isBlank(managedway.getBeforeTgType()) || StringUtils.isBlank(managedway.getAfterTgType())
                || managedway.getCreateUserId() == null || StringUtils.isBlank(managedway.getCreateUserName())) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        managedwayService.submit(managedway);
        BWJsonResult result = new BWJsonResult();
        result.setMessage("申请提交成功！");
        return result;
    }

    /**
     * 查询列表
     * queryList 给租户提供，受权限控制
     * queryAllList 给运营后台提供
     */
    @PostMapping(value = {"/queryList", "queryAllList"})
    public BWJsonResult<MoiraiWorkorderManagedway> queryList(@RequestBody MoiraiWorkorderManagedwayCondition managedwayCondition) {
        if (managedwayCondition.getPageNo() <= 0) {
            managedwayCondition.setPageNo(1);
        }
        if (managedwayCondition.getPageSize() <= 0) {
            managedwayCondition.setPageSize(10);
        }
        Page<Object> page = PageHelper.startPage(managedwayCondition.getPageNo(), managedwayCondition.getPageSize());
        List<MoiraiWorkorderManagedway> list = managedwayService.list(managedwayCondition);
        return new BWJsonResult<>(list, (int) page.getTotal());
    }

    /**
     * 审核工单
     */
    @PostMapping("/update")
    public BWJsonResult update(@RequestBody MoiraiWorkorderManagedway managedway) {
        if (StringUtils.isBlank(managedway.getId()) || StringUtils.isBlank(managedway.getAuditMsg())
                || StringUtils.isBlank(managedway.getAuditStatus()) || StringUtils.isBlank(managedway.getAuditUserName())
                || managedway.getAuditUserId() == null) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        managedwayService.update(managedway);
        return new BWJsonResult<>();
    }
}
