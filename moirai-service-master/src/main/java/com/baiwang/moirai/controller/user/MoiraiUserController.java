package com.baiwang.moirai.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.annotations.ResultMapOpt;
import com.baiwang.moirai.annotations.ResultMapping;
import com.baiwang.moirai.api.MoiraiUserSvc;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleCondition;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.model.user.*;
import com.baiwang.moirai.model.vo.MoiraiProductVo;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiResourcesService;
import com.baiwang.moirai.service.MoiraiUserExtraService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.utils.RegularExpUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@SuppressWarnings("all")
public class MoiraiUserController implements MoiraiUserSvc {

    private static Logger logger = LoggerFactory.getLogger(MoiraiUserController.class);

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    private MoiraiResourcesService moiraiResourcesService;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiUserExtraService moiraiUserExtraService;

    /**
     * 资源拥用权限
     */
    @Override
    public BWJsonResult<String> getResourceAuthc(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || null == moiraiUserCondition.getUserId() || null == moiraiUserCondition.getOrgId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****getResourceAuthc接口请求参数:{}*****", moiraiUserCondition.toString());
        List<String> resourceAuthcService = moiraiUserExtraService.getResourceAuthcService(moiraiUserCondition);
        return new BWJsonResult<>(resourceAuthcService);
    }

