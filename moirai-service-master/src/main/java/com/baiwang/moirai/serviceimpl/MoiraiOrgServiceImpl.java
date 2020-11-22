/*
 * @项目名称: Moirai
 * @文件名称: MoiraiOrgServiceImpl.java
 * @Date: 17-11-7 下午6:08
 * @author Lance cui
 *
 */

package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.RequestContext;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.dao.MoiraiOrgDao;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.event.OrgEvent;
import com.baiwang.moirai.event.UserEvent;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiOrgConfigMapper;
import com.baiwang.moirai.mapper.MoiraiOrgHistoryMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiOrgZomMapper;
import com.baiwang.moirai.mapper.MoiraiProductMapper;
import com.baiwang.moirai.mapper.MoiraiRoleMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserDataScopeMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.MoiraiUserinfoMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgConfig;
import com.baiwang.moirai.model.org.MoiraiOrgHistory;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.org.MoiraiOrgProductVO;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzExample;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiUserMemberService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.HttpInfoUtils;
import com.baiwang.moirai.utils.JacksonUtil;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class MoiraiOrgServiceImpl implements MoiraiOrgService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private MoiraiOrgConfigMapper moiraiOrgConfigMapper;

    @Autowired
    private MoiraiOrgHistoryMapper moiraiOrgHistoryMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiUserinfoMapper moiraiUserinfoMapper;

    @Autowired
    private MoiraiOrgZomMapper moiraiOrgZomMapper;

    @Autowired
    private MoiraiRoleMapper moiraiRoleMapper;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiProductMapper moiraiProductMapper;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    private MoiraiUserMemberService moiraiUserMemberService;

    @Autowired
    private MoiraiUserDataScopeMapper moiraiUserDataScopeMapper;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    @Value("${org.default.products}")
    private String ORG_DEFAULT_PRODUCTS;

    @Value("${use.method}")
    private boolean useFlag;

    @Value("${bi.open.condition}")
    private String biOpenConditon;

    @Value("${is.open.bi}")
    private boolean isOpenBI;

    @Resource
    private ApplicationEventPublisher context;

    @Autowired
    private MoiraiOrgDao moiraiOrgDao;

    @Autowired
    private PasswordService passwordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoiraiUser addorg(MoiraiOrg moiraiOrg) {
        MoiraiTenant tenantInfo = checkAddOrgParam(moiraiOrg);
        Integer orgType = moiraiOrg.getOrgType();
        Long id = seqnumFeignClient.getNum(Constants.MOIRAI_ORG);
        Long nowMinuteTime = DateTimeUtils.nowTimeLong();
        moiraiOrg.setOrgId(id);
        moiraiOrg.setCreateTime(nowMinuteTime);
        moiraiOrg.setModifyUser(moiraiOrg.getCreater());
        moiraiOrg.setModifyTime(nowMinuteTime);
        /**添加非纳税主体组织机构**/
        if (orgType == 2) {
            addNoTaxOrg(moiraiOrg);
        }

        /**添加纳税主体组织机构**/
        if (orgType == 1) {
            validatetaxOrg(moiraiOrg);
            boolean flag = cheackTaxCodeStatus(moiraiOrg);
            if (flag) {
                addTaxOrg(moiraiOrg);
            }
        }
        //添加开通产品的操作
        if (Constants.DEFAULT_ONE.equals(moiraiOrg.getHasDefOrgProduct())) {
            addOrgProducts(moiraiOrg, true, tenantInfo.getQdBm());
        }
        MoiraiUser moiraiUser = null;
        if (Constants.DEFAULT_ONE.equals(moiraiOrg.getHasDefAdminUser())) {
            moiraiUser = addMoiraiUser(moiraiOrg);
        }

        List<MoiraiOrg> orgList = new ArrayList<>(1);
        orgList.add(moiraiOrg);
        List<String> taxCodeList = new ArrayList<>(1);
        taxCodeList.add(moiraiOrg.getTaxCode());
        moiraiSysService.excecutorImport(null, taxCodeList, orgList);
        return moiraiUser;
    }

    private void addNoTaxOrg(MoiraiOrg moiraiOrg) {
        try {
            int result = moiraiOrgDao.addNoTaxOrg(moiraiOrg);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_ORG_ADDORGERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg() + " 非纳税主体", ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDORGERROR);
        }
    }

    private void addTaxOrg(MoiraiOrg moiraiOrg) {
        moiraiOrg.setNeedSeal("1");
        moiraiOrg.setSealId(moiraiOrg.getTaxCode());
        try {
            int result = moiraiOrgDao.addTaxOrg(moiraiOrg);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_ORG_ADDORGERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg() + " 纳税主体", ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDORGERROR);
        }
    }

    /**
     * 添加机构时添加超管账号
     *
     * @param moiraiOrg
     * @return
     */
    public MoiraiUser addMoiraiUser(MoiraiOrg moiraiOrg) {
        //creat user
        MoiraiUser user = new MoiraiUser();
        user.setTenantId(moiraiOrg.getTenantId());
        user.setOrgId(moiraiOrg.getOrgId());
        user.setUserType("B");
        user.setType("orgAdmin");
        user.setUserName("机构管理员");
        user.setHasDefAdminRole(moiraiOrg.getHasDefAdminRole());
        user.setUserAccount("admin_" + moiraiOrg.getOrgId());
        user.setUserCreatetype("0");
        user.setCreater(moiraiOrg.getCreater());
        user.setUserEmail(moiraiOrg.getAdminUserEmail());
        user.setVersion(moiraiOrg.getVersion());
        MoiraiUser moiraiUser = moiraiUserService.addUser(user);
        if (moiraiUser == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ADD_ERROR);
        }
        if (Constants.MOIRAI_VERSION_V2.equals(moiraiOrg.getVersion())) {
            List<String> emailList = new ArrayList<>();
            emailList.add(moiraiOrg.getAdminUserEmail());
            List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
            LazyDynaBean lazyDynaBean = new LazyDynaBean();
            lazyDynaBean.set("emails", emailList);
            lazyDynaBean.set("context", "您的初始化帐号：" + moiraiUser.getUserAccount() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;您的初始化密码：" + moiraiUser.getUserPassword());
            lazyDynaBean.set("userName", moiraiOrg.getOrgName());
            lazyDynaBeans.add(lazyDynaBean);
            passwordService.sendMail(moiraiOrg.getVersion(), "百望云", "ZC_VERIFY", "百望云平台机构注册成功", lazyDynaBeans);
        }
        return moiraiUser;
    }

    /**
     * 验证机构编码不能重复
     */
    @Override
    public MoiraiOrg validOrgCode(MoiraiOrg moiraiOrg) {
        MoiraiOrg org = new MoiraiOrg();
        org.setOrgCode(moiraiOrg.getOrgCode());
        org.setTenantId(moiraiOrg.getTenantId());
        return moiraiOrgMapper.selectOneOrg(org);
    }

    @Override
    public MoiraiOrg selectByOrgId(Long orgId) {
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setOrgId(orgId);
        MoiraiOrg result = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        return result;
    }

    @Override
    public MoiraiOrg getOrgByOrgId(MoiraiOrg moiraiOrg) {
        MoiraiOrg result = moiraiOrgDao.selectOneOrg(moiraiOrg);
        if (result != null) {
            MoiraiUserCondition condition = new MoiraiUserCondition();
            condition.setOrgId(result.getOrgId());
            condition.setUserCreatetype("0");
            List<MoiraiUser> users = moiraiUserMapper.findUserByCondition(condition);
            if (users != null && users.size() > 0) {
                String userAccount = users.get(0).getUserAccount();
                result.setOrgAdminUserAccount(userAccount);
            }
            List<MoiraiOrg> orgList = new ArrayList<>(1);
            orgList.add(result);
            this.setProducts(orgList);
        }
        return result;
    }

    @Override
    public MoiraiOrg selectOneOrg(MoiraiOrg moiraiOrg) {
        return moiraiOrgMapper.selectOneOrg(moiraiOrg);
    }

    @Override
    public MoiraiOrg queryByTaxCode(String taxCode) {
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setTaxCode(taxCode);
        return moiraiOrgMapper.selectOneOrg(moiraiOrg);
    }

    @Override
    public List<MoiraiOrg> queryByOrgName(String orgName) {
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setOrgName(orgName);
        return moiraiOrgMapper.selectMoreOrg(moiraiOrg);
    }

    @Override
    public int updateOrg(MoiraiOrg moiraiOrg) {
        MoiraiOrg org = null;
        if (moiraiOrg.getOrgId() != null) {
            org = this.selectByOrgId(moiraiOrg.getOrgId());
        } else if (StringUtils.isNotBlank(moiraiOrg.getTaxCode())) {
            org = this.queryByTaxCode(moiraiOrg.getTaxCode());
            moiraiOrg.setOrgId(org.getOrgId());
        } else {
        }
        if (org == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        moiraiOrg.setTenantId(org.getTenantId());
        this.validateTaxmsg(moiraiOrg);
        if (!StringUtils.isEmpty(moiraiOrg.getOrgName())) {
            Integer orgType = org.getOrgType();
            if (orgType == null || orgType == 1) {//纳税主体名称税号不能在这修改
                moiraiOrg.setTaxCode(null);
                moiraiOrg.setOrgName(null);
            }
        }

        if (!StringUtils.isEmpty(moiraiOrg.getOrgCode())) {
            /**验证组织机构编码,租户唯一**/
            MoiraiOrg oldOrd = this.validOrgCode(moiraiOrg);
            if (oldOrd != null && !oldOrd.getOrgId().equals(moiraiOrg.getOrgId())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_CODE_DUPLICATE);
            }
        }
        moiraiOrg.setModifyTime(DateTimeUtils.nowTimeLong());
        int i = moiraiOrgDao.updateOrg(moiraiOrg);
        // 修改机构管理员邮箱信息
        if (RegularExpUtils.checkEmail(moiraiOrg.getAdminUserEmail())) {
            MoiraiUserCondition queryUser = new MoiraiUserCondition();
            queryUser.setOrgId(moiraiOrg.getOrgId());
            queryUser.setTenantId(moiraiOrg.getTenantId());
            queryUser.setUserCreatetype("0");
            List<MoiraiUser> userList = moiraiUserMapper.findUserByCondition(queryUser);
            if (!userList.isEmpty()) {
                MoiraiUser update = new MoiraiUser();
                update.setUserId(userList.get(0).getUserId());
                update.setUserEmail(moiraiOrg.getAdminUserEmail());
                update.setEmailValidate(Constants.flag_Y);
                moiraiUserMapper.updateByPrimaryKeySelective(update);
            }
        }
        this.deleteCacheOrg(moiraiOrg.getOrgId(), org.getTaxCode());
        String perfectFlag = moiraiOrg.getPerfectFlag();
        if (perfectFlag != null && "N".equals(perfectFlag)) {
            MoiraiOrg moiraiOrg1 = moiraiOrgMapper.selectOneOrg(moiraiOrg);
            List<MoiraiOrg> orgList = new ArrayList<>();
            moiraiOrg.setTaxCode(moiraiOrg1.getTaxCode());
            moiraiOrg.setOrgName(moiraiOrg1.getOrgName());
            moiraiOrg.setOrgType(moiraiOrg1.getOrgType());
            orgList.add(moiraiOrg);
            setProducts(orgList);
            moiraiSysService.excecutorImport(null, null, orgList);
        }
        return i;
    }

    /**
     * 修改父级或者非纳税改纳税
     */
    @Override
    public void updateOrgEntity(MoiraiOrg moiraiOrg) {
        MoiraiOrg oneOrg = this.selectByOrgId(moiraiOrg.getOrgId());
        if (oneOrg == null || !oneOrg.getTenantId().equals(moiraiOrg.getTenantId())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_UPDATEERROR);
        }
        // 修改父级机构
        if (StringUtils.isNotBlank(moiraiOrg.getParentOrgCode())) {
            MoiraiOrg queryParent = new MoiraiOrg();
            queryParent.setOrgCode(moiraiOrg.getParentOrgCode());
            queryParent.setTenantId(moiraiOrg.getTenantId());
            MoiraiOrg parent = moiraiOrgMapper.selectOneOrg(queryParent);
            if (parent == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_PARENTNOTEXT);
            }
            checkIsChildren(oneOrg, parent.getOrgId());
            MoiraiOrg update = new MoiraiOrg();
            update.setOrgId(oneOrg.getOrgId());
            update.setParentOrg(parent.getOrgId());
            this.updateOrg(update);
        } else if (oneOrg.getOrgType() == 2 && moiraiOrg.getOrgType() == 1) {
            moiraiOrg.setParentOrg(null);
            moiraiOrg.setParentOrgCode(null);
            moiraiOrg.setOrgName(null);
            if (StringUtils.isEmpty(moiraiOrg.getTaxCode()) || StringUtils.isEmpty(moiraiOrg.getTelphone())
                || StringUtils.isEmpty(moiraiOrg.getBusinessAddress()) || StringUtils.isEmpty(moiraiOrg.getTaxProv())
                || StringUtils.isEmpty(moiraiOrg.getTaxQuali()) || StringUtils.isEmpty(moiraiOrg.getExportQualify())
                || StringUtils.isEmpty(moiraiOrg.getDjCompanyType()) || StringUtils.isEmpty(moiraiOrg.getBelongIndustry())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            MoiraiOrg queryTaxCode = new MoiraiOrg();
            queryTaxCode.setTaxCode(moiraiOrg.getTaxCode());
            MoiraiOrg ex = moiraiOrgMapper.selectOneOrg(queryTaxCode);
            if (ex != null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TAXCODE_ERROR);
            }
            this.updateOrg(moiraiOrg);
        } else {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TYPE_CHANGE_ERROR);
        }
    }

    /**
     * 校验是否是当前机构或者下级机构
     */
    private void checkIsChildren(MoiraiOrg org, Long parentId) {
        if (org.getOrgId().equals(parentId)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARENT_IS_CURRENT_ERROR);
        }
        MoiraiOrgCondition queryChildren = new MoiraiOrgCondition();
        queryChildren.setParentOrg(org.getOrgId());
        queryChildren.setTenantId(org.getTenantId());
        List<MoiraiOrg> childrenList = moiraiOrgMapper.queryOrgByCondition(queryChildren);
        for (int i = 0; i < childrenList.size(); i++) {
            if (childrenList.get(i).getOrgId().equals(parentId)) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARENT_IS_CHILDREN_ERROR);
            }
            checkIsChildren(childrenList.get(i), parentId);
        }
    }

    /**
     * 1-返回树形结构 2-返回list结构
     */
    @Override
    public List<MoiraiOrg> getTenantOrgTree(MoiraiOrg moiraiOrg, int struc) {
        Long tenantId = moiraiOrg.getTenantId();
        if (tenantId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrg> moiraiOrgList = moiraiOrgMapper.queryOrgTreeByTenant(tenantId);
        MoiraiUserCondition condition = new MoiraiUserCondition();
        condition.setTenantId(tenantId);
        condition.setUserCreatetype("0");
        List<MoiraiUser> userByCondition = moiraiUserMapper.findUserByCondition(condition);
        Map<Long, MoiraiUser> map = new HashMap<>();
        for (MoiraiUser moiraiUser : userByCondition) {
            map.put(moiraiUser.getOrgId(), moiraiUser);
        }
        List<MoiraiOrgProduct> orgProductList = moiraiOrgProductMapper.queryTenantProducts(tenantId);
        for (MoiraiOrg org : moiraiOrgList) {
            Long orgId = org.getOrgId();
            MoiraiUser moiraiUser = map.get(orgId);
            if (moiraiUser != null) {
                org.setOrgAdminUserAccount(moiraiUser.getUserAccount());
                org.setAdminUserEmail(moiraiUser.getUserEmail());
            }
            List<MoiraiOrgProduct> list = new ArrayList<>();
            for (MoiraiOrgProduct orgProduct : orgProductList) {
                if (org.getOrgId().equals(orgProduct.getOrgId())) {
                    list.add(orgProduct);
                }
            }
            org.setProducts(list);
        }
        Long orgId = moiraiOrg.getOrgId();
        List<MoiraiOrg> moiraiOrgStruc = new ArrayList<MoiraiOrg>();
        if (moiraiOrgList != null && moiraiOrgList.size() > 0 && struc == 1) {
            MoiraiOrg moiraiOrgTree = combineOrgTree(moiraiOrgList, orgId);
            moiraiOrgStruc.add(moiraiOrgTree);
        } else {
            moiraiOrgStruc = moiraiOrgList;
        }

        return moiraiOrgStruc;
    }

    /**
     * 查询租户的组织机构简单展示树
     * <p>
     * 1-返回树形结构 2-返回list结构
     **/
    @Override
    public List<MoiraiOrg> getTenantOrgSimpleTree(MoiraiOrg moiraiOrg, int struc) {

        Long tenantId = moiraiOrg.getTenantId();
        if (tenantId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrg> moiraiOrgList = moiraiOrgMapper.queryOrgSimpleTreeByTenant(tenantId);
        List<MoiraiOrg> moiraiOrgStruc = new ArrayList<MoiraiOrg>();

        if (moiraiOrgList == null && moiraiOrgList.size() > 0) {
            logger.info("getTenantOrgSimpleTree查询结果为空");
            return moiraiOrgStruc;
        }
        Long orgId = moiraiOrg.getOrgId();
        if (struc == 1) {
            MoiraiOrg moiraiOrgTree = combineOrgTree(moiraiOrgList, orgId);
            moiraiOrgStruc.add(moiraiOrgTree);

        } else if (struc == 2 && orgId != null) {

            moiraiOrgStruc = combineOrgList(moiraiOrgList, orgId);
        } else {
            moiraiOrgStruc = moiraiOrgList;
        }

        return moiraiOrgStruc;
    }

    @Override
    public List<MoiraiOrg> getOrgChildren(Long orgId) {
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setParentOrg(orgId);
        return moiraiOrgMapper.selectMoreOrg(moiraiOrg);
    }

    @Override
    public List<MoiraiOrgProductVO> getTenantAllProducts(MoiraiTenant moiraiTenant) {

        return moiraiOrgProductMapper.queryTenantAllProducts(moiraiTenant);
    }

    @Override
    public BWJsonResult<MoiraiOrg> getOrgListPage(MoiraiOrgCondition moiraiOrgCondition) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        PageHelper.startPage(moiraiOrgCondition.getPageNo(), moiraiOrgCondition.getPageSize());
        List<MoiraiOrg> moiraiTenantList = moiraiOrgMapper.queryOrgListPage(moiraiOrgCondition);
        this.setProducts(moiraiTenantList);
        PageInfo<MoiraiOrg> pageInfo = new PageInfo<>(moiraiTenantList);
        return new BWJsonResult<MoiraiOrg>(moiraiTenantList, (int) pageInfo.getTotal());
    }

    @Override
    public BWJsonResult<MoiraiOrg> getOrgListPage2(MoiraiOrgCondition moiraiOrgCondition) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        PageHelper.startPage(moiraiOrgCondition.getPageNo(), moiraiOrgCondition.getPageSize());
        List<MoiraiOrg> moiraiTenantList = moiraiOrgMapper.queryOrgListPage2(moiraiOrgCondition);
        this.setProducts(moiraiTenantList);
        List<Long> tenantIdList = moiraiTenantList.stream().map(item -> item.getTenantId()).collect(Collectors.toList());
        if (!tenantIdList.isEmpty()) {
            List<MoiraiTenant> tenantList = moiraiTenantMapper.batchQueryByIds(tenantIdList);
            Map<Long, MoiraiTenant> tenantMap = new HashMap<>();
            for (MoiraiTenant tenant : tenantList) {
                tenantMap.put(tenant.getTenantId(), tenant);
            }
            for (MoiraiOrg org : moiraiTenantList) {
                org.setTenantName(tenantMap.containsKey(org.getTenantId()) ? tenantMap.get(org.getTenantId()).getTenantName() : "");
            }
        }
        PageInfo<MoiraiOrg> pageInfo = new PageInfo<>(moiraiTenantList);
        return new BWJsonResult<MoiraiOrg>(moiraiTenantList, (int) pageInfo.getTotal());
    }

    /**
     * 向上获取组织机构限制100级
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public MoiraiOrg getOrgTaxEntity(MoiraiOrg moiraiOrg) {

        MoiraiOrg orgInfo = this.selectByOrgId(moiraiOrg.getOrgId());
        if (orgInfo == null) {
            return null;
        }
        Integer orgType = orgInfo.getOrgType();
        Long parentId = orgInfo.getParentOrg();
        //parentId != 0L 必不可少
        int loop = 100;
        while (orgType != 1 && (parentId != null && parentId != 0L) && loop > 0) {
            loop--;
            orgInfo = this.selectByOrgId(parentId);
            if (orgInfo == null) {
                return null;
            }
            orgType = orgInfo.getOrgType();
            parentId = orgInfo.getParentOrg();
        }
        return orgInfo;
    }

    //向上获取组织机构的父级
    @Override
    public MoiraiOrg getOrgFatherInfo(Long orgId) {

        MoiraiOrg org = this.selectByOrgId(orgId);
        if (ObjectUtils.isEmpty(org)) {
            return null;
        }
        Long parentOrgId = org.getParentOrg();
        if (("1".equals(org.getOrgType() + "")) || (parentOrgId == 0)) {
            return org;
        }
        return this.getOrgFatherInfo(parentOrgId);
    }

    @Override
    public BWJsonResult<MoiraiOrg> getOrgByCondition(MoiraiOrgCondition moiraiOrg) {
        if (moiraiOrg.getPageNo() <= 0) {
            moiraiOrg.setPageNo(1);
        }
        if (moiraiOrg.getPageSize() <= 0) {
            moiraiOrg.setPageSize(100);
        }
        PageHelper.startPage(moiraiOrg.getPageNo(), moiraiOrg.getPageSize());
        List<MoiraiOrg> moiraiOrgRes = moiraiOrgMapper.queryOrgByCondition(moiraiOrg);
        if (moiraiOrgRes != null && moiraiOrgRes.size() == 1) {
            MoiraiOrg org = moiraiOrgRes.get(0);
            Long orgId = org.getOrgId();
            MoiraiUserCondition condition = new MoiraiUserCondition();
            condition.setOrgId(orgId);
            condition.setUserCreatetype("0");
            List<MoiraiUser> users = moiraiUserMapper.findUserByCondition(condition);
            if (users != null && users.size() > 0) {
                String userAccount = users.get(0).getUserAccount();
                org.setOrgAdminUserAccount(userAccount);
            }
            moiraiOrgRes = new ArrayList<>();
            moiraiOrgRes.add(org);
        }
        this.setProducts(moiraiOrgRes);
        PageInfo<MoiraiOrg> pageInfo = new PageInfo<>(moiraiOrgRes);
        return new BWJsonResult(pageInfo.getList(), (int) pageInfo.getTotal());
    }

    /**
     * getOrgByCondition接口缓存中获取机构信息
     *
     * @return
     */
    @Override
    public MoiraiOrg getCacheMoiraiOrg(Long orgId, String taxCode) {
        Object orgObj = null;
        if (StringUtils.isNotBlank(taxCode) && orgId == null) {
            Object orgIdStr = redisTemplate.opsForValue().get(Constants.MOIRAI_ORG_INFO + taxCode);
            if (orgIdStr != null) {
                Long orgIdByTax = Long.valueOf(orgIdStr.toString());
                orgObj = redisTemplate.opsForValue().get(Constants.MOIRAI_ORG_INFO + orgIdByTax);
                if (orgObj != null) {
                    MoiraiOrg moiraiOrg = JacksonUtil.jsonStrToObject(orgObj.toString(), MoiraiOrg.class);
                    return moiraiOrg;
                }
            }
        }
        if (orgId != null) {
            orgObj = redisTemplate.opsForValue().get(Constants.MOIRAI_ORG_INFO + orgId);
            if (orgObj != null) {
                MoiraiOrg moiraiOrg = JacksonUtil.jsonStrToObject(orgObj.toString(), MoiraiOrg.class);
                return moiraiOrg;
            }
        }
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setOrgId(orgId);
        moiraiOrg.setTaxCode(taxCode);
        MoiraiOrg org = moiraiOrgDao.selectOneOrg(moiraiOrg);
        List<MoiraiOrg> orgList = new ArrayList<>(1);
        if (org != null) {
            orgList.add(org);
            MoiraiUserCondition condition = new MoiraiUserCondition();
            condition.setOrgId(org.getOrgId());
            condition.setUserCreatetype("0");
            List<MoiraiUser> users = moiraiUserMapper.findUserByCondition(condition);
            if (users != null && users.size() > 0) {
                String userAccount = users.get(0).getUserAccount();
                org.setOrgAdminUserAccount(userAccount);
            }
        }
        this.setProducts(orgList);
        if (org != null) {
            int time = 120 + new Random().nextInt(60);
            if (StringUtils.isNotBlank(org.getTaxCode())) {
                redisTemplate.opsForValue().set(Constants.MOIRAI_ORG_INFO + org.getTaxCode(), org.getOrgId().toString(), time, TimeUnit.MINUTES);
            }
            redisTemplate.opsForValue().set(Constants.MOIRAI_ORG_INFO + org.getOrgId(), org.toString(), time, TimeUnit.MINUTES);
        }
        return org;
    }

    @Override
    public void deleteCacheOrg(Long orgId, String taxCode) {
        redisTemplate.delete(Constants.MOIRAI_ORG_INFO + orgId);
        if (StringUtils.isNotBlank(taxCode)) {
            redisTemplate.delete(Constants.MOIRAI_ORG_INFO + taxCode);
        }
    }

    /**
     * 批量查询机构产品
     *
     * @param moiraiOrg
     * @return
     */
    private void setProducts(List<MoiraiOrg> moiraiOrg) {
        if (moiraiOrg != null && moiraiOrg.size() > 0) {
            List<Long> orgList = new ArrayList<>();
            moiraiOrg.forEach(org -> orgList.add(org.getOrgId()));
            List<MoiraiOrgProduct> orgProductList = null;
            if (orgList != null && orgList.size() > 0) {
                orgProductList = moiraiOrgProductMapper.queryOrgProducts(orgList);
            }
            for (MoiraiOrg org : moiraiOrg) {
                List<MoiraiOrgProduct> list = new ArrayList<>();
                for (MoiraiOrgProduct orgProduct : orgProductList) {
                    if (org.getOrgId().equals(orgProduct.getOrgId())) {
                        list.add(orgProduct);
                    }
                }
                org.setProducts(list);
            }
        }
    }

    @Override
    public List<MoiraiOrg> getOrgListByCondition(Map<String, List> moiraiOrgList) {
        List pageNumList = moiraiOrgList.get("pageNo");
        List pageSizeList = moiraiOrgList.get("pageSize");
        int pageNum = 1;
        int pageSize = 1000;
        if (pageNumList != null && pageNumList.size() > 0) {
            pageNum = (int) pageNumList.get(0);
        }
        if (pageSizeList != null && pageSizeList.size() > 0) {
            pageSize = (int) pageSizeList.get(0);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<MoiraiOrg> result = moiraiOrgMapper.queryOrgListByCondition(moiraiOrgList);
        this.setProducts(result);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrgTaxName(MoiraiOrg moiraiOrg) {

        Long nowMinuteTime = DateTimeUtils.nowTimeLong();
        moiraiOrg.setModifyTime(nowMinuteTime);

        MoiraiOrg oldOrg = this.selectByOrgId(moiraiOrg.getOrgId());
        if (oldOrg == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        if (moiraiOrg.getTaxCode() != null) {
            MoiraiOrgHistory OrgHistory = new MoiraiOrgHistory();
            Long id = seqnumFeignClient.getNum(Constants.MOIRAI_ORG_HISTORY);
            OrgHistory.setHisId(id);
            OrgHistory.setCreater(moiraiOrg.getModifyUser());
            OrgHistory.setCreateTime(nowMinuteTime);
            OrgHistory.setOrgId(moiraiOrg.getOrgId());
            OrgHistory.setTaxCode(oldOrg.getTaxCode());
            OrgHistory.setNewCode(moiraiOrg.getTaxCode());
            OrgHistory.setTenantId(moiraiOrg.getTenantId());
            int addHisResult = moiraiOrgHistoryMapper.insertSelective(OrgHistory);
            //查询百望云老版数据库
            List<String> taxCodeList = new ArrayList<>();
            taxCodeList.add(moiraiOrg.getTaxCode());
            moiraiSysService.excecutorImport(null, taxCodeList, null);
            moiraiSysService.removeTaxCode(oldOrg.getTaxCode());
        }

        if (moiraiOrg.getOrgName() != null) {
            MoiraiOrgHistory OrgHistory = new MoiraiOrgHistory();
            Long id = seqnumFeignClient.getNum(Constants.MOIRAI_ORG_HISTORY);
            OrgHistory.setHisId(id);
            OrgHistory.setCreater(moiraiOrg.getModifyUser());
            OrgHistory.setCreateTime(nowMinuteTime);
            OrgHistory.setOrgId(moiraiOrg.getOrgId());
            OrgHistory.setOrgName(oldOrg.getOrgName());
            OrgHistory.setNewName(moiraiOrg.getOrgName());
            OrgHistory.setTenantId(moiraiOrg.getTenantId());
            int addHisResult = moiraiOrgHistoryMapper.insertSelective(OrgHistory);
            if (oldOrg.getParentOrg() == 0L) {
                MoiraiTenant moiraiTenant = new MoiraiTenant();
                moiraiTenant.setTenantId(moiraiOrg.getTenantId());
                moiraiTenant.setTenantName(moiraiOrg.getOrgName());
                moiraiTenantMapper.updateTenant(moiraiTenant);
            }
        }

        int result = moiraiOrgMapper.updateOrgTaxName(moiraiOrg);
        this.deleteCacheOrg(moiraiOrg.getOrgId(), oldOrg.getTaxCode());
        /*moiraiOrg.setParentOrg(oldOrg.getParentOrg());
        List<MoiraiOrgProduct> products = moiraiOrgProductMapper.selectCPProductsByOrgId(moiraiOrg.getOrgId());
        moiraiOrg.setProducts(products);*/
        context.publishEvent(new OrgEvent(moiraiOrg, true));

        return result;
    }

    /**
     * 税号唯一，但是org_name同一租户下可以重复
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public List<MoiraiOrg> getOrgHistoryTaxOrName(MoiraiOrg moiraiOrg) {
        List<MoiraiOrg> orgzHistory = new ArrayList<>();
        Long orgId = moiraiOrg.getOrgId();
        if (!StrUtils.isEmpty(orgId + "")) {
            MoiraiOrg history = this.selectByOrgId(orgId);
            orgzHistory.add(history);
        } else {
            List<MoiraiOrgHistory> orgHistoryList = moiraiOrgHistoryMapper.selectByHistoryTaxName(moiraiOrg);
            if (orgHistoryList == null || orgHistoryList.size() == 0) {
                return orgzHistory;
            }
            //org_id去重
            List<Long> orgIdList = new ArrayList<>();
            for (MoiraiOrgHistory moh : orgHistoryList) {
                if (!orgIdList.contains(moh.getOrgId())) {
                    orgIdList.add(moh.getOrgId());
                }
            }
            for (Long orgId2 : orgIdList) {
                MoiraiOrg history = this.selectByOrgId(orgId2);
                orgzHistory.add(history);
            }
        }
        return orgzHistory;
    }

    @Override
    public int deleteOrgProducts(Long orgId) {

        return moiraiOrgProductMapper.deleteOrgAllProducts(orgId);
    }

    @Override
    public int addOrgProducts(MoiraiOrg moiraiOrg, boolean haDefault, Long tenantQdbm) {

        List<MoiraiOrgProduct> orgProducts = moiraiOrg.getProducts();
        int insertNum = 0;
        if (haDefault) {
            String[] defProducts = ORG_DEFAULT_PRODUCTS.split(",");
            if (defProducts != null && defProducts.length > 0) {
                for (int i = 0; i < defProducts.length; i++) {
                    String defProductId = defProducts[i];
                    MoiraiOrgProduct product = new MoiraiOrgProduct();
                    product.setProductId(Long.valueOf(defProductId));
                    product.setQdBm(Constants.DEFAULT_QDBM);
                    if (orgProducts == null) {
                        orgProducts = new ArrayList<MoiraiOrgProduct>();
                        moiraiOrg.setProducts(orgProducts);
                    }
                    orgProducts.add(product);
                }
            }
        }
        MoiraiOrgProduct moiraiOrgProduct = this.addIncidentalProduct(moiraiOrg);
        if (moiraiOrgProduct != null) {
            orgProducts.add(moiraiOrgProduct);
        }
        if (moiraiOrg.getProducts() != null && (moiraiOrg.getProducts().size()) > 0) {
            List<MoiraiOrgProduct> products = moiraiOrg.getProducts();
            for (int i = 0; i < products.size(); i++) {
                MoiraiOrgProduct product = products.get(i);
                Long productId = product.getProductId();
                Long qdbm = product.getQdBm();
                if (productId == null) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDORGERROR);
                }
                if (qdbm == null) {
                    if (tenantQdbm != null) {
                        qdbm = tenantQdbm;
                    } else {
                        qdbm = Constants.DEFAULT_QDBM;
                    }
                }
                if (moiraiOrg.getCreater() != null) {
                    product.setCreater(moiraiOrg.getCreater());
                } else {
                    product.setCreater(moiraiOrg.getModifyUser());
                }
                product.setTenantId(moiraiOrg.getTenantId());
                product.setOrgId(moiraiOrg.getOrgId());
                if (product.getOpenType() == null) {
                    product.setOpenType(1L);
                }
                int resultPro = this.addOrgProduct(product);
                insertNum++;
            }
        }
        context.publishEvent(new OrgEvent(moiraiOrg, true));

        return insertNum;
    }

    /**
     * 判断产品继承关系和开通类型限制
     *
     * @param product
     * @return
     */
    private List<Long> checkProductProperty(MoiraiOrgProduct product) {
        List<Long> orgIdList = new ArrayList<>();
        orgIdList.add(product.getOrgId());
        //查询产品信息
        MoiraiProduct moiraiProduct = moiraiProductMapper.selectByPrimaryKey(product.getProductId());
        if (null == moiraiProduct) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_NOPRODUCT);
        } else {
            //判断开通人员类型
            MoiraiUserCondition moiraiUserCondition = new MoiraiUserCondition();
            moiraiUserCondition.setUserAccount(product.getCreater());
            MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(product.getCreater());
            if (null == moiraiUser || !moiraiUser.getOrgId().equals(product.getOrgId())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            switch (moiraiProduct.getProductOpentype()) {
                case "1":
                    if ("0".equals(moiraiUser.getUserCreatetype())) {
                        break;
                    } else {
                        throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_NOOPENAUTH);
                    }
                case "2":
                    if ("0".equals(moiraiUser.getUserCreatetype()) && "Y".equals(moiraiUser.getIsTenantAccount())) {
                        break;
                    } else {
                        throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_NOOPENAUTH);
                    }
                default:
                    break;
            }
            //产品可继承
            if ("1".equals(moiraiProduct.getProductMark())) {
                List<MoiraiOrg> moiraiOrgList = new ArrayList<>();
                this.getChildrenList(product.getOrgId(), moiraiOrgList);
                moiraiOrgList.forEach(moiraiOrg -> orgIdList.add(moiraiOrg.getOrgId()));
            }
        }
        return orgIdList;
    }

    /**
     * 获取当前orgId下的所有子机构
     *
     * @param orgId
     * @param moiraiOrgList
     */
    private void getChildrenList(Long orgId, List<MoiraiOrg> moiraiOrgList) {
        List<MoiraiOrg> orgChildren = this.getOrgChildren(orgId);
        moiraiOrgList.addAll(orgChildren);
        if (null != orgChildren && orgChildren.size() > 0) {
            for (MoiraiOrg moirai : orgChildren) {
                this.getChildrenList(moirai.getOrgId(), moiraiOrgList);
            }
        }
    }

    @Override
    public void openOrgProduct(MoiraiOrg moiraiOrg) {
        Long orgId = moiraiOrg.getOrgId();
        List<MoiraiOrgProduct> products = moiraiOrg.getProducts();
        MoiraiOrgProduct biProduct = this.addIncidentalProduct(moiraiOrg);
        if (biProduct != null) {
            products.add(biProduct);
        }
        for (MoiraiOrgProduct product : products) {
            product.setCreater(moiraiOrg.getCreater());
            product.setTenantId(moiraiOrg.getTenantId());
            product.setOrgId(orgId);
            //判断产品继承关系和开通类型限制
            List<Long> orgIdList = this.checkProductProperty(product);
            //该机构的下级机构
            for (Long orgIds : orgIdList) {
                product.setOrgId(orgIds);
                if (product.getOpenType() == null) {
                    product.setOpenType(1L);
                }
                int resultPro = this.addOrgProduct(product);
            }
        }
        this.deleteCacheOrg(orgId, null);
    }

    @Override
    public int addOrgProduct(MoiraiOrgProduct moiraiOrgProduct) {
        MoiraiProduct moiraiProduct = moiraiProductMapper.selectByPrimaryKey(moiraiOrgProduct.getProductId());
        if (moiraiProduct == null) {
            logger.error("要开通产品:{},不存在", moiraiOrgProduct.getProductId());
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        String productType = moiraiProduct.getProductType();
        Long productId = moiraiProduct.getProductId();
        Long orgId = moiraiOrgProduct.getOrgId();
        List<MoiraiOrgProduct> moiraiProducts = moiraiOrgProductMapper.selectOrgProducts(orgId, null);
        logger.info("机构已开通产品" + moiraiProducts);
        moiraiOrgProduct.setProductType(Long.valueOf(productType));
        boolean openFlag = true;
        for (MoiraiOrgProduct product : moiraiProducts) {
            Long hasProductId = product.getProductId();
            Long belongProduct = product.getBelongProduct();
            if ((Constants.DEFAULT_ZERO.equals(productType) && productId.equals(belongProduct)) || hasProductId.equals(productId)) {
                moiraiOrgProductMapper.deleteOrgProduct(hasProductId, orgId);
            }
            if (Constants.DEFAULT_ONE.equals(productType) && moiraiProduct.getBelongProduct().equals(hasProductId)) {
                openFlag = false;
            }
        }
        int i = 0;
        if (openFlag) {
            Long id = seqnumFeignClient.getNum(Constants.MOIRAI_ORG_PRODUCT);
            Long nowMinuteTime = DateTimeUtils.nowTimeLong();
            if (moiraiOrgProduct.getQdBm() == null) {
                MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(moiraiOrgProduct.getTenantId());
                moiraiOrgProduct.setQdBm(moiraiTenant.getQdBm());
            }
            moiraiOrgProduct.setOpId(id);
            moiraiOrgProduct.setCreateTime(nowMinuteTime);
            moiraiOrgProduct.setUseBegintime(nowMinuteTime);
            i = moiraiOrgProductMapper.insertSelective(moiraiOrgProduct);
        }
        return i;
    }

    @Override
    public List<MoiraiOrgProduct> getOrgProducts(Long orgId) {

        return moiraiOrgProductMapper.selectOrgProducts(orgId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult setOrgConfig(List<MoiraiOrgConfig> moiraiOrgConfigList) {

        if (moiraiOrgConfigList == null || moiraiOrgConfigList.size() == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDCONFIGERROR);
        }
        int count = 0;
        //验证必填字段
        for (MoiraiOrgConfig moiraiOrgConfig : moiraiOrgConfigList) {
            Long orgId = moiraiOrgConfig.getOrgId();
            String item = moiraiOrgConfig.getItem();
            String value = moiraiOrgConfig.getValue();
            Long tenantId = moiraiOrgConfig.getTenantId();
            if (StrUtils.isEmpty(item) || orgId == null || tenantId == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDCONFIGERROR);
            }
        }

        for (MoiraiOrgConfig moiraiOrgConfig : moiraiOrgConfigList) {

            MoiraiOrgConfig mocResult = moiraiOrgConfigMapper.selectByOrgItem(moiraiOrgConfig);
            moiraiOrgConfig.setOperaTime(DateTimeUtils.nowTimeLong().toString());
            if (mocResult != null) {
                moiraiOrgConfig.setOcId(mocResult.getOcId());
                int result = moiraiOrgConfigMapper.updateByPrimaryKeySelective(moiraiOrgConfig);
                count = count + result;
            } else {
                Long id = seqnumFeignClient.getNum(Constants.MOIRAI_ORG_CONFIG);
                moiraiOrgConfig.setOcId(id);
                int result = moiraiOrgConfigMapper.insertSelective(moiraiOrgConfig);
                count = count + result;
            }
        }

        if (count != moiraiOrgConfigList.size()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDCONFIGERROR);
        }
        return new BWJsonResult(count);
    }

    @Override
    public MoiraiOrgConfig getOrgConfig(MoiraiOrgConfig moiraiOrgConfig) {

        return moiraiOrgConfigMapper.selectByOrgItem(moiraiOrgConfig);
    }

    @Override
    public List<MoiraiOrgConfig> getAllOrgConfig(MoiraiOrgConfig moiraiOrgConfig) {

        return moiraiOrgConfigMapper.selectByOrgId(moiraiOrgConfig.getOrgId());
    }

    public List<MoiraiOrg> combineOrgList(List<MoiraiOrg> tenantOrgList, Long topOrgIde) {
        MoiraiOrg orgTree = combineOrgTree(tenantOrgList, topOrgIde);
        List<MoiraiOrg> orgList = new ArrayList<MoiraiOrg>();
        orgList.add(orgTree);
        if (orgTree.getChildren() != null) {
            List<MoiraiOrg> childrenList = orgTree.getChildren();
            orgTree.setChildren(null);
            combineOrgListChildren(orgList, childrenList);
        }

        return orgList;
    }

    private void combineOrgListChildren(List<MoiraiOrg> orgList, List<MoiraiOrg> childrenList) {

        for (MoiraiOrg childrenOrg : childrenList) {
            orgList.add(childrenOrg);
            List<MoiraiOrg> chilidren = childrenOrg.getChildren();
            if (chilidren != null) {
                childrenOrg.setChildren(null);
                combineOrgListChildren(orgList, chilidren);
            }
        }
    }

    public MoiraiOrg combineOrgTree(List<MoiraiOrg> tenantOrgList, Long topOrgId) {
        Map<Long, List<MoiraiOrg>> tenantOrgMap = new HashMap<Long, List<MoiraiOrg>>();
        MoiraiOrg topOrg = null;
        boolean isAllTree = true;
        boolean isFindTop = false;
        if (topOrgId != null) {
            isAllTree = false;
        } else {
            topOrgId = 0L;
        }

        for (int i = 0; i < tenantOrgList.size(); i++) {
            MoiraiOrg tenantOrgItem = tenantOrgList.get(i);
            Long parentId = tenantOrgItem.getParentOrg();

            if (tenantOrgItem.getProducts() != null && tenantOrgItem.getProducts().size() == 0) {
                tenantOrgItem.setProducts(null);
            }

            /**找出顶级组织机构**/
            if (!isFindTop) {
                if (isAllTree && parentId != null) {
                    if (parentId.equals(topOrgId)) {
                        topOrg = tenantOrgItem;
                        isFindTop = true;
                    }
                } else {
                    Long orgItemId = tenantOrgItem.getOrgId();
                    if (orgItemId != null && orgItemId.equals(topOrgId)) {
                        topOrg = tenantOrgItem;
                        isFindTop = true;
                    }
                }

            }

            /**轮询组织机构列表**/
            List<MoiraiOrg> parentList = tenantOrgMap.get(parentId);
            if (parentList != null) {
                parentList.add(tenantOrgItem);
            } else {
                List<MoiraiOrg> parentListitem = new ArrayList<MoiraiOrg>();
                parentListitem.add(tenantOrgItem);
                tenantOrgMap.put(parentId, parentListitem);
            }
        }

        if (topOrg == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_QUERYERROR);
        }

        topOrg.setChildren(tenantOrgMap.get(topOrg.getOrgId()));
        combineTreeChildren(topOrg, tenantOrgMap);

        return topOrg;
    }

    public void combineTreeChildren(MoiraiOrg topOrg, Map<Long, List<MoiraiOrg>> tenantOrgMap) {

        List<MoiraiOrg> topChildren = topOrg.getChildren();
        if (topChildren != null && topChildren.size() > 0) {
            for (int i = 0; i < topChildren.size(); i++) {
                MoiraiOrg childrenItem = topChildren.get(i);
                Long childrenItemId = childrenItem.getOrgId();
                List<MoiraiOrg> childrenOrg = tenantOrgMap.get(childrenItemId);
                if (childrenOrg != null && childrenOrg.size() > 0) {
                    childrenItem.setChildren(childrenOrg);
                    combineTreeChildren(childrenItem, tenantOrgMap);
                }

            }
        }
    }

    /**
     * 添加机构第一页完成点击下一页时校验当前页参数：移除接入代码
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public void validateParam(MoiraiOrg moiraiOrg) {
        /**共同字段**/
        String orgName = moiraiOrg.getOrgName();
        String orgCode = moiraiOrg.getOrgCode();
        if (RegularExpUtils.validName(orgName) || orgName.length() > 150) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ORGNAME_ERROR);
        }
        if (!RegularExpUtils.validOrgCode(orgCode) || orgCode.length() > 36) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ORGCODE_ERROR);
        }
        /**验证组织机构编码,租户唯一**/
        MoiraiOrg org = this.validOrgCode(moiraiOrg);
        if (org != null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_CODE_DUPLICATE);
        }
        /**纳税主体**/
        if (moiraiOrg.getOrgType() == 1) {
            String taxCode = moiraiOrg.getTaxCode();
            Map<String, String> map = new HashMap<>();
            map.put("taxCode", taxCode);
            JSONObject jsonObject = moiraiSysService.commonMethod(map, 3);
            if (null != jsonObject && !"0".equals(jsonObject.get("code"))) {
                throw new MoiraiException(jsonObject.getString("code"), jsonObject.getString("message"));
            }
            String taxProv = moiraiOrg.getTaxProv();
            String taxQuali = moiraiOrg.getTaxQuali();
            String exportQualify = moiraiOrg.getExportQualify();
            String belongIndustry = moiraiOrg.getBelongIndustry();
            String djCompanyType = moiraiOrg.getDjCompanyType();
            if (!RegularExpUtils.validCode(taxCode) || taxCode.length() > 20 || taxCode.length() < 15) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TAX_ERROR);
            }
            MoiraiOrg oldMoiraiOrg = this.queryByTaxCode(taxCode);
            if (oldMoiraiOrg != null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TAXCODE_ERROR);
            }
            if (StrUtils.isEmpty(taxProv)) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TAXPROV_ERROR);
            }
            if (StrUtils.isEmpty(taxQuali)) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TAXQUALI_ERROR);
            }
            if (StrUtils.isEmpty(belongIndustry)) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_BELONGINDUSTRY_ERROR);
            }
            if (StrUtils.isEmpty(djCompanyType)) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_DJCOMPANYTYPE_ERROR);
            }
            if (StrUtils.isEmpty(exportQualify)) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_EXPORTQUALIFY_ERROR);
            }
        }
    }

    /**
     * @param moiraiOrg
     * @return
     */
    @Override
    public boolean cheackTaxCodeStatus(MoiraiOrg moiraiOrg) {
        if (StringUtils.isNotEmpty(moiraiOrg.getTaxCode())) {
            String taxCode = moiraiOrg.getTaxCode();
            MoiraiOrg oldMoiraiOrg = this.queryByTaxCode(taxCode);
            if (oldMoiraiOrg != null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TAXCODE_ERROR);
            }
            //查询百望云老版数据库
            Map<String, String> map = new HashMap<>();
            map.put("taxCode", moiraiOrg.getTaxCode());
            JSONObject jsonObject = moiraiSysService.commonMethod(map, 3);
            if (null != jsonObject && !"0".equals(jsonObject.get("code"))) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_OLD_USER_CENTER_MORE;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
                throw new MoiraiException(jsonObject.getString("code"), jsonObject.getString("message"));
            }
            //删除僵尸租户
            MoiraiOrg zombieOrg = moiraiOrgZomMapper.selectByTaxCode(taxCode);
            if (zombieOrg != null) {
                moiraiOrgZomMapper.deleteLogic(zombieOrg);
            }
        }
        return true;
    }

    /**
     * 删除组织机构时，判断删除相关信息
     *
     * @param moiraiOrg
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrgRel(MoiraiOrg moiraiOrg) {
        Long orgId = moiraiOrg.getOrgId();
        //is have children
        List<MoiraiOrg> orgChildren = this.getOrgChildren(orgId);
        if (orgChildren != null && orgChildren.size() > 0) {
            throw new MoiraiException("2004", "机构存在下级机构");
        }
        //is have role
        MoiraiRole moiraiRole = new MoiraiRole();
        moiraiRole.setOrgId(orgId);
        List<MoiraiRole> moiraiRoles = moiraiRoleMapper.selectByBean(moiraiRole);
        if (moiraiRoles != null && moiraiRoles.size() > 0) {
            throw new MoiraiException("2004", "机构存在角色");
        }
        //is have user
        MoiraiUserCondition moiraiUserCondition = new MoiraiUserCondition();
        moiraiUserCondition.setOrgId(orgId);
        moiraiUserCondition.setUserType("B");
        List<MoiraiUser> result = moiraiUserMapper.findUserByCondition(moiraiUserCondition);
        if (!result.isEmpty()) {
            if (result.size() > 1 || !Constants.DEFAULT_ZERO.equals(result.get(0).getUserCreatetype())) {
                throw new MoiraiException("2004", "机构存在用户");
            }
        }
        //查询开通的CP产品
        List<MoiraiOrgProduct> products = moiraiOrgProductMapper.selectCPProductsByOrgId(orgId);
        moiraiOrg.setProducts(products);
        moiraiOrgDao.deleteOrg(orgId);
        //删除授权
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andAuthOrgEqualTo(orgId);
        moiraiUserAuthzMapper.deleteByExample(example);
        this.deleteOrgProducts(orgId);
        if (result != null && result.size() > 0 && result.get(0) != null) {
            logger.info("删除绑定用户，userId {}", result.get(0).getUserId());
            moiraiUserMemberService.unBindTenant(result.get(0).getUserId());
            moiraiUserMapper.deleteByPrimaryKey(result.get(0).getUserId());
            int i = moiraiUserinfoMapper.deleteByUserId(result.get(0).getUserId());
            context.publishEvent(new UserEvent(result.get(0)));
        }
        this.deleteCacheOrg(orgId, null);
        context.publishEvent(new OrgEvent(moiraiOrg));
    }

    /**
     * 进入销项时判断机构信息是否完整
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult<MoiraiOrg> checkOrgInfo(MoiraiOrg moiraiOrg) {
        //查询机构信息
        MoiraiOrg moiraiOrgInfo = null;
        try {
            moiraiOrgInfo = this.selectByOrgId(moiraiOrg.getOrgId());
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_ORG_QUERYERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_QUERYERROR);
        }
        if (moiraiOrgInfo == null) {
            logger.info("==========查询机构不存在==========");
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        BWJsonResult<MoiraiOrg> bwJsonResult = new BWJsonResult<>(moiraiOrgInfo);
        //判断所需参数是否完整
        if (StringUtils.isBlank(moiraiOrgInfo.getBusinessAddress()) || StringUtils.isBlank(moiraiOrgInfo.getTelphone())) {
            logger.info("==========所需信息不完整==========");
            bwJsonResult.setMessage("false");
        } else {
            bwJsonResult.setMessage("true");
        }
        return bwJsonResult;
    }

    /**
     * 验证纳税主体第二页信息
     *
     * @param moiraiOrg
     */
    @Override
    public void validatetaxOrg(MoiraiOrg moiraiOrg) {
        if (moiraiOrg.getProducts() != null && moiraiOrg.getProducts().size() > 0) {
            List<Long> productIdList = new ArrayList<>();
            moiraiOrg.getProducts().forEach(product -> productIdList.add(product.getProductId()));
            /**销项产品必填字段**/
            if (productIdList.contains(0L)) {
                String bankDeposit = moiraiOrg.getBankDeposit();
                String accountNumber = moiraiOrg.getAccountNumber();
                String telphone = moiraiOrg.getTelphone();
                String businessAddress = moiraiOrg.getBusinessAddress();
//                String tgType = moiraiOrg.getTgType();
//                String deviceType = moiraiOrg.getDeviceType();
//                String deviceCode = moiraiOrg.getDeviceCode();

                /**非必填**/
                if (StringUtils.isNotEmpty(bankDeposit)) {
                    if (RegularExpUtils.validName(bankDeposit) || bankDeposit.length() > 80) {
                        throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_BANKDEPOSIT_ERROR);
                    }
                }
                /**非必填**/
                if (StringUtils.isNotEmpty(accountNumber)) {
                    if (RegularExpUtils.validName(accountNumber) || accountNumber.length() > 50) {
                        throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ACCOUNTNUMBER_ERROR);
                    }
                }
                if (StringUtils.isNotEmpty(bankDeposit) && StringUtils.isNotEmpty(accountNumber) && bankDeposit.length() + accountNumber.length() > 100) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_LENGTH_ERROR);
                }
                if (RegularExpUtils.validName(businessAddress) || businessAddress.length() > 87) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_BUSINESSADDRESS_ERROR);
                }
                if (!(RegularExpUtils.checkMobile(telphone) || RegularExpUtils.isFixedPhone(telphone))) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TELPHONE_ERROR);
                }
