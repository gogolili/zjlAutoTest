package com.baiwang.moirai.controller.tenant;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.api.MoiraiTenantConfigSvc;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfigCondition;
import com.baiwang.moirai.service.MoiraiTenantConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LC
 * @date 2019/12/9 17:16
 */
@RestController
@RequestMapping("/tenantConfig")
public class MoiraiTenantConfigController implements MoiraiTenantConfigSvc {

    @Autowired
    private MoiraiTenantConfigService moiraiTenantConfigService;

    /**
     * 获取租户配置list (分页、查库)
     *
     * @param condition
     */
    @Override
    public BWJsonResult<MoiraiTenantConfig> getConfigList(@RequestBody MoiraiTenantConfigCondition condition) {
        return moiraiTenantConfigService.getConfigList(condition);
    }

    /**
     * 获取租户配置list (缓存)
     *
     * @param config
     */
    @Override
    public BWJsonResult<MoiraiTenantConfig> getConfigListCache(@RequestBody MoiraiTenantConfig config) {
        return BWJsonResult.success(moiraiTenantConfigService.getConfigListCache(config.getTenantId(), config.getConfigCode()));
    }

    /**
     * 清除租户配置cache
     *
     * @param config
     */
    @Override
    public BWJsonResult clearConfigListCache(@RequestBody(required = false) MoiraiTenantConfig config) {
        moiraiTenantConfigService.clearConfigListCache(config != null ? config.getTenantId() : null);
        return BWJsonResult.success();
    }


}
