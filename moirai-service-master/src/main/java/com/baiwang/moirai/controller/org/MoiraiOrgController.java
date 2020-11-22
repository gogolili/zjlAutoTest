package com.baiwang.moirai.controller.org;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.annotations.ResultMapOpt;
import com.baiwang.moirai.annotations.ResultMapping;
import com.baiwang.moirai.api.MoiraiOrgSvc;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgConfig;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.org.MoiraiOrgProductVO;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiTenantAnotherService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.utils.StrUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 组织机构使用相关接口API Author:Lance cui date:2017-11-3
 */
@RestController
@SuppressWarnings("all")
public class MoiraiOrgController implements MoiraiOrgSvc {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    private MoiraiTenantAnotherService moiraiTenantAnotherService;

    @Value("${use.method}")
    private boolean useFlag;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    //默认开启缓存
    @Value("${cache:true}")
    private boolean cache;

    /**
     * 添加组织机构
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @UserCenterOperationLog(moduleName = "机构管理", action = "添加机构", description = "添加机构")
    public BWJsonResult<MoiraiUser> addOrg(@RequestBody MoiraiOrg moiraiOrg) {
        logger.info("添加组织机构入参：{}", moiraiOrg.toString());
        Long parentOrgId = moiraiOrg.getParentOrg();
        Integer orgType = moiraiOrg.getOrgType();
        Long tenantId = moiraiOrg.getTenantId();
        String creater = moiraiOrg.getCreater();
        /**插入数据库必填字段**/
        if (parentOrgId == null || orgType == null || tenantId == null || tenantId == 0 || StrUtils.isEmpty(creater)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiUser moiraiUser = moiraiOrgService.addorg(moiraiOrg);
        BWJsonResult bWJsonResult = new BWJsonResult(moiraiUser);
        bWJsonResult.setMessage("添加组织机构成功");
        return bWJsonResult;
    }

    @Override
    @Transactional
    public BWJsonResult addOrgList(@RequestBody List<MoiraiOrg> moiraiOrgList) {
        if (moiraiOrgList != null && moiraiOrgList.size() > 0) {
            for (int i = 0; i < moiraiOrgList.size(); i++) {
                MoiraiOrg moiraiOrg = moiraiOrgList.get(i);
                BWJsonResult resu = addOrg(moiraiOrg);
                if (!resu.isSuccess()) {
                    throw new MoiraiException("2103", ("第" + i + "条组织机构添加错误。"));
                }
            }
        }
        BWJsonResult bWJsonResult = new BWJsonResult();
        bWJsonResult.setMessage("组织机构添加成功。");
        return bWJsonResult;
    }

    @Override
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    @UserCenterOperationLog(moduleName = "机构管理", action = "编辑机构", description = "编辑机构")
    public BWJsonResult<MoiraiOrg> updateOrg(@RequestBody MoiraiOrg moiraiOrg) {
        //不要再加条件
        if (moiraiOrg.getOrgId() == null && StringUtils.isEmpty(moiraiOrg.getTaxCode())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("更新机构信息：{}", moiraiOrg.toString());
        int updateResult = moiraiOrgService.updateOrg(moiraiOrg);
        if (updateResult == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_UPDATEERROR);
        }
        BWJsonResult BWJsonResult = new BWJsonResult();
        BWJsonResult.setMessage("更新组织机构成功");
        return BWJsonResult;
    }

