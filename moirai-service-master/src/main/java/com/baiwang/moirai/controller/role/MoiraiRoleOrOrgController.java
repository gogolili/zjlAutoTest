package com.baiwang.moirai.controller.role;

import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.api.MoiraiOrgAndRoleSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.role.MoiraiResourceCondition;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzCondition;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiUserExtraService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("all")
public class MoiraiRoleOrOrgController implements MoiraiOrgAndRoleSvc {

    @Autowired
    private MoiraiUserExtraService moiraiUserExtraService;

    /**
     * <B>方法名称：</B>获取用户授权机构<BR>
     * <B>概要说明：</B>销项/进项/供应链页面授权机构下拉<BR>
     *
     * @return
     * @since 2019年6月17日
     */
    @Override
    public BWJsonResult<MoiraiOrg> getUserAuthOrgTree(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (ObjectUtils.isEmpty(moiraiUserCondition) || moiraiUserCondition.getUserId() == null || moiraiUserCondition.getTenantId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiOrg orgTree = moiraiUserExtraService.getUserAuthOrgTree(moiraiUserCondition);
        return new BWJsonResult<>(orgTree);
    }

    /**
     * <B>方法名称：</B>保存授权<BR>
     * <B>概要说明：</B>页面点击保存按钮调用<BR>
     *
     * @return
     * @since 2019年5月19日
     */
    @Override
    @UserCenterOperationLog(moduleName = "用户管理", action = "编辑授权", description = "编辑授权")
    public BWJsonResult saveUserAuth(@RequestBody MoiraiUserAuthz moiraiUserAuthz) {
        if (moiraiUserAuthz == null || moiraiUserAuthz.getTenantId() == null || moiraiUserAuthz.getUserId() == null
            || moiraiUserAuthz.getUserOrg() == null || moiraiUserAuthz.getRoleId() == null
            || StringUtils.isEmpty(moiraiUserAuthz.getCreater()) || moiraiUserAuthz.getRoleOrg() == null
            || StringUtils.isEmpty(moiraiUserAuthz.getScope())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiUserExtraService.saveUserAuth(moiraiUserAuthz);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>E发票助手2.0定制赋权接口<BR>
     * <B>概要说明：</B>入参账号、产品IDList、角色IDList<BR>
     *
     * @return
     * @since 2019年3月22日
     */
    @Override
    public BWJsonResult userEmpowerment(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (moiraiUserCondition == null || StringUtils.isEmpty(moiraiUserCondition.getUserAccount())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiUserExtraService.userEmpowerment(moiraiUserCondition);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>E发票助手2.0判断是否赋权接口<BR>
     * <B>概要说明：</B>入参账号、产品IDList、角色IDList<BR>
     *
     * @return
     * @since 2019年3月22日
     */
    @Override
    public BWJsonResult userEPower(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (moiraiUserCondition == null || StringUtils.isEmpty(moiraiUserCondition.getUserAccount())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiUserExtraService.userEPower(moiraiUserCondition);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>授权、取消授权<BR>
     * <B>概要说明：</B>运营后台2.0批量<BR>
     *
     * @return
     * @since 2020年4月20日
     */
    @Override
    public BWJsonResult batchAuthResource(@RequestBody MoiraiResourceCondition resourceCondition) {
        if (resourceCondition == null || resourceCondition.getResourceId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiUserExtraService.batchAuthResource(resourceCondition);
        return bwJsonResult;
    }


















    /**
     * <B>方法名称：</B>获取用户授权机构<BR>
     * <B>概要说明：</B>销项/进项/供应链页面授权机构下拉<BR>
     *
     * @return
     * @since 2019年6月10日
     */
    @Override
    @Deprecated
    public BWJsonResult<MoiraiOrg> resourceOfOrg(@RequestBody MoiraiUserAuthzCondition auth) {
        if (auth == null || auth.getUserId() == null || auth.getTenantId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrg> authOrg = moiraiUserExtraService.getAuthOrgByResourceId(auth);
        return new BWJsonResult<MoiraiOrg>(authOrg);
    }

    /**
     * <B>方法名称：</B>新授权页面查询角色列表<BR>
     * <B>概要说明：</B>区分授权和非授权<BR>
     *
     * @return
     * @since 2019年5月16日
     */
    @Override
    @Deprecated
    public BWJsonResult<Map<String, List<MoiraiRole>>> getRoleListByAuth(
        @RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (moiraiUserCondition == null || moiraiUserCondition.getTenantId() == null ||
            moiraiUserCondition.getOrgId() == null || moiraiUserCondition.getUserId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult<Map<String, List<MoiraiRole>>> bwJsonResult = moiraiUserExtraService.getRoleListByAuth(moiraiUserCondition);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>新授权页面查询授权机构<BR>
     * <B>概要说明：</B>点击角色时显示授权树展示已授权机构<BR>
     *
     * @return
     * @since 2019年5月19日
     */
    @Override
    @Deprecated
    public BWJsonResult<Map<String, List<Long>>> getAuthOrgIdsByRole(@RequestBody MoiraiUserAuthz moiraiUserAuthz) {
        if (moiraiUserAuthz == null || moiraiUserAuthz.getTenantId() == null ||
            moiraiUserAuthz.getRoleId() == null || moiraiUserAuthz.getUserId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<Long> idsByRole = moiraiUserExtraService.getAuthOrgIdsByRole(moiraiUserAuthz);
        if (idsByRole != null) {
            Map<String, List<Long>> resMap = new HashMap<>();
            resMap.put("authOrg", idsByRole);
            return new BWJsonResult<>(resMap);
        }
        return new BWJsonResult<>();
    }
}
