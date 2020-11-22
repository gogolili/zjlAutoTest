package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.*;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.*;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiResourcesService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MoiraiResourcesServiceImpl implements MoiraiResourcesService {
    private Logger logger = LoggerFactory.getLogger(MoiraiResourcesServiceImpl.class);

    @Autowired
    private SeqnumFeignClient seqnum;

    @Autowired
    private MoiraiProductResourceMapper moiraiProductResourceMapper;

    @Autowired
    private MoiraiResourceMapper moiraiResourceMapper;

    @Autowired
    private MoiraiProductMapper moiraiProductMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private MoiraiRoleResourceMapper roleResourceMapper;

    @Autowired
    private MoiraiSecurityControlMapper moiraiSecurityControlMapper;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addResources(MoiraiResourceCondition resourceCondition) {
        logger.info("添加产品资源请求参数：{}", resourceCondition);

        Long resourceId = seqnum.getNum(Constants.MOIRAI_RESOURCE);
        Long nowTime = DateTimeUtils.nowTimeLong();

        //完善主节点信息
        resourceCondition.setCreatetime(nowTime);
        resourceCondition.setModifyTime(nowTime);
        resourceCondition.setResourceId(resourceId);
        resourceCondition.setModifyUser(resourceCondition.getCreater());
        if (resourceCondition.getPid() == null) {
            resourceCondition.setPid(0L);
        }
        if (StringUtils.isBlank(resourceCondition.getDeviceType())) {
            resourceCondition.setDeviceType("100");
        }
        Long tenantId = resourceCondition.getTenantId();
        String source = resourceCondition.getSource();
        Long productId = resourceCondition.getProductId();
        int i = 0;
        //租户添加
        try {
            if (Constants.DEFAULT_ZERO.equals(source)) {
                List<MoiraiResource> tenantResource = moiraiResourceMapper.selectTenantProductResource(productId, tenantId);
                resourceCondition.setSourceFlag(source);
                if (!tenantResource.isEmpty()) {
                    i = moiraiResourceMapper.insertResourceTenant(resourceCondition);
                } else {
                    Map productResourceMap = new HashMap();
                    productResourceMap.put("productId", productId);
                    List<MoiraiResource> resourceList = moiraiResourceMapper.selectProductResourceList(productResourceMap);
                    if (resourceList != null) {
                        resourceList.forEach(resource -> {
                            resource.setTenantId(resourceCondition.getTenantId());
                            resource.setCreatetime(resourceCondition.getModifyTime());
                            resource.setModifyTime(resourceCondition.getModifyTime());
                            resource.setCreater(resourceCondition.getCreater());
                            resource.setModifyUser(resourceCondition.getCreater());
                            resource.setSourceFlag(Constants.DEFAULT_ONE);
                        });
                        resourceList.add(resourceCondition);
                        moiraiResourceMapper.addResourceTenantBatch(resourceList);
                    }
                }
            } else {
                //运营添加
                i = moiraiResourceMapper.insertSelective(resourceCondition);
                this.addResourceTenant(resourceCondition);
                if (StringUtils.isNotBlank(resourceCondition.getJudgeAuthc())) {
                    redisTemplate.opsForValue().set(Constants.JUDGE_PERMISSION + resourceCondition.getJudgeAuthc(), "[]");
                }
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.Moirai_DB_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }

        Long productResourceId = seqnum.getNum(Constants.MOIRAI_PRODUCT_RESOURCE);
        MoiraiProductResource productResource = new MoiraiProductResource();
        productResource.setProductId(productId);
        productResource.setResourceId(resourceId);
        productResource.setCreatetime(nowTime);
        productResource.setCreater(resourceCondition.getCreater());
        productResource.setProductResourceId(productResourceId);
        //添加主信息
        try {
            i = moiraiProductResourceMapper.insertSelective(productResource);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.Moirai_DB_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return i;
    }

    @Override
    public BWJsonResult<MoiraiResource> queryResourcesTree(MoiraiRole moiraiRole) {
        try {
            if (moiraiRole.getOrgId() != null) {
                List<MoiraiResource> resourceList = new ArrayList<>();
                // 根据orgid查询产品list
                List<MoiraiOrgProduct> moiraiOrgProductList = moiraiOrgService.getOrgProducts(moiraiRole.getOrgId());
                if (StrUtils.isEmptyList(moiraiOrgProductList)) {
                    logger.info("根据orgid查询产品list为空");
                    return new BWJsonResult();
                }
                //MoiraiOrg org = moiraiOrgService.selectByOrgId(moiraiRole.getOrgId());
                //String resFilterType = this.getOrgResourceFilterType(org);
                for (MoiraiOrgProduct product : moiraiOrgProductList) {
                    List<MoiraiResource> moiraiResources = this.selectTenantProductResourceList(product.getProductId(), null, moiraiRole.getTenantId());
                    if (null != moiraiResources && moiraiResources.size() > 0) {
                        List<MoiraiResource> treeList = AdminUtils.getResourceTree(moiraiResources, 0L);
                        //产品强行加入顶级节点-形成树
                        MoiraiResource moiraiResource = new MoiraiResource();
                        moiraiResource.setResourceName(product.getProductName());
                        moiraiResource.setResourceType(10);//表示这个resource是产品
                        moiraiResource.setResourceId(product.getProductId());
                        moiraiResource.setChildren(treeList);
                        resourceList.add(moiraiResource);
                    }
                }
                return new BWJsonResult(resourceList, resourceList.size());
            } else {
                List<MoiraiResource> moiraiResources = moiraiResourceMapper.selectByBean(new MoiraiResource());
                List<MoiraiResource> treeList = AdminUtils.getResourceTree(moiraiResources, 0L);
                return new BWJsonResult(treeList, treeList.size());
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_ERROR));
        }
    }

    /**
     * 查询菜单时的过滤条件---不再使用
     *
     * @param org
     * @return
     */
    @Deprecated
    @Override
    public String getOrgResourceFilterType(MoiraiOrg org) {
        String resFilterType = "0";
        String filterResource = redisTemplate.opsForValue().get(Constants.JUDGE_PERMISSION + "filterResource");
        if (filterResource != null) {
            return resFilterType;
        }
        Integer orgType = org.getOrgType();
        MoiraiOrg deviceOrg = org;
        Long parentOrgId = org.getParentOrg();
        if ((Constants.MOIRAI_IS_NOT_TAXER.equals(orgType + "")) && (parentOrgId != 0)) {
            deviceOrg = moiraiOrgService.getOrgFatherInfo(parentOrgId);
        }
        if (Constants.MOIRAI_IS_NOT_TAXER.equals(deviceOrg.getOrgType() + "")) {
            resFilterType = "14";
            return resFilterType;
        }
        String deviceType = deviceOrg.getDeviceType();
        String tgType = deviceOrg.getTgType();
        //盘用户
        if (Constants.MOIRAI_DEVICE_PANEL.equals(deviceType) || Constants.MOIRAI_DEVICE_TAXBOX.equals(deviceType)) {
            //魔盒用户
            if (Constants.MOIRAI_TGTYPE_PT.equals(tgType)) {
                if (Constants.MOIRAI_IS_TAXER.equals(orgType + "")) {
                    //魔盒纳税主体用户
                    resFilterType = "1";
                } else if (Constants.MOIRAI_IS_NOT_TAXER.equals(orgType + "")) {
                    //魔盒非纳税主体用户
                    resFilterType = "2";
                }

            }
            //普通盘用户
            else if (Constants.MOIRAI_TGTYPE_ZC.equals(tgType)) {
                if (Constants.MOIRAI_IS_TAXER.equals(orgType + "")) {
                    //普通纳税主体用户
                    resFilterType = "3";
                } else if (Constants.MOIRAI_IS_NOT_TAXER.equals(orgType + "")) {
                    //普通非纳税主体用户
                    resFilterType = "4";
                }

            }

        } else if (Constants.MOIRAI_DEVICE_SERVER.equals(deviceType)) {
            //服务器用户
            if (Constants.MOIRAI_IS_TAXER.equals(orgType + "")) {
                //纳税主体用户
                resFilterType = "5";
            } else if (Constants.MOIRAI_IS_NOT_TAXER.equals(orgType + "")) {
                //非纳税主体用户
                String selfManage = deviceOrg.getSelfManage();
                if (Constants.MOIRAI_SELFMANAGER_TRUE.equals(selfManage)) {
                    resFilterType = "6";
                } else if (Constants.MOIRAI_SELFMANAGER_FALSE.equals(selfManage)) {
                    resFilterType = "7";
                }
            }

        } else if (Constants.MOIRAI_DEVICE_VIRTUAL_UKEY.equals(deviceType)) {
            //服务器用户
            if (Constants.MOIRAI_IS_TAXER.equals(orgType + "")) {
                //纳税主体用户
                resFilterType = "8";
            } else if (Constants.MOIRAI_IS_NOT_TAXER.equals(orgType + "")) {
                //非纳税主体用户
                String selfManage = deviceOrg.getSelfManage();
                if (Constants.MOIRAI_SELFMANAGER_TRUE.equals(selfManage)) {
                    resFilterType = "9";
                } else if (Constants.MOIRAI_SELFMANAGER_FALSE.equals(selfManage)) {
                    resFilterType = "10";
                }
            }

        } else if (Constants.MOIRAI_DEVICE_TAX_UKEY.equals(deviceType)) {
            //服务器用户
            if (Constants.MOIRAI_IS_TAXER.equals(orgType + "")) {
                //纳税主体用户
                resFilterType = "11";
            } else if (Constants.MOIRAI_IS_NOT_TAXER.equals(orgType + "")) {
                //非纳税主体用户
                String selfManage = deviceOrg.getSelfManage();
                if (Constants.MOIRAI_SELFMANAGER_TRUE.equals(selfManage)) {
                    resFilterType = "12";
                } else if (Constants.MOIRAI_SELFMANAGER_FALSE.equals(selfManage)) {
                    resFilterType = "13";
                }
            }
        }
        logger.info("用户机构资源过滤类型：" + resFilterType);

        return resFilterType;
    }

    @Override
    public MoiraiResource queryResourcesInfo(MoiraiResourceCondition moiraiResource) {
        logger.info("查询资源入参:{}", moiraiResource);
        MoiraiResource querymoiraiResource = null;
        Long tenantId = moiraiResource.getTenantId();
        Long resourceId = moiraiResource.getResourceId();
        String source = moiraiResource.getSource();
        try {
            List<MoiraiResource> resourceList = this.checkResourceTenantId(tenantId, resourceId, source);
            if (resourceList != null && Constants.DEFAULT_ZERO.equals(source)) {
                logger.info("查询时租户资源返回信息:{}", resourceList.get(0));
                return resourceList.get(0);
            } else {
                querymoiraiResource = moiraiResourceMapper.selectByPrimaryKey(moiraiResource.getResourceId());
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return querymoiraiResource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int editResources(MoiraiResourceCondition moiraiResource) {
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiResource.setCreater(null);
        moiraiResource.setCreatetime(null);
        moiraiResource.setModifyTime(nowTime);

        Long tenantId = moiraiResource.getTenantId();
        Long resourceId = moiraiResource.getResourceId();
        String source = moiraiResource.getSource();
        int i = 0;
        try {
            if (Constants.DEFAULT_ZERO.equals(source)) {
                List<MoiraiResource> tenantResource = this.checkResourceTenantId(tenantId, resourceId, source);
                logger.info("更新时租户资源信息:{}", tenantResource);
                if (tenantResource == null || tenantResource.size() == 0) {
                    MoiraiResource oldResource = moiraiResourceMapper.selectByPrimaryKey(resourceId);
                    if (oldResource == null) {
                        return 0;
                    }
                    //满足条件再同步
                    String resourceName = moiraiResource.getResourceName();
                    Long orderIndex = moiraiResource.getOrderIndex();
                    String resourceIcon = moiraiResource.getResourceIcon();
                    String operCode = moiraiResource.getOperCode();
                    String placeCode = moiraiResource.getPlaceCode();
                    String useFlag = moiraiResource.getUseFlag();
                    String resourceUrl = moiraiResource.getResourceUrl();
                    if ((StringUtils.isNotBlank(resourceName) && !resourceName.equals(oldResource.getResourceName()))
                        || (orderIndex != null && !orderIndex.equals(oldResource.getOrderIndex()))
                        || (StringUtils.isNotBlank(resourceIcon) && !resourceIcon.equals(oldResource.getResourceIcon()))
                        || (StringUtils.isNotBlank(operCode) && !operCode.equals(oldResource.getOperCode()))
                        || (StringUtils.isNotBlank(placeCode) && !placeCode.equals(oldResource.getPlaceCode()))
                        || (StringUtils.isNotBlank(useFlag) && !useFlag.equals(oldResource.getUseFlag()))
                        || (StringUtils.isNotBlank(resourceUrl) && !resourceUrl.equals(oldResource.getResourceUrl()))) {
                        Map productResourceMap = new HashMap();
                        productResourceMap.put("productId", moiraiResource.getProductId());
                        List<MoiraiResource> resourceList = moiraiResourceMapper.selectProductResourceList(productResourceMap);
                        if (resourceList != null) {
                            resourceList.forEach(resource -> {
                                resource.setTenantId(moiraiResource.getTenantId());
                                resource.setCreatetime(moiraiResource.getModifyTime());
                                resource.setModifyTime(moiraiResource.getModifyTime());
                                resource.setCreater(moiraiResource.getCreater());
                                resource.setModifyUser(moiraiResource.getCreater());
                                resource.setSourceFlag(Constants.DEFAULT_ONE);
                            });
                            moiraiResourceMapper.addResourceTenantBatch(resourceList);
                        }
                    }
                }
                i = moiraiResourceMapper.updateResourceTenant(moiraiResource);
            } else {
                //运营
                MoiraiResource resource = moiraiResourceMapper.selectByPrimaryKey(resourceId);
                if (resource != null) {
                    i = moiraiResourceMapper.updateByPrimaryKeySelective(moiraiResource);
                    moiraiResource.setTenantId(null);
                    i = moiraiResourceMapper.updateResourceTenant(moiraiResource);
                    if (moiraiResource.getJudgeAuthc() != null && !moiraiResource.getJudgeAuthc().equals(resource.getJudgeAuthc())) {
                        redisTemplate.delete(Constants.JUDGE_PERMISSION + resource.getJudgeAuthc());
                        if (StringUtils.isNotBlank(moiraiResource.getJudgeAuthc())) {
                            MoiraiResourceSecurityCondition security = new MoiraiResourceSecurityCondition();
                            security.setResourceId(resourceId);
                            security.setResourceType(Constants.DEFAULT_ONE);// 代表资源菜单
                            List<MoiraiSecurityControl> securityList = moiraiSecurityControlMapper.getResourceSecurityList(security);
                            redisTemplate.opsForValue().set(Constants.JUDGE_PERMISSION + moiraiResource.getJudgeAuthc(), securityList.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_UPDATE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return i;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delResources(MoiraiResourceCondition moiraiResource) {
        logger.info("删除资源参数:{}", moiraiResource);
        Long tenantId = moiraiResource.getTenantId();
        Long resourceId = moiraiResource.getResourceId();
        String source = moiraiResource.getSource();
        int i = 0;
        this.checkChildResource(tenantId, resourceId, source);
        try {
            if (Constants.DEFAULT_ZERO.equals(source)) {
                List<MoiraiResource> resourceList = this.checkResourceTenantId(tenantId, resourceId, source);
                if (!resourceList.isEmpty()) {
                    logger.info("查询时租户资源返回信息:{}", resourceList.get(0));
                    i = moiraiResourceMapper.deleteResourceTenant(tenantId, resourceId);
                    MoiraiResource resource = moiraiResourceMapper.selectByPrimaryKey(resourceId);
                    if (resource == null) {
                        MoiraiProductResource productResource = new MoiraiProductResource();
                        productResource.setResourceId(resourceId);
                        i = moiraiProductResourceMapper.deleteByBean(productResource);
                    }
                }
            } else {
                MoiraiResource resource = moiraiResourceMapper.selectByPrimaryKey(resourceId);
                if (resource != null) {
                    i = moiraiResourceMapper.deleteByPrimaryKey(resourceId);
                    MoiraiProductResource productResource = new MoiraiProductResource();
                    productResource.setResourceId(resourceId);
                    i = moiraiProductResourceMapper.deleteByBean(productResource);
                    i = moiraiResourceMapper.deleteResourceTenant(null, resourceId);
                    if (StringUtils.isNotBlank(resource.getJudgeAuthc())) {
                        redisTemplate.delete(Constants.JUDGE_PERMISSION + resource.getJudgeAuthc());
                    }
                    MoiraiResourceSecurity security = new MoiraiResourceSecurity();
                    security.setResourceId(resourceId);
                    security.setResourceType(Constants.DEFAULT_ONE); // 代表资源菜单
                    moiraiSecurityControlMapper.deleteResourceSecurity(security);
                }
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_DEL_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return i;
    }

    @Override
    public List<MoiraiResource> queryResourceTreeByProduct(MoiraiProductCondition productCondition) {
        logger.info("查询左侧资源树请求参数：{}", productCondition);
        MoiraiProduct product = moiraiProductMapper.selectByPrimaryKey(productCondition.getProductId());
        if (product == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_NOPRODUCT);
        }
        // 运营后台展示全树
        List<MoiraiProductResource> parentProductResource = null;
        if (productCondition.getSource().equals(Constants.DEFAULT_ONE) && product.getBelongProduct() != null) {
            MoiraiProductResource moiraiProductResource = new MoiraiProductResource();
            moiraiProductResource.setProductId(product.getBelongProduct());
            parentProductResource = moiraiProductResourceMapper.selectByBean(moiraiProductResource);
        }

        // 选中产品所有菜单
        MoiraiProductResource moiraiProductResource = new MoiraiProductResource();
        moiraiProductResource.setProductId(productCondition.getProductId());
        List<MoiraiProductResource> moiraiProductResourceList = moiraiProductResourceMapper.selectByBean(moiraiProductResource);

        List<Long> resourceId = new ArrayList<>();
        if (parentProductResource != null) {
            parentProductResource.forEach(item -> resourceId.add(item.getResourceId()));
        } else if (!moiraiProductResourceList.isEmpty()) {
            moiraiProductResourceList.forEach(item -> resourceId.add(item.getResourceId()));
        } else {
            return null;
        }
        // 运营后台应用在所属产品里的所有选中菜单
        List<Long> productResource = new ArrayList<>();
        if (parentProductResource != null && !moiraiProductResourceList.isEmpty()) {
            moiraiProductResourceList.forEach(item -> productResource.add(item.getResourceId()));
            logger.info("应用拥有权限的resource集合:{}", resourceId);
        }
        List<MoiraiResource> resourceList = this.getList(resourceId, true, productCondition.getSource(), productCondition.getTenantId(), productResource);

        return resourceList;
    }

    @Override
    public List<MoiraiResource> queryResourceTreeNoButton(MoiraiProductCondition productCondition) {
        logger.info("查询父级资源树请求入参:{}", productCondition);
        String source = productCondition.getSource();
        List<MoiraiResource> resourceList = null;
        if (Constants.DEFAULT_ZERO.equals(source)) {
            resourceList = moiraiResourceMapper.selectResourceTenantNoButton(productCondition);
        }
        if (Constants.DEFAULT_ONE.equals(source) || resourceList == null || resourceList.size() == 0) {
            //租户
            resourceList = moiraiResourceMapper.selectResourceNoButton(productCondition);
        }
        List<Long> resourceId = new ArrayList<>();
        if (resourceList != null) {
            resourceList.forEach(item -> resourceId.add(item.getResourceId()));
        }
        List<MoiraiResource> retResource = this.getList(resourceId, true, productCondition.getSource(), productCondition.getTenantId(), new ArrayList<>());
        return retResource;
    }

    @Override
    public BWJsonResult<MoiraiResource> queryResourceList(MoiraiResourceCondition resourceCondition) {
        logger.info("请求参数：{}", resourceCondition);
        if (resourceCondition.getPageNo() <= 0) {
            resourceCondition.setPageNo(0);
        }
        if (resourceCondition.getPageSize() <= 0) {
            resourceCondition.setPageSize(10);
        }
        Long tenantId = resourceCondition.getTenantId();
        Long productId = resourceCondition.getProductId();
        List<MoiraiResource> resourceList = null;
        if (Constants.DEFAULT_ONE.equals(resourceCondition.getSource())) {
            PageHelper.startPage(resourceCondition.getPageNo(), resourceCondition.getPageSize());
            resourceList = moiraiResourceMapper.queryResourceList(resourceCondition);
        } else {
            List<MoiraiResource> moiraiResourceList = moiraiResourceMapper.selectTenantProductResource(productId, tenantId);
            PageHelper.startPage(resourceCondition.getPageNo(), resourceCondition.getPageSize());
            if (moiraiResourceList == null || moiraiResourceList.size() == 0) {
                resourceList = moiraiResourceMapper.queryResourceList(resourceCondition);
            } else {
                resourceList = moiraiResourceMapper.queryResourceTenantList(resourceCondition);
            }
        }
        PageInfo<MoiraiResource> pageInfo = new PageInfo<>(resourceList);
        BWJsonResult<MoiraiResource> bwJsonResult = new BWJsonResult<>(resourceList, (int) pageInfo.getTotal());
        bwJsonResult.setMessage("查询资源列表成功");
        return bwJsonResult;
    }

    /**
     * 获取产品的资源列表，可以根据组织机构和设备不同添加过滤条件 LC
     */
    @Override
    public List<MoiraiResource> selectTenantProductResourceList(Long productId, String resFilterType, Long tenantId) {
        logger.info("请求参数：" + productId);
        Map productResourceMap = new HashMap();
        productResourceMap.put("productId", productId);
        productResourceMap.put("resFilterType", resFilterType);
        productResourceMap.put("tenantId", tenantId);
        List<MoiraiResource> productResourceList = moiraiResourceMapper.selectTenantProductResourceList(productResourceMap);
        if (productResourceList.isEmpty()) {
            productResourceList = moiraiResourceMapper.selectProductResourceList(productResourceMap);
        }
        return productResourceList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recoveryResource(MoiraiProductCondition productCondition) {
        logger.info("还原资源入参:{}", productCondition);
        Long tenantId = productCondition.getTenantId();
        Long productId = productCondition.getProductId();
        MoiraiOrgProduct orgProduct = new MoiraiOrgProduct();
        orgProduct.setProductId(productId);
        orgProduct.setTenantId(tenantId);
        List<MoiraiOrgProduct> productByCondition = moiraiOrgProductMapper.findOrgProductByCondition(orgProduct);
        if (productByCondition.isEmpty()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_NOPRODUCT);
        }
        List<MoiraiResource> resourceList = moiraiResourceMapper.selectTenantProductResource(productId, tenantId);
        try {
            if (!resourceList.isEmpty()) {
                List<Long> resourceIds = new ArrayList<>();
                resourceList.forEach(resource -> {
                    resourceIds.add(resource.getResourceId());
                });
                moiraiResourceMapper.batchDeleteResourceTenant(tenantId, resourceIds);
            }
            resourceList = moiraiResourceMapper.selectTenantProductResource(productId, tenantId);
            // 有定制菜单再同步
            if (!resourceList.isEmpty()) {
                Map productResourceMap = new HashMap();
                productResourceMap.put("productId", productId);
                resourceList = moiraiResourceMapper.selectProductResourceList(productResourceMap);
                resourceList.forEach(resource -> {
                    resource.setTenantId(tenantId);
                    resource.setSourceFlag(Constants.DEFAULT_ONE);
                });
                moiraiResourceMapper.addResourceTenantBatch(resourceList);
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_DEL_TENANT_MENU_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }

    }

    private List<MoiraiResource> getList(List<Long> resourceId, boolean isTree, String source, Long tenantId,
        List<Long> productResource) {
        Set<Long> hashSet = new HashSet<>(resourceId);
        resourceId = new ArrayList<>(hashSet);
        if (resourceId == null || resourceId.size() == 0) {
            return null;
        }
        List<MoiraiResource> list = null;
        // 租户页面查询
        if (Constants.DEFAULT_ZERO.equals(source)) {
            list = moiraiResourceMapper.selectResourceTenantBatch(resourceId, tenantId);
            if (list.isEmpty()) {
                // 查询启用菜单
                list = moiraiResourceMapper.selectBatchInfoUsed(resourceId);
            }
        } else if (Constants.DEFAULT_ONE.equals(source)) {
            list = moiraiResourceMapper.selectBatchInfo(resourceId);
        }
        if (list == null || list.size() == 0) {
            return null;
        }
        if (!productResource.isEmpty()) {
            list.forEach(item -> {
                productResource.forEach(resource -> {
                    if (resource.equals(item.getResourceId())) {
                        item.setHasPower(Constants.DEFAULT_ONE);
                    }
                });
            });
        }
        if (isTree) {
            return AdminUtils.getResourceTree(list, 0L);
        }
        return list;
    }

    private List<MoiraiResource> checkResourceTenantId(Long tenantId, Long resourceId, String source) {
        List<MoiraiResource> resourceList = null;
        if (Constants.DEFAULT_ZERO.equals(source)) {
            MoiraiResource moiraiResource = new MoiraiResource();
            moiraiResource.setTenantId(tenantId);
            moiraiResource.setResourceId(resourceId);
            resourceList = moiraiResourceMapper.selectTenantResourceByBean(moiraiResource);
        }
        return resourceList;
    }

    private void checkChildResource(Long tenantId, Long resourceId, String source) {
        MoiraiResource moiraiResource = new MoiraiResource();
        moiraiResource.setPid(resourceId);
        moiraiResource.setTenantId(tenantId);
        List<MoiraiResource> resourceList = null;
        if (Constants.DEFAULT_ZERO.equals(source)) {
            resourceList = moiraiResourceMapper.selectTenantResourceByBean(moiraiResource);
        } else {
            resourceList = moiraiResourceMapper.selectByBean(moiraiResource);
        }
        if (!resourceList.isEmpty()) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_RESOURCE_CHILD_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_RESOURCE_CHILD_ERROR);
        }
    }

    @Transactional
    public void addResourceTenant(MoiraiResourceCondition resourceCondition) {
        List<MoiraiResource> moiraiResourceList = moiraiResourceMapper.queryResourceTenantIds(resourceCondition.getProductId());
        int size = moiraiResourceList.size();
        logger.info("租户资源表对应产品资源的租户ID条数:{}", size);
        int count = (size / Constants.USEREXCEL_MAXCOUNT) + 1;
        try {
            for (int i = 0; i < count; i++) {
                List<MoiraiResource> newlist = new ArrayList<>();
                List<MoiraiResource> newResourceList = null;
                if ((i + 1) == count) {
                    int startIndex = (i * Constants.USEREXCEL_MAXCOUNT);
                    int endIndex = size;
                    newResourceList = moiraiResourceList.subList(startIndex, endIndex);
                } else {
                    int startIndex = (i * Constants.USEREXCEL_MAXCOUNT);
                    int endIndex = (i + 1) * Constants.USEREXCEL_MAXCOUNT;
                    newResourceList = moiraiResourceList.subList(startIndex, endIndex);
                }
                newResourceList.forEach(tenantId -> {
                    MoiraiResource moiraiResource = new MoiraiResource();
                    BeanUtils.copyProperties(resourceCondition, moiraiResource);
                    moiraiResource.setTenantId(tenantId.getTenantId());
                    moiraiResource.setSourceFlag(Constants.DEFAULT_ONE);
                    newlist.add(moiraiResource);
                });
                if (newlist != null && newlist.size() > 0) {
                    moiraiResourceMapper.addResourceTenantBatch(newlist);
                }
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.Moirai_DB_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
    }

    /**
     * 根据产品信息查询资源信息
     */
    @Override
    public List<MoiraiResource> selectProductRoleResourceList(MoiraiRole role) {
        if (role == null || role.getAssociatedProductId() == null)
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        MoiraiProduct exP = moiraiProductMapper.selectByPrimaryKey(role.getAssociatedProductId());
        if (exP == null) throw new MoiraiException("", "产品不存在！");
        Map query = new HashMap() {{
            put("productId", role.getAssociatedProductId());
        }};
        List<MoiraiResource> allList = moiraiResourceMapper.selectProductResourceList(query);
        boolean hasPower = false;
        if (role.getRoleId() != null){
            MoiraiRoleResource queryRoleR = new MoiraiRoleResource();
            queryRoleR.setRoleId(role.getRoleId());
            List<MoiraiRoleResource> rrList = roleResourceMapper.selectByBean(queryRoleR);
            if (!rrList.isEmpty()){
                List<Long> roleResId = rrList.stream().map(item -> item.getResourceId()).collect(Collectors.toList());
                for (int i = 0; i < allList.size(); i++){
                    if (roleResId.contains(allList.get(i).getResourceId())){
                        allList.get(i).setHasPower(Constants.DEFAULT_ONE);
                        hasPower = true;
                    }
                }
            }
        }
        Collections.sort(allList, (a, b) -> a.getOrderIndex().compareTo(b.getOrderIndex()));
        List<MoiraiResource> res = new ArrayList<>();
        res.add(new MoiraiResource());
        res.get(0).setResourceName(exP.getProductName());
        res.get(0).setResourceType(Constants.DEFAULT_PRODUCT);
        res.get(0).setResourceId(exP.getProductId());
        res.get(0).setHasPower(hasPower ? Constants.DEFAULT_ONE : Constants.DEFAULT_ZERO);
        res.get(0).setChildren(AdminUtils.getResourceTree(allList, 0L));
        return res;
    }

    /**
     * 查询所有按钮资源信息
     */
    @Override
    public List<HashMap> queryButtonResource() {
        MoiraiResource query = new MoiraiResource();
        query.setResourceType(4);
        List<HashMap> list = moiraiResourceMapper.selectButtonResource();
        HashMap<String, HashMap> map = new HashMap();
        list.forEach(item -> {
            String url = item.get("judgeAuthc").toString();
            if (!map.containsKey(url)) {
                map.put(url, item);
            } else {
                map.get(url).put("resourceName", map.get(url).get("resourceName") + "|" + item.get("resourceName"));
            }
        });
        list.clear();
        map.forEach((key, value) -> list.add(value));
        return list;
    }
}