//                if (StrUtils.isEmpty(tgType)) {
//                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TGTYPE_ERROR);
//                }
//                if (StrUtils.isEmpty(deviceType)) {
//                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_DEVICETYPE_ERROR);
//                }
                //设备类型是盘用户，设备编号不能为空
//                if (Constants.MOIRAI_DEVICE_PANEL.equals(deviceType) && StrUtils.isEmpty(deviceCode)) {
//                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_DEVICE_CODE);
//                }
            }
        }
    }

    /**
     * 注册和更新时验证纳税信息
     * <p>注册时存在开通销项，但是没有销方信息的情况</p>
     *
     * @param moiraiOrg
     */
    @Override
    public void validateTaxmsg(MoiraiOrg moiraiOrg) {
        String bankDeposit = moiraiOrg.getBankDeposit();
        String accountNumber = moiraiOrg.getAccountNumber();
        String telphone = moiraiOrg.getTelphone();
        String businessAddress = moiraiOrg.getBusinessAddress();
        if (StringUtils.isNotEmpty(bankDeposit)) {
            if (RegularExpUtils.validName(bankDeposit) || bankDeposit.length() > 80) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_BANKDEPOSIT_ERROR);
            }
        }
        if (StringUtils.isNotEmpty(accountNumber)) {
            if (RegularExpUtils.validName(accountNumber) || accountNumber.length() > 50) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ACCOUNTNUMBER_ERROR);
            }
        }
        if (StringUtils.isNotEmpty(bankDeposit) && StringUtils.isNotEmpty(accountNumber) && bankDeposit.length() + accountNumber.length() > 100) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_LENGTH_ERROR);
        }
        if (StringUtils.isNotEmpty(businessAddress)) {
            if (RegularExpUtils.validName(businessAddress) || businessAddress.length() > 87) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_BUSINESSADDRESS_ERROR);
            }
        }
        if (StringUtils.isNotEmpty(telphone)) {
            if (!(RegularExpUtils.checkMobile(telphone) || RegularExpUtils.isFixedPhone(telphone))) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_TELPHONE_ERROR);
            }
        }
    }

    /**
     * 验证机构名称和代码
     *
     * @param moiraiOrg
     */
    @Override
    public void validateOrgNameAndCode(MoiraiOrg moiraiOrg) {
        String orgCode = moiraiOrg.getOrgCode();
        String orgName = moiraiOrg.getOrgName();
        if (RegularExpUtils.validName(orgName) || orgName.length() > 150) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ORGNAME_ERROR);
        }
        if (!RegularExpUtils.validOrgCode(orgCode) || orgCode.length() > 36) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ORGCODE_ERROR);
        }
        /**验证组织机构编码,租户唯一**/
        MoiraiOrg org = this.validOrgCode(moiraiOrg);
        if (org != null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_CODE_DUPLICATE);
        }
    }

    /**
     * 根据条件查询税号相关信息  ==>开放平台定制
     *
     * @param map
     * @return
     */
    @Override
    public BWJsonResult<List<Map<String, Object>>> findTaxCodeListInfo(Map<String, Object> map) {
        List<Map<String, Object>> resutlMap = null;
        try {
            resutlMap = moiraiOrgMapper.findTaxCodeListInfo(map);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_GET_OPENPLATE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR.getCode(), e.getMessage());
        }
        return new BWJsonResult(resutlMap);
    }

    @Override
    public void syncOrgs(Long tenantId) {
        //查询租户下所有开通CP产品的机构
        //List<MoiraiOrg> orgs = moiraiOrgMapper.selectByTenantId(tenantId, 30000L);
        context.publishEvent(new OrgEvent(tenantId));
    }

    @Override
    public MoiraiTenant checkAddOrgParam(MoiraiOrg moiraiOrg) {
        /**验证组织机构父级存在**/
        MoiraiOrg parentMoiraiOrg = this.selectByOrgId(moiraiOrg.getParentOrg());
        if (parentMoiraiOrg == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_PARENTNOTEXT);
        }

        MoiraiTenant tenantInfo = moiraiTenantMapper.selectByPrimaryKey(moiraiOrg.getTenantId());
        if (null == tenantInfo) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_IS_NULL);
        }
        String isAuthe = tenantInfo.getIsAuthe();
        String isFetch = tenantInfo.getIsFetch();
        String isProof = tenantInfo.getIsProof();
        if (StrUtils.isEmpty(isProof) || StrUtils.isEmpty(isFetch) || StrUtils.isEmpty(isAuthe)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_SWITCH_STATE_NOT_NULL);
        }
        if ("0".equals(isAuthe)) {
            moiraiOrg.setIsAuthe(isAuthe);
        }
        if ("0".equals(isFetch)) {
            moiraiOrg.setIsFetch(isFetch);
        }
        if ("0".equals(isProof)) {
            moiraiOrg.setIsProof(isProof);
        }

        /**共同字段**/
        this.validateOrgNameAndCode(moiraiOrg);

        //默认字段
        if (StringUtils.isBlank(moiraiOrg.getJrdCode())) {
            moiraiOrg.setJrdCode(Constants.JRD_CODE);
        }

        if (moiraiOrg.getOrgType() == 2) {
            /**继承父类属性*/
            moiraiOrg.setDeviceType(parentMoiraiOrg.getDeviceType());
            moiraiOrg.setTgType(parentMoiraiOrg.getTgType());
            moiraiOrg.setJrdCode(parentMoiraiOrg.getJrdCode());
            moiraiOrg.setTaxQuali(parentMoiraiOrg.getTaxQuali());
            moiraiOrg.setNeedSeal(parentMoiraiOrg.getNeedSeal());
            moiraiOrg.setSealId(parentMoiraiOrg.getSealId());
            moiraiOrg.setIsFetch("0");
        }
        return tenantInfo;
    }

    /**
     * 开通bi 24
     *
     * @param moiraiOrg
     * @return
     */
    public MoiraiOrgProduct addIncidentalProduct(MoiraiOrg moiraiOrg) {
        //加入开关--控制bi产品的开通
        if (!isOpenBI) {
            return null;
        }
        if (StringUtils.isEmpty(biOpenConditon)) {
            return null;
        }
        List<MoiraiOrgProduct> products = moiraiOrg.getProducts();
        List<Long> productIds = new ArrayList<>();
        products.forEach(item -> productIds.add(item.getProductId()));
        if (productIds.contains(Constants.BI_PRODUCT)) {
            return null;
        }
        String[] isOpen = biOpenConditon.split(",");
        List<String> strings = Arrays.asList(isOpen);
        MoiraiOrgProduct bi = new MoiraiOrgProduct();
        if (strings != null && !strings.isEmpty()) {
            strings.forEach(openId -> {
                if (productIds.contains(Long.valueOf(openId))) {
                    bi.setCreateTime(DateTimeUtils.nowTimeLong());
                    bi.setProductId(Constants.BI_PRODUCT);
                    bi.setTenantId(moiraiOrg.getTenantId());
                    bi.setOrgId(moiraiOrg.getOrgId());
                }
            });
        }
        return bi.getProductId() == null ? null : bi;
    }

    /**
     * <B>方法名称：</B>获取用户授权<BR>
     * <B>概要说明：</B>销项/进项/供应链页面授权机构下拉<BR>
     *
     * @return
     * @since 2019年12月10日
     */
    public List<MoiraiUserAuthz> getUserAuthBycondition(Long userId, Long resourceId) {
        Long cacheRoleId = null;
        try {
            HttpServletRequest request = RequestContext.getRequest();
            String bwtoken = HttpInfoUtils.getToken(Constants.BWTOKEN, request);

            Object token = redisTemplate.opsForValue().get(Constants.REDIS_ACCESSTOKEN + bwtoken);
            HashMap hashMap = JacksonUtil.jsonStrToObject(token.toString(), HashMap.class);
            Long cacheUserId = Long.valueOf(hashMap.get("userId").toString());
            if (userId == null) {
                userId = cacheUserId;
            }
            Object objUser = redisTemplate.opsForValue().get(Constants.REDIS_USER + cacheUserId);
            if (objUser != null) {
                MoiraiUser moiraiUser = JacksonUtil.jsonStrToObject(objUser.toString(), MoiraiUser.class);
                cacheRoleId = moiraiUser.getRoleId();
                logger.info("缓存获取信息：userId:{}, roleId:{}", cacheUserId, cacheRoleId);
            }
        } catch (Exception e) {
            if (userId == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
        }
        //兼容roleId和resourceId
        if (resourceId != null) {
            //判断用户所有授权角色,挑出包含该resourceId的授权.
            return moiraiUserAuthzMapper.getUserAuthByResourceId(userId, resourceId);
        } else {
            return moiraiUserAuthzMapper.getUserAuthByUserId(userId, cacheRoleId);
        }
    }

    /**
     * 赋权管理员产品的默认角色
     *
     * @param products
     * @param user
     * @return
     */
    @Override
    public int gainAuthByProductId(List<Long> products, MoiraiUser user) {
        List<MoiraiUserAuthz> authzs = new ArrayList<>();
        MoiraiRole role = new MoiraiRole();
        role.setTenantAutomatic(Constants.flag_Y);
        role.setProducts(products);
        List<MoiraiRole> moiraiRoles = moiraiRoleMapper.selectByBean(role);
        if (!moiraiRoles.isEmpty()) {
            for (MoiraiRole role1 : moiraiRoles) {
                MoiraiUserAuthz authz = new MoiraiUserAuthz();
                authz.setUserOrg(user.getOrgId());
                authz.setTenantId(user.getTenantId());
                authz.setUserId(user.getUserId());
                authz.setAuthOrg(user.getOrgId());
                authz.setCreater(user.getUserAccount());
                authz.setCreateTime(DateTimeUtils.nowTimeLong());
                authz.setRoleId(role1.getRoleId());
                authz.setUorId(seqnumFeignClient.getNum(Constants.MOIRAI_USER_AUTHZ));
                authzs.add(authz);
            }
        }
        int i = 0;
        if (!authzs.isEmpty()) {
            MoiraiUserDataScope dataScope = new MoiraiUserDataScope();
            dataScope.setUserId(user.getUserId());
            dataScope.setCreateTime(user.getCreateTime());
            dataScope.setCreater(user.getCreater());
            dataScope.setScope("0");
            Long id = seqnumFeignClient.getNum(Constants.MOIRAI_USER_DATA_SCOPE);
            dataScope.setUdsId(id);
            moiraiUserDataScopeMapper.insertSelective(dataScope);
            i = moiraiUserAuthzMapper.batchInsert(authzs);
        }
        return i;
    }

    /**
     * 查询组织机构信息
     */
    @Override
    public BWJsonResult<MoiraiOrg> getChannelOrgByCondition(MoiraiOrgCondition moiraiOrg) {
        if (moiraiOrg.getTaxCodes() != null) {
            moiraiOrg.setTaxCodes(moiraiOrg.getTaxCodes().stream().distinct().collect(Collectors.toList()));
            if (moiraiOrg.getTaxCodes().size() > 5000) {
                moiraiOrg.setTaxCodes(moiraiOrg.getTaxCodes().subList(0, 5000));
            }
        }
        if (moiraiOrg.getQdBms() != null) {
            moiraiOrg.setQdBms(moiraiOrg.getQdBms().stream().distinct().collect(Collectors.toList()));
            if (moiraiOrg.getQdBms().size() > 5000) {
                moiraiOrg.setQdBms(moiraiOrg.getQdBms().subList(0, 5000));
            }
        }
        PageHelper.startPage(moiraiOrg.getPageNo(), moiraiOrg.getPageSize());
        List<MoiraiOrg> orgList = moiraiOrgMapper.queryChannelOrg(moiraiOrg);
        PageInfo<MoiraiOrg> pageInfo = new PageInfo<>(orgList);
        return new BWJsonResult<>(orgList, (int) pageInfo.getTotal());
    }
}
