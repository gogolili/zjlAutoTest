package com.baiwang.moirai.controller.role;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.annotations.ResultMapOpt;
import com.baiwang.moirai.annotations.ResultMapping;
import com.baiwang.moirai.api.MoiraiRoleSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleCondition;
import com.baiwang.moirai.service.MoiraiRoleService;
import com.baiwang.moirai.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 角色相关
 *
 * @author 程路超
 */
@RestController
@SuppressWarnings("all")
public class MoiraiRoleController implements MoiraiRoleSvc {
    private Logger logger = LoggerFactory.getLogger(MoiraiRoleController.class);

    @Autowired
    private MoiraiRoleService moiraiRoleService;

    /**
     * <B>方法名称：</B>添加角色<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     */

    @Override
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @UserCenterOperationLog(moduleName = "角色管理",action = "添加角色" ,description = "添加角色")
    public BWJsonResult addRole(@RequestBody MoiraiRole moiraiRole) {
        if (moiraiRole.getTenantId() == null || moiraiRole.getOrgId() == null ||
            StrUtils.isEmpty(moiraiRole.getCreater())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiRoleService.addRole(moiraiRole);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("添加角色成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>增加系统角色<BR>
     *
     * @return
     * @since 2020/2/19
     */
    public BWJsonResult addSysRole(@RequestBody MoiraiRole moiraiRole) {
        if (StrUtils.isEmpty(moiraiRole.getRoleName()) || StrUtils.isEmpty(moiraiRole.getCreater())
            || moiraiRole.getAssociatedProductId() == null || StrUtils.isEmpty(moiraiRole.getLowerSee())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiRoleService.addSysRole(moiraiRole);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("添加系统角色成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>修改角色<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     */
    @Override
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    public BWJsonResult editRoles(@RequestBody MoiraiRole moiraiRole) {
        if (moiraiRole.getRoleId() == null || StrUtils.isEmpty(moiraiRole.getModifyUser())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult rtnMap = moiraiRoleService.editRoles(moiraiRole);
        return rtnMap;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>更新系统角色<BR>
     *
     * @return
     * @since 2020/2/19
     */
    @Override
    public BWJsonResult editSysRole(@RequestBody MoiraiRole moiraiRole) {
        if (moiraiRole.getRoleId() == null || StrUtils.isEmpty(moiraiRole.getModifyUser()) ) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiRoleService.editSysRoles(moiraiRole);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("修改系统角色成功");
        return bwJsonResult;
    }


    /**
     * <B>方法名称：</B>删除角色<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     */
    @Override
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @UserCenterOperationLog(moduleName = "角色管理",action = "删除角色" ,description = "删除角色")
    public BWJsonResult delRoles(@RequestBody MoiraiRole moiraiRole) {
        if (moiraiRole == null || moiraiRole.getRoleId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiRoleService.delRoles(moiraiRole);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>删除系统预制角色<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     */
    @Override
    public BWJsonResult delSysRoles(@RequestBody MoiraiRole moiraiRole) {
        if (moiraiRole == null || moiraiRole.getRoleId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiRoleService.delSysRoles(moiraiRole);
        return bwJsonResult;
    }


    /**
     * <B>方法名称：</B>查询角色资源<BR>
     * <B>概要说明：</B>修改角色时，显示角色资源对应选中状态<BR>
     *
     * @return
     */
    @Override
    public BWJsonResult<MoiraiRole> queryRolesInfo(@RequestBody MoiraiRole moiraiRole) {
        if (moiraiRole.getRoleId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiRole productResource = moiraiRoleService.queryRolesInfo(moiraiRole);
        return new BWJsonResult(productResource);
    }


    /**
     * <B>方法名称：</B>查询角色详细信息<BR>
     * <B>概要说明：</B>查询角色列表时，右侧显示具体某个角色信息<BR>
     *
     * @return
     */
    @Override
    public BWJsonResult<MoiraiRole> queryOrgRoleInfo(@RequestBody MoiraiRole moiraiRole) {
        if (moiraiRole.getRoleId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiRole moiraiRole1 = moiraiRoleService.queryOrgRoleInfo(moiraiRole);
        logger.info("======查询到角色及资源信息=======" + moiraiRole1);
        return new BWJsonResult<>(moiraiRole1);
    }

    /**
     * <B>方法名称：</B>查询角色列表<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     */
    @Override
    public BWJsonResult<MoiraiRoleCondition> queryRolesList(@RequestBody MoiraiRoleCondition moiraiRoleCondition) {
        BWJsonResult<MoiraiRoleCondition> result = moiraiRoleService.queryRolesList(moiraiRoleCondition);
        return result;
    }

    /**
     * 查询内置角色和预制角色列表
     * @param moiraiRole
     * @return
     */
    @Override
    public BWJsonResult<MoiraiRoleCondition> getListDefaultRole(@RequestBody MoiraiRoleCondition moiraiRole){
        if (0 >= moiraiRole.getPageNo()) {
            moiraiRole.setPageNo(1);
        }
        if (0 >= moiraiRole.getPageSize()) {
            moiraiRole.setPageSize(10);
        }
        return moiraiRoleService.getListDefaultRole(moiraiRole);
    }

}