    /**
     * 获取用户所有资源
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiOrgProduct> getResource(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || null == moiraiUserCondition.getUserId() || null == moiraiUserCondition.getOrgId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****getResource接口请求参数:{}*****", moiraiUserCondition.toString());
        List<MoiraiOrgProduct> resourceService = moiraiUserExtraService.getResourceService(moiraiUserCondition, true);
        return new BWJsonResult<>(resourceService);
    }

    /**
     * 获取用户所有资源
     *
     * @param moiraiUserCondition
     * @return
     */
    @PostMapping(value = {"/getResourceExt"})
    public BWJsonResult<MoiraiProductVo> getResourceExt(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || null == moiraiUserCondition.getUserId() || null == moiraiUserCondition.getOrgId()
                || StringUtils.isBlank(moiraiUserCondition.getResourceWay())) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****getResourceExt接口请求参数:{}*****", JSONObject.toJSONString(moiraiUserCondition));
        List<MoiraiProductVo> resourceService = moiraiUserExtraService.getResourceServiceExt(moiraiUserCondition, true);
        return new BWJsonResult<>(resourceService);
    }

    /**
     * 查询用户 分页 添加用户后展示的分页列表
     *
     * @param moiraiUserCondition
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "查询用户列表", description = "根据条件查询用户信息")
    @Override
    public BWJsonResult<MoiraiUser> findUserListByCondition(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        if (0 == moiraiUserCondition.getPageNo()) {
            moiraiUserCondition.setPageNo(1);
        }
        if (0 == moiraiUserCondition.getPageSize()) {
            moiraiUserCondition.setPageSize(100);
        }
        BWJsonResult<MoiraiUser> condition = moiraiUserService.findUserListByCondition(moiraiUserCondition);

        return condition;
    }

    @Override
    public BWJsonResult findUserCountByCondition(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        int count = moiraiUserService.findUserCountByCondition(moiraiUserCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setTotal(count);
        return bwJsonResult;
    }

    /**
     * 查询用户 根据id、邮箱都以此接口查询 查看用户详情
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @PostMapping(value = {"/findUserByCondition", "/findUserByConditionB"}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BWJsonResult<MoiraiUser> findUserByCondition(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (StringUtils.isBlank(moiraiUserCondition.getUserAccount()) && moiraiUserCondition.getUserId() == null &&
            StringUtils.isBlank(moiraiUserCondition.getTelephone()) && StringUtils.isBlank(moiraiUserCondition.getUserEmail())
            && StringUtils.isBlank(moiraiUserCondition.getUserCreatetype()) && moiraiUserCondition.getOrgId() == null) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****findUserByCondition接口请求参数:{}*****", moiraiUserCondition.toString());
        MoiraiUser moiraiUser = moiraiUserService.findUserByCondition(moiraiUserCondition);
        return new BWJsonResult<>(moiraiUser);
    }

    /**
     * 添加B端用户
     *
     * @param user
     * @return
     */
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @UserCenterOperationLog(moduleName = "用户管理", action = "添加用户", description = "添加用户")
    @PostMapping(value = {"/addUser", "/addUserB"}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Override
    public BWJsonResult<MoiraiUser> addUser(@RequestBody MoiraiUser user) {
        //个人注册
        if ("C".equals(user.getUserType())) {
            if (StringUtils.isBlank(user.getTelephone()) || StringUtils.isBlank(user.getNonceStr())
                || user.getQdBm() == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            user = moiraiUserService.addCUser(user);
            return new BWJsonResult<MoiraiUser>(user);
        }
        //企业账号添加
        if (StringUtils.isBlank(user.getUserAccount()) || user.getOrgId() == null) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        //开放平台，查询租户ID
        if ((user.getTenantId() == null || "".equals(user.getTenantId())) && ("2".equals(user.getOriginMark()))) {
            MoiraiOrg moiraiOrg = new MoiraiOrg();
            moiraiOrg.setOrgId(user.getOrgId());
            moiraiOrg.setOrgCode(user.getOrgCode());
            MoiraiOrg org = moiraiOrgService.selectOneOrg(moiraiOrg);

            if (org == null) {
                return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_DB_NULL));
            }
            user.setOrgId(org.getOrgId());
            user.setTenantId(org.getTenantId());
            user.setCreater("开放平台");
        }
        if (user.getTenantId() == null) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****addUser接口请求参数:{}*****", user.toString());
        user = moiraiUserService.addUser(user);
        return new BWJsonResult<MoiraiUser>(user);
    }

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "编辑用户", description = "编辑用户/禁用用户")
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @Override
    public BWJsonResult updateUser(@RequestBody MoiraiUser user) {
        if (null == user || null == user.getUserId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****updateUser接口请求参数:{}*****", user.toString());
        int i = moiraiUserService.updateUser(user);

        return new BWJsonResult();
    }

    /**
     * 删除用户
     *
     * @param moiraiUserCondition
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "删除用户", description = "删除用户")
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @Override
    public BWJsonResult deleteUser(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || null == moiraiUserCondition.getUserId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****deleteUser接口请求参数:{}*****", moiraiUserCondition.toString());
        int i = moiraiUserService.deleteUser(moiraiUserCondition.getUserId(), moiraiUserCondition.getModifyUser());
        return new BWJsonResult();
    }

    /**
     * 用户中心批量操作接口
     * 启用/解锁    /batchOperation
     * 禁用        /batchOperationUnable
     * 删除        /batchOperationDel
     * 重置密码    /batchOperationResetPwd
     * 角色解除人员 /batchOperationRelieve
     *
     * @param moiraiBatchUser
     * @return
     */
    @Override
    @PostMapping(value = {"/batchOperation", "/batchOperationUnable", "/batchOperationDel", "/batchOperationResetPwd",
            "/batchOperationRelieve"}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BWJsonResult batchOperation(@RequestBody List<MoiraiUserCondition> userList) {
        if (userList == null || userList.isEmpty()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        return moiraiUserService.batchOperation(userList);
    }

    /**
     * 重置密码
     *
     * @param moiraiUserCondition
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "重置密码", description = "重置密码")
    public BWJsonResult resetUserPassword(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || null == moiraiUserCondition.getUserId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****resetUserPassword接口请求参数:{}*****", moiraiUserCondition.toString());

        BWJsonResult bwJsonResult = moiraiUserService.resetUserPassword(moiraiUserCondition);

        return bwJsonResult;
    }

    /**
     * 修改密码
     *
     * @param moiraiUserCondition
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "修改密码", description = "修改密码")
    public BWJsonResult updateUserPassword(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if ("C".equals(moiraiUserCondition.getUserType())) {
            if (StringUtils.isBlank(moiraiUserCondition.getTelephone()) ||
                StringUtils.isBlank(moiraiUserCondition.getNewPassword()) ||
                StringUtils.isBlank(moiraiUserCondition.getOldPassword())) {
                return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
            }
            moiraiUserService.updateCPassword(moiraiUserCondition);
            return new BWJsonResult();
        }
        if (null == moiraiUserCondition.getUserAccount() ||
            null == moiraiUserCondition.getNewPassword() || null == moiraiUserCondition.getOldPassword()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****updateUserPassword接口请求参数：userAccount:{}", moiraiUserCondition.getUserAccount());
        moiraiUserService.updateUserPassword(moiraiUserCondition);
        return new BWJsonResult();
    }

    /**
     * 完善用户资料信息
     *
     * @param moiraiUserinfo
     * @return
     */
    @Override
    public BWJsonResult editUserInfo(@RequestBody MoiraiUser moiraiUser) {
        logger.info("*****editUserInfo接口请求参数:{}*****", moiraiUser);
        int i = moiraiUserService.updateUserInfo(moiraiUser);
        return new BWJsonResult();
    }

    /**
     * <B>方法名称：</B>登录校验接口<BR>
     * <B>概要说明：</B>userType:B/C,不传默认校验B<BR>
     *
     * @return
     * @since 2019年12月18日
     */
    @Override
    public BWJsonResult<MoiraiUser> checkLogin(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        this.isMobile(moiraiUserCondition);
        if (Constants.USER_TYPE_C.equals(moiraiUserCondition.getUserType())) {
            if (StringUtils.isBlank(moiraiUserCondition.getTelephone())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            return moiraiUserService.checkCLogin(moiraiUserCondition);
        }
        if (null == moiraiUserCondition.getUserPassword() || null == moiraiUserCondition.getUserAccount()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiUserService.checkLogin(moiraiUserCondition);
    }

    private void isMobile(MoiraiUserCondition moiraiUserCondition) {
        if (StringUtils.isBlank(moiraiUserCondition.getUserType())) {
            boolean mobile = RegularExpUtils.checkMobile(moiraiUserCondition.getTelephone());
            if (mobile) {
                moiraiUserCondition.setUserType(Constants.USER_TYPE_C);
            } else {
                moiraiUserCondition.setUserType(Constants.USER_TYPE_B);
            }
        }
    }

    /**
     * <B>方法名称：</B>登录校验接口<BR>
     * <B>概要说明：</B>此接口仅对auth-sso-service服务提供，禁止外部调用<BR>
     *
     * @return
     * @since 2019年12月20日
     */
    @Override
    public BWJsonResult<MoiraiUser> checkLoginV2(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        this.isMobile(moiraiUserCondition);
        // 新接口使用可配置的密码策略
        moiraiUserCondition.setVersion(Constants.MOIRAI_VERSION_V2);
        if (Constants.USER_TYPE_C.equals(moiraiUserCondition.getUserType())) {
            if (StringUtils.isBlank(moiraiUserCondition.getTelephone())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            BWJsonResult<MoiraiUser> bwJsonResult = moiraiUserService.checkCLogin(moiraiUserCondition);
            if (bwJsonResult.isSuccess()) {
                /** 默认登录机构和双因子 **/
                MoiraiUser moiraiUser = moiraiUserService.getDefaultCompanyUser(bwJsonResult.getData().get(0));
                moiraiUser.setFirstLogin(bwJsonResult.getData().get(0).getFirstLogin());
                moiraiUser.setPwdOverdue(bwJsonResult.getData().get(0).getPwdOverdue());
                return new BWJsonResult<>(moiraiUser);
            }
            return bwJsonResult;
        }
        if (null == moiraiUserCondition.getUserPassword() || null == moiraiUserCondition.getUserAccount()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiUserService.checkLogin(moiraiUserCondition);
    }

    /**
     * 用户登录成功后用token获取用户信息 Author:sxl
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUser> findUserByLogin(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("用户登录时获取用户信息，userAccount:{},userType:{}", moiraiUserCondition.getUserAccount(), moiraiUserCondition.getUserType());
        MoiraiUser moiraiUser = moiraiUserService.findUserByLogin(moiraiUserCondition);
        return new BWJsonResult(moiraiUser);
    }

    /**
     * 提供销项登录暂时使用
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUser> findUserNoRoles(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("获取用户信息，userAccount:{},userType:{}", moiraiUserCondition.getUserAccount(), moiraiUserCondition.getUserType());
        MoiraiUser moiraiUser = moiraiUserService.findUserNoRoles(moiraiUserCondition);
        return new BWJsonResult(moiraiUser);
    }

    /**
     * 更新用户登录历史表
     *
     * @param moiraiUser
     * @return
     */
    @Override
    @Deprecated
    public BWJsonResult updateLastLogin(@RequestBody MoiraiUser moiraiUser, @RequestParam("type") String type) {
        if (null == moiraiUser) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("用户更新登录历史表信息，userId:{},orgId:{},type:{}", moiraiUser.getUserId(), moiraiUser.getOrgId(), type);
        //跳转登录时机构ID可以传
        moiraiUserService.updateLastLogin(moiraiUser, type);
        return new BWJsonResult();
    }

    /**
     * 更新用户登录历史表,切换角色后使用
     *
     * @param moiraiUserLoginHistory
     * @return
     */
    @Override
    public BWJsonResult updateLastLoginHistory(@RequestBody MoiraiUserLoginHistory moiraiUserLoginHistory) {
        if (null == moiraiUserLoginHistory) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        //跳转登录时机构ID可以传
        moiraiUserService.updateLastLoginHistory(moiraiUserLoginHistory);
        return new BWJsonResult();
    }

    /**
     * 更新用户表登录时间
     *
     * @param moiraiUser
     * @return
     */
    @Override
    public BWJsonResult updateUserLastLogin(@RequestBody MoiraiUser moiraiUser) {
        if (moiraiUser == null || moiraiUser.getUserId() == null) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("用户更新登录时间信息，userId:{}", moiraiUser.getUserId());
        moiraiUserService.updateUserLastLogin(moiraiUser);
        return new BWJsonResult();
    }

    /**
     * <B>方法名称：</B>根据账号修改用户状态<BR>
     * <B>概要说明：</B>auth-sso-service使用，不对外提供<BR>
     *
     * @return
     * @since 2019/10/16
     */
    public BWJsonResult updateUserByCondition(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (moiraiUserCondition == null) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("用户更新成功，入参:{}", moiraiUserCondition);
        moiraiUserService.updateUserByCondition(moiraiUserCondition);
        return new BWJsonResult();
    }

    /**
     * 查询用户信息，返回含有UUID盐值的数据，转化为salt 查询用户 根据id、邮箱都以此接口查询 查看用户详情 --开放平台使用
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUserExtra> findUserExtByCondition(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        MoiraiUser moiraiUser = null;
        if (StringUtils.isBlank(moiraiUserCondition.getUserAccount()) && moiraiUserCondition.getUserId() == null &&
            StringUtils.isBlank(moiraiUserCondition.getTelephone()) && StringUtils.isBlank(moiraiUserCondition.getUserEmail())
            && StringUtils.isBlank(moiraiUserCondition.getUserCreatetype()) && moiraiUserCondition.getOrgId() == null) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****findUserExtByCondition接口请求参数:{}*****", moiraiUserCondition.toString());
        moiraiUser = moiraiUserService.findUserByCondition(moiraiUserCondition);
        if (null != moiraiUser) {
            List<MoiraiUserAuthz> roleList = moiraiUserService.findAuthRoleListByUseId(moiraiUser.getUserId());
            moiraiUser.setMoiraiUserAuthzs(roleList);
            MoiraiUserExtra moiraiUserExtra = new MoiraiUserExtra();
            BeanUtils.copyProperties(moiraiUser, moiraiUserExtra);
            moiraiUserExtra.setSalt(moiraiUser.getUuid());
            return new BWJsonResult<>(moiraiUserExtra);
        }
        return new BWJsonResult<>();
    }

    /**
     * 获取用户资料信息
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUserinfo> getUserInfoByUserId(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || null == moiraiUserCondition.getUserId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****getUserInfoByUserId接口请求参数:{}*****", moiraiUserCondition.toString());
        MoiraiUserinfo userInfo = moiraiUserService.getUserInfoByUserId(moiraiUserCondition.getUserId());
        if (null != userInfo) {
            List<MoiraiUserAuthz> roleList = moiraiUserService.findAuthRoleListByUseId(moiraiUserCondition.getUserId());
            StringBuffer roles = new StringBuffer();
            roleList.forEach(role -> roles.append(role.getRoleName() + ","));

            String roleStr = "";
            if (roles.length() > 0) {
                roleStr = roles.toString();
                roleStr = roleStr.substring(0, roleStr.length() - 1);
            }
            userInfo.setRoles(roleStr);
        }
        return new BWJsonResult<MoiraiUserinfo>(userInfo);
    }

    /**
     * 添加授权
     *
     * @param moiraiUserOrgRole
     * @return
     */
    @Override
    public BWJsonResult addUserRole(@RequestBody MoiraiUserAuthz moiraiUserAuthz) {
        if (null == moiraiUserAuthz || null == moiraiUserAuthz.getUserId() || null == moiraiUserAuthz.getTenantId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        List<MoiraiRoleCondition> roleList = moiraiUserAuthz.getRoleMenus();
        logger.info("*****addUserRole接口请求参数:{}*****", moiraiUserAuthz.toString());
        if (roleList != null) {
            for (MoiraiRoleCondition roleItem : roleList) {
                if (roleItem.getRoleId() == null) {
                    return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
                }
            }
        }
        moiraiUserService.addUserRole(moiraiUserAuthz);

        return new BWJsonResult();
    }

    /**
     * 获取用户与角色关联关系
     *
     * @param moiraiUserAuthz
     * @return
     */
    public BWJsonResult<MoiraiRole> getUserRole(@RequestBody MoiraiUserAuthz moiraiUserAuthz) {
        if (null == moiraiUserAuthz || null == moiraiUserAuthz.getTenantId() || null == moiraiUserAuthz.getUserId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****getUserRole接口请求参数:{}*****", moiraiUserAuthz.toString());
        //获取赋权角色list
        List<MoiraiRole> grantRoleList = moiraiUserService.getUserRole(moiraiUserAuthz);
        if (grantRoleList != null) {
            return new BWJsonResult<>(grantRoleList);
        } else {
            return new BWJsonResult<>();
        }
    }

    /**
     * 获取使用某个角色的所有用户
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUser> getUserByRoleId(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || moiraiUserCondition.getRoleId() == null
            || null == moiraiUserCondition.getTenantId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****getUserByRoleId接口请求参数:{}*****", moiraiUserCondition.toString());
        return moiraiUserService.getUserByRoleId(moiraiUserCondition);
    }

    /***
     * 获取组织机构开票员列表
     * @auth Lance cui
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUser> getOrgKpyUsers(@RequestBody MoiraiUserCondition moiraiUserCondition) {

        if (null == moiraiUserCondition || null == moiraiUserCondition.getOrgId()
            || null == moiraiUserCondition.getTenantId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }

        List<MoiraiUser> kpyUserList = moiraiUserService.getOrgKpyUsers(moiraiUserCondition);
        if (kpyUserList == null || kpyUserList.size() == 0) {
            BWJsonResult result = new BWJsonResult<>();
            result.setMessage("该组织机构下没查询到开票员。");
            return result;
        }

        return new BWJsonResult<MoiraiUser>(kpyUserList);
    }

    /**
     * 获取授权用户组织机构 分页获取
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUserAuthzOrg> getUserOrgPage(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || null == moiraiUserCondition.getUserId() || null == moiraiUserCondition.getOrgId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****getUserOrgPage接口请求参数:{}*****", moiraiUserCondition.toString());
        return moiraiUserService.getUserOrgPage(moiraiUserCondition);
    }

    /**
     * <B>方法名称：</B>C端用户绑定B端账号<BR>
     * <B>概要说明：</B><BR>
     * 请使用 /userMember/bindTenant 方法替代
     *
     * @return
     * @since 2019年9月18日
     */
    @Override
    public BWJsonResult bindingBUser(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (StringUtils.isBlank(moiraiUserCondition.getTelephone()) || StringUtils.isBlank(moiraiUserCondition.getUserAccount()) ||
            StringUtils.isBlank(moiraiUserCondition.getUserPassword())) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        moiraiUserService.bindingBUser(moiraiUserCondition);
        return new BWJsonResult();
    }

    /**                  --------------------不再维护接口------------------------                     **/

    /**
     * <B>方法名称：</B>C端用户解绑B端账号<BR>
     * <B>概要说明：</B><BR>
     * 请使用 /userMember/unBindTenant 方法替代
     *
     * @return
     * @since 2019年9月19日
     */
    @Override
    @Deprecated
    public BWJsonResult unbindBUser(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (StringUtils.isBlank(moiraiUserCondition.getTelephone()) || StringUtils.isBlank(moiraiUserCondition.getUserAccount()) ||
            StringUtils.isBlank(moiraiUserCondition.getSmsCode())) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        moiraiUserService.unbindBUser(moiraiUserCondition);
        return new BWJsonResult();
    }

    /**
     * 校验账号是否是初始密码登录，是否是纯数字账号。
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    @Deprecated
    public BWJsonResult checkLoginAccount(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (moiraiUserCondition.getUserId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiUserService.checkLoginAccount(moiraiUserCondition);
        return bwJsonResult;
    }

    /**
     * 删除用户授权
     *
     * @param moiraiUserCondition
     * @return
     */
    @Deprecated
    @Override
    public BWJsonResult<MoiraiUser> delUserRoleByUserIdRoleId(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition || moiraiUserCondition.getRoleId() == null
            || null == moiraiUserCondition.getUserId()) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****delUserRoleByUserIdRoleId接口请求参数:{}*****", moiraiUserCondition.toString());
        moiraiUserService.delUserRoleByUserIdRoleId(moiraiUserCondition);
        return new BWJsonResult<>();
    }

    /**
     * 批量校验用户邮箱
     *
     * @param moiraiEmailCheck
     * @return
     */
    @Deprecated
    @Override
    public BWJsonResult batchEmailCheckRepetition(@RequestBody MoiraiEmailCheck moiraiEmailCheck) {
        if (null == moiraiEmailCheck || null == moiraiEmailCheck || moiraiEmailCheck.getEmails().size() == 0
            || StringUtils.isBlank(moiraiEmailCheck.getUserType())) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****batchEmailCheckRepetition接口请求参数:{}*****", moiraiEmailCheck.toString());
        List<Map<String, Integer>> emailMap = moiraiUserService.batchEmailCheckRepetition(moiraiEmailCheck);
        return new BWJsonResult(emailMap);
    }

    /**
     * 根据授权所属机构和角色查询用户列表
     */
    @PostMapping("/findUserByAuth")
    public BWJsonResult<MoiraiUser> findUserByAuth(@RequestBody MoiraiUserAuthz query) {
        if (query.getUserOrg() == null){
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        List<MoiraiUser> list = moiraiUserService.findUserByAuth(query);
        return  new BWJsonResult<>(list);
    }
}
