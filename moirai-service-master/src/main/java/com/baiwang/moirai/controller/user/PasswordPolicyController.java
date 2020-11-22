package com.baiwang.moirai.controller.user;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.api.PasswordPolicySvc;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.service.PasswordPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密码策略
 *
 * @author LC
 * @date 2019/12/6 17:36
 */
@RestController
@RequestMapping("/passwordPolicy")
public class PasswordPolicyController implements PasswordPolicySvc {

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    /**
     * 获取租户密码策略配置信息
     *
     * @param config
     */
    @Override
    public BWJsonResult<MoiraiTenantConfig> getConfigList(@RequestBody MoiraiTenantConfig config) {
        return new BWJsonResult<>(passwordPolicyService.getPasswordPolicyConfigList(config.getTenantId()));
    }
}
