package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleCondition;

/**
 * @author 程路超
 */
public interface MoiraiRoleService {

    public BWJsonResult editSysRoles(MoiraiRole moiraiRole);

    void addRole(MoiraiRole moiraiRole);

    MoiraiRole queryRolesInfo(MoiraiRole moiraiRole);

    BWJsonResult<MoiraiRoleCondition> queryRolesList(MoiraiRoleCondition moiraiRoleCondition);

    BWJsonResult editRoles(MoiraiRole moiraiRole);

    BWJsonResult delRoles(MoiraiRole moiraiRole);

    /**
     * 查询角色列表时显示角色资源调用
     *
     * @param moiraiRole
     * @return
     */
    MoiraiRole queryOrgRoleInfo(MoiraiRole moiraiRole);

    BWJsonResult<MoiraiRoleCondition> getListDefaultRole(MoiraiRoleCondition moiraiRole);

    /**
     * 获取租户所有角色列表
     * @param condition
     * @return
     */
    BWJsonResult<MoiraiRole> getTenantAllRole(MoiraiOrgCondition condition);

    public void addSysRole(MoiraiRole moiraiRole);

    public BWJsonResult delSysRoles(MoiraiRole moiraiRole);
}