    /**
     * 修改组织机构税号，名称
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult<MoiraiOrg> changeOrgTaxName(@RequestBody MoiraiOrg moiraiOrg) {
        if (moiraiOrg == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        Long orgId = moiraiOrg.getOrgId();
        String orgName = moiraiOrg.getOrgName();
        String taxCode = moiraiOrg.getTaxCode();
        Long tenantId = moiraiOrg.getTenantId();
        if (orgId == null || tenantId == null || (taxCode == null && orgName == null)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        /**检验机构状态**/
        boolean flag = moiraiOrgService.cheackTaxCodeStatus(moiraiOrg);
        if (flag) {
            int updateResult = moiraiOrgService.updateOrgTaxName(moiraiOrg);
        }
        BWJsonResult BWJsonResult = new BWJsonResult();
        BWJsonResult.setMessage("更新组织机构成功");
        return BWJsonResult;
    }

    /**
     * 删除组织机构
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    @UserCenterOperationLog(moduleName = "机构管理", action = "删除机构", description = "删除机构")
    public BWJsonResult<MoiraiOrg> deleteOrg(@RequestBody MoiraiOrg moiraiOrg) {
        if (moiraiOrg == null || moiraiOrg.getOrgId() == null || moiraiOrg.getTenantId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiOrgService.deleteOrgRel(moiraiOrg);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("删除组织机构成功");
        return bwJsonResult;
    }

    @Override
    public BWJsonResult<MoiraiOrg> getOrgByCondition(@RequestBody MoiraiOrgCondition moiraiOrg) {
        BWJsonResult<MoiraiOrg> bwJsonResult = null;
        // 是否走缓存的判断
        // 参数是税号且精确查询
        boolean shouldCache = StringUtils.isNotBlank(moiraiOrg.getTaxCode()) && "false".equals(moiraiOrg.getFuzzyQuery());
        // 以上判断为True 或 参数是机构id
        shouldCache = moiraiOrg.getOrgId() != null || shouldCache;
        // 以上判断为True 且 开启缓存
        shouldCache = cache && shouldCache;
        if (shouldCache) {
            MoiraiOrg org = moiraiOrgService.getCacheMoiraiOrg(moiraiOrg.getOrgId(), moiraiOrg.getTaxCode());
            if (org == null) {
                bwJsonResult = new BWJsonResult<>();
            } else {
                bwJsonResult = new BWJsonResult<>(org);
            }
        } else {
            logger.info("getOrgByCondition 请求未走缓存,请求参数：【{}】", moiraiOrg);
            bwJsonResult = moiraiOrgService.getOrgByCondition(moiraiOrg);
        }
        return bwJsonResult;
    }

    /**
     * 查询组织机构信息
     * @param moiraiOrg
     * @return
     */
    @PostMapping("/getChannelOrgByCondition")
    public BWJsonResult<MoiraiOrg> getChannelOrgByCondition(@RequestBody MoiraiOrgCondition moiraiOrg){
        if(moiraiOrg.getPageNo() <= 0){
            moiraiOrg.setPageNo(1);
        }
        if (moiraiOrg.getPageSize() <= 0){
            moiraiOrg.setPageSize(10);
        }
        logger.info("*****getChannelOrgByCondition接口请求参数:{}*****", moiraiOrg.toString());
        return moiraiOrgService.getChannelOrgByCondition(moiraiOrg);
    }

    /**
     * 对内根据orgId查询机构信息
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult<MoiraiOrg> getOrgByOrgId(@RequestBody MoiraiOrg moiraiOrg) {
        Long orgId = moiraiOrg.getOrgId();
        if (orgId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiOrg org = moiraiOrgService.getOrgByOrgId(moiraiOrg);
        if (org == null) {
            return new BWJsonResult<>();
        } else {
            return new BWJsonResult<>(org);
        }
    }

    /**
     * 获取组织机构 1.根据条件获取组织机构信息 2.查询条件有orgId,taxCode,,orgName,taxEntity 3.查询优先顺序为 orgId,taxCode,taxentityId,orgName
     */
    @Override
    public BWJsonResult<MoiraiOrg> getOrgListByCondition(@RequestBody Map<String, List> moiraiOrgList) {

        if (moiraiOrgList == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrg> moiraiOrgLisResultt = moiraiOrgService.getOrgListByCondition(moiraiOrgList);

        return new BWJsonResult<>(moiraiOrgLisResultt);
    }

    /**
     * 根据历史税号或者名称获取组织机构信息
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult<MoiraiOrg> getOrgHistoryTaxOrName(@RequestBody MoiraiOrg moiraiOrg) {

        if (StrUtils.isEmpty(moiraiOrg.getTaxCode()) && StrUtils.isEmpty(moiraiOrg.getOrgName()) && StrUtils.isEmpty(moiraiOrg.getOrgId() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrg> moiraiOrgLisResult = moiraiOrgService.getOrgHistoryTaxOrName(moiraiOrg);
        return new BWJsonResult<>(moiraiOrgLisResult);
    }

    @Override
    @Deprecated
    public BWJsonResult<MoiraiOrg> getOrgTree(@RequestBody MoiraiOrg moiraiOrg) {
        List<MoiraiOrg> moiraiOrgList = moiraiOrgService.getTenantOrgTree(moiraiOrg, 1);
        MoiraiOrg moiraiOrgTree = null;
        if (moiraiOrgList != null && moiraiOrgList.size() > 0) {
            moiraiOrgTree = moiraiOrgList.get(0);
            return new BWJsonResult<>(moiraiOrgTree);
        }
        return new BWJsonResult<>();
    }

    /**
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult<MoiraiOrg> getOrgSimpleTree(@RequestBody MoiraiOrg moiraiOrg) {
        List<MoiraiOrg> moiraiOrgList = moiraiOrgService.getTenantOrgSimpleTree(moiraiOrg, 1);
        if (!moiraiOrgList.isEmpty()) {
            return new BWJsonResult<>(moiraiOrgList.get(0));
        }
        return new BWJsonResult<>();
    }

    @Override
    public BWJsonResult<MoiraiOrg> getOrgTaxTypeList(@RequestBody MoiraiOrg moiraiOrg) {

        Integer orgType = moiraiOrg.getOrgType();
        Long orgId = moiraiOrg.getOrgId();
        Long tenantId = moiraiOrg.getTenantId();
        if (orgType == null || orgId == null || tenantId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrg> moiraiOrgTaxList = new ArrayList<MoiraiOrg>();
        List<MoiraiOrg> moiraiOrgList = moiraiOrgService.getTenantOrgSimpleTree(moiraiOrg, 2);

        for (MoiraiOrg moiraiOrgItem : moiraiOrgList) {
            if (orgType.equals(moiraiOrgItem.getOrgType())) {
                moiraiOrgTaxList.add(moiraiOrgItem);
            }
        }

        return new BWJsonResult(moiraiOrgTaxList);
    }

    @Override
    public BWJsonResult<MoiraiOrg> getOrgTaxEntity(@RequestBody MoiraiOrg moiraiOrg) {

        if (moiraiOrg.getOrgId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }

        MoiraiOrg orgTaxEntity = moiraiOrgService.getOrgTaxEntity(moiraiOrg);
        if (orgTaxEntity == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_PARENTNOTEXT);
        }

        return new BWJsonResult(orgTaxEntity);
    }

    /**
     * <B>方法名称：</B>获取机构列表<BR>
     * <B>概要说明：</B>运营后台2.0接口，1.0改造<BR>
     *
     * @return
     * @since 2019年4月22日
     */
    @Override
    public BWJsonResult<MoiraiOrg> getTaxEntityList(@RequestBody MoiraiOrgCondition moiraiOrgCondition) {

        BWJsonResult<MoiraiOrg> regResult = null;
        Long count = null;
        if (0 >= moiraiOrgCondition.getPageNo()) {
            moiraiOrgCondition.setPageNo(1);
        }
        if (0 >= moiraiOrgCondition.getPageSize()) {
            moiraiOrgCondition.setPageSize(20);
        }
        /**是否有products产品查询过滤条件*/
        if (moiraiOrgCondition.getProductCon() != null && moiraiOrgCondition.getProductCon().size() > 0) {
            moiraiOrgCondition.setIsNeedProduct(moiraiOrgCondition.getIsNeedProduct() + 2);
        }
        /**是否有渠道查询过滤条件*/
        if (moiraiOrgCondition.getQdBms() != null && moiraiOrgCondition.getQdBms().size() > 0) {
            moiraiOrgCondition.setIsNeedProduct(moiraiOrgCondition.getIsNeedProduct() + 1);
        }
        if (Constants.MOIRAI_VERSION_V2.equals(moiraiOrgCondition.getVersion())){
            regResult = moiraiOrgService.getOrgListPage2(moiraiOrgCondition);
        } else {
            logger.info("请求getTaxEntityList接口版本号:{}", moiraiOrgCondition.getVersion() == null ? "v1" : moiraiOrgCondition.getVersion());
            regResult = moiraiOrgService.getOrgListPage(moiraiOrgCondition);
        }

//        if (regResult.isSuccess() && regResult.getData() != null && regResult.getData().size() > 0) {
//            List<MoiraiOrg> regData = regResult.getData();
//            for (int i = 0; i < regData.size(); i++) {
//                MoiraiOrg regResultItem = regData.get(i);
//                List<MoiraiOrgProduct> products = moiraiOrgService.getOrgProducts(regResultItem.getOrgId());
//                if (products != null && products.size() > 0) {
//                    regResultItem.setProducts(products);
//                }
//            }
//            regResult.setMessage("查询税号列表成功");
//        }

        return regResult;
    }

    @Override
    public BWJsonResult<MoiraiOrgProductVO> getTenantAllProducts(@RequestBody MoiraiTenant moiraiTenant) {

        Long tenantId = moiraiTenant.getTenantId();
        if (tenantId == null || tenantId == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_QUERYERROR);
        }

        List<MoiraiOrgProductVO> productsList = moiraiOrgService.getTenantAllProducts(moiraiTenant);
        Map<Long, List> tenantProductMap = new HashMap<Long, List>();

        for (int i = 0; i < productsList.size(); i++) {
            MoiraiOrgProductVO orgProduct = productsList.get(i);
            Long productId = orgProduct.getProductId();
            List productMapList = tenantProductMap.get(productId);
            if (productMapList != null) {
                productMapList.add(orgProduct);
            } else {
                productMapList = new ArrayList();
                productMapList.add(orgProduct);
                tenantProductMap.put(productId, productMapList);
            }
        }

        return new BWJsonResult(tenantProductMap);
    }

    @Override
    public BWJsonResult<MoiraiOrgConfig> setOrgConfig(@RequestBody List<MoiraiOrgConfig> moiraiOrgConfigList) {

        BWJsonResult addConfigResult = moiraiOrgService.setOrgConfig(moiraiOrgConfigList);

        if (addConfigResult.isSuccess()) {
            addConfigResult.setMessage("设置组织机构相关配置成功");
        }

        return addConfigResult;
    }

    @Override
    public BWJsonResult<MoiraiOrgConfig> getOrgConfig(@RequestBody MoiraiOrgConfig moiraiOrgConfig) {
        Long orgId = moiraiOrgConfig.getOrgId();
        String item = moiraiOrgConfig.getItem();

        if (StrUtils.isEmpty(item) || orgId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiOrgConfig moiraiOrgConfigResu = moiraiOrgService.getOrgConfig(moiraiOrgConfig);

        if (moiraiOrgConfigResu != null) {
            BWJsonResult BWJsonResult = new BWJsonResult(moiraiOrgConfigResu);
            BWJsonResult.setMessage("查询组织机构相关配置成功");
            return BWJsonResult;
        }

        BWJsonResult BWJsonResult = new BWJsonResult();
        BWJsonResult.setMessage("未查询到组织机构相关配置");
        return BWJsonResult;
    }

    @Override
    public BWJsonResult<MoiraiOrgConfig> getAllOrgConfig(@RequestBody MoiraiOrgConfig moiraiOrgConfig) {

        Long orgId = moiraiOrgConfig.getOrgId();

        if (orgId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrgConfig> moiraiOrgConfigList = moiraiOrgService.getAllOrgConfig(moiraiOrgConfig);

        if (moiraiOrgConfigList != null && moiraiOrgConfigList.size() > 0) {
            BWJsonResult BWJsonResult = new BWJsonResult(moiraiOrgConfigList);
            BWJsonResult.setMessage("查询组织机构相关配置成功");
            return BWJsonResult;
        }
        BWJsonResult BWJsonResult = new BWJsonResult();
        BWJsonResult.setMessage("未查询到组织机构相关配置");
        return BWJsonResult;
    }

    /**
     * 组织机构开通产品
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult<MoiraiOrg> openOrgProduct(@RequestBody MoiraiOrg moiraiOrg) {
        //添加开通产品的操作
        logger.info("机构开通产品请求参数：" + moiraiOrg.toString());
        Long orgId = moiraiOrg.getOrgId();
        List<MoiraiOrgProduct> products = moiraiOrg.getProducts();
        if (orgId == null || products == null || products.size() == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiOrgService.openOrgProduct(moiraiOrg);
        BWJsonResult bwR = new BWJsonResult();
        bwR.setMessage("开通产品成功");
        return bwR;
    }

    /**
     * 进入销项时判断机构信息是否完整
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult checkOrgInfo(@RequestBody MoiraiOrg moiraiOrg) {
        if (moiraiOrg == null || StringUtils.isEmpty(moiraiOrg.getOrgId() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("==================请求参数:{}=================", moiraiOrg.getOrgId());
        BWJsonResult<MoiraiOrg> bwJsonResult = moiraiOrgService.checkOrgInfo(moiraiOrg);
        return bwJsonResult;
    }

    /**
     * 添加机构第一页完成点击下一页时校验当前页参数
     *
     * @param moiraiOrg
     * @return
     */
    @Override
    public BWJsonResult validateParam(@RequestBody MoiraiOrg moiraiOrg) {
        if (moiraiOrg.getTenantId() == null || moiraiOrg.getOrgType() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        } else {
            logger.info("==================请求参数:{}=================", moiraiOrg.toString());
            moiraiOrgService.validateParam(moiraiOrg);
        }
        return new BWJsonResult();
    }

    /**
     * 根据用户账号查询用户所在机构的税号  ==>开放平台定制
     *
     * @param moiraiUserCondition
     * @return
     */
    @Override
    public BWJsonResult<String> findTaxCodeByUserAccount(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (moiraiUserCondition == null || StrUtils.isEmpty(moiraiUserCondition.getUserAccount())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("==================请求参数:{}=================", moiraiUserCondition.toString());
        MoiraiOrg moiraiOrg = moiraiUserService.findTaxCodeByUserAccount(moiraiUserCondition.getUserAccount());
        if (moiraiOrg != null) {
            return new BWJsonResult<>(moiraiOrg.getTaxCode());
        } else {
            return new BWJsonResult<>();
        }
    }

    /**
     * 根据条件查询税号相关信息  ==>开放平台定制
     *
     * @param requestMap
     * @return
     */
    @Override
    public BWJsonResult<List<Map<String, Object>>> findTaxCodeListInfo(@RequestBody Map<String, Object> requestMap) {
        logger.info("==================请求参数:{}=================", requestMap.toString());
        BWJsonResult<List<Map<String, Object>>> taxList = moiraiOrgService.findTaxCodeListInfo(requestMap);
        return taxList;
    }

    /**
     * <B>方法名称：</B>机构excel导入<BR>
     * <B>概要说明：</B>校验全部Excel内容，并把错误信息上传至oss，待用户下载，否则导入成功<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    @Override
    @UserCenterOperationLog(moduleName = "机构管理", action = "批量导入机构", description = "批量导入机构")
    public BWJsonResult uploadOrgExcel(@RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
        HttpServletRequest request) {
        String tenantId = request.getParameter("tenantId");
        String orgId = request.getParameter("orgId");
        String creater = request.getParameter("creater");
        String userId = request.getParameter("userId");
        logger.info("************tenantId:{}, orgId:{}, creater:{}*************", tenantId, orgId, creater);
        if (StringUtils.isEmpty(tenantId) || StringUtils.isEmpty(orgId) ||
            StringUtils.isEmpty(creater)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        // 文件不存在的情况
        if (excelFile == null || StringUtils.isEmpty(excelFile.getOriginalFilename())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_EXCELFILE_ERROR);
        }
        Long tenantIdL = Long.valueOf(tenantId);
        Long orgIdL = Long.valueOf(orgId);
        BWJsonResult readExcelRel = moiraiTenantAnotherService.readExcelRel(excelFile, tenantIdL, orgIdL, userId, creater);
        return readExcelRel;
    }
}
