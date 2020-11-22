package com.baiwang.moirai.controller.user.api;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.annotations.ResultMapOpt;
import com.baiwang.moirai.annotations.ResultMapping;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.mapper.MoiraiChannelTenantMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiRoleMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleCondition;
import com.baiwang.moirai.model.role.MoiraiUserAuthzExample;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiUserService;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对开放平台接口，禁止其他地方调用
 */
@RestController
@RequestMapping("/userApi")
public class MoiraiUserApiController {

    private static Logger logger = LoggerFactory.getLogger(MoiraiUserApiController.class);

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    private MoiraiChannelTenantMapper channelTenantMapper;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiRoleMapper moiraiRoleMapper;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    /**
     * 添加B端用户 中通定制
     *
     * @param user
     * @return
     */
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @UserCenterOperationLog(moduleName = "用户管理", action = "添加用户", description = "添加用户-中通")
    @PostMapping("/addUserZT")
    public BWJsonResult<MoiraiUser> addUserZT(@RequestBody MoiraiUser user) {
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
        if (StringUtils.isBlank(user.getUserAccount())) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        if (user.getOrgId() != null) {
            //开放平台，查询租户ID
            if (user.getTenantId() == null && "2".equals(user.getOriginMark())) {
                MoiraiOrg moiraiOrg = new MoiraiOrg();
                moiraiOrg.setOrgId(user.getOrgId());
                moiraiOrg.setOrgCode(user.getOrgCode());
                MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);

                if (org == null) {
                    return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_DB_NULL));
                }
                user.setOrgId(org.getOrgId());
                user.setTenantId(org.getTenantId());
                user.setCreater("开放平台");
            }
        } else if (StringUtils.isNotBlank(user.getOrgCode())) {
            //开放平台，查询租户ID
            if (user.getTenantId() == null && "2".equals(user.getOriginMark())) {
                MoiraiOrgCondition moiraiOrg = new MoiraiOrgCondition();
                moiraiOrg.setOrgCode(user.getOrgCode());
                moiraiOrg.setFuzzyQuery("false");
                List<MoiraiOrg> orgList = moiraiOrgMapper.queryOrgByCondition(moiraiOrg);
                if (orgList.isEmpty()) {
                    return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_DB_NULL));
                }
                for (int i = 0; i < orgList.size(); i++) {
                    MoiraiOrg org = orgList.get(i);
                    MoiraiChannelTenant query = new MoiraiChannelTenant();
                    query.setTenantId(org.getTenantId());
                    // 中通固定渠道编码
                    query.setQdBm(1700000019006L);
                    List<MoiraiChannelTenant> channelTenantList = channelTenantMapper.queryList(query);
                    if (channelTenantList.isEmpty()) {
                        continue;
                    }
                    user.setOrgId(org.getOrgId());
                    user.setTenantId(org.getTenantId());
                    user.setCreater("开放平台");
                    break;
                }
            }
        } else {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        if (user.getTenantId() == null) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("*****addUser接口请求参数:{}*****", user.toString());
        user = moiraiUserService.addUser(user);
        return new BWJsonResult<MoiraiUser>(user);
    }

    /**
     * 添加B端用户
     *
     * @param user
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "添加用户", description = "添加用户")
    @RequestMapping(value = "/addUser")
    public BWJsonResult<MoiraiUser> addUser(@RequestBody MoiraiUser user) {
        Long tenantId = user.getTenantId();
        String orgCode = user.getOrgCode();
        //企业账号添加
        if (StringUtils.isBlank(user.getUserAccount()) || tenantId == null || StringUtils.isBlank(orgCode)) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开放平台addUser接口请求参数:{}", user);
        //根据租户ID和orgCode查询orgId
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setTenantId(tenantId);
        moiraiOrg.setOrgCode(orgCode);
        MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        if (org != null) {
            user.setOrgId(org.getOrgId());
            user = moiraiUserService.addUser(user);
            return new BWJsonResult<>(user);
        } else {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ADD_ERROR);
        }
    }

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "编辑用户", description = "编辑用户/禁用用户")
    @RequestMapping(value = "/updateUser")
    public BWJsonResult updateUser(@RequestBody MoiraiUser user) {
        Long tenantId = user.getTenantId();
        String userAccount = user.getUserAccount();
        if (StringUtils.isBlank(userAccount) || tenantId == null) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        logger.info("开放平台updateUser接口请求参数:{}", user);
        MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(userAccount);
        if (moiraiUser == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        user.setUserId(moiraiUser.getUserId());
        int i = moiraiUserService.updateUser(user);
        return new BWJsonResult();
    }

    /**
     * 查询用户 根据id、邮箱都以此接口查询 查看用户详情
     *
     * @param moiraiUserCondition
     * @return
     */
    @RequestMapping(value = "/findUserByCondition")
    public BWJsonResult<MoiraiUser> findUserByCondition(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (!Constants.USER_TYPE_C.equals(moiraiUserCondition.getUserType())) {
            if (StringUtils.isBlank(moiraiUserCondition.getUserAccount())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            if (StringUtils.isBlank(moiraiUserCondition.getTaxCode()) && moiraiUserCondition.getTenantId() == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            } else if (StringUtils.isNotBlank(moiraiUserCondition.getTaxCode())) {
                MoiraiOrg moiraiOrg = new MoiraiOrg();
                moiraiOrg.setTaxCode(moiraiUserCondition.getTaxCode());
                moiraiOrg.setTenantId(moiraiUserCondition.getTenantId());
                MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
                if (org == null) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_GET_OPENPLATE_ERROR);
                } else {
                    String operation = moiraiUserCondition.getOperation();
                    moiraiUserCondition.setTenantId(org.getTenantId());
                    if (Constants.MOIRAI_USER_ORG.equals(operation)) {
                        moiraiUserCondition.setOrgId(org.getOrgId());
                    }
                }
            }
        } else {
            if (StringUtils.isBlank(moiraiUserCondition.getTelephone())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_PHONE_NULL_ERROR);
            }
        }
        logger.info("开放平台findUserByCondition接口请求参数:{}", moiraiUserCondition);
        MoiraiUser moiraiUser = moiraiUserService.findUserByCondition(moiraiUserCondition);
        return new BWJsonResult<>(moiraiUser);
    }

    /**
     * 用户授权
     *
     * @return
     */
    @RequestMapping(value = "/addUserRole")
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    public BWJsonResult addUserRole(@RequestBody MoiraiUserAuthzOrg moiraiUserAuthz) {
        if (null == moiraiUserAuthz) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        String orgCode = moiraiUserAuthz.getOrgCode();
        List<MoiraiRoleCondition> roleList = moiraiUserAuthz.getRoleMenus();
        String userAccount = moiraiUserAuthz.getUserAccount();
        Long tenantId = moiraiUserAuthz.getTenantId();
        String creater = moiraiUserAuthz.getCreater();
        if (tenantId == null || StringUtils.isBlank(orgCode) || StringUtils.isBlank(userAccount)
            || roleList == null || roleList.size() == 0 || StringUtils.isBlank(creater)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开放平台调用addUserRole接口请求参数:{}", moiraiUserAuthz);

        //查询用户信息
        MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(userAccount);
        if (moiraiUser == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        moiraiUserAuthz.setUserId(moiraiUser.getUserId());
        moiraiUserAuthz.setUserOrg(moiraiUser.getOrgId());

        //授权机构信息
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setTenantId(tenantId);
        moiraiOrg.setOrgCode(orgCode);
        MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        if (org == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_ORG_NOT_NULL);
        }
        moiraiUserAuthz.setAuthOrg(org.getOrgId());

        //授权角色
        for (MoiraiRoleCondition roleItem : roleList) {
            if (roleItem.getRoleId() == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            MoiraiRole moiraiRole = moiraiRoleMapper.selectByPrimaryKey(roleItem.getRoleId());
            if (moiraiRole == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_ROLE_NOT_NULL);
            }
            if (Constants.flag_Y.endsWith(moiraiRole.getDefaultFlag())) {
                roleItem.setRoleOrg(moiraiUser.getOrgId());
            } else {
                roleItem.setRoleOrg(moiraiRole.getOrgId());
            }
        }
        moiraiUserService.addUserRole(moiraiUserAuthz);
        return new BWJsonResult();
    }

    /**
     * 编辑用户授权
     *
     * @return
     */
    @RequestMapping(value = "/editUserRole")
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @Transactional
    public BWJsonResult editUserRole(@RequestBody MoiraiUserAuthzOrg moiraiUserAuthz) {
        if (null == moiraiUserAuthz) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        String orgCode = moiraiUserAuthz.getOrgCode();
        List<MoiraiRoleCondition> roleList = moiraiUserAuthz.getRoleMenus();
        String userAccount = moiraiUserAuthz.getUserAccount();
        Long tenantId = moiraiUserAuthz.getTenantId();
        String creater = moiraiUserAuthz.getCreater();
        if (tenantId == null || StringUtils.isBlank(userAccount) || StringUtils.isBlank(creater)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开放平台调用editUserRole接口请求参数:{}", moiraiUserAuthz);

        //查询用户信息
        MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(userAccount);
        if (moiraiUser == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        //删除用户授权信息
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(moiraiUser.getUserId());
        moiraiUserAuthzMapper.deleteByExample(example);
        if (StringUtils.isBlank(orgCode) && (roleList == null || roleList.size() == 0)) {
            return new BWJsonResult();
        }
        moiraiUserAuthz.setUserId(moiraiUser.getUserId());
        moiraiUserAuthz.setUserOrg(moiraiUser.getOrgId());

        //授权机构信息
        if (StringUtils.isBlank(orgCode)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_ORG_NOT_NULL);
        }
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setTenantId(tenantId);
        moiraiOrg.setOrgCode(orgCode);
        MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        if (org == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_ORG_NOT_NULL);
        }
        moiraiUserAuthz.setAuthOrg(org.getOrgId());

        //授权角色
        if (roleList == null || roleList.size() == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        for (MoiraiRoleCondition roleItem : roleList) {
            if (roleItem.getRoleId() == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            MoiraiRole moiraiRole = moiraiRoleMapper.selectByPrimaryKey(roleItem.getRoleId());
            if (moiraiRole == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_ROLE_NOT_NULL);
            }
            if (Constants.flag_Y.endsWith(moiraiRole.getDefaultFlag())) {
                roleItem.setRoleOrg(moiraiUser.getOrgId());
            } else {
                roleItem.setRoleOrg(moiraiRole.getOrgId());
            }
        }
        moiraiUserService.addUserRole(moiraiUserAuthz);
        return new BWJsonResult();
    }
}
