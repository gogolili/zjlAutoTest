package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiProductMapper;
import com.baiwang.moirai.mapper.MoiraiProductResourceMapper;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiProductCondition;
import com.baiwang.moirai.model.role.MoiraiProductResource;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.service.MoiraiProductService;
import com.baiwang.moirai.service.MoiraiResourcesService;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoiraiProductServiceImpl implements MoiraiProductService {
    private Logger logger = LoggerFactory.getLogger(MoiraiProductServiceImpl.class);

    @Autowired
    private SeqnumFeignClient seqnum;

    @Autowired
    private MoiraiProductMapper moiraiProductMapper;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private MoiraiProductResourceMapper moiraiProductResourceMapper;

    @Autowired
    private MoiraiSysServiceImpl moiraiSysService;

    @Autowired
    private MoiraiResourcesService moiraiResourcesService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addProduct(MoiraiProduct moiraiProduct) {
        logger.info("添加请求参数:{}", moiraiProduct);
        //产品名称不能为空，不能重复
        if (StringUtils.isBlank(moiraiProduct.getProductName())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_NAME_ERROR);
        }
        // 产品类型不能为空
        if (StringUtils.isBlank(moiraiProduct.getProductType())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_TYPE_ERROR);
        }
        if ("1".equals(moiraiProduct.getProductType()) && moiraiProduct.getBelongProduct() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_BELONG_ERROR);
        }
        Long productId = seqnum.getNum(Constants.MOIRAI_PRODUCT);
        moiraiProduct.setOrderIndex(productId);
        if (moiraiProduct.getBelongProduct() != null) {
            MoiraiProduct product = moiraiProductMapper.selectByPrimaryKey(moiraiProduct.getBelongProduct());
            if (product == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_BELONG_NULL_ERROR);
            } else {
                moiraiProduct.setProductIcon(product.getProductIcon());
                moiraiProduct.setProductUrl(product.getProductUrl());
                moiraiProduct.setOrderIndex(product.getOrderIndex());
            }
        }
        // 添加时间，准备插库
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiProduct.setCreatetime(nowTime);
        moiraiProduct.setModifyTime(nowTime);
        moiraiProduct.setProductId(productId);
        moiraiProduct.setProductMark("1");
        if (moiraiProduct.getProductOpentype() == null || "".equals(moiraiProduct.getProductOpentype())) {
            moiraiProduct.setProductOpentype("1");
        }

        //插入数据
        int i = 0;
        try {
            i = moiraiProductMapper.insertSelective(moiraiProduct);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_INSERT_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return i;
    }

    @Override
    public BWJsonResult<MoiraiProduct> queryProductList(MoiraiProductCondition moiraiProductCondition) {
        logger.info("查询入参:{}", moiraiProductCondition);
        List<MoiraiProduct> listMoiraiProduct = null;
        PageInfo<MoiraiProduct> pageInfo = null;
        try {
            if ("1".equals(moiraiProductCondition.getSource())) {
                //产品列表分页
                PageHelper.startPage(moiraiProductCondition.getPageNo(), moiraiProductCondition.getPageSize());
                moiraiProductCondition.setProductType("0");
                listMoiraiProduct = moiraiProductMapper.selectByBean(moiraiProductCondition);
                this.getChildren(listMoiraiProduct);
            } else {
                //客户产品
                PageHelper.startPage(moiraiProductCondition.getPageNo(), moiraiProductCondition.getPageSize());
                listMoiraiProduct = moiraiProductMapper.selectOrgProductDetail(moiraiProductCondition);
            }
            pageInfo = new PageInfo<>(listMoiraiProduct);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_QUERY_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        BWJsonResult<MoiraiProduct> bwJsonResult = new BWJsonResult(listMoiraiProduct, (int) pageInfo.getTotal());
        bwJsonResult.setMessage("查询成功");
        return bwJsonResult;
    }

    private void getChildren(List<MoiraiProduct> productList) {
        for (MoiraiProduct product : productList) {
            MoiraiProduct moiraiProduct = new MoiraiProduct();
            moiraiProduct.setBelongProduct(product.getProductId());
            List<MoiraiProduct> childProducts = moiraiProductMapper.selectByBean(moiraiProduct);
            product.setChildren(childProducts);
        }
    }

    @Override
    public List<MoiraiProduct> queryParentProduct(MoiraiProduct moiraiProduct) {
        moiraiProduct = new MoiraiProduct();
        moiraiProduct.setProductType("0");
        List<MoiraiProduct> productList = moiraiProductMapper.selectByBean(moiraiProduct);
        return productList;
    }

    @Override
    public MoiraiProduct queryProductInfo(MoiraiProduct moiraiProduct) {
        logger.info("查询入参：" + moiraiProduct);
        MoiraiProduct queryMoiraiProduct = null;
        try {
            queryMoiraiProduct = moiraiProductMapper.selectByPrimaryKey(moiraiProduct.getProductId());
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_QUERY_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }

        return queryMoiraiProduct;
    }

    @Override
    public int editProduct(MoiraiProduct moiraiProduct) {
        logger.info("更新入参：{}", moiraiProduct);
        moiraiProduct.setProductType(null);
        moiraiProduct.setCreater(null);
        moiraiProduct.setCreatetime(null);
        Long nowTime = DateTimeUtils.nowTimeLong();
        moiraiProduct.setModifyTime(nowTime);
        int i = moiraiProductMapper.updateByPrimaryKeySelective(moiraiProduct);
        return i;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delProduct(MoiraiProduct moiraiProduct) {
        logger.info("删除入参：{}", moiraiProduct);
        MoiraiProduct product = moiraiProductMapper.selectByPrimaryKey(moiraiProduct.getProductId());
        if (product == null) {
            return 0;
        }
        if ("0".equals(product.getProductType())) {
            MoiraiProduct childProduct = new MoiraiProduct();
            childProduct.setBelongProduct(moiraiProduct.getProductId());
            List<MoiraiProduct> productList = moiraiProductMapper.selectByBean(childProduct);
            if (productList != null && productList.size() > 0) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_CHILD_ERROR);
            }
        }
        // 查询租户是否在使用
        List<MoiraiOrgProduct> vos = moiraiOrgProductMapper.queryOrgByProduct(moiraiProduct.getProductId());
        if (vos != null && vos.size() > 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PRODUCT_ORG_ERROR);
        }

        int i = 0;
        try {
            i = moiraiProductMapper.deleteByPrimaryKey(moiraiProduct.getProductId());

            MoiraiProductResource delMoiraiProductResource = new MoiraiProductResource();
            delMoiraiProductResource.setProductId(moiraiProduct.getProductId());
            moiraiProductResourceMapper.deleteByBean(delMoiraiProductResource);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_DEL_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return i;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addProductResource(MoiraiProduct moiraiProduct) {
        logger.info("请求参数：{}", moiraiProduct);
        Long productId = moiraiProduct.getProductId();
        MoiraiProduct product = moiraiProductMapper.selectByPrimaryKey(productId);
        if (product == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DB_NULL);
        }
        Long nowTime = DateTimeUtils.nowTimeLong();
        List<MoiraiResource> resourceList = moiraiProduct.getResourceList();
        List<Long> productResourceIds = moiraiSysService.getNums(resourceList, Constants.MOIRAI_PRODUCT_RESOURCE);
        List<MoiraiProductResource> productResourceList = new ArrayList<>();
        for (int i = 0; i < resourceList.size(); i++) {
            MoiraiProductResource productResource = new MoiraiProductResource();
            productResource.setProductId(productId);
            productResource.setResourceId(resourceList.get(i).getResourceId());
            productResource.setCreater(moiraiProduct.getCreater());
            productResource.setCreatetime(nowTime);
            productResource.setProductResourceId(productResourceIds.get(i));
            productResourceList.add(productResource);
        }
        int i = 0;
        try {
            if (!productResourceList.isEmpty()) {
                List<HashMap<Long, Long>> maps = moiraiProductResourceMapper.selectprimaryKey(productId);
                Set<Long> idSet = null;
                if (!maps.isEmpty()) {
                    List<Long> idList = new ArrayList<>();
                    idSet = new HashSet<>();
                    for (HashMap<Long, Long> map : maps) {
                        idList.add(map.get("productResourceId"));
                        idSet.add(map.get("tenantId"));
                    }
                    moiraiProductResourceMapper.deleteProductResource(productId, idList);
                }
                i = moiraiProductResourceMapper.insertBatchInfo(productResourceList);
                // 还原租户菜单
                if (!idSet.isEmpty()) {
                    MoiraiProductCondition productCondition = new MoiraiProductCondition();
                    productCondition.setProductId(productId);
                    for (Long tenantId : idSet) {
                        productCondition.setTenantId(tenantId);
                        moiraiResourcesService.recoveryResource(productCondition);
                    }
                }
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_PORDUCT_RESOURCE_INSERT_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return i;
    }

    /**
     * 查询产品资源树
     */
    @Override
    public List<MoiraiProduct> queryProductResourceTree() {
        MoiraiProductCondition queryProduct = new MoiraiProductCondition();
        queryProduct.setProductType("0");
        List<MoiraiProduct> list = moiraiProductMapper.selectByBean(queryProduct);
        for (int i = 0; i < list.size(); i++) {
            MoiraiProductCondition queryRes = new MoiraiProductCondition();
            queryRes.setProductId(list.get(i).getProductId());
            queryRes.setSource(Constants.DEFAULT_ONE);
            List<MoiraiResource> resList = moiraiResourcesService.queryResourceTreeByProduct(queryRes);
            list.get(i).setResourceList(resList);
        }
        return list;
    }
}
