package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.user.MoiraiUserMember;
import com.baiwang.moirai.model.user.MoiraiUserMemberCondition;

import java.util.List;
import java.util.Map;

/**
 * @author LC
 * @date 2019/12/3 20:15
 */
public interface MoiraiUserMemberService {

    /**
     * 发送短信验证码
     * @param phone
     */
    void sendPhoneCode(String phone, String account, String cert);

    /**
     * 绑定企业信息
     *
     * @param moiraiUserCondition 待绑定信息
     */
    BWJsonResult bindTenant(MoiraiUserCondition moiraiUserCondition);

    public BWJsonResult getBindTenantInfo(MoiraiUserMemberCondition moiraiUserMemberCondition);

    public BWJsonResult getBindUserInfo(MoiraiUserMemberCondition condition);

    public void updateUserMember(Map<String,String> map);
    /**
     * 接触企业账号绑定
     *
     * @param memberId 绑定id
     */
    void unBindTenant(Long memberId, Long userId);

    /**
     * 解绑用户 兼容老接口 后期删除
     * @param cUser
     * @param bUser
     */
    @Deprecated
    void unBindTenant(MoiraiUser cUser, MoiraiUser bUser);

    /**
     * 根据B端用户id解除绑定关系
     * @param bUserId
     */
    void unBindTenant(Long bUserId);

    public List<MoiraiUserMemberCondition> getAuthTenant(MoiraiUserMemberCondition condition);
}
