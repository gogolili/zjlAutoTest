package com.baiwang.moirai.controller.user;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.api.MoiraiUserMemberSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.user.MoiraiUserMember;
import com.baiwang.moirai.model.user.MoiraiUserMemberCondition;
import com.baiwang.moirai.service.MoiraiUserMemberService;
import com.baiwang.moirai.utils.StrUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 绑定企业账号
 *
 * @author LC
 * @date 2019/12/3 19:44
 */
@RestController
@RequestMapping("/userMember")
public class MoiraiUserMemberController implements MoiraiUserMemberSvc {


    @Autowired
    private MoiraiUserMemberService moiraiUserMemberService;

    /**
     * 发送短信验证码
     *
     * @param moiraiUserCondition
     */
    @Override
    public BWJsonResult sendPhoneCode(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        moiraiUserMemberService.sendPhoneCode(moiraiUserCondition.getTelephone(), moiraiUserCondition.getUserAccount(), moiraiUserCondition.getCert());
        BWJsonResult result = new BWJsonResult();
        result.setMessage("发送短信验证码成功！");
        return result;
    }

    /**
     * 绑定企业用户
     *
     * @param moiraiUserCondition
     */
    @Override
    public BWJsonResult bindTenant(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (StringUtils.isBlank(moiraiUserCondition.getSmsCode())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_SMSCODE_NULL_ERROR);
        }
        return moiraiUserMemberService.bindTenant(moiraiUserCondition);
    }

    /**
     * 解除企业绑定
     *
     * @param moiraiUserMember
     */
    @Override
    public BWJsonResult unBindTenant(@RequestBody MoiraiUserMember moiraiUserMember) {
        moiraiUserMemberService.unBindTenant(moiraiUserMember.getMemberId(), moiraiUserMember.getUserId());
        BWJsonResult result = new BWJsonResult();
        result.setMessage("解除绑定成功！");
        return result;
    }

    /// userMember /getTenants
    /**
     * <B>方法名称：</B>返回用户关联的租户列表<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/5
     */
    public BWJsonResult getBindTenantInfo(@RequestBody MoiraiUserMemberCondition moiraiUserMemberCondition){
        Long userId = moiraiUserMemberCondition.getUserId();
        if (userId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiUserMemberService.getBindTenantInfo(moiraiUserMemberCondition);
    }

    /**
     * <B>方法名称：</B>获取B端账号被关联的C端用户信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/5
     */
    public BWJsonResult<MoiraiUser> getBindUserInfo(@RequestBody MoiraiUserMemberCondition condition){
        Long userId = condition.getBindUserId();
        if (userId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiUserMemberService.getBindUserInfo(condition);
    }

    /**
     * <B>方法名称：</B>更新默认登录机构信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since
     */
    public BWJsonResult updateUserMember(@RequestBody Map<String,String> map){

        String userId = map.get("userId");
        String defaultOrgFlag = map.get("defaultOrgFlag");
        if (StrUtils.isEmpty(userId) || StrUtils.isEmpty(defaultOrgFlag)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiUserMemberService.updateUserMember(map);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("信息更新成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>设置默认登录时获取开启双因子的租户列表信息<BR>
     *
     * @return
     * @since 2019/12/18
     */
    public BWJsonResult getAuthTenant(@RequestBody MoiraiUserMemberCondition condition){

        Long userId = condition.getUserId();
        if (userId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiUserMemberCondition> authTenant = moiraiUserMemberService.getAuthTenant(condition);
        return new BWJsonResult(authTenant);
    }
}
