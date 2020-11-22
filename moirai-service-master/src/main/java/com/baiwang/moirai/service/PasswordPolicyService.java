package com.baiwang.moirai.service;

import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.model.user.MoiraiUser;

import java.util.List;

/**
 * 密码策略
 *
 * @author LC
 * @date 2019/12/1 13:19
 */
public interface PasswordPolicyService {

    /**
     * 获取租户密码策略配置列表
     *
     * @param tenantId
     */
    List<MoiraiTenantConfig> getPasswordPolicyConfigList(Long tenantId);

    /**
     * 校验首次登陆
     * @param tenantId
     * @return
     */
    Boolean checkFirstLogin(Long tenantId);

    /**
     * 校验用户密码是否超过有效期
     *
     * @param tenantId 租户id
     * @param userId 用户id
     */
    Boolean checkPwdOverdue(Long tenantId, Long userId);

    /**
     * 校验注册密码策略
     *
     * @param tenantId 租户id
     * @param password 明文用户密码
     * @param account  用户名称/电话
     */
    Boolean checkRegisterPolicy(Long tenantId, String password, String account);

    /**
     * 校验更新用户密码策略
     *
     * @param tenantId 租户id
     * @param userId      用户id
     * @param newPassword 明文新密码
     */
    void checkUpdatePolicy(Long tenantId, Long userId, String newPassword);

    /**
     * 存储历史密码
     * @param moiraiUser
     */
    void addHistoryPassword(MoiraiUser moiraiUser);
}
