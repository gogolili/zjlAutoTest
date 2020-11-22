package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiProductMapper;
import com.baiwang.moirai.mapper.MoiraiResourceMapper;
import com.baiwang.moirai.mapper.MoiraiRoleMapper;
import com.baiwang.moirai.mapper.MoiraiRoleResourceMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.Menu;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleCondition;
import com.baiwang.moirai.model.role.MoiraiRoleResource;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiRoleService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoiraiRoleServiceImpl implements MoiraiRoleService {
    private Logger logger = LoggerFactory.getLogger(MoiraiRoleServiceImpl.class);

    @Autowired
    private MoiraiRoleMapper roleMapper;

    @Autowired
    private MoiraiRoleResourceMapper roleResourceMapper;

    @Autowired
    private MoiraiProductMapper moiraiProductMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    private SeqnumFeignClient seqnum;

    @Autowired
    private MoiraiResourceMapper moiraiResourceMapper;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Value("${system.prepared.role.init.time}")
    private String sysRoleInitTime;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(MoiraiRole moiraiRole) {
        logger.info("添加角色请求参数：" + moiraiRole);
        String name = moiraiRole.getRoleName();
        if (StrUtils.isEmpty(name) || name.length() > 30) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_NAME_ERROR);
        }
        if (!StrUtils.isEmpty(moiraiRole.getRoleDescribe()) && moiraiRole.getRoleDescribe().length() > 100) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_DESCRIPTION_ERROR);
        }
        MoiraiRole checkRole = new MoiraiRole();
        checkRole.setRoleName(name);
        checkRole.setTenantId(moiraiRole.getTenantId());
        List<MoiraiRole> checkNameist = roleMapper.selectByBean(checkRole);
        if (checkNameist.size() > 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_NAME_REPEAT);
        }
        MoiraiOrg org = moiraiOrgService.selectByOrgId(moiraiRole.getOrgId());
        moiraiRole.setLowerSee("N");
        if (org != null && org.getParentOrg().equals(0L)) {
            moiraiRole.setLowerSee("Y");
        }
        Long roleId = seqnum.getNum(Constants.MOIRAI_ROLE);
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiRole.setCreatetime(nowTime);
        moiraiRole.setModifyTime(nowTime);
        //准备插库
        moiraiRole.setRoleId(roleId);
        insertRoleResource(moiraiRole);

        moiraiRole.setCreatetime(nowTime);
        moiraiRole.setModifyTime(nowTime);
        moiraiRole.setModifyUser(moiraiRole.getCreater());
        roleMapper.insertSelective(moiraiRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSysRole(MoiraiRole moiraiRole) {
        logger.info("添加系统角色请求参数：" + moiraiRole);
        String name = moiraiRole.getRoleName();
        if (StrUtils.isEmpty(name) || name.length() > 30) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_NAME_ERROR);
        }
        if (!StrUtils.isEmpty(moiraiRole.getRoleDescribe()) && moiraiRole.getRoleDescribe().length() > 100) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_DESCRIPTION_ERROR);
        }
        MoiraiRole checkRole = new MoiraiRole();
        checkRole.setRoleName(name);
        checkRole.setTenantId(1L);
        List<MoiraiRole> checkNameist = roleMapper.selectByBean(checkRole);
        if (checkNameist.size() > 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_NAME_REPEAT);
        }

        Long roleId = seqnum.getNum(Constants.MOIRAI_ROLE);
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiRole.setCreatetime(nowTime);
        moiraiRole.setModifyTime(nowTime);
        //准备插库
        moiraiRole.setRoleId(roleId);
        moiraiRole.setTenantId(1L);
        moiraiRole.setOrgId(1L);
        insertRoleResource(moiraiRole);
        moiraiRole.setDefaultFlag("Y");
        moiraiRole.setLowerSee(moiraiRole.getLowerSee());
        moiraiRole.setCreatetime(nowTime);
        moiraiRole.setModifyTime(nowTime);
        moiraiRole.setModifyUser(moiraiRole.getCreater());
        roleMapper.insertSelective(moiraiRole);
    }

    /**
     * 修改角色时调用
     *
     * @param moiraiRole
     * @return
     */
    @Override
    public MoiraiRole queryRolesInfo(MoiraiRole moiraiRole) {
        logger.info("查询角色请求参数：" + moiraiRole);
        try {
            MoiraiRole queryMoiraiRole = roleMapper.selectByPrimaryKey(moiraiRole.getRoleId());
            if (queryMoiraiRole == null) {
                return null;
            }
            //根据角色id查询关联表信息
            MoiraiRoleResource moiraiRoleResource = new MoiraiRoleResource();
            moiraiRoleResource.setRoleId(moiraiRole.getRoleId());
            //该角色拥有的资源列表
            List<MoiraiRoleResource> listMoiraiRoleResource = roleResourceMapper.selectByBean(moiraiRoleResource);
            List<Long> idListRole = new ArrayList<>();
            int MoiraiRoleResourceSize = listMoiraiRoleResource.size();
            for (int i = 0; i < MoiraiRoleResourceSize; i++) {
                idListRole.add(listMoiraiRoleResource.get(i).getResourceId());
            }
            // 根据orgid查询产品list
            List<MoiraiOrgProduct> moiraiOrgProductList = moiraiOrgService.getOrgProducts(moiraiRole.getOrgId());
            if (StrUtils.isEmptyList(moiraiOrgProductList)) {
                return null;
            }
            //获取产品id列表
            List<MoiraiResource> resourceList = new ArrayList<>();
            for (MoiraiOrgProduct product : moiraiOrgProductList) {
                List<MoiraiResource> moiraiResources = this.getProductResources(moiraiRole.getTenantId(), product.getProductId());
                if (moiraiResources != null && moiraiResources.size() > 0) {
                    MoiraiResource moiraiResource = new MoiraiResource();
                    for (MoiraiResource resource : moiraiResources) {
                        if (idListRole.contains(resource.getResourceId())) {
                            resource.setHasPower(Constants.DEFAULT_ONE);
                            if (Constants.DEFAULT_ZERO.equals(moiraiResource.getHasPower())) {
                                moiraiResource.setHasPower(Constants.DEFAULT_ONE);
                            }
                        }
                    }
                    List<MoiraiResource> resourceTree = AdminUtils.getResourceTree(moiraiResources, 0L);
                    //产品强行加入顶级节点-形成树
                    moiraiResource.setResourceName(product.getProductName());
                    //表示这个resource是产品
                    moiraiResource.setResourceType(Constants.DEFAULT_PRODUCT);
                    moiraiResource.setResourceId(product.getProductId());
                    moiraiResource.setChildren(resourceTree);
                    resourceList.add(moiraiResource);
                }
            }
            queryMoiraiRole.setResourceList(resourceList);
            return queryMoiraiRole;
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_ERROR);
        }
    }

    /**
     * 查询角色列表时显示角色资源调用
     *
     * @param moiraiRole
     * @return
     */
    @Override
    public MoiraiRole queryOrgRoleInfo(MoiraiRole moiraiRole) {
        logger.info("查询角色请求参数：" + moiraiRole);
        try {
            MoiraiRole queryMoiraiRole = roleMapper.selectByPrimaryKey(moiraiRole.getRoleId());
            if (queryMoiraiRole == null) {
                return null;
            }
            //根据角色id查询关联表信息
            MoiraiRoleResource moiraiRoleResource = new MoiraiRoleResource();
            moiraiRoleResource.setRoleId(moiraiRole.getRoleId());
            //该角色拥有的资源列表-----------------------
            List<MoiraiRoleResource> listMoiraiRoleResource = roleResourceMapper.selectByBean(moiraiRoleResource);
            List<Long> idListRole = new ArrayList<>();
            int listMoiraiRoleResourceSize = listMoiraiRoleResource.size();
            for (int i = 0; i < listMoiraiRoleResourceSize; i++) {
                idListRole.add(listMoiraiRoleResource.get(i).getResourceId());
            }
            List<MoiraiResource> resourceList = new ArrayList<>();
            String defaultFlag = queryMoiraiRole.getDefaultFlag();
            String lowerSee = queryMoiraiRole.getLowerSee();
            Long productId = queryMoiraiRole.getAssociatedProductId();
            if ("Y".equals(defaultFlag) && "Y".equals(lowerSee)) {
                MoiraiProduct moiraiProduct = moiraiProductMapper.selectByPrimaryKey(productId);
                MoiraiResource moiraiResource = new MoiraiResource();
                List<MoiraiResource> moiraiResources = moiraiResourceMapper.selectBatchInfo(idListRole);
                List<MoiraiResource> resourceTree = AdminUtils.getResourceTree(moiraiResources, 0L);
                moiraiResource.setResourceName(moiraiProduct.getProductName());
                moiraiResource.setChildren(resourceTree);
                resourceList.add(moiraiResource);
                queryMoiraiRole.setResourceList(resourceList);
                return queryMoiraiRole;
            }
            List<MoiraiProduct> moiraiProducts = moiraiProductMapper.selectBatchInfo(idListRole);
            //根据组织id获取产品列表
            List<MoiraiOrgProduct> moiraiOrgProductList = moiraiOrgService.getOrgProducts(queryMoiraiRole.getOrgId());
            if (StrUtils.isEmptyList(moiraiOrgProductList)) {
                return null;
            }
            List<Long> list = new ArrayList();
            moiraiOrgProductList.forEach(item -> list.add(item.getProductId()));
            moiraiProducts = moiraiProducts.stream().filter(item -> list.contains(item.getProductId())).collect(Collectors.toList());
            //获取产品id列表

            for (MoiraiProduct product : moiraiProducts) {
                List<MoiraiResource> moiraiResources = this.getProductResources(moiraiRole.getTenantId(), product.getProductId());
                if (moiraiResources != null && moiraiResources.size() > 0) {
                    MoiraiResource moiraiResource = new MoiraiResource();
                    //取交集
                    List<MoiraiResource> result = moiraiResources.stream().filter(item -> idListRole.contains(item.getResourceId())).collect(Collectors.toList());
                    List<MoiraiResource> resourceTree = AdminUtils.getResourceTree(result, 0L);
                    //产品强行加入顶级节点-形成树
                    moiraiResource.setResourceName(product.getProductName());
                    moiraiResource.setChildren(resourceTree);
                    resourceList.add(moiraiResource);
                }
            }
            queryMoiraiRole.setResourceList(resourceList);
            return queryMoiraiRole;
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_ERROR);
        }
    }

    @Override
    public BWJsonResult<MoiraiRoleCondition> queryRolesList(MoiraiRoleCondition moiraiRoleCondition) {
        logger.info("查询角色列表请求参数：" + moiraiRoleCondition);

        Long userId = moiraiRoleCondition.getUserId();
        String roleName = moiraiRoleCondition.getRoleName();
        Long tenantId = moiraiRoleCondition.getTenantId();
        Long resourceId = moiraiRoleCondition.getResourceId();
        Long orgId = moiraiRoleCondition.getOrgId();
        Long createTime = null;
        String tenantName = null;
        List<MoiraiRole> moiraiRoles;
        if ("Y".equals(moiraiRoleCondition.getDefaultFlag())) {
            PageHelper.startPage(moiraiRoleCondition.getPageNo(), moiraiRoleCondition.getPageSize());
            moiraiRoles = roleMapper.selectByBean(moiraiRoleCondition);
        } else {
            MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
            if (moiraiTenant == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
            }
            createTime = moiraiTenant.getCreateTime();
            tenantName = moiraiTenant.getTenantName();
            List<Long> productList = null;
            if (!StrUtils.isEmpty(createTime + "") && createTime >= Long.valueOf(sysRoleInitTime) && !StrUtils.isEmpty(orgId + "")) {
                MoiraiOrgProduct product = new MoiraiOrgProduct();
                product.setOrgId(orgId);
                product.setUseFlag("Y");
                product.setDelFlag("N");
                List<MoiraiOrgProduct> products = moiraiOrgProductMapper.findOrgProductByCondition(product);
                productList = products.stream().map(item -> item.getProductId()).collect(Collectors.toList());
            }
            if (userId == null) {
                userId = moiraiSysService.gainCacheUserId();
            }
            PageHelper.startPage(moiraiRoleCondition.getPageNo(), moiraiRoleCondition.getPageSize());
            moiraiRoles = roleMapper.selectOrgShowRoles(roleName, tenantId, productList, userId, resourceId);
        }

        if (moiraiRoles != null && moiraiRoles.size() > 0) {
            PageInfo<MoiraiRole> pageInfo = new PageInfo(moiraiRoles);
            List<Long> orgIdList = new ArrayList<>();
            moiraiRoles.forEach(role -> orgIdList.add(role.getOrgId()));
            Map<String, List> orgIdMap = new HashMap<>();
            orgIdMap.put("orgId", orgIdList);
            List<MoiraiOrg> orgList = moiraiOrgMapper.queryOrgListByCondition(orgIdMap);
            List<MoiraiUserAuthz> topUserAuth = moiraiUserAuthzMapper.selectTopOrgUserAuth(userId, tenantId);
            List<MoiraiRoleCondition> returnRoles = new ArrayList<>(moiraiRoles.size());
            for (MoiraiRole role : moiraiRoles) {
                MoiraiRoleCondition roleCondition = new MoiraiRoleCondition();
                BeanUtils.copyProperties(role, roleCondition);
                String defaultFlag = role.getDefaultFlag();
                String lowerSee = role.getLowerSee();
                if ("Y".equals(defaultFlag) && "Y".equals(lowerSee)) {
                    roleCondition.setOrgName(tenantName);
                    roleCondition.setModifyUser("系统");
                    roleCondition.setModifyTime(createTime);
                } else {
                    for (MoiraiOrg org : orgList) {
                        if (role.getOrgId().equals(org.getOrgId())) {
                            roleCondition.setOrgName(org.getOrgName());
                        }
                    }
                    if ("N".equals(defaultFlag) && "Y".equals(lowerSee) && topUserAuth.isEmpty()) {
                        roleCondition.setIsCheck("N");
                    }
                }
                returnRoles.add(roleCondition);
            }
            return new BWJsonResult<>(returnRoles, (int) pageInfo.getTotal());
        }
        return new BWJsonResult<>();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult editRoles(MoiraiRole moiraiRole) {
        logger.info("修改角色信息请求参数：" + moiraiRole);
        MoiraiRole checkRoleByid = roleMapper.selectByPrimaryKey(moiraiRole.getRoleId());
        if (checkRoleByid == null) {
            return new BWJsonResult();
        }
        if (!StrUtils.isEmpty(moiraiRole.getRoleName())) {
            String roleNameDb = checkRoleByid.getRoleName();
            String roleName = moiraiRole.getRoleName();
            MoiraiRole checkRole = new MoiraiRole();
            if (!roleNameDb.equals(roleName)) {
                checkRole.setRoleName(roleName);
                checkRole.setTenantId(checkRoleByid.getTenantId());
                logger.info("查询内容入参：" + checkRole);
                List<MoiraiRole> checkNameistByName = roleMapper.selectByBean(checkRole);
                if (checkNameistByName.size() > 0) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_NAME_REPEAT);
                }
            }
        }
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiRole.setModifyTime(nowTime);
        insertRoleResource(moiraiRole);

        //特定参数不可修改
        moiraiRole.setCreatetime(null);
        moiraiRole.setCreater(null);
        moiraiRole.setTenantId(null);
        moiraiRole.setOrgId(null);
        moiraiRole.setLowerSee(null);
        roleMapper.updateByPrimaryKeySelective(moiraiRole);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("修改成功！");
        return bwJsonResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult editSysRoles(MoiraiRole moiraiRole) {
        logger.info("修改系统角色信息请求参数：" + moiraiRole);
        MoiraiRole checkRoleByid = roleMapper.selectByPrimaryKey(moiraiRole.getRoleId());
        if (checkRoleByid == null) {
            return new BWJsonResult();
        }
        if (checkRoleByid.getAssociatedProductId() == null || Constants.flag_N.equals(checkRoleByid.getDefaultFlag())) {
            throw new MoiraiException("-1", "该角色不能修改！");
        }
        if (!StrUtils.isEmpty(moiraiRole.getRoleName())) {
            String roleNameDb = checkRoleByid.getRoleName();
            String roleName = moiraiRole.getRoleName();
            MoiraiRole checkRole = new MoiraiRole();
            if (!roleNameDb.equals(roleName)) {
                checkRole.setRoleName(roleName);
                checkRole.setTenantId(1L);
                logger.info("查询内容入参：" + checkRole);
                List<MoiraiRole> checkNameistByName = roleMapper.selectByBean(checkRole);
                if (checkNameistByName.size() > 0) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ROLE_NAME_REPEAT);
                }
            }
        }

        int val = moiraiUserAuthzMapper.findCountByRoleId(moiraiRole.getRoleId());
        if (val > 0) {
            moiraiRole.setLowerSee(null);
        }

        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiRole.setModifyTime(nowTime);
        moiraiRole.setModifyUser(moiraiRole.getModifyUser());
        moiraiRole.setTenantId(1L);
        moiraiRole.setOrgId(1L);
        insertRoleResource(moiraiRole);

        //特定参数不可修改
        moiraiRole.setCreatetime(null);
        moiraiRole.setCreater(null);
        moiraiRole.setTenantId(null);
        moiraiRole.setOrgId(null);
        //系统内置角色 预制角色
        moiraiRole.setDefaultFlag(null);
        moiraiRole.setAssociatedProductId(null);
        roleMapper.updateByPrimaryKeySelective(moiraiRole);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("修改成功！");
        return bwJsonResult;
    }

    private void insertRoleResource(MoiraiRole moiraiRole) {
        if (moiraiRole.getMenuList() != null && moiraiRole.getMenuList().size() > 0) {
            MoiraiRoleResource moiraiRoleResource = new MoiraiRoleResource();
            moiraiRoleResource.setRoleId(moiraiRole.getRoleId());
            roleResourceMapper.deleteById(moiraiRoleResource);
            logger.info("角色资源删除成功");

            List<MoiraiRoleResource> moiraiRoleResourceList = new ArrayList<>();
            List<Menu> menuList = moiraiRole.getMenuList();
            List<Long> nums = moiraiSysService.getNums(menuList, Constants.MOIRAI_ROLE_RESOURCE);
            Long productId = null;
            for (int i = 0; i < menuList.size(); i++) {
                if (Integer.valueOf(10).equals(menuList.get(i).getResourceType())) {
                    productId = menuList.get(i).getResourceId();
                    continue;
                }
                moiraiRoleResource = new MoiraiRoleResource();
                moiraiRoleResource.setProductId(productId);
                moiraiRoleResource.setRoleResourceId(nums.get(i));
                moiraiRoleResource.setRoleId(moiraiRole.getRoleId());
                moiraiRoleResource.setResourceId(menuList.get(i).getResourceId());
                moiraiRoleResource.setOrgId(moiraiRole.getOrgId());
                moiraiRoleResource.setTenantId(moiraiRole.getTenantId());
                moiraiRoleResource.setCreater(moiraiRole.getModifyUser());
                moiraiRoleResource.setCreatetime(moiraiRole.getModifyTime());
                moiraiRoleResource.setModifyTime(moiraiRole.getModifyTime());
                moiraiRoleResourceList.add(moiraiRoleResource);
            }
            roleResourceMapper.insertBatchInfo(moiraiRoleResourceList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult delRoles(MoiraiRole moiraiRole) {
        logger.info("删除角色信息请求参数：" + moiraiRole);
        //查询是否有用户正在使用该角色
        int val = moiraiUserAuthzMapper.findCountByRoleId(moiraiRole.getRoleId());

        if (val > 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_ROLE_RELATION_ERROR);
        } else {
            //删除角色表信息
            roleMapper.deleteByPrimaryKey(moiraiRole.getRoleId());

            //删除角色资源关联表信息
            MoiraiRoleResource moiraiRoleResource;
            moiraiRoleResource = new MoiraiRoleResource();
            moiraiRoleResource.setRoleId(moiraiRole.getRoleId());
            roleResourceMapper.deleteById(moiraiRoleResource);
            BWJsonResult bwJsonResult = new BWJsonResult();
            bwJsonResult.setMessage("删除成功！");
            return bwJsonResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult delSysRoles(MoiraiRole moiraiRole) {
        logger.info("删除系统角色信息请求参数：" + moiraiRole);
        //查询是否有用户正在使用该角色
        int val = moiraiUserAuthzMapper.findCountByRoleId(moiraiRole.getRoleId());

        if (val > 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_ROLE_RELATION_ERROR);
        } else {
            //删除角色表信息
            roleMapper.deleteByPrimaryKey(moiraiRole.getRoleId());

            //删除角色资源关联表信息
            MoiraiRoleResource moiraiRoleResource;
            moiraiRoleResource = new MoiraiRoleResource();
            moiraiRoleResource.setRoleId(moiraiRole.getRoleId());
            roleResourceMapper.deleteById(moiraiRoleResource);
            BWJsonResult bwJsonResult = new BWJsonResult();
            bwJsonResult.setMessage("删除成功！");
            return bwJsonResult;
        }
    }

    private List<MoiraiResource> getProductResources(Long tenantId, Long id) {
        Map productResourceMap = new HashMap();
        productResourceMap.put("productId", id);
        productResourceMap.put("tenantId", tenantId);
        List<MoiraiResource> moiraiResources = moiraiResourceMapper.selectTenantProductResourceList(productResourceMap);
        if (moiraiResources.isEmpty()) {
            moiraiResources = moiraiResourceMapper.selectProductResourceList(productResourceMap);
        }
        return moiraiResources;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>获取预制角色相关信息<BR>
     *
     * @return
     * @since 2020/2/5
     */
    @Override
    public BWJsonResult<MoiraiRoleCondition> getListDefaultRole(MoiraiRoleCondition moiraiRole) {
        moiraiRole.setDefaultFlag("Y");
        Page page = PageHelper.startPage(moiraiRole.getPageNo(), moiraiRole.getPageSize());
        List<MoiraiRole> moiraiRoles = roleMapper.selectByBean(moiraiRole);
        List<MoiraiRoleCondition> res = new ArrayList<>(moiraiRoles.size());
        for (int i = 0; i < moiraiRoles.size(); i++) {
            MoiraiRoleCondition item = new MoiraiRoleCondition();
            BeanUtils.copyProperties(moiraiRoles.get(i), item);
            int val = moiraiUserAuthzMapper.findCountByRoleId(item.getRoleId());
            item.setAuthCount(val);
            if (item.getAssociatedProductId() != null) {
                MoiraiProduct product = moiraiProductMapper.selectByPrimaryKey(item.getAssociatedProductId());
                if (product != null) {
                    item.setProductName(product.getProductName());
                }
            }
            res.add(item);
        }
        return new BWJsonResult<MoiraiRoleCondition>(res, (int) page.getTotal());
    }

    /**
     * 获取租户所有角色列表
     *
     * @param condition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiRole> getTenantAllRole(MoiraiOrgCondition condition) {
        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(condition.getTenantId());
        MoiraiRole queryRole = new MoiraiRole();
        queryRole.setTenantId(tenant.getTenantId());
        if (!StrUtils.isEmpty(tenant.getCreateTime() + "") && tenant.getCreateTime() >= Long.valueOf(sysRoleInitTime)) {
            MoiraiOrgProduct product = new MoiraiOrgProduct();
            product.setTenantId(condition.getTenantId());
            product.setUseFlag("Y");
            product.setDelFlag("N");
            List<MoiraiOrgProduct> products = moiraiOrgProductMapper.findOrgProductByCondition(product);
            List<Long> productList = products.stream().map(item -> item.getProductId()).collect(Collectors.toList());
            queryRole.setProducts(productList);
        }
        List<MoiraiRole> moiraiRoleList = new ArrayList<>();
        if (condition.getPageNo() != 0) {
            Page page = PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
            moiraiRoleList = roleMapper.selectAllAuthRoles(queryRole);
            return new BWJsonResult<MoiraiRole>(moiraiRoleList, (int) page.getTotal());
        }
        moiraiRoleList = roleMapper.selectAllAuthRoles(queryRole);
        return new BWJsonResult<MoiraiRole>(moiraiRoleList);
    }
}
