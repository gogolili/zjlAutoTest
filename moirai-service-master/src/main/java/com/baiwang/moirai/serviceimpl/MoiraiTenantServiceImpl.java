/*
 * @项目名称: Moirai
 * @文件名称: MoiraiTenantServiceImpl.java
 * @Date: 17-11-4 下午2:59
 * @author Lance cui
 *
 */

package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiChannelTenantMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiOrgZomMapper;
import com.baiwang.moirai.mapper.MoiraiProductMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiTenantZomMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.MoiraiUserMemberMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantListCondition;
import com.baiwang.moirai.model.tenant.MoiraiTenantListVO;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.user.MoiraiUserMember;
import com.baiwang.moirai.model.user.MoiraiUserMemberCondition;
import com.baiwang.moirai.service.MoiraiChannelTenantService;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiTenantService;
import com.baiwang.moirai.service.MoiraiUserMemberService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.JacksonUtil;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.baiwang.moirai.enumutil.MoiraiErrorEnum.MOIRAI_DB_NULL;

@Service
public class MoiraiTenantServiceImpl implements MoiraiTenantService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private MoiraiTenantZomMapper moiraiTenantZomMapper;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiOrgZomMapper moiraiOrgZomMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private MoiraiProductMapper moiraiProductMapper;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private MoiraiUserMemberService moiraiUserMemberService;

    @Autowired
    private MoiraiUserMemberMapper moiraiUserMemberMapper;

    @Autowired
    private MoiraiChannelTenantService moiraiChannelTenantService;

    @Autowired
    private MoiraiChannelTenantMapper moiraiChannelTenantMapper;

    /**
     * 查询租户的状态 0-僵尸租户 1-正式租户-名称存在 100-无此租户 101-税号已存在 4-编码已存在
     */
    @Override
    public int tenantRegisterStatus(String tenantName, String taxCode) {
        //判断正式租户
//        if (!StrUtils.isEmpty(tenantName)) {
//            List<MoiraiOrg> tenantOrgName = moiraiOrgService.queryByOrgName(tenantName);
//            if (tenantOrgName != null && tenantOrgName.size() > 0) {
//                return 1;
//            }
//        }
        if (!StrUtils.isEmpty(taxCode)) {
            MoiraiOrg moiraiOrg = new MoiraiOrg();
            moiraiOrg.setTaxCode(taxCode);
            MoiraiOrg tenantOrg = moiraiOrgMapper.selectOneOrg(moiraiOrg);
            if (null != tenantOrg) {
                return 101;
            }
        }

        //查询百望云老版数据库
        Map<String, String> map = new HashMap<>();
//        map.put("orgName", tenantName);
        map.put("taxCode", taxCode);
        JSONObject jsonObject = moiraiSysService.commonMethod(map, 3);
        if (null != jsonObject && !"0".equals(jsonObject.get("code"))) {
//            String message = jsonObject.getString("message");
//            if (message.contains("组织名称")) {
//                return 1;
//            } else {
            return 101;
//            }
        }

        //判断僵尸租户
//        List<MoiraiOrg> zombieByName = moiraiOrgZomMapper.selectByOrgName(tenantName);
//        if (zombieByName != null && zombieByName.size() > 0) {
//            return 0;
//        }
        if (!StrUtils.isEmpty(taxCode)) {
            MoiraiOrg zombieOrg = moiraiOrgZomMapper.selectByTaxCode(taxCode);
            if (zombieOrg != null) {
                return 0;
            }
        }
        return 100;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteZombieTenant(String tenantName, String taxCode) {

        MoiraiOrg zombieOrg = null;
        List<MoiraiOrg> zombieOrg1 = new ArrayList<>();
        zombieOrg1 = moiraiOrgZomMapper.selectByOrgName(tenantName);
        if (zombieOrg1.size() == 0 && !StrUtils.isEmpty(taxCode)) {
            zombieOrg = moiraiOrgZomMapper.selectByTaxCode(taxCode);
            zombieOrg1.add(zombieOrg);
        }
        if (zombieOrg1 != null && zombieOrg1.size() > 1) {
            for (MoiraiOrg moiraiOrg : zombieOrg1) {
                moiraiOrgZomMapper.deleteLogic(moiraiOrg);
            }
            moiraiTenantZomMapper.deleteLogic(zombieOrg1.get(0).getTenantId());
            return 1;
        }
        return 0;
    }

    /**
     * 优化注册和添加租户
     *
     * @param moiraiTenant
     */
    private void commonRegTenant(MoiraiTenantVO moiraiTenant) {
        Long id = seqnumFeignClient.getNum(Constants.MOIRAI_TENANT);
        Long nowMinuteTime = DateTimeUtils.nowTimeLong();
        moiraiTenant.setTenantId(id);
        if (StringUtils.isEmpty(moiraiTenant.getTenantCode())) {
            moiraiTenant.setTenantCode("TENANT" + nowMinuteTime);
        }
        if (moiraiTenant.getRegisterTime() == null) {
            moiraiTenant.setRegisterTime(nowMinuteTime);
        }
        moiraiTenant.setCreateTime(nowMinuteTime);
        moiraiTenant.setModifyTime(nowMinuteTime);
        moiraiTenant.setTenantType("1");
        moiraiTenant.setCreater(moiraiTenant.getCreater());
        moiraiTenant.setModifyUser(moiraiTenant.getCreater());
        if (StringUtils.isNotBlank(moiraiTenant.getSystemTask())) {
            moiraiTenant.setSystemTask(moiraiTenant.getSystemTask() + id);
        }
        moiraiTenantMapper.insertTenant(moiraiTenant);

        Set<Long> qdbmSet = new HashSet<>();
        if (moiraiTenant.getQdBm() != null) {
            qdbmSet.add(moiraiTenant.getQdBm());
        }
        if (StringUtils.isNotBlank(moiraiTenant.getQdBms())) {
            String[] qdbms = moiraiTenant.getQdBms().split(",");
            for (int i = 0; i < qdbms.length; i++) {
                qdbmSet.add(Long.valueOf(qdbms[i]));
            }
        }
        if (qdbmSet.isEmpty()) {
            qdbmSet.add(1L);
        }
        moiraiChannelTenantService.addChannelTenantList(moiraiTenant.getTenantId(), new ArrayList<>(qdbmSet));
    }

    /**
     * 注册
     *
     * @param moiraiTenantOrg
     */
    private MoiraiUser regOrgAndUser(MoiraiOrg moiraiTenantOrg, MoiraiTenantVO moiraiTenant) {
        int resultOrg = 0;
        if (moiraiTenantOrg.getOrgType() == 1) {
            resultOrg = moiraiOrgMapper.addTaxOrg(moiraiTenantOrg);
        } else if (moiraiTenantOrg.getOrgType() == 2) {
            resultOrg = moiraiOrgMapper.addNoTaxOrg(moiraiTenantOrg);
        }
        if (resultOrg == 1) {
            boolean flag = true;
            if ("0".equals(moiraiTenant.getHasDefOrgProduct())) {
                flag = false;
            }
            moiraiOrgService.addOrgProducts(moiraiTenantOrg, flag, moiraiTenant.getQdBm());
        } else {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDORGERROR);
        }
        List<Long> tenantIdList = new ArrayList<>(1);
        tenantIdList.add(moiraiTenantOrg.getTenantId());
        List<String> taxCodeList = new ArrayList<>(1);
        taxCodeList.add(moiraiTenantOrg.getTaxCode());
        moiraiSysService.excecutorImport(tenantIdList, taxCodeList, null);

        //creat user
        MoiraiUser user = new MoiraiUser();
        Long id = seqnumFeignClient.getNum(Constants.MOIRAI_USER);
        user.setUserId(id);
        user.setTenantId(moiraiTenant.getTenantId());
        user.setOrgId(moiraiTenant.getOrgId());
        user.setUserEmail(moiraiTenant.getTenantEmail());
        user.setUserType("B");
        user.setType("default");
        user.setUserName("租户管理员");
        user.setHasDefAdminRole(moiraiTenant.getHasDefAdminRole());
        user.setUserAccount("admin_" + moiraiTenant.getOrgId());
        user.setUserCreatetype("0");
        user.setIsTenantAccount("Y");
        user.setCreater(moiraiTenant.getCreater());
        user.setModifyUser(moiraiTenant.getCreater());
        user.setVersion(moiraiTenant.getVersion());
        String telephone = moiraiTenant.getTelephone();
        if (StringUtils.isNotBlank(telephone)) {
            user.setTelephone(telephone);
            if (RegularExpUtils.checkMobile(user.getTelephone())) {
                user.setPhoneValidate("Y");
            }
        }
        user.setMoiraiUserAuthzs(moiraiTenant.getMoiraiUserAuthzs());
        // 赋权管理员产品的默认角色
        List<MoiraiOrgProduct> orgProductList = moiraiTenantOrg.getProducts();
        List<Long> products = new ArrayList<>();
        String context = "<br>&nbsp;&nbsp;&nbsp;&nbsp;您注册开通产品：";
        if (orgProductList != null) {
            for (MoiraiOrgProduct orgProduct : orgProductList) {
                MoiraiProduct product = moiraiProductMapper.selectByPrimaryKey(orgProduct.getProductId());
                if (product == null) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_NOPRODUCT);
                }
                products.add(orgProduct.getProductId());
                if (!orgProduct.getProductId().equals(Long.valueOf(Constants.DEFAULT_PRODUCT)) && !orgProduct.getProductId().equals(Constants.BI_PRODUCT)) {
                    context += product.getProductName() + "、";
                }
            }
            moiraiOrgService.gainAuthByProductId(products, user);
        }
        MoiraiUser moiraiUser = moiraiUserService.addUser(user);
        if (moiraiUser == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_ADD_ERROR);
        }
        if (StringUtils.isNotBlank(telephone)) {
            MoiraiUser userByTelephone = moiraiUserMapper.getCUserByTelephone(telephone);
            if (userByTelephone != null) {
                MoiraiUserCondition bindCAndB = new MoiraiUserCondition();
                bindCAndB.setUserId(userByTelephone.getUserId());
                bindCAndB.setUserAccount(moiraiUser.getUserAccount());
                bindCAndB.setUserPassword(moiraiUser.getUserPassword());
                bindCAndB.setTelephone(telephone);
                moiraiUserMemberService.bindTenant(bindCAndB);
            }
        } else {
            logger.info("注册租户绑定C端账号和B端账号失败！C端账号电话为空！");
        }
        if (Constants.MOIRAI_VERSION_V2.equals(moiraiTenant.getVersion())) {
            if (StringUtils.isBlank(moiraiUser.getUserEmail())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            context = "您的初始化帐号：" + moiraiUser.getUserAccount() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;您的初始化密码：" + moiraiUser.getUserPassword() + context;
            context = context.substring(0, context.length() - 1);
            List<String> emailList = new ArrayList<>();
            emailList.add(moiraiUser.getUserEmail());
            List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
            LazyDynaBean lazyDynaBean = new LazyDynaBean();
            lazyDynaBean.set("emails", emailList);
            lazyDynaBean.set("context", context);
            lazyDynaBean.set("userName", moiraiTenant.getTenantName());
            lazyDynaBean.set("moiraiOrg", moiraiTenantOrg);
            lazyDynaBeans.add(lazyDynaBean);
            moiraiSysService.excecutorSendEmail(lazyDynaBeans, Constants.DEFAULT_THREE);
        }
        return moiraiUser;
    }

    /**
     * 注册租户
     *
     * @param moiraiTenant
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoiraiUser regTenant(MoiraiTenantVO moiraiTenant) {
        this.commonRegTenant(moiraiTenant);
        //0:不添加默认角色，1:添加销项管理员
        MoiraiOrg moiraiTenantOrg = tenantCoverOrgInfo(moiraiTenant, 0);
        return this.regOrgAndUser(moiraiTenantOrg, moiraiTenant);
    }

    /**
     * 添加租户
     *
     * @param moiraiTenant
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoiraiUser addTenant(MoiraiTenantVO moiraiTenant) {
        this.commonRegTenant(moiraiTenant);
        //0:不添加默认角色，1:添加销项管理员
        //添加租户，纳税主体字段需要传入
        MoiraiOrg moiraiTenantOrg = tenantCoverOrgInfo(moiraiTenant, 1);
        return this.regOrgAndUser(moiraiTenantOrg, moiraiTenant);
    }

    /**
     * 租户转机构
     *
     * @param moiraiTenant
     * @param regOrAdd 0:前台注册，1:后台注册，需要完善字段
     * @return
     */
    private MoiraiOrg tenantCoverOrgInfo(MoiraiTenantVO moiraiTenant, int regOrAdd) {
        MoiraiOrg moiraiOrgTenant = new MoiraiOrg();
        Long id = seqnumFeignClient.getNum(Constants.MOIRAI_ORG);
        moiraiOrgTenant.setOrgId(id);
        moiraiTenant.setOrgId(id);
        moiraiOrgTenant.setTenantId(moiraiTenant.getTenantId());
        if (StrUtils.isEmpty(moiraiTenant.getTaxCode())) {
            //非纳税主体
            moiraiOrgTenant.setOrgType(Integer.valueOf(Constants.MOIRAI_IS_NOT_TAXER));
        } else {
            //纳税主体
            moiraiOrgTenant.setOrgType(Integer.valueOf(Constants.MOIRAI_IS_TAXER));
            moiraiOrgTenant.setTaxCode(moiraiTenant.getTaxCode());
        }
        moiraiOrgTenant.setOrgName(moiraiTenant.getTenantName());
        moiraiOrgTenant.setOrgCode(moiraiTenant.getTenantCode());
        moiraiOrgTenant.setParentOrg(0L);
        moiraiOrgTenant.setCreater(moiraiTenant.getCreater());
        moiraiOrgTenant.setCreateTime(moiraiTenant.getCreateTime());
        moiraiOrgTenant.setModifyUser(moiraiTenant.getModifyUser());
        moiraiOrgTenant.setModifyTime(moiraiTenant.getModifyTime());
        moiraiOrgTenant.setHasDefOrgProduct(moiraiTenant.getHasDefOrgProduct());
        moiraiOrgTenant.setIsProof(moiraiTenant.getIsProof());
        moiraiOrgTenant.setIsFetch(moiraiTenant.getIsFetch());
        moiraiOrgTenant.setIsAuthe(moiraiTenant.getIsAuthe());
        moiraiOrgTenant.setPerfectFlag(moiraiTenant.getPerfectFlag());
        /**以上为前端注册需要信息**/
        if (StringUtils.isBlank(moiraiTenant.getJrdCode())) {
            moiraiOrgTenant.setJrdCode(Constants.JRD_CODE);
        } else {
            moiraiOrgTenant.setJrdCode(moiraiTenant.getJrdCode());
        }
        if (regOrAdd == 1) {
            moiraiOrgTenant.setBankDeposit(moiraiTenant.getBankDeposit());
            moiraiOrgTenant.setAccountNumber(moiraiTenant.getAccountNumber());
            moiraiOrgTenant.setTelphone(moiraiTenant.getTenantPhone());
            moiraiOrgTenant.setBusinessAddress(moiraiTenant.getBusinessAddress());
            moiraiOrgTenant.setDeviceType(moiraiTenant.getDeviceType());
            moiraiOrgTenant.setTgType(moiraiTenant.getTgType());
            moiraiOrgTenant.setProducts(moiraiTenant.getProducts());
            moiraiOrgService.validateTaxmsg(moiraiOrgTenant);
            moiraiOrgTenant.setLegalName(moiraiTenant.getLegalName());
            moiraiOrgTenant.setTaxProv(moiraiTenant.getTaxProv());
            moiraiOrgTenant.setDjCompanyType(moiraiTenant.getDjCompanyType());
            moiraiOrgTenant.setBelongIndustry(moiraiTenant.getBelongIndustry());
            moiraiOrgTenant.setRegProv(moiraiTenant.getRegProv());
            moiraiOrgTenant.setRegCity(moiraiTenant.getRegCity());
            moiraiOrgTenant.setRegArea(moiraiTenant.getRegArea());
            moiraiOrgTenant.setTaxQuali(moiraiTenant.getTaxQuali());
            moiraiOrgTenant.setSelfManage(moiraiTenant.getSelfManage());
            moiraiOrgTenant.setNeedSeal(moiraiTenant.getNeedSeal());
            moiraiOrgTenant.setSealId(moiraiTenant.getSealId());
            moiraiOrgTenant.setDeviceCode(moiraiTenant.getDeviceCode());
            moiraiOrgTenant.setDeviceName(moiraiTenant.getDeviceName());
            moiraiOrgTenant.setTaxentityId(moiraiTenant.getTaxentityId());

            //新加
            moiraiOrgTenant.setRegisterAddress(moiraiTenant.getRegisterAddress());
            moiraiOrgTenant.setExportQualify(moiraiTenant.getExportQualify());
            moiraiOrgTenant.setContactNumber(moiraiTenant.getContactNumber());
            moiraiOrgTenant.setAddressee(moiraiTenant.getAddressee());
            moiraiOrgTenant.setMoilingAddress(moiraiTenant.getMoilingAddress());
            //税控盒子必填字段
            if ("3".equals(moiraiTenant.getDeviceType())) {
                if (StringUtils.isEmpty(moiraiTenant.getDeviceSn()) || StringUtils.isEmpty(moiraiTenant.getDevicePassword())) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_DEVICE_ERROR);
                }
                moiraiOrgTenant.setDeviceSn(moiraiTenant.getDeviceSn());
                moiraiOrgTenant.setDevicePassword(moiraiTenant.getDevicePassword());
                moiraiOrgTenant.setDeviceKey(moiraiTenant.getDeviceKey());
            }
            moiraiOrgTenant.setContractNum(moiraiTenant.getContractNum());
        }
        return moiraiOrgTenant;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeTenantInfo(MoiraiTenantVO moiraiTenant) {
        Long nowMinuteTime = DateTimeUtils.nowTimeLong();

        moiraiTenant.setModifyTime(nowMinuteTime);
        //更新租户
        logger.info(moiraiTenant.toString());
        MoiraiOrg moiraiOrgTenant = coverUptateOrgInfo(moiraiTenant);
        moiraiTenantMapper.updateTenant(moiraiTenant);
        // 租户邮箱不为空 且邮箱有效 更新租户管理员邮箱信息
        if (RegularExpUtils.checkEmail(moiraiTenant.getTenantEmail())) {
            MoiraiUserCondition queryUser = new MoiraiUserCondition();
            queryUser.setTenantId(moiraiTenant.getTenantId());
            queryUser.setOrgId(moiraiTenant.getOrgId());
            queryUser.setUserCreatetype(Constants.DEFAULT_ZERO);
            List<MoiraiUser> userList = moiraiUserMapper.findUserByCondition(queryUser);
            if (!userList.isEmpty()) {
                MoiraiUser update = new MoiraiUser();
                update.setUserId(userList.get(0).getUserId());
                update.setUserEmail(moiraiTenant.getTenantEmail());
                update.setEmailValidate(Constants.flag_Y);
                moiraiUserMapper.updateByPrimaryKeySelective(update);
            }
        }

        // 更新租户渠道编码
        if (StringUtils.isNotBlank(moiraiTenant.getQdBms())) {
            MoiraiChannelTenant query = new MoiraiChannelTenant();
            query.setTenantId(moiraiTenant.getTenantId());
            List<MoiraiChannelTenant> exQdtList = moiraiChannelTenantMapper.queryList(query);
            List<String> qdbms = Arrays.asList(moiraiTenant.getQdBms().split(","));
            for (int i = 0; i < exQdtList.size(); i++) {
                if (!qdbms.contains(exQdtList.get(i).getQdBm() + "")) {
                    moiraiChannelTenantMapper.deleteByPrimaryKey(exQdtList.get(i).getChannelTenantId());
                }
            }
            List<MoiraiChannelTenant> addQdList = new ArrayList<>();
            for (int i = 0; i < qdbms.size(); i++) {
                boolean has = false;
                for (int j = 0; j < exQdtList.size(); j++) {
                    if (qdbms.get(i).equals(exQdtList.get(j).getQdBm() + "")) {
                        has = true;
                    }
                }
                if (!has) {
                    MoiraiChannelTenant addQdbm = new MoiraiChannelTenant();
                    addQdbm.setChannelTenantId(seqnumFeignClient.getNum(Constants.MOIRAI_CHANNEL_TENANT));
                    addQdbm.setQdBm(Long.valueOf(qdbms.get(i)));
                    addQdbm.setTenantId(moiraiTenant.getTenantId());
                    addQdList.add(addQdbm);
                }
            }
            if (!addQdList.isEmpty()) {
                moiraiChannelTenantMapper.insertList(addQdList);
            }
        }

        //update org
        logger.info(moiraiOrgTenant.toString());
        moiraiOrgMapper.updateOrg(moiraiOrgTenant);

        //update products
        boolean flag = true;
        if ("0".equals(moiraiTenant.getHasDefOrgProduct())) {
            flag = false;
        }
        moiraiOrgService.addOrgProducts(moiraiOrgTenant, flag, moiraiTenant.getQdBm());
        this.deleteCacheTenant(moiraiTenant.getTenantId(), moiraiTenant.getOrgId());
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTenant(MoiraiTenant moiraiTenant) {
        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(moiraiTenant.getTenantId());
        if (tenant == null) {
            throw new MoiraiException(MOIRAI_DB_NULL);
        }
        Long nowTimeLong = DateTimeUtils.nowTimeLong();
        moiraiTenant.setModifyTime(nowTimeLong);
        //更新租户
        logger.info(moiraiTenant.toString());
        int resultTen = moiraiTenantMapper.updateTenant(moiraiTenant);

        updateUserMember(moiraiTenant);
        updateAdminUserEmail(moiraiTenant.getTenantId(), moiraiTenant.getTenantEmail());
        String authe = moiraiTenant.getIsAuthe();
        String fetch = moiraiTenant.getIsFetch();
        String proof = moiraiTenant.getIsProof();
        if (StringUtils.isNotBlank(authe) || StringUtils.isNotBlank(fetch) || StringUtils.isNotBlank(proof)) {
            MoiraiOrg moiraiOrg = new MoiraiOrg();
            moiraiOrg.setTenantId(moiraiTenant.getTenantId());
            if (authe != null && !authe.equals(tenant.getIsAuthe())) {
                moiraiOrg.setIsAuthe(authe);
            }
            if (fetch != null && !fetch.equals(tenant.getIsFetch())) {
                moiraiOrg.setIsFetch(fetch);
            }
            if (proof != null && !proof.equals(tenant.getIsProof())) {
                moiraiOrg.setIsProof(proof);
            }
            if (moiraiOrg.getIsAuthe() != null || moiraiOrg.getIsFetch() != null || moiraiOrg.getIsProof() != null) {
                moiraiOrgMapper.updateOrgSwitchState(moiraiOrg);
            }
        }
        this.deleteCacheTenant(moiraiTenant.getTenantId(), null);
        return resultTen;
    }

    /**
     * 租户更新时设置默认双因子，只有dualFactor为0时才会触发
     *
     * @param moiraiTenant
     */
    private void updateUserMember(MoiraiTenant moiraiTenant) {
        if (Constants.MOIRAI_DUCL_FACTOR_DEFAULT.equals(moiraiTenant.getDualFactor())) {
            MoiraiUserMemberCondition condition = new MoiraiUserMemberCondition();
            /** 取消双因子 **/
            condition.setTenantId(moiraiTenant.getTenantId());
            condition.setDefaultValidationFlag(Constants.flag_Y);
            List<MoiraiUserMember> members = moiraiUserMemberMapper.selectByBean(condition);
            if (!members.isEmpty()) {
                members.forEach(member -> {
                    member.setDefaultValidationFlag(Constants.flag_N);
                    member.setUpdateTime(new Date());
                    member.setUpdateUser(moiraiTenant.getModifyUser());
                });
                moiraiUserMemberMapper.updateMoreMember(members);
            }
        }
    }

    private void updateAdminUserEmail(Long tenantId, String email) {
        if (email != null) {
            try {
                MoiraiUserCondition userCondition = new MoiraiUserCondition();
                userCondition.setTenantId(tenantId);
                userCondition.setIsTenantAccount("Y");
                List<MoiraiUser> userList = moiraiUserMapper.findUserByCondition(userCondition);
                if (userList != null) {
                    MoiraiUser findUser = userList.get(0);
                    findUser.setUserEmail(email);
                    moiraiUserMapper.updateByPrimaryKeySelective(findUser);
                }
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.Moirai_DB_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_UPDATEERROR);
            }
        }
    }

    @Override
    public BWJsonResult<MoiraiTenantListVO> getTenantListPage(MoiraiTenantListCondition tenantListCondition) {

        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        PageHelper.startPage(tenantListCondition.getPageNo(), tenantListCondition.getPageSize());
        List<MoiraiTenantListVO> moiraiTenantList = moiraiTenantMapper.queryTenantListPage(tenantListCondition);
        for (MoiraiTenantListVO moiraiTenant : moiraiTenantList) {
            int count = moiraiOrgMapper.queryTenantTaxCodeTotal(moiraiTenant.getTenantId());
            moiraiTenant.setTaxTotal(count + "");
        }
        PageInfo<MoiraiTenantListVO> pageInfo = new PageInfo<>(moiraiTenantList);
        BWJsonResult<MoiraiTenantListVO> moiraiTenantResult = new BWJsonResult<>(moiraiTenantList, (int) pageInfo.getTotal());
        return moiraiTenantResult;
    }

    /**
     * 查询租户信息 单表查询 运营后台租户查询
     */
    @Override
    public BWJsonResult<MoiraiTenantListVO> getTenantList2Page(MoiraiTenantListCondition tenantListCondition) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        PageHelper.startPage(tenantListCondition.getPageNo(), tenantListCondition.getPageSize());
        List<MoiraiTenantListVO> moiraiTenantList = moiraiTenantMapper.queryTenantList2Page(tenantListCondition);
        PageInfo<MoiraiTenantListVO> pageInfo = new PageInfo<>(moiraiTenantList);
        if (moiraiTenantList.isEmpty()) {
            return new BWJsonResult<>(moiraiTenantList, (int) pageInfo.getTotal());
        }
        List<Long> tenantIdList = moiraiTenantList.stream().map(item -> item.getTenantId()).collect(Collectors.toList());
        // 补偿组织机构信息
        List<MoiraiOrg> exOrgList = moiraiOrgMapper.selectTopOrgListByTenantIdList(tenantIdList);
        Map<Long, List<MoiraiOrg>> tenantOrgMap = new HashMap<>(exOrgList.size());
        for (int i = 0; i < exOrgList.size(); i++) {
            if (!tenantOrgMap.containsKey(exOrgList.get(i).getTenantId())) {
                tenantOrgMap.put(exOrgList.get(i).getTenantId(), new ArrayList<>());
            }
            tenantOrgMap.get(exOrgList.get(i).getTenantId()).add(exOrgList.get(i));
        }

        // 补偿渠道编码信息
        List<MoiraiChannelTenant> exQdbmList = moiraiChannelTenantMapper.selectListByTenantIdList(tenantIdList);
        Map<Long, List<MoiraiChannelTenant>> tenantQdbmMap = new HashMap<>(tenantIdList.size());
        for (int i = 0; i < exQdbmList.size(); i++) {
            if (!tenantQdbmMap.containsKey(exQdbmList.get(i).getTenantId())) {
                tenantQdbmMap.put(exQdbmList.get(i).getTenantId(), new ArrayList<>());
            }
            tenantQdbmMap.get(exQdbmList.get(i).getTenantId()).add(exQdbmList.get(i));
        }
        List<Long> qdbms = new ArrayList<>();
        if (tenantListCondition.getQdBms() != null) {
            qdbms = tenantListCondition.getQdBms();
        }

        // 补偿组织机构统计信息
        List<Map<String, Object>> tenantOrgCountList = moiraiOrgMapper.queryTenantTaxCodeTotalList(tenantIdList);

        // 补偿租户管理员账号信息
        List<MoiraiUser> tenantUserList = moiraiUserMapper.selectTenantAccountUserList(tenantIdList);
        Map<Long, String> tenantAccountMap = new HashMap<>();
        Map<Long, String> tenantEmailMap = new HashMap<>();
        for (int i = 0; i < tenantUserList.size(); i++){
            tenantAccountMap.put(tenantUserList.get(i).getTenantId(), tenantUserList.get(i).getUserAccount());
            tenantEmailMap.put(tenantUserList.get(i).getTenantId(), tenantUserList.get(i).getUserEmail());
        }

        for (MoiraiTenantListVO moiraiTenant : moiraiTenantList) {
            List<MoiraiOrg> exOrgs = tenantOrgMap.get(moiraiTenant.getTenantId());
            if (exOrgs != null) {
                for (int j = 0; j < exOrgs.size(); j++) {
                    if (exOrgs.get(j).getTenantId().equals(moiraiTenant.getTenantId())) {
                        moiraiTenant.setTaxCode(exOrgs.get(j).getTaxCode());
                        moiraiTenant.setTaxProv(exOrgs.get(j).getTaxProv());
                        moiraiTenant.setOrgId(exOrgs.get(j).getOrgId());
                        moiraiTenant.setDeviceType(exOrgs.get(j).getDeviceType());
                        moiraiTenant.setTgType(exOrgs.get(j).getTgType());
                        moiraiTenant.setContractNum(exOrgs.get(j).getContractNum());
                    }
                }
            }
            for (int i = 0; i < tenantOrgCountList.size(); i++) {
                if (moiraiTenant.getTenantId().equals(tenantOrgCountList.get(i).get("tenantId"))) {
                    moiraiTenant.setTaxTotal(tenantOrgCountList.get(i).get("orgCount") + "");
                }
            }
            List<MoiraiChannelTenant> exQdbms = tenantQdbmMap.get(moiraiTenant.getTenantId());
            if (exQdbms != null) {
                List<String> exQ = new ArrayList<>();
                if (qdbms.isEmpty()) {
                    exQ = exQdbms.stream().map(k -> k.getQdBm() != null ? k.getQdBm() + "" : "").collect(Collectors.toList());
                } else {
                    for (int j = 0; j < exQdbms.size(); j++) {
                        if (qdbms.contains(exQdbms.get(j).getQdBm())) {
                            exQ.add(exQdbms.get(j).getQdBm() + "");
                        }
                    }
                }
                moiraiTenant.setQdBms(String.join(",", exQ));
            }
            moiraiTenant.setUserAccount(tenantAccountMap.get(moiraiTenant.getTenantId()));
            moiraiTenant.setTenantEmail(tenantEmailMap.get(moiraiTenant.getTenantId()));
        }
        return new BWJsonResult<>(moiraiTenantList, (int) pageInfo.getTotal());
    }

    @Override
    public BWJsonResult<MoiraiTenantListVO> getTenantZomListPage(MoiraiTenantListCondition tenantListCondition) {

        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        PageHelper.startPage(tenantListCondition.getPageNo(), tenantListCondition.getPageSize());
        List<MoiraiTenantListVO> moiraiTenantList = moiraiTenantZomMapper.queryTenantListPage(tenantListCondition);
        PageInfo<MoiraiTenantListVO> pageInfo = new PageInfo<>(moiraiTenantList);

        return new BWJsonResult<>(moiraiTenantList, (int) pageInfo.getTotal());
    }

    @Override
    public List<MoiraiTenantVO> getTenantByCondition(MoiraiTenantVO moiraiTenantVO) {
        PageHelper.startPage(moiraiTenantVO.getPageNo(), moiraiTenantVO.getPageSize());
        List<MoiraiTenantVO> moiraiTenantList = moiraiTenantMapper.queryTenantByCondition(moiraiTenantVO);
        return moiraiTenantList;
    }

    @Override
    public MoiraiTenantVO getCacheMoiraiTenant(Long tenantId) {
        Object obj = redisTemplate.opsForValue().get(Constants.MOIRAI_TENANT_INFO + tenantId);
        if (obj != null) {
            MoiraiTenantVO tenantVO = JacksonUtil.jsonStrToObject(obj.toString(), MoiraiTenantVO.class);
            return tenantVO;
        }
        if (redisTemplate.hasKey(Constants.MOIRAI_TENANT_INFO + tenantId)) {
            return null;
        }
        MoiraiTenantVO tenantVO = new MoiraiTenantVO();
        tenantVO.setTenantId(tenantId);
        List<MoiraiTenantVO> moiraiTenantList = moiraiTenantMapper.queryTenantByCondition(tenantVO);
        int time = 1;
        MoiraiTenantVO moiraiTenantVO = null;
        if (!moiraiTenantList.isEmpty()) {
            Random random = new Random();
            time = 300 + random.nextInt(10);
            moiraiTenantVO = moiraiTenantList.get(0);
            MoiraiChannelTenant channelTenant = new MoiraiChannelTenant();
            channelTenant.setTenantId(tenantId);
            List<MoiraiChannelTenant> channelTenants = moiraiChannelTenantMapper.queryList(channelTenant);
            moiraiTenantVO.setQdBmList(channelTenants);
            redisTemplate.opsForValue().set(Constants.MOIRAI_TENANT_INFO + tenantId, moiraiTenantVO.toString(), time, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().set(Constants.MOIRAI_TENANT_INFO + tenantId, "", time, TimeUnit.SECONDS);
        }
        return moiraiTenantVO;
    }

    @Override
    public MoiraiTenantVO queryTenantByTaxCode(MoiraiOrg moiraiOrg) {
        MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        if (org == null) {
            throw new MoiraiException(MOIRAI_DB_NULL.getCode(), "税号" + MOIRAI_DB_NULL.getMsg());
        }
        if (org.getParentOrg() != 0) {
            moiraiOrg = new MoiraiOrg();
            moiraiOrg.setTenantId(org.getTenantId());
            moiraiOrg.setParentOrg(0L);
            org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        }
        MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(org.getTenantId());
        if (moiraiTenant == null) {
            throw new MoiraiException(MOIRAI_DB_NULL.getCode(), "租户信息" + MOIRAI_DB_NULL.getMsg());
        }
        MoiraiUserCondition condition = new MoiraiUserCondition();
        condition.setTenantId(org.getTenantId());
        condition.setOrgId(org.getOrgId());
        condition.setIsTenantAccount(Constants.flag_Y);
        List<MoiraiUser> userList = moiraiUserMapper.findUserByCondition(condition);
        if (userList.isEmpty()) {
            throw new MoiraiException(MOIRAI_DB_NULL.getCode(), "顶级机构管理员账号" + MOIRAI_DB_NULL.getMsg());
        }
        MoiraiTenantVO moiraiTenantVO = new MoiraiTenantVO();
        moiraiTenantVO.setTenantName(moiraiTenant.getTenantName());
        moiraiTenantVO.setTaxCode(org.getTaxCode());
        moiraiTenantVO.setUserAccount(userList.get(0).getUserAccount());
        moiraiTenantVO.setTenantEmail(moiraiTenant.getTenantEmail());
        return moiraiTenantVO;
    }

    private void deleteCacheTenant(Long tenantId, Long orgId) {
        redisTemplate.delete(Constants.MOIRAI_TENANT_INFO + tenantId);
        redisTemplate.delete(Constants.MOIRAI_ORG_INFO + orgId);
    }

    private MoiraiOrg coverUptateOrgInfo(MoiraiTenantVO moiraiTenant) {
        MoiraiOrg moiraiOrgTenant = new MoiraiOrg();
        /** 新加字段 */
        String taxCode = moiraiTenant.getTaxCode();
        if (!StrUtils.isEmpty(taxCode)) {
            MoiraiOrg oldMoiraiOrg = moiraiOrgService.queryByTaxCode(taxCode);
            if (oldMoiraiOrg != null) {
                throw new MoiraiException("2103", "税号已经注册过，不能重复注册");
            }
            //查询百望云老版数据库
            Map<String, String> map = new HashMap<>();
            map.put("taxCode", taxCode);
            JSONObject jsonObject = moiraiSysService.commonMethod(map, 3);
            if (null != jsonObject && !"0".equals(jsonObject.get("code"))) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_OLD_USER_CENTER_MORE;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());

                throw new MoiraiException(jsonObject.getString("code"), jsonObject.getString("message"));
            }
            moiraiOrgTenant.setTaxCode(taxCode);
            moiraiOrgTenant.setOrgType(1);
            if (StringUtils.isBlank(oldMoiraiOrg.getJrdCode())) {
                if (StringUtils.isBlank(moiraiTenant.getJrdCode())) {
                    moiraiOrgTenant.setJrdCode(Constants.JRD_CODE);
                } else {
                    moiraiOrgTenant.setJrdCode(moiraiTenant.getJrdCode());
                }
            }
        }
        moiraiOrgTenant.setOrgCode(moiraiTenant.getTenantCode());
        moiraiOrgTenant.setTelphone(moiraiTenant.getTenantPhone());
        moiraiOrgTenant.setBankDeposit(moiraiTenant.getBankDeposit());
        moiraiOrgTenant.setAccountNumber(moiraiTenant.getAccountNumber());
        moiraiOrgTenant.setTgType(moiraiTenant.getTgType());
        moiraiOrgTenant.setDeviceType(moiraiTenant.getDeviceType());
        moiraiOrgTenant.setBusinessAddress(moiraiTenant.getBusinessAddress());
        moiraiOrgTenant.setProducts(moiraiTenant.getProducts());
        moiraiOrgTenant.setOrgId(moiraiTenant.getOrgId());
        moiraiOrgTenant.setTaxProv(moiraiTenant.getTaxProv());
        moiraiOrgTenant.setTenantId(moiraiTenant.getTenantId());
        moiraiOrgTenant.setModifyUser(moiraiTenant.getModifyUser());
        moiraiOrgTenant.setModifyTime(moiraiTenant.getModifyTime());
        moiraiOrgTenant.setLegalName(moiraiTenant.getLegalName());
        moiraiOrgTenant.setDjCompanyType(moiraiTenant.getDjCompanyType());
        moiraiOrgTenant.setBelongIndustry(moiraiTenant.getBelongIndustry());
        moiraiOrgTenant.setRegProv(moiraiTenant.getRegProv());
        moiraiOrgTenant.setRegCity(moiraiTenant.getRegCity());
        moiraiOrgTenant.setRegArea(moiraiTenant.getRegArea());
        moiraiOrgTenant.setTaxQuali(moiraiTenant.getTaxQuali());
        moiraiOrgTenant.setSelfManage(moiraiTenant.getSelfManage());
        moiraiOrgTenant.setNeedSeal(moiraiTenant.getNeedSeal());
        moiraiOrgTenant.setSealId(moiraiTenant.getSealId());
        moiraiOrgTenant.setDeviceCode(moiraiTenant.getDeviceCode());
        moiraiOrgTenant.setDeviceName(moiraiTenant.getDeviceName());
        moiraiOrgService.validatetaxOrg(moiraiOrgTenant);
        moiraiOrgTenant.setTaxentityId(moiraiTenant.getTaxentityId());
        //新加
        moiraiOrgTenant.setRegisterAddress(moiraiTenant.getRegisterAddress());
        moiraiOrgTenant.setExportQualify(moiraiTenant.getExportQualify());
        moiraiOrgTenant.setContactNumber(moiraiTenant.getContactNumber());
        moiraiOrgTenant.setAddressee(moiraiTenant.getAddressee());
        moiraiOrgTenant.setMoilingAddress(moiraiTenant.getMoilingAddress());
        moiraiOrgTenant.setIsProof(moiraiTenant.getIsProof());
        moiraiOrgTenant.setIsFetch(moiraiTenant.getIsFetch());
        moiraiOrgTenant.setIsAuthe(moiraiTenant.getIsAuthe());
        //税控盒子必填字段
        if ("3".equals(moiraiTenant.getDeviceType())) {
            if (StringUtils.isEmpty(moiraiTenant.getDeviceSn()) || StringUtils.isEmpty(moiraiTenant.getDevicePassword())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_DEVICE_ERROR);
            }
            moiraiOrgTenant.setDeviceSn(moiraiTenant.getDeviceSn());
            moiraiOrgTenant.setDevicePassword(moiraiTenant.getDevicePassword());
            moiraiOrgTenant.setDeviceKey(moiraiTenant.getDeviceKey());
        }
        moiraiOrgTenant.setContractNum(moiraiTenant.getContractNum());
        return moiraiOrgTenant;
    }

    @Override
    public void openTenantProduct(MoiraiOrg moiraiOrg) {
        MoiraiOrg org = moiraiOrgService.selectByOrgId(moiraiOrg.getOrgId());
        if (org == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_IS_NULL);
        }
        if (org.getParentOrg() != 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_OPENPRODUCT_ERROR);
        }
        moiraiOrgService.openOrgProduct(moiraiOrg);
        MoiraiUser moiraiUser = moiraiUserMapper.getUserByUserAccount(moiraiOrg.getCreater());
        List<Long> products = new ArrayList<>();
        moiraiOrg.getProducts().forEach(item -> products.add(item.getProductId()));
        moiraiOrgService.gainAuthByProductId(products, moiraiUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTenant(Long tenantId) {
        List<MoiraiOrgProduct> orgProductList = moiraiOrgProductMapper.queryTenantProducts(tenantId);
        boolean delFlag = false;
        for (MoiraiOrgProduct orgProduct : orgProductList) {
            if (Constants.MOBILE_INVOICE_PRODUCT.equals(orgProduct.getProductId())) {
                delFlag = true;
                break;
            }
        }
        if (delFlag) {
            int i = 0;
            try {
                i = moiraiTenantMapper.deleteTenantByTenantId(tenantId);
                moiraiTenantMapper.deleteUserByTenantId(tenantId);
                moiraiTenantMapper.deleteOrgByTenantId(tenantId);
                moiraiTenantMapper.deleteOrgProductByTenantId(tenantId);
                moiraiTenantMapper.deleteUserAuthByExample(tenantId);
                moiraiTenantMapper.deleteRoleByTenantId(tenantId);
                moiraiTenantMapper.deleteRoleResourceByTenantId(tenantId);
                moiraiTenantMapper.deleteOrgConfigByTenantId(tenantId);
                moiraiTenantMapper.deleteUserMember(tenantId);
                this.deleteCacheTenant(tenantId, orgProductList.get(0).getOrgId());
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_DEL_TENANT_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

                throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
            }
            return i;
        }
        return 0;
    }

    /**
     * 获取当前登录租户信息
     */
    @Override
    public MoiraiTenantVO getCurrentLoginTenantInfo() {
        MoiraiUser moiraiUser = moiraiSysService.gainCacheUser();
        if (moiraiUser == null){
            return null;
        }
        return getCacheMoiraiTenant(moiraiUser.getTenantId());
    }

    /**
     * 更新资质文件
     */
    @Override
    public void updateQualiFilepath(MoiraiTenant tenant) {
        MoiraiUser moiraiUser = moiraiSysService.gainCacheUser();
        if (moiraiUser == null){
            throw new MoiraiException("-1", "请登录后操作！");
        }
        MoiraiTenant update = new MoiraiTenant();
        update.setTenantId(moiraiUser.getTenantId());
        update.setQualiFilepath(tenant.getQualiFilepath());
        updateTenant(update);
    }
}
