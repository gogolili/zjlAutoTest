package com.baiwang.moirai.controller.tenant.api;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.event.TenantEvent;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiTenantService;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author LC
 * @date 2020/5/19 15:33
 */
@RestController
@RequestMapping("/tenantApi")
public class MoiraiTenantApiController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiTenantService moiraiTenantService;

    @Resource
    private ApplicationEventPublisher context;

    /**
     * 租户添加
     *
     * @param moiraiTenant
     * @return
     */
    @PostMapping("/addTenant")
    @Transactional
    public BWJsonResult<MoiraiUser> addTenant(@RequestBody MoiraiTenantVO moiraiTenant) {
        //必要字段判空
        String tenantName = moiraiTenant.getTenantName();
        String tenantEmail = moiraiTenant.getTenantEmail();
        String taxCode = moiraiTenant.getTaxCode();
        if (RegularExpUtils.validName(tenantName) || StrUtils.isEmpty(tenantEmail)) {
            throw new MoiraiException("2101", "租户必填字段为空或不正确");
        }
        if (!RegularExpUtils.checkEmail(tenantEmail)) {
            throw new MoiraiException("2103", "租户邮箱不合法");
        }
        int tenantStatus = moiraiTenantService.tenantRegisterStatus(tenantName, taxCode);

        logger.info(tenantName + "----" + taxCode + "----" + "注册状态tenantStatus:" + tenantStatus);

        if (tenantStatus == 1 || tenantStatus == 101 || tenantStatus == 4) {
            throw new MoiraiException("2102", "租户名称或者税号已经注册过");
        }
        //删除僵尸租户。（如果失败如何操作）
        if (tenantStatus == 0) {
            int delZombie = moiraiTenantService.deleteZombieTenant(tenantName, taxCode);
        }

        logger.info(moiraiTenant.toString());
        moiraiTenant.setCreater("开放平台");
        moiraiTenant.setOriginMark("4");
        MoiraiUser regResult = moiraiTenantService.addTenant(moiraiTenant);
        if (regResult == null) {
            throw new MoiraiException("2103", "租户注册添加失败");
        }
        context.publishEvent(new TenantEvent(regResult, moiraiTenant));
        BWJsonResult bwJsonResult = new BWJsonResult<>(regResult);
        bwJsonResult.setMessage("租户添加成功");
        return bwJsonResult;
    }
}
