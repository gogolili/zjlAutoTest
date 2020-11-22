package com.baiwang.moirai.event;

import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import com.baiwang.moirai.model.user.MoiraiUser;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-20 11:13
 * @Description:
 */
public class TenantEvent {

    private MoiraiUser moiraiUser;

    private MoiraiTenantVO moiraiTenant;

    /**
     * 添加租户
     * @param moiraiUser
     * @param moiraiTenant
     */
    public TenantEvent(MoiraiUser moiraiUser, MoiraiTenantVO moiraiTenant) {
        this.moiraiUser = moiraiUser;
        this.moiraiTenant = moiraiTenant;
    }

    public MoiraiUser getMoiraiUser() {
        return moiraiUser;
    }

    public void setMoiraiUser(MoiraiUser moiraiUser) {
        this.moiraiUser = moiraiUser;
    }

    public MoiraiTenantVO getMoiraiTenant() {
        return moiraiTenant;
    }

    public void setMoiraiTenant(MoiraiTenantVO moiraiTenant) {
        this.moiraiTenant = moiraiTenant;
    }
}
