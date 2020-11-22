/*
 * @项目名称: BOP-Moirai
 * @文件名称: MoiraiTenantConstoller.java
 * @Date: 17-10-26 下午2:29
 * @author Lance cui
 *
 */

package com.baiwang.moirai.controller.tenant;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.utils.HttpClientUtils;
import com.baiwang.moirai.annotations.ResultMapOpt;
import com.baiwang.moirai.annotations.ResultMapping;
import com.baiwang.moirai.api.MoiraiTenantSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.event.TenantEvent;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantListCondition;
import com.baiwang.moirai.model.tenant.MoiraiTenantListVO;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import com.baiwang.moirai.model.tenant.TenantRegisterStatusVO;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiTenantService;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageInfo;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户使用相关接口API Author:Lance cui date:2017-11-3
 */
@RestController
@SuppressWarnings("all")
public class MoiraiTenantController implements MoiraiTenantSvc {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiTenantService moiraiTenantService;

    //默认开启缓存
    @Value("${cache:true}")
    private boolean cache;

    /**
     * 云抬头查询来源
     */
    @Value("${bwCloud.bwapi.companySearch.url:moirai}")
    private String companySearchUrl;

    /**
     * 云抬头token
     */
    @Value("${bwCloud.bwapi.companySearch.appId:appid}")
    private String companySearchAppId;

    @Resource
    private ApplicationEventPublisher context;

    /**
     * 注册租户 Author:Lance cui
     *
     * @param moiraiTenantVOList
     * @return BWJsonResult
     * @throws Exception
     */
    @Override
    @Transactional
    @ResultMapping(ResultMapOpt.ARRAY_TO_SINGLE)
    public BWJsonResult<MoiraiUser> registerTenant(@RequestBody MoiraiTenantVO moiraiTenant) {
        if (moiraiTenant == null) {
            throw new MoiraiException("2100", "没有租户需要注册");
        }
        //必要字段判空
        String originMark = moiraiTenant.getOriginMark();
        String tenantName = moiraiTenant.getTenantName();
        String tenantEmail = moiraiTenant.getTenantEmail();
        String taxCode = moiraiTenant.getTaxCode();
        if (StrUtils.isEmpty(originMark) || RegularExpUtils.validName(tenantName) || StrUtils.isEmpty(tenantEmail)) {
            throw new MoiraiException("2101", "租户必填字段为空或不正确");
        }
        if (!RegularExpUtils.checkEmail(tenantEmail)) {
            throw new MoiraiException("2103", "租户邮箱不合法");
        }
        int tenantStatus = moiraiTenantService.tenantRegisterStatus(tenantName, taxCode);
        if (tenantStatus == 1 || tenantStatus == 101 || tenantStatus == 4) {
            throw new MoiraiException("2102", "租户名称或者税号已经注册过");
        }
        //删除僵尸租户。（如果失败如何操作）
        if (tenantStatus == 0) {
            int delZombie = moiraiTenantService.deleteZombieTenant(tenantName, taxCode);
        }

        logger.info(moiraiTenant.toString());
        //租户注册
        MoiraiUser regItem = moiraiTenantService.regTenant(moiraiTenant);
        if (regItem == null) {
            throw new MoiraiException("2103", "租户注册添加失败");
        }
        BWJsonResult jsonResult = new BWJsonResult(regItem);
        jsonResult.setMessage("注册租户成功");
        return jsonResult;
    }

