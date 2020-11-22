package com.baiwang.moirai.controller.workorder;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccount;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccountCondition;
import com.baiwang.moirai.service.MoiraiWorkorderAccountService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LC
 */
@RestController
@RequestMapping("/workorder/account")
public class WorkorderAccountController {

    @Autowired
    private MoiraiWorkorderAccountService workOrderService;

    /**
     * 租户找回密码
     *
     * @return
     */
    @PostMapping("/submitWorkorder")
    public BWJsonResult backPassword(@RequestBody MoiraiWorkorderAccount workorderPwd) {
        if (StringUtils.isBlank(workorderPwd.getTenantName()) || StringUtils.isBlank(workorderPwd.getTaxCode())
            || StringUtils.isBlank(workorderPwd.getTenantFile()) || StringUtils.isBlank(workorderPwd.getProvFile())
            || StringUtils.isBlank(workorderPwd.getContactName()) || StringUtils.isBlank(workorderPwd.getContactPhone())
            || StringUtils.isBlank(workorderPwd.getContactEmail())) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        workOrderService.backPassword(workorderPwd);
        BWJsonResult result = new BWJsonResult();
        result.setMessage("申请提交成功，请注意查收邮件！");
        return result;
    }

    /**
     * <B>方法名称：</B>查询工单列表<BR>
     * <B>概要说明：</B>2.0运营后台使用<BR>
     *
     * @return
     * @since 2020年5月27日
     */
    @PostMapping("/queryList")
    public BWJsonResult<MoiraiWorkorderAccount> queryList(@RequestBody MoiraiWorkorderAccountCondition workorderPwd) {
        return workOrderService.queryList(workorderPwd);
    }

    /**
     * <B>方法名称：</B>找回账号工单审核<BR>
     * <B>概要说明：</B>2.0运营后台使用<BR>
     *
     * @return
     * @since 2020年5月27日
     */
    @PostMapping("/updateWorkorder")
    public BWJsonResult<MoiraiWorkorderAccount> updateWorkorder(@RequestBody MoiraiWorkorderAccount workorder) {
        if (StringUtils.isBlank(workorder.getId()) || StringUtils.isBlank(workorder.getTaxCode()) ||
            StringUtils.isBlank(workorder.getAuditStatus()) || StringUtils.isBlank(workorder.getTenantName()) ||
            StringUtils.isBlank(workorder.getContactEmail())) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        int i = workOrderService.updateWorkorder(workorder);
        return new BWJsonResult<>();
    }

}
