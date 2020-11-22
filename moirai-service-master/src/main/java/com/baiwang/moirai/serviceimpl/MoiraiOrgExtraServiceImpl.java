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
import com.baiwang.moirai.dao.MoiraiOrgDao;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.SysProvCityDistMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.model.sys.SysProvCityDistTree;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiExtService;
import com.baiwang.moirai.service.MoiraiOrgExtraService;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.utils.ExcelPoiUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class MoiraiOrgExtraServiceImpl implements MoiraiOrgExtraService {

    private final Logger logger = LoggerFactory.getLogger(MoiraiOrgExtraServiceImpl.class);

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private SysProvCityDistMapper sysProvCityDistMapper;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiExtService moiraiExtService;

    @Autowired
    private MoiraiOrgDao moiraiOrgDao;

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>获取机构列表<BR>
     *
     * @return
     * @since 2019/10/16
     */
    public BWJsonResult<MoiraiOrg> getOrgList(MoiraiOrgCondition moiraiOrgCondition) {
        Long userId = moiraiOrgCondition.getUserId();
        Long resourceId = moiraiOrgCondition.getResourceId();
        Long tenantId = moiraiOrgCondition.getTenantId();
        String orgName = moiraiOrgCondition.getOrgName();
        String orgCode = moiraiOrgCondition.getOrgCode();
        String taxCode = moiraiOrgCondition.getTaxCode();
        Long orgId = moiraiOrgCondition.getOrgId();
        logger.info("机构列表查询请求字段: userId:{}, resourceId:{}, orgId:{}, tenantId:{}, orgName:{}, orgCode:{}, taxCode:{}",
            userId, resourceId, orgId, tenantId, orgName, orgCode, taxCode);
        List<Long> authOrgs = new ArrayList<>();
        List<Long> rtnList = new ArrayList<>();
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setTenantId(tenantId);
        moiraiOrg.setOrgId(orgId);
        List<MoiraiOrg> childList = moiraiOrgService.getTenantOrgSimpleTree(moiraiOrg, 2);
        for (MoiraiOrg org : childList) {
            rtnList.add(org.getOrgId());
        }
        List<MoiraiUserAuthz> authRoleListByUseId = moiraiOrgService.getUserAuthBycondition(userId, resourceId);
        for (MoiraiUserAuthz moiraiUserAuthz : authRoleListByUseId) {
            Long authOrg = moiraiUserAuthz.getAuthOrg();
            if (rtnList.contains(authOrg)) {
                authOrgs.add(authOrg);
            }
        }

        long total = 0;
        List<MoiraiOrg> moiraiOrgs = null;
        //机构开通的产品
        if (authOrgs != null && authOrgs.size() > 0) {
            PageHelper.startPage(moiraiOrgCondition.getPageNo(), moiraiOrgCondition.getPageSize());
            moiraiOrgCondition.setItems(authOrgs);
            moiraiOrgs = moiraiOrgDao.selectOrgBatcher(moiraiOrgCondition);
            PageInfo<MoiraiOrg> pageInfo = new PageInfo<>(moiraiOrgs);
            List<MoiraiOrgProduct> moiraiOrgProducts = moiraiOrgProductMapper.queryOrgProducts(authOrgs);
            total = pageInfo.getTotal();
            for (MoiraiOrg org : moiraiOrgs) {
                List<MoiraiOrgProduct> list = new ArrayList<>();
                for (MoiraiOrgProduct orgProduct : moiraiOrgProducts) {
                    if (org.getOrgId().equals(orgProduct.getOrgId())) {
                        list.add(orgProduct);
                    }
                }
                org.setProducts(list);
            }
        }

        BWJsonResult<MoiraiOrg> bwJsonResult = new BWJsonResult<>(moiraiOrgs);
        bwJsonResult.setTotal((int) total);
        return bwJsonResult;
    }

    /**
     * 切换机构下拉
     *
     * @param authz
     * @return
     */
    public BWJsonResult<MoiraiUserAuthz> getOrgsOfUserService(MoiraiUserAuthzOrg authz) {
        MoiraiUser moiraiUser = moiraiSysService.gainCacheUser();
        if (moiraiUser != null) {
            String orgName = authz.getOrgName();
            authz.setUserId(moiraiUser.getUserId());
            authz.setTenantId(moiraiUser.getTenantId());
            authz.setOrgName(orgName);
        } else {
            moiraiUser = moiraiUserMapper.selectByPrimaryKey(authz.getUserId());
        }
        if (authz.getPageNo() != -1){
            PageHelper.startPage(authz.getPageNo(), 100);
        }
        List<MoiraiUserAuthz> selectByExample = moiraiUserAuthzMapper.selectDiffAuthOrg(authz);
        //设置默认机构时，默认选中机构
        for (MoiraiUserAuthz userAuthz : selectByExample) {
            if (moiraiUser != null && userAuthz.getAuthOrg().equals(moiraiUser.getLoginOrg())) {
                userAuthz.setUserId(moiraiUser.getUserId());
                break;
            }
        }
        BWJsonResult<MoiraiUserAuthz> result = new BWJsonResult<>(selectByExample);
        if (authz.getPageNo() != -1){
            result.setTotal((int) new PageInfo<>(selectByExample).getTotal());
        }
        return result;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>批量导出机构信息<BR>
     *
     * @return
     * @since 2019/12/23
     */
    public void batchReadOrgInfoService(HttpServletRequest request, HttpServletResponse response,
        MoiraiOrgCondition moiraiOrgCondition) {
        Long tenantId = moiraiOrgCondition.getTenantId();
        Long userId = moiraiOrgCondition.getUserId();
        Long resourceId = moiraiOrgCondition.getResourceId();
        logger.info("机构导出: tenantId:{}, userId:{}, resourceId:{}", tenantId, userId, resourceId);

        Map<Long, String> proMap = new HashMap<>();
        if (userId == null) {
            moiraiOrgCondition.setUserId(moiraiSysService.gainCacheUserId());
        }
        List<MoiraiOrg> moiraiOrgs = moiraiOrgDao.selectOrgByAuth(moiraiOrgCondition);
        this.getOrgProduct(moiraiOrgCondition.getTenantId(), proMap);
        String fileName = "机构信息.xls";
        ServletOutputStream outputStream = null;

        try {
            // 判断使用的浏览器
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && -1 != agent.indexOf("MSIE")) {
                response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", " ") + "\"");
            } else if (null != agent && -1 != agent.indexOf("Firefox")) {
                response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(fileName.getBytes(), "iso8859-1") + "\"");
            } else {
                response.setHeader("Content-Disposition", "attachment;filename="
                    + new String(java.net.URLEncoder.encode(fileName, "utf-8").getBytes(), "iso8859-1"));
            }
            response.setContentType("application/vnd.ms-excel");
            // 判断使用的浏览器
            JSONObject jsonObject = moiraiExtService.getTemplateDate(String.valueOf(moiraiOrgCondition.getTenantId()), String.valueOf(moiraiOrgCondition.getOrgId()), String.valueOf(userId), Constants.MOIRAI_ORG_PAGE_UNIQUE_NAME);
            List<ExcelExportEntity> templateEntity = new ArrayList<>();
            List<Map<String, Object>> templateMapList = new ArrayList<>();

            JSONObject excelObj = (JSONObject) jsonObject.get(Constants.MOIRAI_ORG_EXPORT_TAG);
            JSONArray jsonArray = (JSONArray) excelObj.get("data");
            ExcelPoiUtil.buildExcelExportEntity(jsonArray, templateEntity);
            long startTime = System.currentTimeMillis();

            logger.info("start write : startTime = " + String.valueOf(startTime) + " ms");
            // sheet 对应一个工作页

            Map<Integer, String> addressInfo = this.getAddressInfo();
            for (MoiraiOrg moiraiOrg : moiraiOrgs) {
                if (moiraiOrg.getParentOrg() != null) {
                    MoiraiOrg parentOrg = new MoiraiOrg();
                    parentOrg.setOrgId(moiraiOrg.getParentOrg());
                    parentOrg = moiraiOrgMapper.selectOneOrg(parentOrg);
                    if (parentOrg != null) {
                        moiraiOrg.setBackup(parentOrg.getOrgCode());
                    }
                }
                Integer orgType = moiraiOrg.getOrgType();
                if (orgType != null) {
                    moiraiOrg.setTaxCodeName("1".equals(orgType.toString()) ? "纳税主体" : "非纳税主体");
                }
                String tgType = moiraiOrg.getTgType();
                if ("0".equals(tgType)) {
                    tgType = "平台托管";
                    moiraiOrg.setTgType(tgType);
                } else if ("1".equals(tgType)) {
                    tgType = "企业自建";
                    moiraiOrg.setTgType(tgType);
                } else if ("2".equals(tgType)) {
                    tgType = "小智自持";
                    moiraiOrg.setTgType(tgType);
                } else {
                    tgType = "";
                }
                String deviceType = moiraiOrg.getDeviceType();
                if ("0".equals(deviceType)) {
                    deviceType = "核心板";
                } else if ("1".equals(deviceType)) {
                    deviceType = "税控盘";
                } else if ("2".equals(deviceType)) {
                    deviceType = "SIMKEY";
                } else if ("3".equals(deviceType)) {
                    deviceType = "税控盒子";
                } else if ("4".equals(deviceType)) {
                    deviceType = "虚拟UKey";
                } else if ("5".equals(deviceType)) {
                    deviceType = "税务UKey";
                } else if ("6".equals(deviceType)) {
                    deviceType = "简易税控盘";
                } else if ("7".equals(deviceType)) {
                    deviceType = "金税盘";
                } else {
                    deviceType = "";
                }
                moiraiOrg.setDeviceType(deviceType);
                String taxQuali = moiraiOrg.getTaxQuali();
                if ("1".equals(taxQuali)) {
                    taxQuali = "一般纳税人";
                    moiraiOrg.setTaxQuali(taxQuali);
                } else {
                    taxQuali = "小规模纳税人";
                    moiraiOrg.setTaxQuali(taxQuali);
                }
                String useSelfinfo = moiraiOrg.getUseSelfinfo();
                if ("0".equals(useSelfinfo)) {
                    useSelfinfo = "否";
                    moiraiOrg.setUseSelfinfo(useSelfinfo);
                } else {
                    useSelfinfo = "是";
                    moiraiOrg.setUseSelfinfo(useSelfinfo);
                }

                //开通服务
                Long orgId1 = moiraiOrg.getOrgId();
                String proStr = proMap.get(orgId1);
                if (proStr != null) {
                    String openFW = null;
                    if (proStr.endsWith(",")) {
                        openFW = proStr.substring(0, proStr.length() - 1);
                    } else {
                        openFW = proStr;
                    }
                    moiraiOrg.setOpenFW(openFW);
                }

                //出口资质
                String exportQualify = moiraiOrg.getExportQualify();

                if ("1".equals(exportQualify)) {
                    exportQualify = "非外贸企业";
                } else if ("2".equals(exportQualify)) {
                    exportQualify = "外贸企业";
                } else if ("3".equals(exportQualify)) {
                    exportQualify = "外贸综合服务企业";
                } else {
                    exportQualify = "";
                }
                moiraiOrg.setExportQualify(exportQualify);

                moiraiOrg.setDetailProv(moiraiOrg.getRegProv() == null ? "" : addressInfo.get(Integer.valueOf(moiraiOrg.getRegProv() + "")));

                moiraiOrg.setDetailCity(moiraiOrg.getRegCity() == null ? "" : addressInfo.get(Integer.valueOf(moiraiOrg.getRegCity() + "")));

                moiraiOrg.setDetailArea(moiraiOrg.getRegArea() == null ? "" : addressInfo.get(Integer.valueOf(moiraiOrg.getRegArea() + "")));
            }
            ExportParams exportParams = new ExportParams();
            exportParams.setSheetName("机构信息");
            for (MoiraiOrg vo : moiraiOrgs) {
                moiraiExtService.buildExcelData(jsonArray, templateMapList, vo, vo.getExt());
            }
            outputStream = response.getOutputStream();
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, templateEntity, templateMapList);
            workbook.write(outputStream);
            response.flushBuffer();
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_ORG_BATCH_READ_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_BATCH_READ_ERROR);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException io) {
                    String requestURI = WebContext.getRequest().getRequestURI();
                    MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
                    logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), io);
                }
            }
        }
    }

    public void getOrgProduct(Long tenantId, Map<Long, String> proMap) {
        List<MoiraiOrgProduct> moiraiOrgProducts = moiraiOrgProductMapper.queryTenantProducts(tenantId);
        for (MoiraiOrgProduct product : moiraiOrgProducts) {
            Long orgId = product.getOrgId();
            Long productId = product.getProductId();
            String proStr = proMap.get(orgId);
            if (proStr == null) {
                proMap.put(orgId, productId + "");
            } else {
                proMap.put(orgId, proStr + "," + productId);
            }
        }
    }

    @Override
    public BWJsonResult<MoiraiOrg> getOrgMoveTree(MoiraiOrgCondition condition) {
        BWJsonResult<MoiraiOrg> bwJsonResult = new BWJsonResult<>();
        List<MoiraiOrg> orgList = moiraiOrgMapper.queryOrgSimpleTreeByTenant(condition.getTenantId());
        if (orgList != null && orgList.size() > 0) {
            List<Long> childOrgList = new ArrayList<>();
            childOrgList.add(condition.getOrgId());
            try {
                this.getChildOrgList(orgList, childOrgList, condition.getOrgId());
                List<MoiraiOrg> returnOrgs = orgList.stream().filter(org -> !childOrgList.contains(org.getOrgId())).collect(Collectors.toList());
                MoiraiOrg moiraiOrg = moiraiOrgService.combineOrgTree(returnOrgs, null);
                bwJsonResult.addData(moiraiOrg);
                bwJsonResult.setMessage("查询机构树成功");
                return bwJsonResult;
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_ORG_QUERYERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_QUERYERROR);
            }
        }
        return bwJsonResult;
    }

    private void getChildOrgList(List<MoiraiOrg> orgList, List<Long> childOrgList, Long orgId) {
        for (MoiraiOrg org : orgList) {
            if (orgId.equals(org.getParentOrg())) {
                childOrgList.add(org.getOrgId());
                this.getChildOrgList(orgList, childOrgList, org.getOrgId());
            }
        }
    }

    /**
     * 获取 所有的省市县信息
     *
     * @return
     */
    public Map<Integer, String> getAddressInfo() {
        List<SysProvCityDistTree> sysProvCityDistTrees = sysProvCityDistMapper.selectAll();
        Map<Integer, String> addMap = new HashMap();
        for (SysProvCityDistTree sysProvCityDistTree : sysProvCityDistTrees) {
            //省
            if ("0".equals(sysProvCityDistTree.getType())) {
                addMap.put(sysProvCityDistTree.getId(), sysProvCityDistTree.getProvince());
            }
            //市
            if ("1".equals(sysProvCityDistTree.getType())) {
                addMap.put(sysProvCityDistTree.getId(), sysProvCityDistTree.getCity());
            }
            //县 区
            if ("2".equals(sysProvCityDistTree.getType())) {
                addMap.put(sysProvCityDistTree.getId(), sysProvCityDistTree.getDistrict());
            }
        }
        return addMap;
    }

    @Override
    public MoiraiOrg getAuthOrgTree(MoiraiOrgCondition moiraiOrgCondition) {
        Long tenantId = moiraiOrgCondition.getTenantId();
        Long userId = moiraiOrgCondition.getUserId();
        Long resourceId = moiraiOrgCondition.getResourceId();
        logger.info("查询授权树/列表:[tenantId:{}, userId:{}]", tenantId, userId);
        List<MoiraiOrg> moiraiOrgList = moiraiOrgMapper.queryOrgSimpleTreeByTenant(tenantId);
        MoiraiOrg topOrg = null;
        Map<Long, List<MoiraiOrg>> tenantOrgMap = new HashMap<>();
        //用户授权机构
        List<MoiraiUserAuthz> authzs = moiraiOrgService.getUserAuthBycondition(userId, resourceId);
        for (int i = 0; i < moiraiOrgList.size(); i++) {
            MoiraiOrg tenantOrgItem = moiraiOrgList.get(i);
            Long parentId = tenantOrgItem.getParentOrg();
            Long orgId = tenantOrgItem.getOrgId();
            /**找出顶级组织机构**/
            if (parentId.equals(0L)) {
                topOrg = tenantOrgItem;
            }
            /**轮询组织机构列表**/
            List<MoiraiOrg> parentList = tenantOrgMap.get(parentId);
            if (parentList != null) {
                parentList.add(tenantOrgItem);
            } else {
                List<MoiraiOrg> parentListitem = new ArrayList<>();
                parentListitem.add(tenantOrgItem);
                tenantOrgMap.put(parentId, parentListitem);
            }

            /**标记授权机构**/
            for (MoiraiUserAuthz auth : authzs) {
                if (auth.getAuthOrg().equals(orgId)) {
                    tenantOrgItem.setIsCheck("Y");
                    break;
                }
            }
        }
        if (topOrg == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_QUERYERROR);
        }
        topOrg.setChildren(tenantOrgMap.get(topOrg.getOrgId()));
        moiraiOrgService.combineTreeChildren(topOrg, tenantOrgMap);
        return topOrg;
    }

    @Override
    public List<MoiraiOrg> getParentOrgTree(List<MoiraiOrgCondition> moiraiOrgCondition) {
        List<MoiraiOrg> moiraiOrgList = new ArrayList<>();
        for (MoiraiOrgCondition condition : moiraiOrgCondition) {
            if (condition.getOrgId() == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            MoiraiOrg org = moiraiOrgMapper.selectOneOrg(condition);
            MoiraiOrg moiraiOrg = getParentOrg(org);
            moiraiOrgList.add(moiraiOrg);
        }
        return moiraiOrgList;
    }

    private MoiraiOrg getParentOrg(MoiraiOrg org) {
        if (org != null && org.getParentOrg() != 0) {
            MoiraiOrg moiraiOrg = new MoiraiOrg();
            moiraiOrg.setOrgId(org.getParentOrg());
            MoiraiOrg parentOrg = moiraiOrgMapper.selectOneOrg(moiraiOrg);
            List<MoiraiOrg> orgList = new ArrayList<>();
            orgList.add(org);
            parentOrg.setChildren(orgList);
            return getParentOrg(parentOrg);
        } else {
            return org;
        }
    }

}