    /**
     * 租户添加 Author:Lance cui
     *
     * @param moiraiTenantVOList
     * @return BWJsonResult
     * @throws Exception
     */
    @Override
    @Transactional
    public BWJsonResult<MoiraiUser> addTenant(@RequestBody List<MoiraiTenantVO> moiraiTenantVOList) {
        List<MoiraiUser> regResultUser = new ArrayList<MoiraiUser>();
        for (int i = 0; i < moiraiTenantVOList.size(); i++) {

            MoiraiTenantVO moiraiTenant = moiraiTenantVOList.get(i);
            //必要字段判空
            String originMark = moiraiTenant.getOriginMark();
            String tenantName = moiraiTenant.getTenantName();
            String tenantEmail = moiraiTenant.getTenantEmail();
            String taxCode = moiraiTenant.getTaxCode();
            if (StrUtils.isEmpty(originMark) || RegularExpUtils.validName(tenantName) || StrUtils.isEmpty(tenantEmail)) {
                throw new MoiraiException("2101", "第" + (i + 1) + "条租户必填字段为空或不正确");
            }
            if (!RegularExpUtils.checkEmail(tenantEmail)) {
                throw new MoiraiException("2103", "第" + (i + 1) + "条租户邮箱不合法");
            }
            int tenantStatus = moiraiTenantService.tenantRegisterStatus(tenantName, taxCode);
            if (tenantStatus == 1 || tenantStatus == 101 || tenantStatus == 4) {
                throw new MoiraiException("2102", "第" + (i + 1) + "条租户名称或者税号已经注册过");
            }
            //删除僵尸租户。（如果失败如何操作）
            if (tenantStatus == 0) {
                int delZombie = moiraiTenantService.deleteZombieTenant(tenantName, taxCode);
            }

            logger.info(moiraiTenant.toString());
            MoiraiUser regResult = moiraiTenantService.addTenant(moiraiTenant);
            if (regResult != null) {
                regResultUser.add(regResult);
            } else {
                throw new MoiraiException("2103", "第" + (i + 1) + "条租户注册添加失败");
            }
            context.publishEvent(new TenantEvent(regResult, moiraiTenant));
        }
        BWJsonResult bwJsonResult = new BWJsonResult<>(regResultUser);
        bwJsonResult.setMessage("租户添加成功");
        return bwJsonResult;
    }

    /**
     * 根据公司名称和税号判断租户状态，主要判断是否注册过或者为僵尸户 Author:Lance cui
     *
     * @param tenant
     * @return int
     * @throws Exception
     */
    @Override
    public BWJsonResult<TenantRegisterStatusVO> tenantRegisterStatus(@RequestBody TenantRegisterStatusVO tenant) {
        String tenantName = tenant.getTenantName();
        String taxCode = tenant.getTaxCode();
        int status = moiraiTenantService.tenantRegisterStatus(tenantName, taxCode);
        tenant.setStatus(status + "");
        BWJsonResult<TenantRegisterStatusVO> tenantResut = new BWJsonResult<TenantRegisterStatusVO>(tenant);
        return tenantResut;
    }

    /**
     * 查询租户的状态 0-僵尸租户 1-正式租户-名称存在 100-无此租户 101-税号已存在 4-编码已存在
     */
    @Override
    public BWJsonResult<TenantRegisterStatusVO> tenantBatchStatus(
        @RequestBody List<TenantRegisterStatusVO> tenantList) {
        if (tenantList != null) {
            for (int i = 0; i < tenantList.size(); i++) {
                TenantRegisterStatusVO item = tenantList.get(i);
                String tenantName = item.getTenantName();
                String taxCode = item.getTaxCode();
                int status = moiraiTenantService.tenantRegisterStatus(tenantName, taxCode);
                item.setStatus(status + "");
            }
        }
        BWJsonResult<TenantRegisterStatusVO> tenantResult = new BWJsonResult<TenantRegisterStatusVO>(tenantList);
        return tenantResult;
    }

    /**
     * 完善租户信息，更新租户和组织机构信息 Author:Lance cui
     *
     * @param moiraiTenant
     * @return BWJsonResult
     * @throws Exception
     */
    @Override
    public BWJsonResult<MoiraiTenantVO> completeTenantInfo(@RequestBody MoiraiTenantVO moiraiTenant) {
        BWJsonResult bwJsonResult = new BWJsonResult<>();

        //必要字段判空
        Long tenantid = moiraiTenant.getTenantId();
        Long orgId = moiraiTenant.getOrgId();
        if (tenantid == null || tenantid == 0 || orgId == null || orgId == 0) {
            return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }

        logger.info(moiraiTenant.toString());
        int regResult = moiraiTenantService.completeTenantInfo(moiraiTenant);

        bwJsonResult.setMessage("租户更新成功");
        return bwJsonResult;
    }

