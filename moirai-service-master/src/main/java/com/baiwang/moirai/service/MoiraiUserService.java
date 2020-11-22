package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.user.*;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * 用户service
 *
 * @author zjt 2017-11-2
 */
public interface MoiraiUserService {

//    public void checkParam(MoiraiUser moiraiUser);

    public List<MoiraiUserAuthz> findAuthRoleListByUseId(Long userId) throws MoiraiException;

    /**
     * 添加用户
     *
     * @param moiraiUser
     * @return
     */
    MoiraiUser addUser(MoiraiUser moiraiUser);

    /**
     * 修改用户
     *
     * @param moiraiUser
     * @return
     */
    int updateUser(MoiraiUser moiraiUser);

    /**
     * 修改用户信息
     *
     * @param moiraiUser
     * @return
     */
    int updateUserInfo(MoiraiUser moiraiUser);

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    MoiraiUserinfo getUserInfoByUserId(Long userId);

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    int deleteUser(Long userId, String modifyUser);

    /**
     * 查询用户数据
     *
     * @param moiraiUserCondition
     * @return
     * @throws Exception
     */
    BWJsonResult<MoiraiUser> findUserListByCondition(MoiraiUserCondition moiraiUserCondition);

    /**
     * 导出用户数据
     */
    Workbook exportUserList(MoiraiUserCondition condition);

    /**
     * 查询用户数据
     *
     * @param moiraiUserCondition
     * @return
     * @throws Exception
     */
    MoiraiUser findUserByCondition(MoiraiUserCondition moiraiUserCondition) throws MoiraiException;

    /**
     * 增加角色
     *
     * @param moiraiUserAuthz
     * @return
     * @throws Exception
     */
    int addUserRole(MoiraiUserAuthz moiraiUserAuthz) throws MoiraiException;

    /**
     * 获取用户角色
     *
     * @param moiraiUserAuthz
     * @return
     * @throws Exception
     */
    List<MoiraiRole> getUserRole(MoiraiUserAuthz moiraiUserAuthz);

    /**
     * 获取授权组织机构分页+ 参数查询
     *
     * @param condition
     * @return
     * @throws Exception
     */
    BWJsonResult<MoiraiUserAuthzOrg> getUserOrgPage(MoiraiUserCondition condition) throws MoiraiException;

    /***
     * 批量判重邮箱
     * @return
     * @throws MoiraiException
     */
    List<Map<String, Integer>> batchEmailCheckRepetition(MoiraiEmailCheck moiraiEmailCheck) throws MoiraiException;

    /***
     * 获取角色分配的用户
     * @return
     * @throws MoiraiException
     */
    BWJsonResult<MoiraiUser> getUserByRoleId(MoiraiUserCondition moiraiUserCondition);

    /***
     * 删除角色分配的用户
     * @return
     * @throws MoiraiException
     */
    int delUserRoleByUserIdRoleId(MoiraiUserCondition moiraiUserCondition);

    /***
     * 获取组织机构开票员列表接口
     * @return
     * @author Lance cui
     */
    List<MoiraiUser> getOrgKpyUsers(MoiraiUserCondition moiraiUserCondition);

    /**
     * 根据租户ID查询所有用户 == 》南航
     *
     * @param moiraiUserCondition
     * @return
     */
    BWJsonResult<MoiraiUser> findUserByTenantId(MoiraiUserCondition moiraiUserCondition);

    /**
     * 更新用户登录历史表
     *
     * @param moiraiUser
     * @param type
     */
    void updateLastLogin(MoiraiUser moiraiUser, String type);

    /**
     * 更新用户登录历史表
     *
     * @param moiraiUserLoginHistory
     */
    void updateLastLoginHistory(MoiraiUserLoginHistory moiraiUserLoginHistory);

    /**
     * 查验用户登录
     *
     * @param moiraiUserCondition
     * @return
     */
    BWJsonResult checkLogin(MoiraiUserCondition moiraiUserCondition);

    /**
     * 赋值最后一次登录机构
     *
     * @param moiraiUser
     * @return
     */
    MoiraiUser selectLastLogin(MoiraiUser moiraiUser);

    /**
     * 查询登录用户信息
     *
     * @param moiraiUserCondition
     * @return
     */
    MoiraiUser findUserByLogin(MoiraiUserCondition moiraiUserCondition);

    /**
     * 校验账号是否是初始密码登录，是否是纯数字账号。
     *
     * @param moiraiUserCondition
     * @return
     */
    BWJsonResult checkLoginAccount(MoiraiUserCondition moiraiUserCondition);

    /**
     * 修改密码
     *
     * @param moiraiUserCondition
     * @return
     */
    void updateUserPassword(MoiraiUserCondition moiraiUserCondition);

    /**
     * 重置密码
     *
     * @param moiraiUserCondition
     * @return
     */
    BWJsonResult resetUserPassword(MoiraiUserCondition moiraiUserCondition);

    /**
     * 更新用户表登录时间
     *
     * @param moiraiUser
     */
    void updateUserLastLogin(MoiraiUser moiraiUser);

    /**
     * 通过用户id 获取用户数据权限范围
     *
     * @param scope
     * @return
     */
    MoiraiUserDataScope getScopeByUser(MoiraiUserDataScope scope);

    MoiraiUser addCUser(MoiraiUser moiraiUser);

    /**
     * 通过用户获取CP授权资源信息
     *
     * @param user
     * @return
     */
    List<MoiraiResource> getCPResource(MoiraiUser user);

    void getUsersByTenantId(Long teanantId);

    BWJsonResult<MoiraiUser> checkCLogin(MoiraiUserCondition moiraiUserCondition);

    void updateCPassword(MoiraiUserCondition moiraiUserCondition);

    @Deprecated
    void bindingBUser(MoiraiUserCondition moiraiUserCondition);

    @Deprecated
    void unbindBUser(MoiraiUserCondition moiraiUserCondition);

    int findUserCountByCondition(MoiraiUserCondition moiraiUserCondition);

    BWJsonResult batchOperation(List<MoiraiUserCondition> userList);

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>根据条件更新用户信息<BR>
     *
     * @return
     * @since 2019/10/25
     */
    public void updateUserByCondition(MoiraiUserCondition moiraiUserCondition);

    MoiraiOrg findTaxCodeByUserAccount(String userAccount);

    MoiraiUser findUserNoRoles(MoiraiUserCondition moiraiUserCondition);

    MoiraiUser getDefaultCompanyUser(MoiraiUser moiraiUser);

    void saveUserDataScope(Long userId, Long createTime, String creater, String scope);

    /**
     * 校验密码
     */
    boolean checkPassword(MoiraiUser user, String userPassword, String passwordTrans, String passwordCheckType);

    /**
     * 根据第三方用户信息获取百望用户
     * @param ssoUserInfo
     * @return
     */
    MoiraiUser getUserBySsoUserInfo(BWToken ssoUserInfo);

    /**
     * 绑定第三方用户信息
     * @param user
     * @return
     */
    MoiraiUser bindSsoUser(MoiraiUser user);

    List<MoiraiUser> findUserByAuth(MoiraiUserAuthz query);
}
