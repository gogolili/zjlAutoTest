package com.baiwang.moirai.serviceimpl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.dao.MoiraiUserDao;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.event.UserEvent;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiRoleMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserDataScopeMapper;
import com.baiwang.moirai.mapper.MoiraiUserLoginHistoryMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.MoiraiUserMemberMapper;
import com.baiwang.moirai.mapper.MoiraiUserinfoMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleCondition;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzExample;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.scale.MoiraiUserDataScopeExample;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.BWToken;
import com.baiwang.moirai.model.user.MoiraiEmailCheck;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.user.MoiraiUserLoginHistory;
import com.baiwang.moirai.model.user.MoiraiUserMember;
import com.baiwang.moirai.model.user.MoiraiUserMemberCondition;
import com.baiwang.moirai.model.user.MoiraiUserinfo;
import com.baiwang.moirai.model.user.UserInfo;
import com.baiwang.moirai.service.MoiraiExtService;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiUserMemberService;
import com.baiwang.moirai.service.MoiraiUserPwdService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.service.PasswordPolicyService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.Base64Utils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.ExcelPoiUtil;
import com.baiwang.moirai.utils.RSAUtils;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class MoiraiUserServiceImpl implements MoiraiUserService {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiUserServiceImpl.class);

    @Autowired
    private MoiraiUserinfoMapper moiraiUserinfoMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private MoiraiUserLoginHistoryMapper moiraiUserLoginHistoryMapper;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private MoiraiUserDataScopeMapper moiraiUserDataScopeMapper;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiUserPwdService moiraiUserPwdService;

    @Autowired
    private MoiraiRoleMapper moiraiRoleMapper;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiUserDao moiraiUserDao;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    @Value("${tenant.role}")
    private String tenantRole;

    @Value("${org.role}")
    private String orgRole;

    @Value("${data.openInvoicePowerList}")
    private String openInvoicePowerList;

    @Resource
    private ApplicationEventPublisher context;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private MoiraiUserMemberMapper moiraiUserMemberMapper;

    @Autowired
    private MoiraiUserMemberService moiraiUserMemberService;

    @Autowired
    private MoiraiExtService moiraiExtService;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired(required = false)
    private PasswordService passwordService;

    /**
     * 查询用户数据
     *
     * @param moiraiUserCondition
     * @return
     * @throws Exception
     */
    public BWJsonResult<MoiraiUser> findUserListByCondition(MoiraiUserCondition moiraiUserCondition) {
        if (Constants.USER_TYPE_B.equals(moiraiUserCondition.getUserType())) {
            if (moiraiUserCondition.getAuthOrgIds() == null || moiraiUserCondition.getAuthOrgIds().size() == 0) {
                if (moiraiUserCondition.getTenantId() == null && moiraiUserCondition.getUserId() == null && moiraiUserCondition.getOrgId() == null) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
                }
            }
        }
        //扩展字段
        List<MoiraiUser> userList = moiraiUserDao.findUserListByCondition(moiraiUserCondition);
        if (null == userList || userList.isEmpty()) {
            return new BWJsonResult<>();
        } else {
            if (Constants.USER_TYPE_B.equals(moiraiUserCondition.getUserType())) {
                List<Long> orgIdList = new ArrayList<>();
                userList.forEach(user -> orgIdList.add(user.getOrgId()));
                Map<String, List> moiraiOrgList = new HashMap<>();
                moiraiOrgList.put("orgId", orgIdList);
                List<MoiraiOrg> orgList = moiraiOrgMapper.queryOrgListByCondition(moiraiOrgList);
                if (!orgList.isEmpty()) {
                    for (MoiraiUser user : userList) {
                        for (MoiraiOrg org : orgList) {
                            if (user.getOrgId().equals(org.getOrgId())) {
                                user.setOrgName(org.getOrgName());
                            }
                        }
                    }
                }
            }
        }
        PageInfo pageInfo = new PageInfo(userList);
        return new BWJsonResult<>(userList, (int) pageInfo.getTotal());
    }

    /**
     * 导出用户数据
     */
    @Override
    public Workbook exportUserList(MoiraiUserCondition condition) {
        long start = System.currentTimeMillis();
        if (Constants.USER_TYPE_B.equals(condition.getUserType())
            && (condition.getAuthOrgIds() == null || condition.getAuthOrgIds().size() == 0)) {
            logger.info("默认查询:{}所有授权机构下的用户", condition.getUserId());
            if (condition.getUserId() != null) {
                List<MoiraiUserAuthz> userAuthz = moiraiOrgService.getUserAuthBycondition(condition.getUserId(), null);
                List<Long> authOrgIds = userAuthz.stream().map(item -> item.getAuthOrg()).collect(Collectors.toList());
                condition.setAuthOrgIds(authOrgIds);
            }
        }

        MoiraiUser exUser = moiraiUserMapper.selectByPrimaryKey(condition.getUserId());
        // 查询扩展字段信息
        JSONObject jsonObject = moiraiExtService.getTemplateDate(condition.getTenantId() + "", exUser.getOrgId() + "", exUser.getUserId() + "", Constants.MOIRAI_USER_PAGE_UNIQUE_NAME);
        // 用户数据
        condition.setOrgId(null);
        List<MoiraiUser> userList = moiraiUserDao.findUserListByCondition(condition);
        // 导出模板
        List<ExcelExportEntity> templateEntity = new ArrayList<>();
        // 导出数据
        List<Map<String, Object>> templateMapList = new ArrayList<>();
        // 封装模板数据
        JSONArray jsonArray = jsonObject.getJSONObject(Constants.MOIRAI_USER_EXPORT_TAG).getJSONArray("data");
        ExcelPoiUtil.buildExcelExportEntity(jsonArray, templateEntity);

        if (userList.isEmpty()) {
            return ExcelExportUtil.exportExcel(new ExportParams(null, "用户信息"), templateEntity, templateMapList);
        }

        // 查询所有组织机构信息
        MoiraiOrgCondition moiraiOrgCondition = new MoiraiOrgCondition();
        moiraiOrgCondition.setTenantId(condition.getTenantId());
        List<MoiraiOrg> allOrg = moiraiOrgMapper.queryOrgByCondition(moiraiOrgCondition);
        Map<Long, String> orgMap = new HashMap<>();
        allOrg.forEach(item -> orgMap.put(item.getOrgId(), item.getOrgName()));

        // 查询所有角色信息
        MoiraiOrgProduct product = new MoiraiOrgProduct();
        product.setTenantId(condition.getTenantId());
        List<MoiraiOrgProduct> products = moiraiOrgProductMapper.findOrgProductByCondition(product);
        List<Long> productList = products.stream().map(item -> item.getProductId()).collect(Collectors.toList());
        MoiraiRole queryRole = new MoiraiRole();
        queryRole.setTenantId(condition.getTenantId());
        queryRole.setProducts(productList);
        List<MoiraiRole> allRole = moiraiRoleMapper.selectAllAuthRoles(queryRole);
//        List<MoiraiRole> allRole = moiraiRoleMapper.selectOrgShowRoles(null, condition.getTenantId(), null,productList);
        Map<Long, String> roleMap = new HashMap<>();
        allRole.forEach(item -> roleMap.put(item.getRoleId(), item.getRoleName()));

        try {
            for (int i = 0; i < userList.size(); i++) {
                MoiraiUserCondition user = new MoiraiUserCondition();
                BeanUtils.copyProperties(userList.get(i), user);
                MoiraiUserAuthzExample userAuthzExample = new MoiraiUserAuthzExample();
                MoiraiUserAuthzExample.Criteria criteria = userAuthzExample.createCriteria();
                criteria.andUserIdEqualTo(user.getUserId());
                List<MoiraiUserAuthz> userAuth = moiraiUserAuthzMapper.selectByExample(userAuthzExample);
                Set<String> roleName = new HashSet<>();
                Set<String> authOrgName = new HashSet<>();
                for (MoiraiUserAuthz auth : userAuth) {
                    roleName.add(roleMap.get(auth.getRoleId()));
                    authOrgName.add(orgMap.get(auth.getAuthOrg()));
                }
                user.setOrgName(orgMap.get(user.getOrgId()));
                user.setRoleName(StringUtils.join(roleName, ","));
                user.setAuthOrgName(StringUtils.join(authOrgName, ","));
                moiraiExtService.buildExcelData(jsonArray, templateMapList, user, userList.get(i).getExt());
            }
            logger.info("所有导出数据：【{}】", JSONObject.toJSONString(templateMapList));
        } catch (IllegalAccessException e) {
            logger.error("数据封装失败！", e);
            throw new MoiraiException("", "数据封装失败！");
        } catch (NoSuchMethodException e) {
            logger.error("数据封装失败！", e);
            throw new MoiraiException("", "数据封装失败！");
        } catch (InvocationTargetException e) {
            logger.error("数据封装失败！", e);
            throw new MoiraiException("", "数据封装失败！");
        }
        logger.info("耗费时间 {}", System.currentTimeMillis() - start);
        return ExcelExportUtil.exportExcel(new ExportParams(null, "用户信息"), templateEntity, templateMapList);
    }

    @Override
    public int findUserCountByCondition(MoiraiUserCondition moiraiUserCondition) {
        return moiraiUserMapper.findUserCountByCondition(moiraiUserCondition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoiraiUser addCUser(MoiraiUser user) {
        logger.info("个人用户注册入参:{}", user);
        moiraiUserPwdService.checkNonceStr(user.getNonceStr(), user.getTelephone(), null);
        //校验手机号是否已经注册
        String telephone = user.getTelephone();
        boolean flag = RegularExpUtils.checkMobile(telephone);
        if (!flag) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TELEPHONE_ERROR);
        }
        MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(telephone);
        if (userByTelephone != null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TELEPHONE_ALREADY_REG);
        }
        user.setBindCompany("N");
        moiraiUserPwdService.completeCUser(user);
        if (Constants.MOIRAI_VERSION_V2.equals(user.getVersion())) {
            // 为2时删除手机号
            redisTemplate.delete(Constants.MOIRAI_PWD_SMSCODE + telephone);
        }
        return user;
    }

    /**
     * 企业添加用户
     *
     * @param moiraiUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoiraiUser addUser(MoiraiUser moiraiUser) {
        this.checkParam(moiraiUser);
        moiraiUser = this.completeUser(moiraiUser);
        this.addUserAuth(moiraiUser);
        context.publishEvent(new UserEvent(moiraiUser, true));
        return moiraiUser;
    }

    private void addUserAuth(MoiraiUser moiraiUser) {
        if ("default".equals(moiraiUser.getType()) || "orgAdmin".equals(moiraiUser.getType())) {
            //创建默认角色
            List<MoiraiUserAuthz> roleList = createRole(moiraiUser);
            if (null != roleList && roleList.size() > 0) {
                moiraiUserAuthzMapper.batchInsert(roleList);
            }
        }
        //MoiraiUserAuthz对应一个分配组织机构
        List<MoiraiUserAuthz> records = new ArrayList<>();
        List<MoiraiUserAuthz> userRoles = moiraiUser.getMoiraiUserAuthzs();
        if (!StrUtils.isEmptyList(userRoles)) {
            MoiraiUserAuthz authz;
            Long authzId;
            Long roleId;
            for (MoiraiUserAuthz role : userRoles) {
                authz = new MoiraiUserAuthz();
                authzId = seqnumFeignClient.getNum(Constants.MOIRAI_USER_AUTHZ);
                roleId = role.getRoleId();
                authz.setUorId(authzId);
                authz.setTenantId(moiraiUser.getTenantId());
                authz.setCreater(moiraiUser.getCreater());
                Long nowMinuteTime = DateTimeUtils.nowTimeLong();
                authz.setCreateTime(nowMinuteTime);
                authz.setRoleId(roleId);
                //分配的组织机构
                authz.setAuthOrg(role.getAuthOrg() == null ? moiraiUser.getOrgId() : role.getAuthOrg());
                //角色所属的组织机构
                authz.setRoleOrg(role.getRoleOrg() == null ? moiraiUser.getOrgId() : role.getRoleOrg());
                authz.setUserId(moiraiUser.getUserId());
                //用户所属的组织机构
                authz.setUserOrg(moiraiUser.getOrgId());
                records.add(authz);
            }
            int insertRnt = moiraiUserAuthzMapper.batchInsert(records);
            logger.info("用户分配机构角色实例化结果OrgAndRole=" + insertRnt);
            if (insertRnt < 0) {
                throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
            }
        }
    }

    private MoiraiUser completeUser(MoiraiUser moiraiUser) {
        String version = moiraiUser.getVersion();
        if (Constants.MOIRAI_VERSION_V2.equals(version)) {
            if (StringUtils.isBlank(moiraiUser.getUserEmail())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
        }
        if (moiraiUser.getUserId() == null) {
            moiraiUser.setUserId(seqnumFeignClient.getNum(Constants.MOIRAI_USER));
        }
        String uuid = AdminUtils.getUuid();
        //创建用户允许传入初始密码
        String userPassword = moiraiUser.getUserPassword();
        if (StrUtils.isEmpty(userPassword)) {
            userPassword = passwordService.calculatePassword(version);
        } else {
            if (Constants.DEFAULT_ONE.equals(moiraiUser.getPasswordTrans())) {
                userPassword = Base64Utils.decodeString(userPassword);
            } else if (Constants.DEFAULT_TWO.equals(moiraiUser.getPasswordTrans())) {
                try {
                    userPassword = RSAUtils.decryptByPrivateKey(userPassword, Constants.PRIVATE_KEY);
                } catch (Exception e) {
                    String requestURI = WebContext.getRequest().getRequestURI();
                    MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_USER_PASSWORD_DECRYPT_FAIL;
                    logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_DECRYPT_FAIL);
                }
            }
        }
        //如果添加用户是简称不为空，则用户账号为简称_账号
        if (!StrUtils.isEmpty(moiraiUser.getShortName())) {
            moiraiUser.setUserAccount(moiraiUser.getShortName() + "_" + moiraiUser.getUserAccount());
        }
        String password = AdminUtils.getUuidPasswd(userPassword, uuid);//指定默认密码为123456
        moiraiUser.setUserPassword(password);
        moiraiUser.setUuid(uuid);

        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiUser.setCreateTime(nowTime);
        moiraiUser.setModifyTime(nowTime);
        moiraiUser.setModifyUser(moiraiUser.getCreater());
        Long userInfoId = seqnumFeignClient.getNum(Constants.MOIRAI_USERINFO);
        moiraiUser.setUserinfoId(userInfoId);
        moiraiUser.setUserType(Constants.USER_TYPE_B);
        if (StringUtils.isEmpty(moiraiUser.getPhoneValidate())) {
            moiraiUser.setPhoneValidate(Constants.flag_N);
        }
        moiraiUser.setEmailValidate(Constants.flag_N);
        moiraiUser.setDelFlag(Constants.flag_N);//删除标志默认置N
        moiraiUser.setUseFlag(Constants.flag_Y);//启用标志默认置Y
        moiraiUser.setFirstLogin(Constants.flag_Y);
        if (StringUtils.isEmpty(moiraiUser.getUserCreatetype())) {
            moiraiUser.setUserCreatetype("4");
        }
        if (StringUtils.isBlank(moiraiUser.getIsTenantAccount())) { //租户账号添加  默认类型添加，用户账号系统指定
            moiraiUser.setIsTenantAccount(Constants.flag_N);
        }

        int i = moiraiUserDao.insertSelective(moiraiUser);
        if (null != moiraiUser.getUserinfo()) {
            moiraiUser.getUserinfo().setUserinfoId(userInfoId);
            moiraiUserinfoMapper.insert(moiraiUser.getUserinfo());
        }
        if (i > 0 && Constants.MOIRAI_VERSION_V2.equals(version) && "4".equals(moiraiUser.getUserCreatetype())) {
            List<String> emailList = new ArrayList<>();
            emailList.add(moiraiUser.getUserEmail());
            List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
            LazyDynaBean lazyDynaBean = new LazyDynaBean();
            lazyDynaBean.set("emails", emailList);
            lazyDynaBean.set("context", "您的初始化帐号：" + moiraiUser.getUserAccount() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;您的初始化密码：" + userPassword);
            lazyDynaBean.set("userName", "");
            lazyDynaBeans.add(lazyDynaBean);
            passwordService.sendMail(version, "百望云", "ZC_VERIFY", "百望云登录账号注册成功", lazyDynaBeans);
        }
        moiraiUser.setUserPassword(userPassword);
        return moiraiUser;
    }

    private List<MoiraiUserAuthz> createRole(MoiraiUser moiraiUser) {
        String[] roles = null;
        if ("default".equals(moiraiUser.getType()) && "1".equals(moiraiUser.getHasDefAdminRole())) {
            //租户账号添加  默认类型添加，用户账号系统指定
            roles = tenantRole.split(",");
        } else if ("orgAdmin".equals(moiraiUser.getType()) && "1".equals(moiraiUser.getHasDefAdminRole())) {
            //组织机构用户 默认类型添加，用户账号系统指定
            roles = orgRole.split(",");
        } else {
            return null;
        }
        List<MoiraiUserAuthz> roleList = new ArrayList<MoiraiUserAuthz>();
        MoiraiUserAuthz role;
        for (String roleId : roles) {
            role = new MoiraiUserAuthz();
            role.setUorId(seqnumFeignClient.getNum(Constants.MOIRAI_USER_AUTHZ));
            role.setUserOrg(moiraiUser.getOrgId());
            role.setTenantId(moiraiUser.getTenantId());
            role.setUserId(moiraiUser.getUserId());
            role.setAuthOrg(moiraiUser.getOrgId());
            role.setCreater(moiraiUser.getCreater());
            role.setCreateTime(moiraiUser.getCreateTime());
            role.setRoleId(Long.parseLong(roleId));
            roleList.add(role);
        }
        return roleList;
    }

    /**
     * 校验用户参数
     *
     * @param moiraiUser
     */
    private void checkParam(MoiraiUser moiraiUser) {
        String userAccount = moiraiUser.getUserAccount();
        String userEmail = moiraiUser.getUserEmail();
        String telephone = moiraiUser.getTelephone();
        Long tenantId = moiraiUser.getTenantId();
        //==========================用户判重start==============================
        if (!StringUtils.isBlank(userAccount)) {
            if (userAccount.length() >= 32 || userAccount.length() < 1) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_USER_ACCOUNT_NOT_RIGHT);
            }
            boolean mobile = RegularExpUtils.checkMobile(userAccount);
            if (mobile) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ACCOUNT_NOT_PHONE);
            }
            boolean valid = RegularExpUtils.valid(userAccount);
            if (!valid) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_USER_ACCOUNT_NOT_RIGHT);
            }
            MoiraiUser yhList = moiraiUserMapper.getUserByUserAccount(userAccount);
            if (null != yhList) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_USER_DUPLICATION_ERROR);
            }
        }
        //==========================用户判重end==============================

        //==========================邮箱判重start==============================
        if (!StringUtils.isBlank(userEmail)) {
            boolean checkEmail = RegularExpUtils.checkEmail(userEmail);
            if (!checkEmail) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_EMAIL_FORMAT_ERROR);
            }
        }
        //==========================邮箱判重end==============================

        //==========================电话start==============================
        if (!StringUtils.isBlank(telephone)) {
            boolean checkMobile = RegularExpUtils.checkMobile(telephone);
            boolean fixedPhone = RegularExpUtils.isFixedPhone(telephone);
            if (!checkMobile && !fixedPhone) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_TELEPHONE_ERROR);
            }
            MoiraiUser userListByTelephone = moiraiUserMapper.getUserByTelephone(telephone, tenantId);
            if (null != userListByTelephone) {
                throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PHONE_DUPLICATION_ERROR);
            }
        }
        //==========================电话end==============================

        String userName = moiraiUser.getUserName();
        if (!StringUtils.isBlank(userName)) {
            if (userName.length() > 20) {
                throw new MoiraiException("3035", "用户名不能大于20位");
            }
        }

        //查询百望云老版数据库
        Map<String, String> map = new HashMap<>();
        map.put("username", userAccount);
        JSONObject jsonObject = moiraiSysService.commonMethod(map, 4);
        if (null != jsonObject && !"0".equals(jsonObject.get("code"))) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_OLD_USER_CENTER_MORE;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
            throw new MoiraiException(jsonObject.getString("code"), jsonObject.getString("message"));
        }
    }

    /**
     * 修改用户
     *
     * @param moiraiUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(MoiraiUser moiraiUser) {
        MoiraiUser user = moiraiUserMapper.selectByPrimaryKey(moiraiUser.getUserId());
        if (null == user) {
            return 0;
        }
        //邮箱
        String userEmail = moiraiUser.getUserEmail();
        if (!StringUtils.isBlank(userEmail)) {
            boolean checkEmail = RegularExpUtils.checkEmail(userEmail);
            if (!checkEmail) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_EMAIL_FORMAT_ERROR);
            }
        }

        //电话
        String telephone = moiraiUser.getTelephone();
        if (!StringUtils.isBlank(telephone)) {
            boolean checkMobile = RegularExpUtils.checkMobile(telephone);
            boolean fixedPhone = RegularExpUtils.isFixedPhone(telephone);
            if (!checkMobile && !fixedPhone) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TELEPHONE_ERROR);
            }
            MoiraiUser phoneUser = moiraiUserMapper.getUserByTelephone(telephone, user.getTenantId());
            if (null != phoneUser && !moiraiUser.getUserId().equals(phoneUser.getUserId())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PHONE_DUPLICATION_ERROR);
            }
            if (null == phoneUser || !"Y".equals(phoneUser.getPhoneValidate())) {
                moiraiUser.setPhoneValidate("N");
            }
        }

        //账号
        String userAccount = moiraiUser.getUserAccount();
        if (!StringUtils.isBlank(userAccount)) {
            if (userAccount.length() >= 32 || userAccount.length() < 1) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ACCOUNT_NOT_RIGHT);
            }
            boolean mobile = RegularExpUtils.checkMobile(userAccount);
            if (mobile) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ACCOUNT_NOT_PHONE);
            }
            boolean valid = RegularExpUtils.valid(userAccount);
            if (!valid) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ACCOUNT_NOT_RIGHT);
            }
            MoiraiUser accUser = moiraiUserMapper.getUserByUserAccount(userAccount);
            if (null != accUser && !moiraiUser.getUserId().equals(accUser.getUserId())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_DUPLICATION_ERROR);
            } else if (null == accUser) {
                //查询百望云老版数据库
                Map<String, String> map = new HashMap<>();
                map.put("username", userAccount);
                JSONObject jsonObject = moiraiSysService.commonMethod(map, 4);
                if (null != jsonObject && !"0".equals(jsonObject.get("code"))) {
                    String requestURI = WebContext.getRequest().getRequestURI();
                    MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_OLD_USER_CENTER_MORE;
                    logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
                    throw new MoiraiException(jsonObject.getString("code"), jsonObject.getString("message"));
                }
            }
        }

        String userName = moiraiUser.getUserName();
        if (!StringUtils.isBlank(userName)) {
            if (userName.length() > 20) {
                throw new MoiraiException("3035", "用户名不能大于20位");
            }
        }

        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiUser.setModifyTime(nowTime);
        int i = moiraiUserDao.updateByPrimaryKeySelective(moiraiUser);
        if (null != moiraiUser.getUserinfo()) {
            moiraiUser.getUserinfo().setUserinfoId(user.getUserinfoId());
            moiraiUser.getUserinfo().setModifyTime(nowTime);
            moiraiUser.getUserinfo().setModifyUser(moiraiUser.getModifyUser());
            MoiraiUserinfo userinfo = moiraiUser.getUserinfo();
            if (StringUtils.isNotEmpty(userinfo.getZipCode()) && userinfo.getZipCode().length() > 10) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR.getCode(), "邮政编码不能超过10位");
            }
            Long userinfoId = userinfo.getUserinfoId();
            MoiraiUserinfo userInfoById = moiraiUserinfoMapper.getUserInfoById(userinfoId);
            if (userInfoById != null) {
                i = moiraiUserinfoMapper.updateByPrimaryKeySelective(userinfo);
            } else {
                i = moiraiUserinfoMapper.updateByUserId(userinfo);
            }

        }
        // 电话号码发生变更时，解除该B端用户和C端用户的绑定关系
        if ((StringUtils.isNotBlank(moiraiUser.getTelephone()) && !moiraiUser.getTelephone().equals(user.getTelephone()))
            || (StringUtils.isNotBlank(user.getTelephone()) && !user.getTelephone().equals(moiraiUser.getTelephone()))) {
            if (Constants.USER_TYPE_B.equals(user.getUserType()) && moiraiUser.getTelephone() != null) {
                logger.info("修改用户信息，电话有变更，自动解绑，用户id {}", user.getUserId());
                moiraiUserMemberService.unBindTenant(user.getUserId());
            }
        }
        context.publishEvent(new UserEvent(moiraiUser, true));
        return i;
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUser(Long userId, String modifyUser) {
        int i = 0;
        //管理员不允许删除
        MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(userId);
        if (null == moiraiUser) {
            return i;
        }
        if (moiraiUser != null) {
            if (Constants.flag_Y.equals(moiraiUser.getIsTenantAccount())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ADMIN_ERROR);
            }
        }
        moiraiUserinfoMapper.deleteByUserId(userId);
        i = moiraiUserDao.deleteByPrimaryKey(userId);
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiUser.setModifyUser(modifyUser);
        moiraiUser.setModifyTime(nowTime);
        MoiraiUserinfo userinfo = moiraiUser.getUserinfo();
        //插入历史表
        if (null != userinfo) {
            userinfo.setModifyTime(nowTime);
            userinfo.setModifyUser(moiraiUser.getModifyUser());
            moiraiUserinfoMapper.insertUserInfoHistory(userinfo);
        }
        moiraiUserMapper.insertUserHistory(moiraiUser);
        //删除用户授权信息
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(userId);
        moiraiUserAuthzMapper.deleteByExample(example);
        //删除数据范围
        MoiraiUserDataScopeExample dataExample = new MoiraiUserDataScopeExample();
        dataExample.createCriteria().andUserIdEqualTo(userId);
        moiraiUserDataScopeMapper.deleteByExample(dataExample);

        // 解除该B端用户的绑定关系
        logger.info("删除用户解绑用户 用户id {}", moiraiUser.getUserId());
        moiraiUserMemberService.unBindTenant(moiraiUser.getUserId());

        context.publishEvent(new UserEvent(moiraiUser));
        return i;
    }

    @Override
    @Transactional
    public BWJsonResult batchOperation(@RequestBody List<MoiraiUserCondition> userList) {
        if (userList.isEmpty()) {
            return new BWJsonResult();
        }
        BWJsonResult bwJsonResult = new BWJsonResult();
        StringBuilder stringBuilder = new StringBuilder();
        for (MoiraiUserCondition user : userList) {
            try {
                String operation = user.getOperation();
                if (Constants.MOIRAI_USER_OPEN.equals(operation)) {
                    user.setUseFlag("Y");
                    Long nowTime = DateTimeUtils.nowTimeLong();
                    user.setModifyTime(nowTime);
                    if (StringUtils.isNotEmpty(user.getModifyOptUser())) {
                        user.setModifyOptTime(new Date());
                    }
                    moiraiUserMapper.updateByPrimaryKeySelective(user);
                    moiraiUserPwdService.unlockAccount(user);
                } else if (Constants.MOIRAI_USER_CLOSE.equals(operation)) {
                    user.setUseFlag("N");
                    Long nowTime = DateTimeUtils.nowTimeLong();
                    user.setModifyTime(nowTime);
                    if (StringUtils.isNotEmpty(user.getModifyOptUser())) {
                        user.setModifyOptTime(new Date());
                    }
                    moiraiUserMapper.updateByPrimaryKeySelective(user);
                } else if (Constants.MOIRAI_USER_DELETES.equals(operation)) {
                    deleteUser(user.getUserId(), user.getModifyUser());
                } else if (Constants.MOIRAI_USER_RESETPASSWORD.equals(operation)) {
                    resetUserPassword(user);
                } else if (Constants.MOIRAI_USER_UNBINDUSER.equals(operation)) {
                    moiraiUserMapper.delUserRoleByUserIdRoleId(user);
                } else {
                }
            } catch (MoiraiException e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.Moirai_DB_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
                stringBuilder.append("(" + user.getUserAccount() + ")操作失败，" + e.getMessage() + ";");
                bwJsonResult.setMessage(stringBuilder.toString());
                continue;
            } catch (Exception e) {
                throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
            }
        }
        if (StringUtils.isBlank(stringBuilder.toString())) {
            bwJsonResult.setMessage("操作成功");
        }
        return bwJsonResult;
    }

    /**
     * 查询用户数据
     *
     * @param moiraiUserCondition
     * @return
     * @throws Exception
     */
    public MoiraiUser findUserByCondition(MoiraiUserCondition moiraiUserCondition) throws MoiraiException {
        List<MoiraiUser> list = moiraiUserDao.findUserByCondition(moiraiUserCondition);
        if (null == list || list.size() == 0) {
            return null;
        }
        if (null != list && list.size() > 1) {
            String requestURI = WebContext.getRequest().getRequestURI();
            logger.error(
                new ErrorMessage(
                    requestURI, MoiraiErrorEnum.MOIRAI_SYSTEM_ERROR.getCode(), MoiraiErrorEnum.MOIRAI_SYSTEM_ERROR.getMsg(), ErrorType.CustomerError).toString());
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_SYSTEM_ERROR);
        }
        MoiraiUser user = list.get(0);
        if (user == null) {
            return null;
        }
        if (Constants.USER_TYPE_C.equals(user.getUserType())) {
            return user;
        }
        if (!moiraiSysService.getUserOfLanders(user)) {
            return null;
        }

        MoiraiUserinfo userinfo = moiraiUserinfoMapper.getUserInfoByUserId(user.getUserId());
        user.setUserinfo(userinfo);
        MoiraiOrg moiraiOrg = moiraiOrgService.selectByOrgId(user.getOrgId());
        if (moiraiOrg != null) {
            user.setOrgName(moiraiOrg.getOrgName());
        }
        MoiraiUserDataScopeExample example = new MoiraiUserDataScopeExample();
        example.createCriteria().andUserIdEqualTo(user.getUserId());
        List<MoiraiUserDataScope> scopes = moiraiUserDataScopeMapper.selectByExample(example);
        user.setMuds(scopes);
        return user;
    }

    public List<MoiraiUserAuthz> findAuthRoleListByUseId(Long userId) throws MoiraiException {
        return moiraiUserAuthzMapper.findAuthRoleListByUseId(userId);
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    public MoiraiUserinfo getUserInfoByUserId(Long userId) {
        return moiraiUserinfoMapper.getUserInfoByUserId(userId);
    }

    /**
     * 增加角色
     *
     * @param moiraiUserAuthz
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addUserRole(MoiraiUserAuthz moiraiUserAuthz) throws MoiraiException {
        //MoiraiUserAuthz对应一个分配组织机构
        List<MoiraiUserAuthz> records = new ArrayList<>();
        List<MoiraiRoleCondition> userRoles = moiraiUserAuthz.getRoleMenus();
        if (!StrUtils.isEmptyList(userRoles)) {
            MoiraiUserAuthz authz;
            for (MoiraiRoleCondition role : userRoles) {
                authz = new MoiraiUserAuthz();
                authz.setUorId(seqnumFeignClient.getNum(Constants.MOIRAI_USER_AUTHZ));
                authz.setTenantId(moiraiUserAuthz.getTenantId());
                authz.setCreater(moiraiUserAuthz.getCreater());
                authz.setCreateTime(DateTimeUtils.nowTimeLong());
                authz.setRoleId(role.getRoleId());
                //分配的组织机构
                authz.setAuthOrg(moiraiUserAuthz.getAuthOrg() == null ? moiraiUserAuthz.getUserOrg() : moiraiUserAuthz.getAuthOrg());
                //角色所属的组织机构
                authz.setRoleOrg(role.getRoleOrg());
                authz.setUserId(moiraiUserAuthz.getUserId());
                //用户所属的组织机构
                authz.setUserOrg(moiraiUserAuthz.getUserOrg());
                records.add(authz);
            }
        }
        int insertRnt = moiraiUserAuthzMapper.batchInsert(records);
        logger.info("用户分配机构角色实例化结果OrgAndRole=" + insertRnt);
        if (insertRnt < 0) {
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return insertRnt;
    }

    /**
     * 获取用户角色
     *
     * @param moiraiUserAuthz
     * @return
     * @throws Exception
     */
    public List<MoiraiRole> getUserRole(MoiraiUserAuthz moiraiUserAuthz) {
        //获取授权
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(moiraiUserAuthz.getUserId());
        List<MoiraiUserAuthz> roleList = moiraiUserAuthzMapper.selectByExample(example);
        List<Long> roleIds = new ArrayList<>();
        roleList.forEach(role -> {
            if (!roleIds.contains(role.getRoleId())) {
                roleIds.add(role.getRoleId());
            }
        });
        if (roleIds.size() > 0) {
            return moiraiRoleMapper.selectRoleBatch(roleIds);
        }
        return null;
    }

    /***
     * 获取角色分配的用户
     * @return
     * @throws MoiraiException
     */
    public BWJsonResult<MoiraiUser> getUserByRoleId(MoiraiUserCondition moiraiUserCondition) {
        MoiraiUser moiraiUser = moiraiSysService.gainCacheUser();
        if (moiraiUser != null) {
            moiraiUserCondition.setTenantId(moiraiUser.getTenantId());
        }
        PageHelper.startPage(moiraiUserCondition.getPageNo(), moiraiUserCondition.getPageSize());
        List<MoiraiUser> user = moiraiUserMapper.getUserByRoleId(moiraiUserCondition);
        PageInfo<MoiraiUser> pageInfo = new PageInfo(user);
        return new BWJsonResult<>(user, (int) pageInfo.getTotal());
    }

    /**
     * 获取授权组织机构分页+ 参数查询
     *
     * @param condition
     * @return
     * @throws Exception
     */
    public BWJsonResult<MoiraiUserAuthzOrg> getUserOrgPage(MoiraiUserCondition condition) throws MoiraiException {
        if (null != condition.getPageSize() && null != condition.getPageNo()) {
            //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
            PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        }
        List<MoiraiUserAuthzOrg> moiraiUserAuthzOrgs = moiraiUserAuthzMapper.getUserAuthzOrg(condition);
        long total = moiraiUserAuthzOrgs.size();
        if (null != condition.getPageSize() && null != condition.getPageNo()) {
            PageInfo<MoiraiUserAuthzOrg> pageInfo = new PageInfo(moiraiUserAuthzOrgs);
            total = pageInfo.getTotal();
        }
        //判断是否已经包含本级组织机构
        boolean hasCurent = false;
        Long userOrgId = condition.getOrgId();
        if (moiraiUserAuthzOrgs != null && moiraiUserAuthzOrgs.size() > 0) {
            //兼容字段
            moiraiUserAuthzOrgs.forEach(orgs -> orgs.setOrgId(orgs.getAuthOrg()));
            for (MoiraiUserAuthzOrg userOrgExtItem : moiraiUserAuthzOrgs) {
                Long itemOrgId = userOrgExtItem.getAuthOrg();
                if (itemOrgId.equals(userOrgId)) {
                    hasCurent = true;
                    break;
                }
            }
            //添加用户所在本级组织机构
            if (!hasCurent) {
                MoiraiOrg userCurrentOrg = moiraiOrgService.selectByOrgId(condition.getOrgId());
                if (userCurrentOrg != null) {
                    MoiraiUserAuthzOrg orgExt = new MoiraiUserAuthzOrg();
                    //兼容字段
                    orgExt.setOrgId(userCurrentOrg.getOrgId());
                    orgExt.setUserId(condition.getUserId());
                    orgExt.setUserOrg(condition.getOrgId());
                    orgExt.setAuthOrg(condition.getOrgId());
                    orgExt.setOrgName(userCurrentOrg.getOrgName());
                    orgExt.setOrgCode(userCurrentOrg.getOrgCode());
                    orgExt.setOrgType(userCurrentOrg.getOrgType());
                    orgExt.setParentOrg(userCurrentOrg.getParentOrg());
                    orgExt.setTaxCode(userCurrentOrg.getTaxCode());
                    orgExt.setTenantId(userCurrentOrg.getTenantId());
                    moiraiUserAuthzOrgs.add(0, orgExt);
                }
            }
        }
        return new BWJsonResult<>(moiraiUserAuthzOrgs, (int) total);
    }

    /***
     * 获取组织机构开票员列表接口
     * @return
     * @author Lance cui
     */
    @Override
    public List<MoiraiUser> getOrgKpyUsers(MoiraiUserCondition moiraiUserCondition) {
        Map kpyMap = new HashMap();
        kpyMap.put("tenantId", moiraiUserCondition.getTenantId());
        kpyMap.put("kpyResource", openInvoicePowerList);
        List<MoiraiRole> kpyRoles = moiraiRoleMapper.selectTenantKpyRoles(kpyMap);

        if (kpyRoles == null || kpyRoles.size() == 0) {
            return null;
        }
        List kpyRoleIds = new ArrayList();
        kpyRoles.forEach(kpyRole -> {
            kpyRoleIds.add(kpyRole.getRoleId());
        });
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserOrgEqualTo(moiraiUserCondition.getOrgId()).andAuthOrgEqualTo(moiraiUserCondition.getOrgId()).andRoleIdIn(kpyRoleIds);
        List<MoiraiUserAuthz> kpyUserIds = moiraiUserAuthzMapper.selectByExample(example);
        if (kpyUserIds == null || kpyUserIds.size() == 0) {
            return null;
        }

        return moiraiUserMapper.selectByIds(kpyUserIds);
    }

    /**
     * 更新用户登录历史表
     *
     * @param moiraiUserLoginHistory
     */
    @Override
    @Transactional
    public void updateLastLoginHistory(MoiraiUserLoginHistory moiraiUserLoginHistory) {
        MoiraiUserLoginHistory loginHistory = new MoiraiUserLoginHistory();
        loginHistory.setUserId(moiraiUserLoginHistory.getUserId());
        if (StringUtils.isBlank(moiraiUserLoginHistory.getLoginType())) {
            moiraiUserLoginHistory.setLoginType(Constants.DEFAULT_ZERO);
        }
        loginHistory.setLoginType(moiraiUserLoginHistory.getLoginType());
        List<MoiraiUserLoginHistory> histories = moiraiUserLoginHistoryMapper.selectLastLogin(loginHistory);
        if (histories.isEmpty()) {
            Long id = seqnumFeignClient.getNum(Constants.MOIRAI_USER_LOGIN_HISTORY);
            moiraiUserLoginHistory.setUlhId(id);
            Long nowTime = DateTimeUtils.nowTimeLong();
            moiraiUserLoginHistory.setLoginTime(nowTime);
            moiraiUserLoginHistory.setCreateTime(nowTime);
            moiraiUserLoginHistoryMapper.insetLastLogin(moiraiUserLoginHistory);
        } else {
            MoiraiUserLoginHistory history = histories.get(0);
            history.setLoginTime(DateTimeUtils.nowTimeLong());
            moiraiUserLoginHistoryMapper.updateLastLogin(history);
        }
    }

    /**
     * 更新用户登录历史表
     *
     * @param moiraiUser
     * @param type post或get
     */
    @Override
    public void updateLastLogin(MoiraiUser moiraiUser, String type) {
        MoiraiUserLoginHistory moiraiUserLoginHistory = new MoiraiUserLoginHistory();
        moiraiUserLoginHistory.setTenantId(moiraiUser.getTenantId());
        moiraiUserLoginHistory.setUserId(moiraiUser.getUserId());
        //查询登录历史表是否存在此条数据
        List<MoiraiUserLoginHistory> historyList = moiraiUserLoginHistoryMapper.selectLastLogin(moiraiUserLoginHistory);
        if (historyList == null || historyList.size() == 0) {
            moiraiUserLoginHistory.setCreater(moiraiUser.getUserAccount());
            moiraiUserLoginHistory.setOrgId(moiraiUser.getOrgId());
            this.insertLogin(moiraiUserLoginHistory);
        } else {
            //跟新最后登录那一条
            if ("post".equalsIgnoreCase(type)) {
                MoiraiUserLoginHistory history = new MoiraiUserLoginHistory();
                history.setUlhId(historyList.get(0).getUlhId());
                this.updateLogin(history);
            } else {
                List<Long> orgIds = new ArrayList<>();
                historyList.forEach(history -> orgIds.add(history.getOrgId()));
                //更新这条存在的为最新登录的
                if (orgIds.contains(moiraiUser.getOrgId())) {
                    moiraiUserLoginHistory.setOrgId(moiraiUser.getOrgId());
                    this.updateLogin(moiraiUserLoginHistory);
                } else {
                    moiraiUserLoginHistory.setCreater(moiraiUser.getUserAccount());
                    moiraiUserLoginHistory.setOrgId(moiraiUser.getOrgId());
                    this.insertLogin(moiraiUserLoginHistory);
                }
            }
        }
    }

    private int updateLogin(MoiraiUserLoginHistory history) {
        history.setLoginTime(DateTimeUtils.nowTimeLong());
        return moiraiUserLoginHistoryMapper.updateLastLogin(history);

    }

    private int insertLogin(MoiraiUserLoginHistory moiraiUserLoginHistory) {
        Long id = seqnumFeignClient.getNum(Constants.MOIRAI_USER_LOGIN_HISTORY);
        moiraiUserLoginHistory.setUlhId(id);
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiUserLoginHistory.setLoginTime(nowTime);
        moiraiUserLoginHistory.setCreateTime(nowTime);
        return moiraiUserLoginHistoryMapper.insetLastLogin(moiraiUserLoginHistory);
    }

    /**
     * 取登录机构
     *
     * @param user
     * @return
     */
    @Override
    public MoiraiUser selectLastLogin(MoiraiUser user) {
        //orgId为最后一次登录机构
        user.setUserOrgId(user.getOrgId());
        //查询登录历史表
        //查询授权机构
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andTenantIdEqualTo(user.getTenantId()).andUserIdEqualTo(user.getUserId()).
            andUserOrgEqualTo(user.getOrgId());
        example.setOrderByClause("create_time DESC");
        List<MoiraiUserAuthz> moiraiUserAuthzs = moiraiUserAuthzMapper.selectByExample(example);
        if (user.getLoginOrg() != null && moiraiUserAuthzs != null && moiraiUserAuthzs.size() > 0) {
            user.setOrgId(moiraiUserAuthzs.get(0).getAuthOrg());
            for (MoiraiUserAuthz authz : moiraiUserAuthzs) {
                if (authz.getAuthOrg().equals(user.getLoginOrg())) {
                    user.setOrgId(user.getLoginOrg());
                    break;
                }
            }
        } else if (moiraiUserAuthzs != null && moiraiUserAuthzs.size() > 0) {
            //没设置默认机构，取最后授权
            user.setOrgId(moiraiUserAuthzs.get(0).getAuthOrg());
        } else {
        }
        logger.info("登录的orgId为:{}", user.getOrgId());
        return user;
    }

    /**
     * 查询登录用户信息
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public MoiraiUser findUserByLogin(MoiraiUserCondition moiraiUserCondition) {
        MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(moiraiUserCondition.getUserId());
        if (null == moiraiUser) {
            throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        MoiraiUserinfo userinfo = moiraiUserinfoMapper.getUserInfoByUserId(moiraiUser.getUserId());
        moiraiUser.setUserinfo(userinfo);
        if (Constants.USER_TYPE_B.equals(moiraiUser.getUserType())) {
            if (moiraiUserCondition.getLoginOrg() != null) {
                moiraiUser.setLoginOrg(moiraiUserCondition.getLoginOrg());
            }
            moiraiUser = selectLastLogin(moiraiUser);
            MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(moiraiUser.getTenantId());
            moiraiUser.setRemark(moiraiTenant.getDualFactor());
        }
        return moiraiUser;
    }

    @Override
    public BWJsonResult<MoiraiUser> checkCLogin(MoiraiUserCondition moiraiUserCondition) {
        logger.info("个人用户登录校验请求参数:{}", moiraiUserCondition);
        //用户是否存在
        MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(moiraiUserCondition.getTelephone());
        if (userByTelephone == null) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR));
        }
        //登录方式
        if (StringUtils.isNotBlank(moiraiUserCondition.getUserPassword())) {
            boolean checkPassword = checkPassword(userByTelephone, moiraiUserCondition.getUserPassword(),
                moiraiUserCondition.getPasswordTrans(), moiraiUserCondition.getPasswordCheckType());
            if (!checkPassword) {
                BWJsonResult<MoiraiUser> bwJsonResult = new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR));
                bwJsonResult.addData(userByTelephone);
                return bwJsonResult;
            }
            BWJsonResult result = checkUserState(userByTelephone, Constants.USER_TYPE_C, moiraiUserCondition.getVersion());
            if (result != null) {
                return result;
            }
        } else if (StringUtils.isNotBlank(moiraiUserCondition.getSmsCode())) {
            //验证验证码
            moiraiUserPwdService.checkSMSCode(moiraiUserCondition);
        } else {
            return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR.getCode(), "登录方式错误"));
        }
        return new BWJsonResult<>(userByTelephone);
    }

    /**
     * 查验用户登录状态
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUser> checkLogin(MoiraiUserCondition moiraiUserCondition) {
        String userAccount = moiraiUserCondition.getUserAccount();
        String userPassword = moiraiUserCondition.getUserPassword();
        String passwordTrans = moiraiUserCondition.getPasswordTrans();
        String passwordCheckType = moiraiUserCondition.getPasswordCheckType();
        logger.info("当前验证账号:{}, 密码传输方式:{}, 验证加密前或后:{}", userAccount, passwordTrans, passwordCheckType);
        /** 1、查询用户信息 **/
        MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(userAccount);
        if (null == moiraiUser) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg() + ",用户账号为：" + userAccount, ErrorType.CustomerError).toString());
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR));
        }
        /** 2、对比密码，确定用户 **/
        boolean checkPassword = checkPassword(moiraiUser, userPassword, passwordTrans, passwordCheckType);
        if (!checkPassword) {
            BWJsonResult<MoiraiUser> bwJsonResult = new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR));
            bwJsonResult.addData(moiraiUser);
            return bwJsonResult;
        }
        /** 3、验证租户、组织、用户状态 **/
        BWJsonResult bwJsonResult = checkUserState(moiraiUser, Constants.USER_TYPE_B, moiraiUserCondition.getVersion());
        if (bwJsonResult != null) {
            return bwJsonResult;
        }
        return new BWJsonResult(moiraiUser);
    }

    @Override
    public MoiraiUser getDefaultCompanyUser(MoiraiUser moiraiUser) {
        Long personalUserId = moiraiUser.getUserId();
        MoiraiUserMemberCondition condition = new MoiraiUserMemberCondition();
        condition.setUserId(personalUserId);
        List<MoiraiUserMember> members = moiraiUserMemberMapper.selectByBean(condition);
        if (!members.isEmpty()) {
            Long tenantId = null;
            Long duUserId = null;
            for (MoiraiUserMember member : members) {
                /** 默认登录企业 **/
                if (Constants.flag_Y.equals(member.getDefaultOrgFlag())) {
                    MoiraiUser userB = moiraiUserMapper.selectByPrimaryKey(member.getBindUserId());
                    if (userB != null) {
                        BWJsonResult bwJsonResult = checkUserState(userB, Constants.USER_TYPE_B, moiraiUser.getVersion());
                        if (bwJsonResult == null) {
                            moiraiUser = userB;
                        }
                    }
                }
                /** 默认双因子 **/
                if (Constants.flag_Y.equals(member.getDefaultValidationFlag())) {
                    tenantId = member.getTenantId();
                    duUserId = member.getBindUserId();
                }
            }
            /** 设置双因子 **/
            if (tenantId == null) {
                MoiraiUserMember memberCondition = new MoiraiUserMember();
                memberCondition.setUserId(personalUserId);
                MoiraiUserMember member = moiraiUserMemberMapper.selectBindFactorTenant(memberCondition);
                if (member != null) {
                    tenantId = member.getTenantId();
                    duUserId = member.getBindUserId();
                }
            }
            if (tenantId != null) {
                MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
                if (!moiraiUser.getUserId().equals(duUserId)) {
                    MoiraiUser user = moiraiUserMapper.selectByPrimaryKey(duUserId);
                    if (Constants.MOIRAI_DUCL_FACTOR_PHONE.equals(moiraiTenant.getDualFactor())) {
                        moiraiUser.setDualFactorinfo(user.getTelephone());
                    } else if (Constants.MOIRAI_DUCL_FACTOR_EMAIL.equals(moiraiTenant.getDualFactor())) {
                        moiraiUser.setDualFactorinfo(user.getUserEmail());
                    }
                } else {
                    if (Constants.MOIRAI_DUCL_FACTOR_PHONE.equals(moiraiTenant.getDualFactor())) {
                        moiraiUser.setDualFactorinfo(moiraiUser.getTelephone());
                    } else if (Constants.MOIRAI_DUCL_FACTOR_EMAIL.equals(moiraiTenant.getDualFactor())) {
                        moiraiUser.setDualFactorinfo(moiraiUser.getUserEmail());
                    }
                }
                moiraiUser.setRemark(moiraiTenant.getDualFactor());
            }
        }
        return moiraiUser;
    }

    /**
     * 校验密码
     */
    @Override
    public boolean checkPassword(MoiraiUser user, String userPassword, String passwordTrans,
        String passwordCheckType) {
        boolean result = false;
        if (Constants.DEFAULT_ONE.equals(passwordTrans)) {
            userPassword = Base64Utils.decodeString(userPassword);
        } else if (Constants.DEFAULT_TWO.equals(passwordTrans)) {
            try {
                userPassword = RSAUtils.decryptByPrivateKey(userPassword, Constants.PRIVATE_KEY);
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR);
            }
        }
        //密码校验
        if (Constants.DEFAULT_ONE.equals(passwordCheckType)) {
            if (user.getUserPassword().equals(userPassword)) {
                result = true;
            }
        } else {
            String password = AdminUtils.getUuidPasswd(userPassword, user.getUuid());
            if (password.equals(user.getUserPassword())) {
                result = true;
            }
        }
        return result;
    }

    private BWJsonResult checkUserState(MoiraiUser user, String userType, String version) {
        if (Constants.USER_TYPE_B.equals(userType)) {
            //租户是否锁定
            MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(user.getTenantId());
            if (moiraiTenant == null || Constants.flag_N.equals(moiraiTenant.getUseFlag())) {
                return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_LOCK));
            }
            if (Constants.MOIRAI_VERSION_V2.equals(version) && Constants.DEFAULT_THREE.equals(moiraiTenant.getLoginMark())) {
                return new BWJsonResult(new MoiraiException("-1", "只能通过单点进行登陆！"));
            }
            user.setRemark(moiraiTenant.getDualFactor());
            if (Constants.MOIRAI_DUCL_FACTOR_PHONE.equals(moiraiTenant.getDualFactor())) {
                user.setDualFactorinfo(user.getTelephone());
            } else if (Constants.MOIRAI_DUCL_FACTOR_EMAIL.equals(moiraiTenant.getDualFactor())) {
                user.setDualFactorinfo(user.getUserEmail());
            }
            //暂时只判断所在机构是否锁定
            MoiraiOrg moiraiOrg = moiraiOrgService.selectByOrgId(user.getOrgId());
            if (moiraiOrg == null || Constants.flag_N.equals(moiraiOrg.getUseFlag())) {
                return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_LOCK));
            }
        }
        String useMark = user.getUseFlag();
        //锁定
        if (Constants.flag_L.equals(useMark)) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_LOCK_ERROR));
        }
        //冻结
        if (Constants.flag_N.equals(useMark)) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_FROZEN_ERROR));
        }
        if (Constants.MOIRAI_VERSION_V2.equals(version)) {
            // 校验密码是否超期
            boolean pwdOverdue = passwordPolicyService.checkPwdOverdue(user.getTenantId(), user.getUserId());
            user.setPwdOverdue(pwdOverdue ? Constants.flag_Y : Constants.flag_N);
            // 校验是否首次登陆
            if (Constants.flag_Y.equals(user.getFirstLogin()) && passwordPolicyService.checkFirstLogin(user.getTenantId())) {
                user.setFirstLogin(Constants.flag_Y);
            } else {
                user.setFirstLogin(Constants.flag_N);
            }
        }
        return null;
    }

    @Override
    public void updateCPassword(MoiraiUserCondition moiraiUserCondition) {
        logger.info("需要修改密码的个人用户:{}", moiraiUserCondition.getTelephone());
        if (!Constants.MOIRAI_VERSION_V2.equals(moiraiUserCondition.getVersion())) {
            boolean rule = RegularExpUtils.checkPasswordRule(moiraiUserCondition.getNewPassword(), null);
            if (!rule) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PASSWORD_ERROR);
            }
        }
        MoiraiUser moiraiUser = null; //moiraiSysService.gainCacheUser();
        if (moiraiUser == null) {
            moiraiUser = moiraiUserMapper.getCUserByTelephone(moiraiUserCondition.getTelephone());
        }
        if (moiraiUser == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        if (Constants.MOIRAI_VERSION_V2.equals(moiraiUserCondition.getVersion())) {
            // 个人用户使用平台密码策略校验密码
            moiraiUser.setTenantId(null);
            moiraiUser.setUserAccount(null);
        }
        updatePassword(moiraiUserCondition, moiraiUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindingBUser(MoiraiUserCondition moiraiUserCondition) {
        String telephone = moiraiUserCondition.getTelephone();
        String userAccount = moiraiUserCondition.getUserAccount();
        logger.info("个人用户:{},需要绑定企业账号:{}", telephone, userAccount);
        MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(telephone);
        if (userByTelephone == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
//        String account = userByTelephone.getBindAccount();
//        if (StringUtils.isNotBlank(account)) {
//            if (account.equals(userAccount)) {
//                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_C_USER_ALREADY_BIND);
//            }
//            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_C_USER_ALREADY_BIND.getCode(), "已绑定" + account + ",请先解绑");
//        }
        MoiraiUser userByUserAccount = moiraiUserMapper.getUserByUserAccount(moiraiUserCondition.getUserAccount());
        if (userByUserAccount == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        if (StringUtils.isNotBlank(userByUserAccount.getTelephone()) && "Y".equals(userByUserAccount.getPhoneValidate())) {
            if (!userByUserAccount.getTelephone().equals(telephone)) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_B_USER_ALREADY_BIND);
            }
        }
        //验证企业账号密码
        boolean checkPassword = checkPassword(userByUserAccount, moiraiUserCondition.getUserPassword(),
            moiraiUserCondition.getPasswordTrans(), moiraiUserCondition.getPasswordCheckType());
        if (!checkPassword) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR);
        }
        Long nowTime = DateTimeUtils.nowTimeLong();
        MoiraiUser moiraiUser = new MoiraiUser();
        moiraiUser.setUserId(userByTelephone.getUserId());
        moiraiUser.setBindAccount(userByUserAccount.getUserAccount());
        moiraiUser.setUserCompany("1");
        moiraiUser.setBindCompany("Y");
        moiraiUser.setDefaultUser("C");
        moiraiUser.setModifyTime(nowTime);
        moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);
        moiraiUser = new MoiraiUser();
        moiraiUser.setUserId(userByUserAccount.getUserId());
        moiraiUser.setTelephone(telephone);
        moiraiUser.setPhoneValidate("Y");
        moiraiUser.setModifyTime(nowTime);
        moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);

        // 新增绑定企业用户
        moiraiUserCondition.setUserId(userByTelephone.getUserId());
        moiraiUserMemberService.bindTenant(moiraiUserCondition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindBUser(MoiraiUserCondition moiraiUserCondition) {
        String telephone = moiraiUserCondition.getTelephone();
        String userAccount = moiraiUserCondition.getUserAccount();
        logger.info("个人用户:{},需要绑定企业账号:{}", telephone, userAccount);
        //验证验证码
        moiraiUserPwdService.checkSMSCode(moiraiUserCondition);
        MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(telephone);
        if (userByTelephone == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        if (!userAccount.equals(userByTelephone.getBindAccount())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_C_UNBIND_ERROR);
        }
        Long nowTime = DateTimeUtils.nowTimeLong();
        MoiraiUser account = moiraiUserMapper.getUserByUserAccount(userAccount);
        if (account != null) {
            account.setTelephone("");
            account.setPhoneValidate("N");
            account.setModifyTime(nowTime);
            moiraiUserMapper.updateByPrimaryKeySelective(account);
        }
        MoiraiUser moiraiUser = new MoiraiUser();
        moiraiUser.setUserId(userByTelephone.getUserId());
        moiraiUser.setBindAccount("");
        moiraiUser.setBindCompany("N");
        moiraiUser.setModifyTime(nowTime);
        moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);

        moiraiUserMemberService.unBindTenant(userByTelephone, account);
    }

    /**
     * 修改密码
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public void updateUserPassword(MoiraiUserCondition moiraiUserCondition) {
        MoiraiUser moiraiUser = moiraiSysService.gainCacheUser();
        if (moiraiUser == null) {
            List<MoiraiUser> list = moiraiUserMapper.findUserByCondition(moiraiUserCondition);
            if (null == list || list.size() == 0) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            moiraiUser = list.get(0);
        }
        updatePassword(moiraiUserCondition, moiraiUser);
    }

    private void updatePassword(MoiraiUserCondition moiraiUserCondition, MoiraiUser user) {
        String uuid = user.getUuid();
        String oldPassword = moiraiUserCondition.getOldPassword();
        String newPassword = moiraiUserCondition.getNewPassword();
        if (Constants.DEFAULT_ONE.equals(moiraiUserCondition.getPasswordTrans())) {
            oldPassword = Base64Utils.decodeString(oldPassword);
            newPassword = Base64Utils.decodeString(newPassword);
        } else if (Constants.DEFAULT_TWO.equals(moiraiUserCondition.getPasswordTrans())) {
            try {
                oldPassword = RSAUtils.decryptByPrivateKey(oldPassword, Constants.PRIVATE_KEY);
                newPassword = RSAUtils.decryptByPrivateKey(newPassword, Constants.PRIVATE_KEY);
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_USER_PASSWORD_DECRYPT_FAIL;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR);
            }
        }
        String oldPwd = AdminUtils.getUuidPasswd(oldPassword, uuid);
        if (!oldPwd.equals(user.getUserPassword())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_MISSPASSWORD_ERROR);
        }
        String newyhMm = AdminUtils.getUuidPasswd(newPassword, uuid);
        if (Constants.MOIRAI_VERSION_V2.equals(moiraiUserCondition.getVersion())) {
            // 校验密码组成规则
            String userAccount = user.getUserAccount();
            if (Constants.USER_TYPE_C.equals(user.getUserType())) {
                userAccount = user.getTelephone();
            }
            passwordPolicyService.checkRegisterPolicy(user.getTenantId(), newPassword, userAccount);
            if (newyhMm.equals(user.getUserPassword())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_NEW_AND_OLD_PWD_EQUALS);
            }
            // 校验密码 是否和最近几次相同
            passwordPolicyService.checkUpdatePolicy(user.getTenantId(), user.getUserId(), newPassword);
        }
        MoiraiUser moiraiUser = new MoiraiUser();
        moiraiUser.setUserPassword(newyhMm);
        moiraiUser.setUserId(user.getUserId());
        moiraiUser.setModifyUser(moiraiUserCondition.getModifyUser());
        moiraiUser.setModifyTime(DateTimeUtils.nowTimeLong());
        moiraiUser.setFirstLogin(Constants.flag_N);

        // 存储旧密码到历史密码表中
        passwordPolicyService.addHistoryPassword(user);

        int i = moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);
    }

    /**
     * 重置密码
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult resetUserPassword(MoiraiUserCondition moiraiUserCondition) {
        MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(moiraiUserCondition.getUserId());
        if (null == moiraiUser) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR));
        }
        String uuid = moiraiUser.getUuid();
        String version = moiraiUserCondition.getVersion();
        MoiraiUser user = new MoiraiUser();
        String userPassword = passwordService.calculatePassword(version);
        String newyhMm = AdminUtils.getUuidPasswd(userPassword, uuid);
        user.setUserPassword(newyhMm);
        user.setModifyUser(moiraiUserCondition.getModifyUser());
        user.setFirstLogin(Constants.flag_Y);
        user.setUserId(moiraiUserCondition.getUserId());
        user.setModifyTime(DateTimeUtils.nowTimeLong());
        user.setUseFlag(Constants.flag_Y);
        if (StringUtils.isNotEmpty(moiraiUserCondition.getModifyOptUser())) {
            user.setModifyOptUser(moiraiUserCondition.getModifyOptUser());
            user.setModifyOptTime(new Date());
        }
        // 存储旧密码到历史密码表中
        passwordPolicyService.addHistoryPassword(moiraiUser);
        int i = moiraiUserMapper.updateByPrimaryKeySelective(user);

        if (Constants.MOIRAI_VERSION_V2.equals(version)) {
            if (StringUtils.isBlank(moiraiUser.getUserEmail())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR.getCode(), "请补全邮箱");
            }
            List<String> emailList = new ArrayList<>();
            emailList.add(moiraiUser.getUserEmail());
            List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
            LazyDynaBean lazyDynaBean = new LazyDynaBean();
            lazyDynaBean.set("emails", emailList);
            lazyDynaBean.set("context", "您的初始化帐号：" + moiraiUser.getUserAccount() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;您的初始化密码：" + userPassword);
            lazyDynaBean.set("userName", "");
            lazyDynaBeans.add(lazyDynaBean);
            passwordService.sendMail(version, "百望云重置密码通知", "ZC_VERIFY", "百望云登录账号重置密码成功", lazyDynaBeans);
        }
        moiraiUserPwdService.unlockAccount(moiraiUser);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("重置密码成功");
        return bwJsonResult;
    }

    @Override
    public void updateUserLastLogin(MoiraiUser moiraiUser) {
        MoiraiUser user = new MoiraiUser();
        user.setUserId(moiraiUser.getUserId());
        user.setLastLogintime(DateTimeUtils.nowTimeLong());
        moiraiUserMapper.updateByPrimaryKeySelective(user);
    }

    public void updateUserByCondition(MoiraiUserCondition moiraiUserCondition) {
        moiraiUserMapper.updateByCondition(moiraiUserCondition);
    }

    @Override
    public MoiraiOrg findTaxCodeByUserAccount(String userAccount) {
        MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(userAccount);
        if (null != moiraiUser) {
            MoiraiOrg moiraiOrg = moiraiOrgService.selectByOrgId(moiraiUser.getOrgId());
            return moiraiOrg;
        }
        return null;
    }

    @Override
    public MoiraiUserDataScope getScopeByUser(MoiraiUserDataScope scope) {
        return moiraiUserDataScopeMapper.selectByUserId(scope.getUserId());
    }

    @Override
    public List<MoiraiResource> getCPResource(MoiraiUser user) {
        return moiraiUserAuthzMapper.selectResourceByUser(user.getUserId(), user.getLoginOrg());
    }

    @Override
    public void getUsersByTenantId(Long teanantId) {
        context.publishEvent(new UserEvent(teanantId));
    }

    /**
     * 根据租户ID查询所有用户 == 》南航
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUser> findUserByTenantId(MoiraiUserCondition moiraiUserCondition) {
        PageHelper.startPage(moiraiUserCondition.getPageNo(), moiraiUserCondition.getPageSize());
        List<MoiraiUser> moiraiUserList = moiraiUserMapper.findUserByTenantId(moiraiUserCondition);
        for (MoiraiUser moiraiUser : moiraiUserList) {
            MoiraiUserinfo userinfo = moiraiUserinfoMapper.getUserInfoById(moiraiUser.getUserinfoId());
            moiraiUser.setUserinfo(userinfo);
            //授权的组织机构
            MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
            example.createCriteria().andTenantIdEqualTo(moiraiUserCondition.getTenantId()).andUserIdEqualTo(moiraiUser.getUserId());
            List<MoiraiUserAuthz> roleList = moiraiUserAuthzMapper.selectByExample(example);
            if (roleList != null && roleList.size() > 0) {
                for (MoiraiUserAuthz moiraiUserAuthz : roleList) {
                    Long authOrg = moiraiUserAuthz.getAuthOrg();
                    MoiraiOrg selectByOrgId = moiraiOrgService.selectByOrgId(authOrg);
                    String orgName = selectByOrgId.getOrgName();
                    moiraiUserAuthz.setOrgName(orgName);
                }
                moiraiUser.setMoiraiUserAuthzs(roleList);
            }
        }
        PageInfo<MoiraiUser> pageInfo = new PageInfo<>(moiraiUserList);
        BWJsonResult<MoiraiUser> moiraiUser = new BWJsonResult<>(pageInfo.getList(), (int) pageInfo.getTotal());
        return moiraiUser;
    }

    /**
     * 校验账号是否是初始密码登录，是否是纯数字账号。
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult checkLoginAccount(MoiraiUserCondition moiraiUserCondition) {
        //查询用户信息
        MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(moiraiUserCondition.getUserId());
        if (null == moiraiUser) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        boolean numeric = RegularExpUtils.isNumeric(moiraiUser.getUserAccount());

        String passwd = AdminUtils.getUuidPasswd(Constants.INIT_PWD, moiraiUser.getUuid());
        boolean flag = false;
        if (passwd.equals(moiraiUser.getUserPassword())) {
            flag = true;
        }
        BWJsonResult bwJsonResult = null;
        if (numeric && flag) {
            bwJsonResult = new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_LOGIN_PWD_ACCOUNT));
        } else if (numeric) {
            bwJsonResult = new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_LOGIN_ACCOUNT_DIGITAL));
        } else if (flag) {
            bwJsonResult = new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_LOGIN_INITIAL_PWD));
        } else {
            bwJsonResult = new BWJsonResult<>();
            bwJsonResult.setMessage("登录成功");
        }
        bwJsonResult.setSuccess(true);
        return bwJsonResult;
    }

    /***
     * 删除角色分配的用户
     * @return
     * @throws MoiraiException
     */
    public int delUserRoleByUserIdRoleId(MoiraiUserCondition moiraiUserCondition) {
        return moiraiUserMapper.delUserRoleByUserIdRoleId(moiraiUserCondition);
    }

    /***
     * 批量判重邮箱
     * @return
     * @throws MoiraiException
     */
    public List<Map<String, Integer>> batchEmailCheckRepetition(
        MoiraiEmailCheck moiraiEmailCheck) throws MoiraiException {
        return moiraiUserMapper.batchEmailCheckRepetition(moiraiEmailCheck);
    }

    /**
     * 修改用户信息
     *
     * @param moiraiUser
     * @return
     */
    public int updateUserInfo(MoiraiUser moiraiUser) {
        MoiraiUser user = moiraiSysService.gainCacheUser();
        if (user == null) {
            return 0;
        }
        moiraiUser.setUserId(user.getUserId());
        moiraiUser.setUserinfoId(user.getUserinfoId());
        moiraiUser.setUserAccount(null);
        return this.updateUser(moiraiUser);
    }

    @Override
    public MoiraiUser findUserNoRoles(MoiraiUserCondition moiraiUserCondition) {
        boolean phone = RegularExpUtils.checkMobile(moiraiUserCondition.getUserAccount());
        MoiraiUser moiraiUser;
        if (phone) {
            moiraiUser = moiraiUserMapper.getCUserByTelephone(moiraiUserCondition.getUserAccount());
        } else {
            moiraiUser = moiraiUserMapper.getUserByUserAccount(moiraiUserCondition.getUserAccount());
        }
        return moiraiUser;
    }

    @Override
    public void saveUserDataScope(Long userId, Long createTime, String creater, String scope) {
        MoiraiUserDataScopeExample dataScopeExample = new MoiraiUserDataScopeExample();
        dataScopeExample.createCriteria().andUserIdEqualTo(userId);
        moiraiUserDataScopeMapper.deleteByExample(dataScopeExample);
        MoiraiUserDataScope dataScope = new MoiraiUserDataScope();
        dataScope.setUserId(userId);
        dataScope.setCreateTime(createTime);
        dataScope.setCreater(creater);
        dataScope.setScope(scope);
        Long id = seqnumFeignClient.getNum(Constants.MOIRAI_USER_DATA_SCOPE);
        dataScope.setUdsId(id);
        moiraiUserDataScopeMapper.insertSelective(dataScope);
    }

    public List<MoiraiUser> getAdminUser(Long tenantId, Long orgId) {
        if (tenantId == null && orgId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiUserCondition condition = new MoiraiUserCondition();
        condition.setTenantId(tenantId);
        condition.setOrgId(orgId);
        condition.setUserCreatetype(Constants.DEFAULT_ZERO);
        return moiraiUserMapper.findUserByCondition(condition);
    }

    /**
     * 根据第三方用户信息获取百望用户信息
     *
     * @param ssoInfo
     * @return
     */
    @Override
    public MoiraiUser getUserBySsoUserInfo(BWToken ssoInfo) {
        if (ssoInfo == null || StringUtils.isBlank(ssoInfo.getSysCode())
            || ssoInfo.getUserInfo() == null || StringUtils.isBlank(ssoInfo.getUserInfo().getUserId())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiUserCondition query = new MoiraiUserCondition();
        if (!"ali".equals(ssoInfo.getSysCode())) {
            // sysCode不是 ali时，必须校验租户id
            if (ssoInfo.getTenantId() == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            query.setTenantId(ssoInfo.getTenantId());
        }
        if ("userAccount".equals(ssoInfo.getUserCheckType())) {
            query.setUserAccount(ssoInfo.getUserInfo().getUserId());
        } else {
            query.setSysCode(ssoInfo.getSysCode());
            query.setSysId(ssoInfo.getUserInfo().getUserId());
        }
        List<MoiraiUser> userList = moiraiUserMapper.findUserByCondition(query);
        if (userList.size() > 1) {
            logger.info("第三方用户信息查询出多个百望用户信息 {}", JSONObject.toJSONString(userList));
            throw new MoiraiException("-1", "根据第三方用户信息未查到绑定的百望用户信息！");
        }
        MoiraiUser moiraiUser = null;
        if (userList.isEmpty()) {
            if ("Y".equals(ssoInfo.getCreateAccountFlag())) {
                moiraiUser = ssoCreateBwUser(ssoInfo);
            } else {
                return null;
            }
        } else {
            moiraiUser = userList.get(0);
        }
        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(moiraiUser.getTenantId());
        if (Constants.DEFAULT_TWO.equals(tenant.getLoginMark())) {
            throw new MoiraiException("-1", "只能通过百望云进行登陆！");
        }
        setDualFactor(moiraiUser);
        return moiraiUser;
    }

    /**
     * 第三方单点登陆创建百望账号
     *
     * @return
     */
    private MoiraiUser ssoCreateBwUser(BWToken ssoInfo) {
        // 根据shortName 和 userAccount创建百望账号
        UserInfo info = ssoInfo.getUserInfo();
        if (StringUtils.isBlank(info.getShortName())
            || StringUtils.isBlank(info.getUserAccount())
            || StringUtils.isBlank(info.getOrgCode())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        //根据租户ID和orgCode查询orgId
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setTenantId(ssoInfo.getTenantId());
        moiraiOrg.setOrgCode(info.getOrgCode());
        MoiraiOrg org = moiraiOrgService.selectOneOrg(moiraiOrg);
        if (org == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_QUERYERROR);
        }
        MoiraiUser addUser = new MoiraiUser();
        addUser.setTenantId(ssoInfo.getTenantId());
        addUser.setOrgId(org.getOrgId());
        addUser.setUserAccount(info.getShortName() + "_" + info.getUserAccount());
        addUser.setUserName(info.getUserName());
        addUser.setTelephone(info.getTelephone());
        addUser.setUserEmail(info.getEmail());
        if (StringUtils.isBlank(info.getPwd())) {
            String pwd = "123!@#" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            addUser.setUserPassword(pwd);
        }
        if (info.getRoleMenus() != null && !info.getRoleMenus().isEmpty()) {
            List<MoiraiUserAuthz> authzs = new ArrayList<>();
            for (int i = 0; i < info.getRoleMenus().size(); i++) {
                JSONObject item = info.getRoleMenus().getJSONObject(i);
                if (item.containsKey("roleId") && item.containsKey("roleOrg")) {
                    MoiraiUserAuthz authz = new MoiraiUserAuthz();
                    authz.setRoleId(item.getLong("roleId"));
                    authz.setRoleOrg(item.getLong("roleOrg"));
                    authzs.add(authz);
                }
            }
            addUser.setMoiraiUserAuthzs(authzs);
        }
        addUser.setSysCode(ssoInfo.getSysCode());
        addUser.setSysId(info.getUserId());
        addUser = addUser(addUser);
        return addUser;
    }

    /**
     * 绑定第三方用户信息
     *
     * @param user
     * @return
     */
    @Override
    public MoiraiUser bindSsoUser(MoiraiUser user) {
        if (user == null || user.getUserId() == null || StringUtils.isBlank(user.getSysCode()) || StringUtils.isBlank(user.getSysId())
            || user.getTenantId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiUser ex = moiraiUserMapper.selectByPrimaryKey(user.getUserId());
        if (ex == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR);
        }
        if (!user.getTenantId().equals(ex.getTenantId())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR);
        }
        if (StringUtils.isNotBlank(ex.getSysCode()) || StringUtils.isNotBlank(ex.getSysId())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_BW_ACCOUNT_BIND);
        }
        MoiraiUserCondition query = new MoiraiUserCondition();
        query.setTenantId(user.getTenantId());
        query.setSysCode(user.getSysCode());
        query.setSysId(user.getSysId());
        List<MoiraiUser> userList = moiraiUserMapper.findUserByCondition(query);
        if (!userList.isEmpty()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_SSO_ACCOUNT_BIND);
        }

        MoiraiUser bind = new MoiraiUser();
        bind.setUserId(user.getUserId());
        bind.setSysCode(user.getSysCode());
        bind.setSysId(user.getSysId());
        moiraiUserMapper.updateByPrimaryKeySelective(bind);
        MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(user.getUserId());
        setDualFactor(moiraiUser);
        return moiraiUser;
    }

    private void setDualFactor(MoiraiUser moiraiUser) {
        Long tenantId = moiraiUser.getTenantId();
        if (tenantId != null) {
            MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
            if (Constants.MOIRAI_DUCL_FACTOR_PHONE.equals(moiraiTenant.getDualFactor())) {
                moiraiUser.setDualFactorinfo(moiraiUser.getTelephone());
            } else if (Constants.MOIRAI_DUCL_FACTOR_EMAIL.equals(moiraiTenant.getDualFactor())) {
                moiraiUser.setDualFactorinfo(moiraiUser.getUserEmail());
            }
            moiraiUser.setRemark(moiraiTenant.getDualFactor());
        }
    }

    @Override
    public List<MoiraiUser> findUserByAuth(MoiraiUserAuthz query) {
        List<MoiraiUser> list = new ArrayList<>();
        if (query.getRoleId() == null) {
            MoiraiUserCondition queryUser = new MoiraiUserCondition();
            queryUser.setOrgId(query.getUserOrg());
            list = moiraiUserMapper.findUserListByCondition(queryUser);
        } else {
            list = moiraiUserMapper.selectUserListByAuth(query);
        }
        return list;
    }
}
