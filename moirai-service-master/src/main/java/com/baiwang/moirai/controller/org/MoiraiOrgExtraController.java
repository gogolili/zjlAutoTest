package com.baiwang.moirai.controller.org;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.annotations.ResultMapOpt;
import com.baiwang.moirai.annotations.ResultMapping;
import com.baiwang.moirai.api.MoiraiExtraOrgSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.service.MoiraiOrgExtraService;
import com.baiwang.moirai.utils.StrUtils;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MoiraiOrgExtraController implements MoiraiExtraOrgSvc {

    @Autowired
    private MoiraiOrgExtraService moiraiOrgExtraService;

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>获取当前用户下的授权结构列表、以及查询符合条件的授权列表信息<BR>
     *
     * @return
     * @since 2019/10/16
     */
    @Override
    public BWJsonResult<MoiraiOrg> getOrgList(@RequestBody MoiraiOrgCondition moiraiOrgCondition) {
        if (moiraiOrgCondition == null && moiraiOrgCondition.getOrgId() == null && moiraiOrgCondition.getTenantId() == null
            && moiraiOrgCondition.getUserId() == null) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        int pageNo = moiraiOrgCondition.getPageNo();
        int pageSize = moiraiOrgCondition.getPageSize();
        if (pageNo == 0) {
            moiraiOrgCondition.setPageNo(1);
        }
        if (pageSize == 0) {
            moiraiOrgCondition.setPageSize(10);
        }
        return moiraiOrgExtraService.getOrgList(moiraiOrgCondition);
    }

    /**
     * <B>方法名称：</B>批量导出机构<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年6月24日
     */
    @Override
    @UserCenterOperationLog(moduleName = "机构管理", action = "批量导出机构", description = "批量导出机构")
    public BWJsonResult batchReadOrgInfo(HttpServletRequest request, HttpServletResponse response,
        @RequestBody MoiraiOrgCondition condition) {
        BWJsonResult bwJsonResult = new BWJsonResult();
        if (condition == null || StrUtils.isEmpty(condition.getTenantId() + "") || StrUtils.isEmpty(condition.getOrgId() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }

        moiraiOrgExtraService.batchReadOrgInfoService(request, response, condition);
        bwJsonResult.setMessage("数据成功导出!");
        bwJsonResult.setSuccess(true);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>获取可移动的机构树<BR>
     * <B>概要说明：</B>满足客户对机构树的移动<BR>
     *
     * @return
     * @since 2019年6月24日
     */
    @Override
    public BWJsonResult<MoiraiOrg> getOrgMoveTree(@RequestBody MoiraiOrgCondition moiraiOrgCondition) {
        if (moiraiOrgCondition == null || moiraiOrgCondition.getOrgId() == null || moiraiOrgCondition.getTenantId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult<MoiraiOrg> bwJsonResult = moiraiOrgExtraService.getOrgMoveTree(moiraiOrgCondition);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>查询租户机构树<BR>
     * <B>概要说明：</B>标记授权<BR>
     *
     * @return
     * @since 2019年12月5日
     */
    @Override
    public BWJsonResult<MoiraiOrg> getAuthOrgTree(@RequestBody MoiraiOrgCondition moiraiOrgCondition) {
        if (moiraiOrgCondition.getTenantId() == null || moiraiOrgCondition.getUserId() == null) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        MoiraiOrg moiraiOrg = moiraiOrgExtraService.getAuthOrgTree(moiraiOrgCondition);
        return new BWJsonResult<>(moiraiOrg);
    }

    /**
     * 切换机构的下拉选择
     *
     * @param authz
     * @return
     */
    @Override
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    public BWJsonResult<MoiraiUserAuthz> getOrgsOfUser(@RequestBody MoiraiUserAuthzOrg authz) {
        if (authz == null || authz.getTenantId() == null || authz.getUserId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiOrgExtraService.getOrgsOfUserService(authz);
    }

    /**
     * <B>方法名称：</B>查询直属父级机构树<BR>
     * <B>概要说明：</B>开放平台使用<BR>
     *
     * @return
     * @since 2020年6月5日
     */
    @Override
    public BWJsonResult<MoiraiOrg> getParentOrgTree(@RequestBody List<MoiraiOrgCondition> moiraiOrgCondition) {
        List<MoiraiOrg> moiraiOrg = moiraiOrgExtraService.getParentOrgTree(moiraiOrgCondition);
        return new BWJsonResult<>(moiraiOrg);
    }
}





