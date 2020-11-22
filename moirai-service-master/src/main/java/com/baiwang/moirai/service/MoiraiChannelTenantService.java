package com.baiwang.moirai.service;

import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;

import java.util.List;

/**
 * @author LC
 * @date 2020/2/28 12:43
 */
public interface MoiraiChannelTenantService {

    /**
     * 批量添加租户渠道关系
     * @param channelTenants
     */
    void addChannelTenantList(Long tenantId,List<Long> qdbmList);
}