    /**
     * 修改租户 Author:Lance cui
     *
     * @param moiraiTenant
     * @return BWJsonResult
     * @throws Exception
     */
    @Override
    public BWJsonResult<MoiraiTenant> updateTenant(@RequestBody MoiraiTenant moiraiTenant) {

        Long tenantId = moiraiTenant.getTenantId();
        if (tenantId == null) {
            return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_UPDATEERROR));
        }
        int regResult = moiraiTenantService.updateTenant(moiraiTenant);

        BWJsonResult bwR = new BWJsonResult();
        bwR.setMessage("更新租户成功");
        return bwR;
    }

    @Override
    public BWJsonResult<MoiraiTenantVO> getTenantByCondition(@RequestBody MoiraiTenantVO moiraiTenantVO) {
        BWJsonResult bwJsonResult = null;
        if (moiraiTenantVO == null) {
            return new BWJsonResult<>(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_QUERYERROR));
        }
        Long tenantId = moiraiTenantVO.getTenantId();
        if (tenantId != null && cache) {
            MoiraiTenantVO moiraiTenant = moiraiTenantService.getCacheMoiraiTenant(tenantId);
            if (moiraiTenant == null) {
                bwJsonResult = new BWJsonResult<>();
            } else {
                bwJsonResult = new BWJsonResult<>(moiraiTenant);
            }
        } else {
            if (0 >= moiraiTenantVO.getPageNo()) {
                moiraiTenantVO.setPageNo(1);
            }
            if (0 >= moiraiTenantVO.getPageSize()) {
                moiraiTenantVO.setPageSize(100);
            }
            logger.info("getTenantByCondition 请求未走缓存,请求参数：【{}】", moiraiTenantVO);
            List<MoiraiTenantVO> regResult = moiraiTenantService.getTenantByCondition(moiraiTenantVO);
            PageInfo<MoiraiTenantVO> pageInfo = new PageInfo<>(regResult);
            bwJsonResult = new BWJsonResult(regResult);
            bwJsonResult.setTotal((int) pageInfo.getTotal());
        }
        return bwJsonResult;
    }

    /**
     * 获取租户列表 Author:Lance cui
     *
     * @param tenantListCondition
     * @return List<MoiraiTenantListVO>
     * @throws Exception
     */
    @Override
    public BWJsonResult<MoiraiTenantListVO> getTenantListPage(
        @RequestBody MoiraiTenantListCondition tenantListCondition) {
        BWJsonResult<MoiraiTenantListVO> regResult = null;
        if (0 >= tenantListCondition.getPageNo()) {
            tenantListCondition.setPageNo(1);
        }
        if (0 >= tenantListCondition.getPageSize()) {
            tenantListCondition.setPageSize(20);
        }
        /**是否有products产品查询过滤条件*/
        if (tenantListCondition.getProducts() != null && tenantListCondition.getProducts().size() > 0) {
            tenantListCondition.setIsNeedProduct(tenantListCondition.getIsNeedProduct() + 2);
        }
        /**是否有渠道查询过滤条件*/
//        if (tenantListCondition.getQdBms() != null && tenantListCondition.getQdBms().size() > 0) {
//            tenantListCondition.setIsNeedProduct(tenantListCondition.getIsNeedProduct() + 1);
//        }
        logger.info("getTenantListPage 请求参数：【{}】", tenantListCondition);
        if ("1".equals(tenantListCondition.getTenantType())) {
            regResult = moiraiTenantService.getTenantListPage(tenantListCondition);
        } else if ("0".equals(tenantListCondition.getTenantType())) {
            regResult = moiraiTenantService.getTenantZomListPage(tenantListCondition);
        } else {
            return new BWJsonResult<>();
        }
        return regResult;
    }

    /**
     * 租户查询 运营后台租户查询
     */
    @Override
    public BWJsonResult<MoiraiTenantListVO> getTenantList2Page(@RequestBody MoiraiTenantListCondition condition) {
        Long s = System.currentTimeMillis();
        logger.info("getTenantList2Page start：{}", condition.toString());
        if (0 >= condition.getPageNo()) {
            condition.setPageNo(1);
        }
        if (0 >= condition.getPageSize()) {
            condition.setPageSize(20);
        }
        /**是否有products产品查询过滤条件*/
        if (condition.getProducts() != null && condition.getProducts().size() > 0) {
            condition.setIsNeedProduct(condition.getIsNeedProduct() + 2);
        }
        BWJsonResult<MoiraiTenantListVO> result = new BWJsonResult<>();
        if ("1".equals(condition.getTenantType())) {
            result = moiraiTenantService.getTenantList2Page(condition);
        } else if ("0".equals(condition.getTenantType())) {
            result = moiraiTenantService.getTenantZomListPage(condition);
        }
        logger.info("getTenantList2Page end, time {} ms", System.currentTimeMillis() - s);
        return result;
    }

    /**
     * 注册租户时首页校验邮箱
     *
     * @param moiraiTenant
     * @return
     */
    @Override
    @Deprecated
    public BWJsonResult validateEmailAndName(@RequestBody MoiraiTenantVO moiraiTenant) {
        if (moiraiTenant == null || (StringUtils.isEmpty(moiraiTenant.getTenantEmail()) &&
            StringUtils.isEmpty(moiraiTenant.getTenantName()) && StringUtils.isEmpty(moiraiTenant.getTenantPhone()))) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        String tenantEmail = moiraiTenant.getTenantEmail();
        String tenantPhone = moiraiTenant.getTenantPhone();
        String tenantName = moiraiTenant.getTenantName();
        if (StringUtils.isNotEmpty(tenantEmail)) {
            if (!RegularExpUtils.checkEmail(moiraiTenant.getTenantEmail())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_EMAIL_ERROR);
            }
        }
        if (StringUtils.isNotEmpty(tenantPhone)) {
            if (!(RegularExpUtils.checkMobile(tenantPhone) || RegularExpUtils.isFixedPhone(tenantPhone))) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_PHONE_ERROR);
            }
        }
        return new BWJsonResult();
    }

    /**
     * <B>方法名称：</B>税号查询所在租户信息<BR>
     * <B>概要说明：</B>运营后台2.0使用<BR>
     *
     * @return
     * @since 2019年7月17日
     */
    @PostMapping("/queryTenantByTaxCode")
    public BWJsonResult<MoiraiTenantVO> queryTenantByTaxCode(@RequestBody MoiraiOrg moiraiOrg) {
        if (moiraiOrg == null || StringUtils.isBlank(moiraiOrg.getTaxCode())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiTenantVO moiraiTenantVO = moiraiTenantService.queryTenantByTaxCode(moiraiOrg);
        return new BWJsonResult<>(moiraiTenantVO);
    }

    /**
     * <B>方法名称：</B>租户相关信息删除接口<BR>
     * <B>概要说明：</B>移动开票删除租户接口<BR>
     *
     * @return
     * @since 2019年7月17日
     */
    @Override
    public BWJsonResult deleteTenant(@RequestBody MoiraiTenant moiraiTenant) {
        if (moiraiTenant == null || moiraiTenant.getTenantId() == null) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        int i = moiraiTenantService.deleteTenant(moiraiTenant.getTenantId());
        BWJsonResult bwJsonResult = new BWJsonResult();
        if (i > 0) {
            bwJsonResult.setMessage("租户删除成功");
        } else {
            bwJsonResult.setMessage("租户不存在或不满足删除条件");
        }
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>顶级机构开通产品<BR>
     * <B>概要说明：</B>增加给管理员绑定预制角色<BR>
     *
     * @return
     * @since 2020年8月20日
     */
    @PostMapping("/openTenantProduct")
    public BWJsonResult openTenantProduct(@RequestBody MoiraiOrg moiraiOrg) {
        Long orgId = moiraiOrg.getOrgId();
        if (orgId == null || moiraiOrg.getProducts() == null || moiraiOrg.getProducts().size() == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiTenantService.openTenantProduct(moiraiOrg);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("开通产品成功");
        return bwJsonResult;
    }

    /**
     * 云抬头查询接口 不支持本地部署版
     *
     * @param data
     * @return
     */
    @Override
    public BWJsonResult companySearch(@RequestBody JSONObject data) {
        logger.info("查询云抬头 入参: {}", data.toJSONString());
        if (!data.containsKey("companyName") && !data.containsKey("taxId")) {
            throw new MoiraiException("", "公司名称或者税号不能为空！");
        }
        BWJsonResult result = new BWJsonResult();
        try {
            data.put("appId", companySearchAppId);
            String object = HttpClientUtils.sendPostJsonRequest(companySearchUrl, data.toJSONString());
            JSONObject jsonObject = JSONObject.parseObject(object);
            logger.info("object: {}", JSONObject.toJSONString(jsonObject));
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            result = new BWJsonResult(jsonArray);
        } catch (Exception e) {
            logger.error("云抬头查询失败！ url:{}, appId: {}", companySearchUrl, companySearchAppId, e);
            result = BWJsonResult.error("100", "云抬头查询失败！");
        }
        return result;
    }

    /**
     * 获取当前登录租户信息
     * @return
     */
    @GetMapping("/getCurrentLoginTenantInfo")
    public BWJsonResult<MoiraiTenantVO> getCurrentLoginTenantInfo(){
        return new BWJsonResult<>(moiraiTenantService.getCurrentLoginTenantInfo());
    }

    /**
     * 更新租户资质文件
     * @param tenant
     * @return
     */
    @PostMapping("/updateQualiFilepath")
    public BWJsonResult updateQualiFilepath(@RequestBody MoiraiTenant tenant){
        if (StringUtils.isBlank(tenant.getQualiFilepath())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiTenantService.updateQualiFilepath(tenant);
        return BWJsonResult.success();
    }
}
