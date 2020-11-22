package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.MoiraiProductClient;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiProductMapper;
import com.baiwang.moirai.mapper.MoiraiRoleMapper;
import com.baiwang.moirai.mapper.MoiraiRoleResourceMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserDataScopeMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.po.ProductOpenPO;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiResourceCondition;
import com.baiwang.moirai.model.role.MoiraiResourceExt;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleResource;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzCondition;
import com.baiwang.moirai.model.role.MoiraiUserAuthzExample;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.scale.MoiraiUserDataScopeExample;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.user.MoiraiUserExtra;
import com.baiwang.moirai.model.vo.MoiraiProductVo;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiResourcesService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiUserExtraService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.HttpInfoUtils;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.baiwang.moirai.utils.WriteExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoiraiUserExtraServiceImpl implements MoiraiUserExtraService {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiUserExtraServiceImpl.class);

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiProductMapper moiraiProductMapper;

    @Autowired
    private MoiraiRoleMapper moiraiRoleMapper;

    @Autowired
    private MoiraiRoleResourceMapper moiraiRoleResourceMapper;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    private MoiraiResourcesService moiraiResourcesService;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private MoiraiUserDataScopeMapper moiraiUserDataScopeMapper;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private MoiraiProductClient productClient;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private StringRedisTemplate redisTemplate;

    @Value("${system.prepared.role.init.time}")
    private String sysRoleInitTime;

    public List<String> getResourceAuthcService(MoiraiUserCondition moiraiUserCondition) {
        List<MoiraiOrgProduct> resourceService = this.getResourceService(moiraiUserCondition, false);
        List<String> judges = new ArrayList<>();
        if (resourceService != null && resourceService.size() > 0) {
            for (MoiraiOrgProduct moiraiOrgProduct : resourceService) {
                List<MoiraiResource> resourceList = moiraiOrgProduct.getResourceList();
                if (resourceList != null) {
                    for (MoiraiResource moiraiResource : resourceList) {
                        String judgeAuthc = moiraiResource.getJudgeAuthc();
                        if (StringUtils.isNotEmpty(judgeAuthc) && !judges.contains(judgeAuthc)) {
                            judges.add(judgeAuthc);
                        }
                    }
                }
            }
        }
        return judges;
    }

    /**
     * 获取资源树
     *
     * @param moiraiUserCondition
     * @param treeFlag 是否生成树
     * @return
     */
    public List<MoiraiOrgProduct> getResourceService(MoiraiUserCondition moiraiUserCondition, boolean treeFlag) {
        MoiraiUser moiraiUser = moiraiSysService.gainCacheUser();
        Long orgId = moiraiUserCondition.getOrgId();
        Long tenantId = moiraiUserCondition.getTenantId();
        List<MoiraiOrgProduct> returnList = new ArrayList<>();
        MoiraiUserAuthz orgRole = new MoiraiUserAuthz();
        orgRole.setUserId(moiraiUser != null ? moiraiUser.getUserId() : moiraiUserCondition.getUserId());
        orgRole.setTenantId(tenantId);
        orgRole.setAuthOrg(orgId);
        orgRole.setRoleId(moiraiUserCondition.getRoleId());
        //用户拥有的资源ID
        List<Long> resourceIds = moiraiRoleResourceMapper.selectResourceIds(orgRole);
        if (resourceIds == null || resourceIds.size() == 0) {
            return null;
        }

        //获取组织机构拥有的产品列表
        List<MoiraiOrgProduct> products = moiraiOrgService.getOrgProducts(orgId);
        if (null == products || products.size() == 0) {
            return null;
        }

        //判断组织机构资源过滤类型----放到角色查询那里-----
        //MoiraiOrg org = moiraiOrgService.selectByOrgId(moiraiUserCondition.getOrgId());
        //String resFilterType = moiraiResourcesService.getOrgResourceFilterType(org);

        List<MoiraiOrgProduct> otherList = new ArrayList<>();
        List<Long> productIds = new ArrayList<Long>();
        //获取根据组织机构和设备过滤后的产品对应的功能列表
        for (MoiraiOrgProduct product : products) {
            Long productId = product.getProductId();
            List<MoiraiResource> orgResources = moiraiResourcesService.selectTenantProductResourceList(productId, null, tenantId);
            if (orgResources.isEmpty()) {
                returnList.add(product);
            }
            List<MoiraiResource> resourceList = new ArrayList<>();
            orgResources.forEach(item -> {
                if (resourceIds.contains(item.getResourceId())) {
                    resourceList.add(item);
                }
            });
            if (!resourceList.isEmpty()) {
                if (productIds.contains(product.getProductId())) {
                    returnList.remove(product);
                }
                productIds.add(product.getProductId());
                product.setResourceList(resourceList);
                returnList.add(product);

                //多应用
                if (Constants.DEFAULT_ONE.equals(product.getProductType().toString())) {
                    for (int i = 0; i < otherList.size(); i++) {
                        MoiraiOrgProduct orgProduct = otherList.get(i);
                        if (product.getBelongProduct().equals(orgProduct.getBelongProduct())) {
                            resourceList.addAll(orgProduct.getResourceList());
                            returnList.remove(orgProduct);
                            otherList.remove(orgProduct);
                            List<Long> ids = new ArrayList<>();
                            List<MoiraiResource> collect = resourceList.stream().filter(resource -> {
                                boolean contains = !ids.contains(resource.getResourceId());
                                ids.add(resource.getResourceId());
                                return contains;
                            }).collect(Collectors.toList());
                            product.setResourceList(collect);
                        }
                    }
                    otherList.add(product);
                }
            }
        }

        //是否生成树
        if (treeFlag) {
            for (MoiraiOrgProduct p : returnList) {
                List<MoiraiResource> treeList = AdminUtils.getResourceTree(p.getResourceList(), 0L);
                p.setResourceList(treeList);
            }
        }
        return returnList;
    }

    @Override
    public List<MoiraiProductVo> getResourceServiceExt(MoiraiUserCondition query, boolean treeFlag) {
        // 根据当前登陆人账号和机构查询角色资源信息
        MoiraiUser moiraiUser = moiraiSysService.gainCacheUser();
        MoiraiUserAuthz queryAuth = new MoiraiUserAuthz();
        queryAuth.setUserId(moiraiUser != null ? moiraiUser.getUserId() : query.getUserId());
        queryAuth.setTenantId(query.getTenantId());
        queryAuth.setAuthOrg(query.getOrgId());
        queryAuth.setRoleId(query.getRoleId());
        List<MoiraiRoleResource> authList = moiraiRoleResourceMapper.selectAuthResource(queryAuth);
        if (null == authList || authList.size() == 0) {
            return new ArrayList<>();
        }
        Map<Long, MoiraiRoleResource> authMap = authList.stream()
                .collect(Collectors.toMap(MoiraiRoleResource::getResourceId, MoiraiRoleResource -> MoiraiRoleResource, (v1, v2) -> "Y".equals(v2.getIncludeSon()) ? v2: v1));

        //获取组织机构拥有的产品列表
        List<MoiraiOrgProduct> products = moiraiOrgService.getOrgProducts(query.getOrgId());
        if (null == products || products.size() == 0) {
            return new ArrayList<>();
        }
        List<Long> productIds = products.stream().map(item -> item.getProductId()).collect(Collectors.toList());

        // 查询产品资源信息
        List<MoiraiProductVo> returnList = new ArrayList<>();
        ProductOpenPO queryProduct = new ProductOpenPO();
        queryProduct.setProductIds(productIds);
        queryProduct.setResourceWay("20");
        queryProduct.setResourceType(query.getResourceType());
        try {
            BWJsonResult<MoiraiProductVo> voResult = productClient.queryProductAndResMore(queryProduct);
            if (!voResult.isSuccess()) {
                logger.error("获取产品资源失败！ {}", voResult.getErrorMsg());
                return new ArrayList<>();
            }
            for (MoiraiProductVo vo : voResult.getData()) {
                if ("0".equals(vo.getProductShow())) {
                    continue;
                }
                if (vo.getResourceExtList() == null || vo.getResourceExtList().isEmpty()) {
                    returnList.add(vo);
                    continue;
                }
                List<MoiraiResourceExt> list = getAuthResourceList(vo.getResourceExtList(), authMap);
                if (!list.isEmpty()) {
                    vo.setResourceExtList(list);
                    returnList.add(vo);
                }
            }
            if (!"20".equals(query.getResourceWay())){
                ProductOpenPO queryRelation = new ProductOpenPO();
                queryRelation.setResourceWay(query.getResourceWay());
                queryRelation.setProductResource(returnList);
                queryRelation.setResourceType(query.getResourceType());
                BWJsonResult<MoiraiProductVo> relResult = productClient.queryRelationResource(queryRelation);
                if (!relResult.isSuccess()) {
                    logger.error("获取产品资源失败！ {}", relResult.getErrorMsg());
                    return new ArrayList<>();
                }
                returnList = relResult.getData();
            }
        } catch (Exception e) {
            logger.error("获取产品资源信息失败！", e);
            throw new MoiraiException("-1", "获取产品资源信息失败！");
        }
        return returnList;
    }

    /**
     * 获取授权菜单
     */
    private List<MoiraiResourceExt> getAuthResourceList(List<MoiraiResourceExt> source, Map<Long, MoiraiRoleResource> authMap) {
        List<MoiraiResourceExt> retList = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return retList;
        }
        for (int i = 0; i < source.size(); i++) {
            MoiraiRoleResource item = authMap.get(source.get(i).getResourceId());
            if (item != null) {
                if (!"Y".equals(item.getIncludeSon())) {
                    source.get(i).setChildren(getAuthResourceList(source.get(i).getChildren(), authMap));
                }
                retList.add(source.get(i));
            }
        }
        return retList;
    }

    /**
     * <B>方法名称：</B>获取用户授权机构<BR>
     * <B>概要说明：</B>百望云页面授权机构下拉<BR>
     *
     * @param moiraiUserCondition
     * @return
     * @since 2019年6月10日
     */
    public MoiraiOrg getUserAuthOrgTree(MoiraiUserCondition moiraiUserCondition) {
        Long userId = moiraiUserCondition.getUserId();
        Long tenantId = moiraiUserCondition.getTenantId();
        Long resourceId = moiraiUserCondition.getResourceId();
        logger.info("授权机构树请求字段：用户ID:{},租户ID:{},资源ID:{}", userId, tenantId, resourceId);
        List<MoiraiOrg> orgList = moiraiOrgMapper.queryOrgSimpleTreeByTenant(tenantId);
        Set<Long> orgIdSet = new HashSet<>();
        Set<Long> authOrgSet = new HashSet<>();
        //查询用户授权机构
        List<MoiraiUserAuthz> authzs = moiraiOrgService.getUserAuthBycondition(userId, resourceId);
        if (authzs.isEmpty()) {
            return null;
        }
        if (authzs.size() != orgList.size()) {
            for (MoiraiUserAuthz auth : authzs) {
                Long authOrg = auth.getAuthOrg();
                authOrgSet.add(authOrg);
                orgIdSet.add(authOrg);
                this.getAuthOrgParent(authOrg, orgList, orgIdSet);
            }
        } else {
            for (MoiraiUserAuthz auth : authzs) {
                Long authOrg = auth.getAuthOrg();
                authOrgSet.add(authOrg);
                orgIdSet.add(authOrg);
            }
        }
        orgList = orgList.stream().filter(org -> {
            if (authOrgSet.contains(org.getOrgId())) {
                org.setIsCheck("Y");
            }
            if (orgIdSet.contains(org.getOrgId())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        return moiraiOrgService.combineOrgTree(orgList, null);
    }

    private void getAuthOrgParent(Long authOrg, List<MoiraiOrg> orgList, Set<Long> orgIdSet) {
        for (MoiraiOrg org : orgList) {
            if (authOrg == 0) {
                break;
            }
            if (authOrg.equals(org.getOrgId())) {
                Long parentOrg = org.getParentOrg();
                if (!orgIdSet.contains(parentOrg)) {
                    orgIdSet.add(parentOrg);
                    this.getAuthOrgParent(parentOrg, orgList, orgIdSet);
                }
                break;
            }
        }
    }

    /**
     * 1、根据resourceId获取 2、根据授权的资源获取
     */

    public List<MoiraiOrg> getAuthOrgByResourceId(MoiraiUserAuthzCondition auth) {
        Long userId = auth.getUserId();
        Long resourceId = auth.getResourceId();
        Integer orgType = auth.getOrgType();
        String isNeedTaxCode = auth.getIsNeedTaxCode();
        logger.info("moiraiUserExtraService-->getAuthOrgByResourceId,userId={},resourceId={},orgType={},isNeedTaxCode={}", userId, resourceId, orgType, isNeedTaxCode);
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andTenantIdEqualTo(auth.getTenantId()).andUserIdEqualTo(auth.getUserId());
        List<MoiraiUserAuthz> selectByExample = moiraiUserAuthzMapper.selectByExample(example);
        Set<Long> orgSet = new HashSet<>();
        if (resourceId == null) {
            for (MoiraiUserAuthz moiraiUserAuthz : selectByExample) {
                Long authOrg = moiraiUserAuthz.getAuthOrg();
                orgSet.add(authOrg);
            }
        } else {
            //获取授权了某个资源的机构
            List<MoiraiUserAuthz> moiraiUserAuthzList = new ArrayList<>();
            for (MoiraiUserAuthz moiraiUserAuthz : selectByExample) {
                Long roleId = moiraiUserAuthz.getRoleId();
                MoiraiRoleResource moiraiRoleResource = new MoiraiRoleResource();
                moiraiRoleResource.setResourceId(resourceId);
                moiraiRoleResource.setRoleId(roleId);
                moiraiRoleResource.setTenantId(auth.getTenantId());
                List<MoiraiRoleResource> resourceIds = moiraiRoleResourceMapper.selectByBean(moiraiRoleResource);
                if (resourceIds != null && resourceIds.size() > 0) {
                    moiraiUserAuthzList.add(moiraiUserAuthz);
                }
            }
            for (MoiraiUserAuthz authz : moiraiUserAuthzList) {
                orgSet.add(authz.getAuthOrg());
            }
        }
        List<MoiraiOrg> selectOrgBatcher = null;
        if (orgSet != null && orgSet.size() > 0) {
            MoiraiOrgCondition moiraiOrgCondition = new MoiraiOrgCondition();
            moiraiOrgCondition.setItems(new ArrayList(orgSet));
            selectOrgBatcher = moiraiOrgMapper.selectOrgBatcher(moiraiOrgCondition);
        }
        //获取纳税机构
        if (orgType != null) {
            if (selectOrgBatcher != null && selectOrgBatcher.size() > 0) {
                List<MoiraiOrg> nsOrgList = new ArrayList<>();
                for (MoiraiOrg moiraiOrg : selectOrgBatcher) {
                    Integer orgType2 = moiraiOrg.getOrgType();
                    if (orgType.toString().equals(orgType2.toString())) {
                        nsOrgList.add(moiraiOrg);
                    }
                }
                selectOrgBatcher = nsOrgList;
            }
        }
        //获取机构【纳税主体、非纳税主体(税号继承上级纳税主体税号)】
        if (isNeedTaxCode != null && "1".equals(isNeedTaxCode)) {
            if (selectOrgBatcher != null && selectOrgBatcher.size() > 0) {
                List<MoiraiOrg> nsOrgList = new ArrayList<>();
                for (MoiraiOrg moiraiOrg : selectOrgBatcher) {
                    String taxCode = moiraiOrg.getTaxCode();
                    logger.info("moiraiOrg=={},taxCode={}" + moiraiOrg.getOrgId() + taxCode);
                    if (taxCode == null) {
                        MoiraiOrg orgTaxEntity = moiraiOrgService.getOrgTaxEntity(moiraiOrg);
                        String taxCode2 = orgTaxEntity.getTaxCode();
                        if (taxCode2 == null) {
                            continue;
                        }
                        moiraiOrg.setTaxCode(taxCode2);
                    }
                    nsOrgList.add(moiraiOrg);
                }
                selectOrgBatcher = nsOrgList;
            }
        }
        return selectOrgBatcher;
    }

    /**
     * 获取当前组织机构下的用户列表信息
     */
    public Map<String, Object> getUserList(MoiraiUserCondition condition) {
        condition.setOrgId(null);
        //按条件查询用户列表信息
        PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        List<MoiraiUser> users = moiraiUserMapper.findUserListByCondition(condition);
        if (users == null || users.size() <= 0) {
            return new HashMap<>();
        }
        PageInfo<MoiraiUser> pageInfo = new PageInfo<>(users);
        Long total = pageInfo.getTotal();
        List<MoiraiUserExtra> list = new ArrayList<>();
        MoiraiUserExtra extra;
        MoiraiOrg moiraiOrg;
        for (MoiraiUser moiraiUser : users) {
            extra = new MoiraiUserExtra();
            moiraiOrg = new MoiraiOrg();
            moiraiOrg.setOrgId(moiraiUser.getOrgId());
            String orgName = moiraiOrgMapper.selectOneOrg(moiraiOrg).getOrgName();
            extra.setUserAccount(moiraiUser.getUserAccount());
            extra.setUserName(moiraiUser.getUserName());
            extra.setOrgName(orgName);
            extra.setUserDetaile(moiraiUser.getUserDetaile());
            extra.setModifyTime(moiraiUser.getModifyTime());
            extra.setTenantId(moiraiUser.getTenantId());
            extra.setOrgId(moiraiUser.getOrgId());
            extra.setUserId(moiraiUser.getUserId());
            extra.setUseFlag(moiraiUser.getUseFlag());
            list.add(extra);
        }
        return this.dealUserListInfo(list, total);
    }

    private Map<String, Object> dealUserListInfo(List<MoiraiUserExtra> userList, Long total) {
        List<MoiraiUserAuthzCondition> list = new ArrayList<>();
        MoiraiUserAuthzCondition authzCondition;
        MoiraiUserCondition condition1;
        List<String> roleList;
        List<MoiraiUserAuthz> userRoles;
        for (MoiraiUserExtra moiraiUser : userList) {
            authzCondition = new MoiraiUserAuthzCondition();
            BeanUtils.copyProperties(moiraiUser, authzCondition);
            authzCondition.setUserOrg(moiraiUser.getOrgId());
            authzCondition.setOrgName(moiraiUser.getOrgName());
            authzCondition.setTenantId(moiraiUser.getTenantId());
            //获取用户当前机构下的角色
            condition1 = new MoiraiUserCondition();
            roleList = new ArrayList<>();
            condition1.setUserId(moiraiUser.getUserId());
            condition1.setTenantId(moiraiUser.getTenantId());
            condition1.setOrgId(moiraiUser.getOrgId());
            userRoles = this.getUserRole(condition1);
            if (!StrUtils.isEmptyList(userRoles)) {
                StringBuffer buffer = new StringBuffer();
                int userRoleSize = userRoles.size();
                MoiraiUserAuthz moiraiUserAuthz;
                MoiraiRole role;
                MoiraiOrg org;
                for (int i = 0; i < userRoleSize; i++) {
                    moiraiUserAuthz = userRoles.get(i);
                    role = moiraiRoleMapper.selectByPrimaryKey(moiraiUserAuthz.getRoleId());
                    org = moiraiOrgService.selectByOrgId(moiraiUserAuthz.getAuthOrg());
                    if (i < userRoles.size() - 1) {
                        if (role != null && org != null) {
                            String orgName = org.getOrgName();
                            String roleName = role.getRoleName();
                            String orgRole = orgName + "-" + roleName + "，";
                            buffer.append(orgRole);
                        } else if (org != null) {
                            String orgName = org.getOrgName() + "，";
                            buffer.append(orgName);
                        }
                    } else {
                        if (role != null && org != null) {
                            String orgName = org.getOrgName();
                            String roleName = role.getRoleName();
                            String orgRole = orgName + "-" + roleName;
                            buffer.append(orgRole);
                        } else if (org != null) {
                            String orgName = org.getOrgName();
                            buffer.append(orgName);
                        }
                    }

                }
                authzCondition.setRoles(buffer.toString());
            }
            //获取数据
            List<MoiraiUserDataScope> userScopes = this.getUserScope(condition1);
            if (!StrUtils.isEmptyList(userScopes)) {
                authzCondition.setScope(userScopes.get(0).getScope());
            }

            list.add(authzCondition);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("userList", list);
        return map;
    }

    /**
     * 获取用户的分配的角色
     */
    public List<MoiraiUserAuthz> getUserRole(MoiraiUserCondition condition) {
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(condition.getUserId()).
            andTenantIdEqualTo(condition.getTenantId())
            .andUserOrgEqualTo(condition.getOrgId())
            .andRoleIdIsNotNull();
        return moiraiUserAuthzMapper.selectByExample(example);
    }

    /**
     * 获取用户对应的数据范围
     */
    public List<MoiraiUserDataScope> getUserScope(MoiraiUserCondition condition) {
        MoiraiUserDataScopeExample example = new MoiraiUserDataScopeExample();
        example.createCriteria().andUserIdEqualTo(condition.getUserId());
        return moiraiUserDataScopeMapper.selectByExample(example);
    }

    /**
     * 添加用户验证用户信息
     */
    public void checkUserInfo(MoiraiUserCondition condition) {
//        String userAccount = condition.getUserAccount();
//        String userEmail = condition.getUserEmail();
//        String telephone = condition.getTelephone();
//
//        MoiraiUser moiraiUser = new MoiraiUser();
//        moiraiUser.setUserAccount(userAccount);
//        moiraiUser.setTelephone(telephone);
//        moiraiUser.setUserEmail(userEmail);
//        moiraiUserService.checkParam(moiraiUser);
    }

    /**
     * 获取用户的数据范围信息
     */
    public List<MoiraiUserDataScope> getUserDataScope(MoiraiUserCondition condition) {
        MoiraiUserDataScopeExample example = new MoiraiUserDataScopeExample();
        example.createCriteria().andUserIdEqualTo(condition.getUserId());
        return moiraiUserDataScopeMapper.selectByExample(example);
    }

    /**
     * <B>方法名称：</B>E发票助手2.0赋权接口<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年3月22日
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult userEmpowerment(MoiraiUserCondition moiraiUserCondition) {
        logger.info("记录E发票调用定制赋权接口:{}", moiraiUserCondition);
        MoiraiUser moiraiUser = this.getEUser(moiraiUserCondition);
        List<Long> productList = moiraiUserCondition.getProductList();
        boolean flag = this.choseEPower(moiraiUser.getOrgId(), productList);
        //开通E发票应用
        if (!flag) {
            for (Long productId : productList) {
                if (productId == null) {
                    continue;
                }
                MoiraiOrgProduct moiraiOrgProduct = new MoiraiOrgProduct();
                moiraiOrgProduct.setProductId(productId);
                moiraiOrgProduct.setOrgId(moiraiUser.getOrgId());
                moiraiOrgProduct.setTenantId(moiraiUser.getTenantId());
                moiraiOrgProduct.setProductType(Constants.MOIRAI_PRODUCT_TYPE);
                moiraiOrgProduct.setOpenType(Constants.MOIRAI_PRODUCT_OPEN_TYPE);
                moiraiOrgProduct.setCreater(moiraiUser.getUserAccount());
                moiraiOrgService.addOrgProduct(moiraiOrgProduct);
            }
            moiraiOrgService.deleteCacheOrg(moiraiUser.getOrgId(), null);
        }
        List<Long> roleList = moiraiUserCondition.getRoleList();
        boolean roleFlag = this.choseERole(moiraiUser, roleList);
        //添加E发票默认角色
        if (!roleFlag) {
            for (Long roleId : roleList) {
                if (roleId == null) {
                    continue;
                }
                MoiraiUserAuthz authz = new MoiraiUserAuthz();
                authz.setAuthOrg(moiraiUser.getOrgId());
                authz.setUserId(moiraiUser.getUserId());
                authz.setUserOrg(moiraiUser.getOrgId());
                authz.setTenantId(moiraiUser.getTenantId());
                authz.setCreater(moiraiUser.getUserAccount());
                authz.setCreateTime(DateTimeUtils.nowTimeLong());
                authz.setRoleId(roleId);
                Long authzId = seqnumFeignClient.getNum(Constants.MOIRAI_USER_AUTHZ);
                authz.setUorId(authzId);
                moiraiUserAuthzMapper.insertSelective(authz);
            }
        }
        return new BWJsonResult();
    }

    private boolean choseERole(MoiraiUser moiraiUser, List<Long> roleList) {
        //是否有默认E发票角色
        boolean roleFlag = false;
        if (roleList != null && roleList.size() > 0) {
            MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
            example.createCriteria().andTenantIdEqualTo(moiraiUser.getTenantId()).andUserOrgEqualTo(moiraiUser.getOrgId())
                .andAuthOrgEqualTo(moiraiUser.getOrgId()).andUserIdEqualTo(moiraiUser.getUserId()).andRoleIdIsNotNull();
            List<MoiraiUserAuthz> moiraiUserAuthzs = moiraiUserAuthzMapper.selectByExample(example);
            if (moiraiUserAuthzs == null || moiraiUserAuthzs.size() == 0) {
                return roleFlag;
            }
            int i = roleList.size();
            for (Long roleId : roleList) {
                if (roleId == null) {
                    i--;
                    continue;
                }
                for (MoiraiUserAuthz authz : moiraiUserAuthzs) {
                    if (authz.getRoleId().equals(roleId)) {
                        i--;
                    }
                }
            }
            if (i == 0) {
                roleFlag = true;
            }
        } else {
            roleFlag = true;
        }
        return roleFlag;
    }

    private boolean choseEPower(Long orgId, List<Long> productList) {
        //判断机构是否满足E发票项目所需权限
        boolean flag = false;
        if (productList != null && productList.size() > 0) {
            List<MoiraiOrgProduct> orgProductList = moiraiOrgProductMapper.selectOrgProducts(orgId, null);
            if (orgProductList == null || orgProductList.size() == 0) {
                return flag;
            }
            int i = productList.size();
            for (Long productNew : productList) {
                if (productNew == null) {
                    i--;
                    continue;
                }
                MoiraiProduct product = moiraiProductMapper.selectByPrimaryKey(productNew);
                if (product == null) {
                    return flag;
                }
                Long belongProduct = product.getBelongProduct();
                for (int j = 0; j < orgProductList.size(); j++) {
                    MoiraiOrgProduct orgProduct = orgProductList.get(j);
                    Long productOld = orgProduct.getProductId();
                    Long belongProductOld = orgProduct.getBelongProduct();
                    if (productOld.equals(productNew) || productOld.equals(belongProduct)) {
                        i--;
                        break;
                    }
                    if (Constants.OUTPUT_FREE_PRODUCT.equals(productNew) || (Constants.OUTPUT_STANDARD_PRODUCT.equals(productNew) && Constants.OUTPUT_FREE_PRODUCT.equals(productOld))) {
                        continue;
                    }
                    /** 临时方案--标准版应用不能与销项其他应用同时开通，免费版除外，E发票盒子略过 **/
                    if (Constants.OUTPUT_STANDARD_PRODUCT.equals(productOld) && belongProduct != null && belongProduct.equals(belongProductOld) && productNew.equals(9001L)) {
                        i--;
                        orgProductList.remove(orgProduct);
                        continue;
                    }
                    if (Constants.OUTPUT_STANDARD_PRODUCT.equals(productNew) && belongProduct.equals(belongProductOld)) {
                        throw new MoiraiException(MoiraiErrorEnum.MOIRAI_OUTPUT_STANDARD_PRODUCT_ERROR);
                    }
                    if (Constants.OUTPUT_STANDARD_PRODUCT.equals(productOld) && belongProduct != null && belongProduct.equals(belongProductOld)) {
                        throw new MoiraiException(MoiraiErrorEnum.MOIRAI_OUTPUT_STANDARD_PRODUCT_ERROR);
                    }
                }
            }
            if (i == 0) {
                flag = true;
            }
        } else {
            flag = true;
        }
        return flag;
    }

    private MoiraiUser getEUser(MoiraiUserCondition moiraiUserCondition) {
        //查询机构管理员
        List<MoiraiUser> moiraiUser = moiraiUserMapper.findUserByCondition(moiraiUserCondition);
        if (moiraiUser == null || moiraiUser.size() == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
        }
        return moiraiUser.get(0);
    }

    @Override
    public BWJsonResult userEPower(MoiraiUserCondition moiraiUserCondition) {
        logger.info("检查用户是否满足E发票所需权限:{}", moiraiUserCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        MoiraiUser moiraiUser = this.getEUser(moiraiUserCondition);
        //判断机构是否开通E发票或销项
        boolean flag = this.choseEPower(moiraiUser.getOrgId(), moiraiUserCondition.getProductList());
        if (!flag) {
            bwJsonResult.setMessage("无权限");
            bwJsonResult.setSuccess(false);
            return bwJsonResult;
        }
        boolean roleFlag = this.choseERole(moiraiUser, moiraiUserCondition.getRoleList());
        if (!roleFlag) {
            bwJsonResult.setMessage("无权限");
            bwJsonResult.setSuccess(false);
            return bwJsonResult;
        }
        bwJsonResult.setMessage("有权限");
        return bwJsonResult;
    }

    @Override
    public BWJsonResult<Map<String, List<MoiraiRole>>> getRoleListByAuth(MoiraiUserCondition condition) {
        //查询本机构角色及通用角色
        MoiraiRole role = new MoiraiRole();
        role.setTenantId(condition.getTenantId());
        role.setOrgId(condition.getOrgId());
        List<MoiraiRole> moiraiRoles = moiraiRoleMapper.selectAuthRoles(role);
        if (moiraiRoles == null || moiraiRoles.size() == 0) {
            logger.info("***角色列列表为空***");
            return new BWJsonResult<>();
        }
        //查询用户已授权角色
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(condition.getUserId()).andTenantIdEqualTo(condition.getTenantId());
        List<MoiraiUserAuthz> authzs = moiraiUserAuthzMapper.selectByExample(example);
        Map<String, List<MoiraiRole>> resMap = new HashMap<>();
        if (authzs == null || authzs.size() == 0) {
            logger.info("***已授权角色为空***");
            resMap.put("unAuthRoleList", moiraiRoles);
        } else {
            List<MoiraiRole> unAuthRoleList = new ArrayList<>();
            List<MoiraiRole> authRoleList = new ArrayList<>();
            List<Long> roleIds = new ArrayList<>();
            authzs.forEach(auth -> roleIds.add(auth.getRoleId()));
            moiraiRoles.forEach(roles -> {
                if (roleIds.contains(roles.getRoleId())) {
                    authRoleList.add(roles);
                } else {
                    unAuthRoleList.add(roles);
                }
            });
            resMap.put("unAuthRoleList", unAuthRoleList);
            resMap.put("authRoleList", authRoleList);
        }
        BWJsonResult bwJsonResult = new BWJsonResult(resMap);
        bwJsonResult.setMessage("查询成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>心授权页面查询角色列表<BR>
     * <B>概要说明：</B>区分授权和非授权<BR>
     *
     * @return
     * @since 2019年5月16日
     */
    @Override
    public List<MoiraiRole> getRoleListByUserAuth(MoiraiUserCondition condition) {
        Long tenantId = condition.getTenantId();
        Long orgId = condition.getOrgId();
        //查询本机构角色及通用角色
        MoiraiRole role = new MoiraiRole();
        role.setTenantId(condition.getTenantId());
        role.setOrgId(condition.getOrgId());
        MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
        Long createTime = moiraiTenant.getCreateTime();
        List<Long> productList = null;
        if (!StrUtils.isEmpty(createTime + "") && createTime >= Long.valueOf(sysRoleInitTime) && !StrUtils.isEmpty(orgId + "")) {
            MoiraiOrgProduct product = new MoiraiOrgProduct();
            product.setOrgId(orgId);
            product.setUseFlag("Y");
            product.setDelFlag("N");
            List<MoiraiOrgProduct> products = moiraiOrgProductMapper.findOrgProductByCondition(product);
            productList = products.stream().map(item -> item.getProductId()).collect(Collectors.toList());
            role.setProducts(productList);
        }
        List<MoiraiRole> moiraiRoles = moiraiRoleMapper.selectAuthRoles(role);
        if (moiraiRoles == null || moiraiRoles.size() == 0) {
            logger.info("***角色列列表为空***");
            return null;
        }
        //查询用户已授权角色
        Long userId = condition.getUserId();
        if (userId == null) {
            condition.setUserId(moiraiSysService.gainCacheUserId());
        }
        MoiraiUserAuthzExample userAuthzExample = new MoiraiUserAuthzExample();
        MoiraiUserAuthzExample.Criteria criteria = userAuthzExample.createCriteria();
        criteria.andUserIdEqualTo(userId);
        List<MoiraiUserAuthz> authzs = moiraiUserAuthzMapper.selectByExample(userAuthzExample);
        List<MoiraiRole> unAuthRoleList = new ArrayList<>();
        List<MoiraiRole> authRoleList = new ArrayList<>();
        List<Long> roleIds = new ArrayList<>();
        authzs.forEach(auth -> roleIds.add(auth.getRoleId()));
        moiraiRoles.forEach(roles -> {
            if (roleIds.contains(roles.getRoleId())) {
                authRoleList.add(roles);
            } else {
                unAuthRoleList.add(roles);
            }
        });
        if (Constants.MOIRAI_USER_UNAUTHLIST.equals(condition.getOperation())) {
            return unAuthRoleList;
        } else {
            return authRoleList;
        }
    }

    @Override
    public List<MoiraiOrg> getAuthOrgByRole(MoiraiUserAuthz userAuthz) {
        //查询用户已授权机构
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(userAuthz.getUserId()).andTenantIdEqualTo(userAuthz.getTenantId()).andRoleIdEqualTo(userAuthz.getRoleId());
        List<MoiraiUserAuthz> authzs = moiraiUserAuthzMapper.selectByExample(example);
        List<Long> list = new ArrayList<>();
        authzs.forEach(auth -> {
            list.add(auth.getAuthOrg());
        });
        Map<String, List> moiraiOrgList = new HashMap<>();
        moiraiOrgList.put("orgId", list);
        return moiraiOrgMapper.queryOrgListByCondition(moiraiOrgList);
    }

    /**
     * <B>方法名称：</B>心授权页面查询授权机构<BR>
     * <B>概要说明：</B>点击角色时显示授权树展示以授权机构<BR>
     *
     * @return
     * @since 2019年5月19日
     */
    @Override
    public List<Long> getAuthOrgIdsByRole(MoiraiUserAuthz authz) {
        //查询用户已授权机构
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(authz.getUserId()).andTenantIdEqualTo(authz.getTenantId()).andRoleIdEqualTo(authz.getRoleId());
        List<MoiraiUserAuthz> authzs = moiraiUserAuthzMapper.selectByExample(example);
        if (authzs != null && authzs.size() > 0) {
            List<Long> list = new ArrayList<>();
            authzs.forEach(auth -> {
                list.add(auth.getAuthOrg());
            });
            return list;
        }
        return null;
    }

    /**
     * <B>方法名称：</B>保存授权<BR>
     * <B>概要说明：</B>页面点击保存按钮调用<BR>
     *
     * @return
     * @since 2019年5月19日
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult saveUserAuth(MoiraiUserAuthz authz) {
        if (authz.getAuthOrgs() == null || authz.getAuthOrgs().size() == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_AUTHZ_ORG_NOT_NULL);
        }
        Long tenantId = authz.getTenantId();
        Long userId = authz.getUserId();
        BWJsonResult bwJsonResult = new BWJsonResult();
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andUserIdEqualTo(userId).
            andUserOrgEqualTo(authz.getUserOrg()).andRoleIdEqualTo(authz.getRoleId());
        int count = moiraiUserAuthzMapper.deleteByExample(example);
        List<Long> authOrgs = authz.getAuthOrgs();
        Long nowTimeLong = DateTimeUtils.nowTimeLong();
        List<MoiraiUserAuthz> authzs = new ArrayList<>();
        List<Long> nums = moiraiSysService.getNums(authOrgs, Constants.MOIRAI_USER_AUTHZ);
        int i = 0;
        for (Long orgId : authOrgs) {
            MoiraiUserAuthz moiraiUserAuthz = new MoiraiUserAuthz();
            moiraiUserAuthz.setUorId(nums.get(i));
            moiraiUserAuthz.setTenantId(tenantId);
            moiraiUserAuthz.setUserId(userId);
            moiraiUserAuthz.setUserOrg(authz.getUserOrg());
            moiraiUserAuthz.setRoleId(authz.getRoleId());
            moiraiUserAuthz.setRoleOrg(authz.getRoleOrg());
            moiraiUserAuthz.setAuthOrg(orgId);
            moiraiUserAuthz.setCreateTime(nowTimeLong);
            moiraiUserAuthz.setCreater(authz.getCreater());
            authzs.add(moiraiUserAuthz);
            i++;
        }
        moiraiUserAuthzMapper.batchInsert(authzs);
        moiraiUserService.saveUserDataScope(userId, nowTimeLong, authz.getCreater(), authz.getScope());
        resetUserAuth(tenantId, userId);
        bwJsonResult.setMessage("授权成功");
        return bwJsonResult;
    }

    @Override
    public BWJsonResult delUserAuth(MoiraiUserAuthz authz) {
        logger.info("移除授权请求:{}", authz);
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andTenantIdEqualTo(authz.getTenantId()).andUserIdEqualTo(authz.getUserId()).andRoleIdEqualTo(authz.getRoleId());
        int count = moiraiUserAuthzMapper.deleteByExample(example);
        resetUserAuth(authz.getTenantId(), authz.getUserId());
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("移除授权成功");
        return bwJsonResult;
    }

    public void resetUserAuth(Long tenantId, Long userId) {
        HttpServletRequest request = WebContext.getRequest();
        String bwToken = HttpInfoUtils.getToken(Constants.BWTOKEN, request);
        Object token = redisTemplate.opsForValue().get(Constants.REDIS_ACCESSTOKEN + bwToken);
        if (token != null) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(token.toString());
                Long cacheUserId = Long.valueOf(jsonObject.get("userId").toString());
                Long cacheOrgId = Long.valueOf(jsonObject.get("orgId").toString());
                if (userId.equals(cacheUserId)) {
                    MoiraiUserCondition condition = new MoiraiUserCondition();
                    condition.setTenantId(tenantId);
                    condition.setUserId(userId);
                    condition.setOrgId(cacheOrgId);
                    List<String> service = getResourceAuthcService(condition);
                    jsonObject.put("userAuth", service);
                    redisTemplate.opsForValue().set(Constants.REDIS_ACCESSTOKEN + bwToken, jsonObject.toString(), 3600l, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_TENANT_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            }
        }
    }

    @Override
    public void downloadUserAuthTemplate(Long userId, HttpServletResponse response) {
        logger.info("当前操作用户ID:{}", userId);
        //查询用户已授权机构
        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<MoiraiUserAuthz> authzs = moiraiUserAuthzMapper.selectByExample(example);
        List<Long> authOrgId = new ArrayList<>();
        authzs.forEach(orgId -> authOrgId.add(orgId.getAuthOrg()));

        if (!authOrgId.isEmpty()) {
            MoiraiUserCondition userCondition = new MoiraiUserCondition();
            userCondition.setAuthOrgIds(authOrgId);
            List<MoiraiUser> user = moiraiUserMapper.findUserListByCondition(userCondition);
            MoiraiUser moiraiUser = user.get(0);
            MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(moiraiUser.getTenantId());
            createSheet(user, moiraiTenant.getDualFactor(), response);
        } else {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
    }

    @Override
    public BWJsonResult batchAuthResource(MoiraiResourceCondition resourceCondition) {
        Long resourceId = resourceCondition.getResourceId();
        String delFlag = resourceCondition.getDelFlag();
        //取消赋权
        if (Constants.flag_Y.equals(delFlag)) {
            MoiraiRoleResource roleResource = new MoiraiRoleResource();
            roleResource.setResourceId(resourceId);
            moiraiRoleResourceMapper.deleteById(roleResource);
            return new BWJsonResult();
        }
        //批量赋权
        Long pid = resourceCondition.getPid();
        if (pid == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiRoleResource roleResource = new MoiraiRoleResource();
        roleResource.setResourceId(pid);
        List<MoiraiRoleResource> roleResources = moiraiRoleResourceMapper.selectNoAuthRole(pid, resourceId);
        int size = roleResources.size();
        logger.info("需要批量赋权的角色ID条数:{}", size);
        if (size == 0) {
            return new BWJsonResult();
        }
        int count = (size / Constants.USEREXCEL_MAXCOUNT) + 1;
        Long nowTimeLong = DateTimeUtils.nowTimeLong();
        for (int i = 0; i < count; i++) {
            List<MoiraiRoleResource> newlist = new ArrayList<>();
            List<MoiraiRoleResource> newResourceList = null;
            if ((i + 1) == count) {
                int startIndex = (i * Constants.USEREXCEL_MAXCOUNT);
                int endIndex = size;
                newResourceList = roleResources.subList(startIndex, endIndex);
            } else {
                int startIndex = (i * Constants.USEREXCEL_MAXCOUNT);
                int endIndex = (i + 1) * Constants.USEREXCEL_MAXCOUNT;
                newResourceList = roleResources.subList(startIndex, endIndex);
            }
            List<Long> nums = seqnumFeignClient.getNums(Constants.MOIRAI_ROLE_RESOURCE, newResourceList.size());
            for (int j = 0; j < newResourceList.size(); j++) {
                MoiraiRoleResource resource = new MoiraiRoleResource();
                MoiraiRoleResource role = newResourceList.get(j);
                resource.setRoleId(role.getRoleId());
                resource.setResourceId(resourceId);
                resource.setRoleResourceId(nums.get(j));
                resource.setTenantId(role.getTenantId());
                resource.setOrgId(role.getOrgId());
                resource.setCreater(resourceCondition.getCreater());
                resource.setCreatetime(nowTimeLong);
                resource.setModifyUser(resourceCondition.getCreater());
                resource.setModifyTime(nowTimeLong);
                resource.setProductId(role.getProductId());
                newlist.add(resource);
            }
            if (newlist != null && newlist.size() > 0) {
                moiraiRoleResourceMapper.insertBatchInfo(newlist);
            }
        }
        return new BWJsonResult();
    }

    private void createSheet(List<MoiraiUser> userList, String dualFactor, HttpServletResponse response) {
        // create an Excel workbook
        WriteExcel writer = new WriteExcel();
        // create a first worksheet
        Sheet sheet1 = writer.createSheet("用户认证信息批量导入文件");
        // create header of table for first worksheet with style
        writer.createRow(sheet1, new String[] {"用户认证信息批量导入文件"}, true, true);
        // merge cell
        CellRangeAddress rg0_0 = new CellRangeAddress(0, 0, 0, 2);
        sheet1.addMergedRegion(rg0_0);
        switch (dualFactor) {
            case Constants.MOIRAI_DUCL_FACTOR_DEFAULT:
            case Constants.MOIRAI_DUCL_FACTOR_EMAIL:
                writer.createRow(sheet1, new String[] {"用户账号", "用户姓名", "邮箱地址"}, true, true);
                for (MoiraiUser moiraiUser : userList) {
                    String email = moiraiUser.getUserEmail();
                    if (!RegularExpUtils.checkEmail(email)) {
                        writer.createRow(sheet1, new String[] {moiraiUser.getUserAccount(), moiraiUser.getUserName(), email}, false, true);
                    }
                }
                break;
            case Constants.MOIRAI_DUCL_FACTOR_PHONE:
                writer.createRow(sheet1, new String[] {"用户账号", "用户姓名", "手机号码"}, true, true);
                for (MoiraiUser moiraiUser : userList) {
                    String telephone = moiraiUser.getTelephone();
                    if (!RegularExpUtils.checkMobile(telephone)) {
                        writer.createRow(sheet1, new String[] {moiraiUser.getUserAccount(), moiraiUser.getUserName(), telephone}, false, true);
                    }
                }
                break;
            default:
                break;
        }
        Sheet sheet2 = writer.createSheet("备注");
        writer.createRow(sheet2, new String[] {"字段名称", "备注"}, true, true);
        writer.createRow(sheet2, new String[] {"用户账号", "系统导出时的用户账号，不要修改"}, false, true);
        writer.createRow(sheet2, new String[] {"用户姓名", "系统导出时用户姓名，不需修改，起到参照作用"}, false, true);
        writer.createRow(sheet2, new String[] {"*手机号码/邮箱地址*", "**根据用户账号补充填写账号对应的手机号码/邮箱地址**"}, false, true);
        writer.setAutoSizeColumns(sheet1, false);
        writer.setAutoSizeColumns(sheet2, false);

        response.setContentType("application/vnd.ms-excel");
        try {
            response.setHeader("Content-Disposition", "attachment;filename="
                + new String(java.net.URLEncoder.encode("userTemplate.xlsx", "utf-8").getBytes(), "iso8859-1"));
            ServletOutputStream stream = response.getOutputStream();
            writer.getWorkbook().write(stream);
        } catch (IOException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR);
        }
    }

    @Override
    public BWJsonResult<MoiraiUserCondition> findBUserListByCondition(MoiraiUserCondition condition) {
        PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        List<MoiraiUserCondition> userList = moiraiUserMapper.findBUserListByCondition(condition);
        PageInfo pageInfo = new PageInfo(userList);
        return new BWJsonResult<>(userList, (int) pageInfo.getTotal());
    }

}
