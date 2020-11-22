package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfigCondition;

import java.util.List;

/**
 * @author LC
 * @date 2019/12/9 14:14
 */
public interface MoiraiTenantConfigService {

    /**
     * 查询租户配置信息
     *
     * @param condition
     */
    BWJsonResult<MoiraiTenantConfig> getConfigList(MoiraiTenantConfigCondition condition);

    /**
     * 查询租户配置信息 cache
     * @param tenantId
     */
    List<MoiraiTenantConfig> getConfigListCache(Long tenantId, String configCode);

    /**
     * 清除租户配置信息缓存
     * @param tenantId
     */
    void clearConfigListCache(Long tenantId);

    /**
     * 添加租户配置信息
     *
     * @param moiraiTenant
     */
    void addConfig(MoiraiTenant moiraiTenant);

    /**
     * 更新配置信息
     *
     * @param moiraiTenant
     */
    void updateConfig(MoiraiTenant moiraiTenant);

    /**
     * 删除配置信息
     *
     * @param id
     */
    void delConfig(Long id);
}
