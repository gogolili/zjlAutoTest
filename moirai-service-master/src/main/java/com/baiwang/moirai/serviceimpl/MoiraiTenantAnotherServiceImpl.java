package com.baiwang.moirai.serviceimpl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.cloud.sdk.SendEventUtils;
import com.baiwang.cloud.sdk.model.EventModle;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.dao.MoiraiOrgDao;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.event.OrgEvent;
import com.baiwang.moirai.event.UserEvent;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.ManagementServiceClient;
import com.baiwang.moirai.mapper.MoiraiChannelTenantMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.moirai.mapper.MoiraiProductMapper;
import com.baiwang.moirai.mapper.MoiraiRoleMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.SysProvCityDistMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.model.sys.SysFileResponse;
import com.baiwang.moirai.model.sys.SysProvCityDistTree;
import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiExtService;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiTenantAnotherService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.ImportExcel;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.baiwang.moirai.utils.WriteExcel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MoiraiTenantAnotherServiceImpl implements MoiraiTenantAnotherService {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiTenantAnotherServiceImpl.class);

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    @Autowired
    private MoiraiProductMapper moiraiProductMapper;

    @Autowired
    private SysProvCityDistMapper sysProvCityDistMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiChannelTenantMapper moiraiChannelTenantMapper;

    @Autowired
    private MoiraiRoleMapper moiraiRoleMapper;

    @Autowired
    private MoiraiExtService moiraiExtService;

    @Autowired
    private MoiraiOrgDao moiraiOrgDao;

    @Autowired
    private SendEventUtils sendEventUtils;

    @Value("${use.method}")
    private boolean useFlag;

    @Value("${org.default.products}")
    private String ORG_DEFAULT_PRODUCTS;

    @Value("${tenant.role}")
    private String tenantRole;

    @Value("${bi.open.condition}")
    private String biOpenConditon;

    @Resource
    private ApplicationEventPublisher context;

    @Autowired
    private ManagementServiceClient managementServiceClient;

    @Autowired(required = false)
    private PasswordService passwordService;

    public List<MoiraiTenant> batchQueryTenant(List<String> tenantIds) {
        List<MoiraiTenant> batchQueryByIds = moiraiTenantMapper.batchQueryByIds(tenantIds);
        return batchQueryByIds;
    }

    // 机构Excel导入
    public BWJsonResult readExcelRel(MultipartFile excelFile, Long tenantId, Long orgId, String userId,
        String creater) {
        InputStream inputStream = null;
        List<Map<String, String>> list = null;
        try {
            ImportParams params = new ImportParams();
            //设置excel文件中数据开始的位置
            params.setHeadRows(1);
            params.setTitleRows(1);
            inputStream = excelFile.getInputStream();
            list = ExcelImportUtil.importExcel(inputStream, Map.class, params);
        } catch (Exception e) {
            logger.error("机构批量导入发生异常 ：【{}】", e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e1) {
                logger.error("关闭流异常" + e1.getMessage(), e1);
            }
        }
        if (list == null || list.size() <= 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_EXCELFILE_ERROR);
        }
        if (list.get(0).size() < 24) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_READ_EXCEL_FAIL);
        }
        Map<String, Map<String, String>> dicMap = moiraiExtService.calExtMap(String.valueOf(tenantId), String.valueOf(orgId), userId, Constants.MOIRAI_ORG_PAGE_UNIQUE_NAME, Constants.MOIRAI_ORG_IMPORT_TAG);
        Map<String, String> extMap = dicMap.get("extMap");
        Map<String, Object> excelMap = new HashMap<>();
        List<ArrayList<String>> sheetList = new ArrayList<>();
        List<Map<String, Object>> extList = new ArrayList<>();
        excelMap.put("sheetList", sheetList);
        excelMap.put("extList", extList);
        for (Map<String, String> map : list) {
            Map<String, Object> ext = new HashMap<>();
            ArrayList<String> rowList = new ArrayList<>();
            for (String cnName : map.keySet()) {//excel列名
                //实体类属性
                String extFiled = extMap.get(cnName);
                Object value = map.get(cnName);
                if (extFiled != null) {
                    String extSelectLists = extMap.get(cnName + "SelectList");
                    if (StringUtils.isBlank(extSelectLists)) {
                        ext.put(extFiled, value);
                        continue;
                    }
                    if (value == null) {
                        ext.put(extFiled, value);
                        continue;
                    }
                    JSONArray extSelectList = JSON.parseArray(extSelectLists);
                    for (int i = 0; i < extSelectList.size(); i++) {
                        JSONObject item = extSelectList.getJSONObject(i);
                        if (value.equals(item.getString("dictCode"))
                            || value.equals(item.getString("dictName"))) {
                            ext.put(extFiled, item.getString("dictCode"));
                        }
                    }
                    continue;
                }
                if (value != null) {
                    if (value instanceof Number) {
                        DecimalFormat format = new DecimalFormat("0");
                        value = format.format(value);
                    }
                    rowList.add(value.toString());
                } else {
                    rowList.add(null);
                }
            }
            sheetList.add(rowList);
            extList.add(ext);
        }
        return this.checkAndInsertOrgList(excelMap, tenantId, orgId, creater, userId);
    }

    /**
     * 1、读取Excel放入List<List<></>></> 2、校验参数合规 3、拼接完整参数 4、出入数据库信息 5、调用工单
     *
     * @param tenantId
     * @param orgId
     * @param creater
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult checkAndInsertOrgList(Map<String, Object> excelMap, Long tenantId, Long orgId,
        String creater, String userId) {
        StringBuffer str = new StringBuffer();//拼接错误信息
        Map<String, Object> map = this.checkOrgList(excelMap, str, creater, orgId, tenantId, userId);
        if (StringUtils.isNotEmpty(str.toString())) {
            if (useFlag) {
                return moiraiSysService.uploadErrorFile(str, Constants.ORGEXCEL_FILENAME);
            } else {
                return moiraiSysService.uploadString(str.toString(), Constants.MOIRAI_ORG_ERRORFILE, orgId);
            }
        } else {
            List<MoiraiOrg> moiraiOrgList = (List<MoiraiOrg>) map.get("org");
            List<String> taxCodeList = (List<String>) map.get("taxCode");
            Map<String, Long> parentOrgCode = (Map<String, Long>) map.get("parentOrgCode");
            this.excecutorImportOrg(moiraiOrgList, parentOrgCode, orgId, (Long) map.get("qdbm"));

            moiraiSysService.excecutorImport(null, taxCodeList, moiraiOrgList);
            //同步cp机构和用户
            logger.info("开始同步cp，TenantId=[{}]", tenantId);
            context.publishEvent(new OrgEvent(tenantId));
            context.publishEvent(new UserEvent(tenantId));
            logger.info("机构Excel导入成功");
        }
        return new BWJsonResult("Excel导入成功");
    }

    /**
     * <B>方法名称：</B>校验excel文件拼装数据<BR>
     * <B>概要说明：</B>查询所有需要数据，遍历Excel校验<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    private Map<String, Object> checkOrgList(Map<String, Object> excelMap, StringBuffer str, String creater,
        Long parentOrgId, Long tenantId, String userId) {
        //查询租户
        MoiraiTenant tenantInfo = moiraiTenantMapper.selectByPrimaryKey(tenantId);
        if (null == tenantInfo) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_IS_NULL);
        }
        //查询父级机构
        MoiraiOrgCondition condition = new MoiraiOrgCondition();
        condition.setOrgId(parentOrgId);
        BWJsonResult<MoiraiOrg> orgByCondition = moiraiOrgService.getOrgByCondition(condition);
        MoiraiOrg parentOrg = orgByCondition.getData().get(0);
        if (null == parentOrg) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_PARENTNOTEXT);
        }
        //纳税省份
        List<String> PROV = moiraiSysService.getDictCode("PROV");
        //所属行业
        List<String> SSHY = moiraiSysService.getDictCode("SSHY");
        //登记类型
        List<String> DJLX = moiraiSysService.getDictCode("DJLX");
        //省市区
        List<SysProvCityDistTree> provCityDistTreeList = sysProvCityDistMapper.selectAll();
        Map<String, Integer> addMap = new HashMap();
        for (SysProvCityDistTree sysProvCity : provCityDistTreeList) {
            //省
            if ("0".equals(sysProvCity.getType())) {
                addMap.put(sysProvCity.getProvince(), sysProvCity.getId());
            }
            //市
            if ("1".equals(sysProvCity.getType())) {
                addMap.put(sysProvCity.getCity() + sysProvCity.getProvinceId(), sysProvCity.getId());
            }
            //县 区
            if ("2".equals(sysProvCity.getType())) {
                addMap.put(sysProvCity.getDistrict() + sysProvCity.getCityId(), sysProvCity.getId());
            }
        }

        List<String> taxCodeList = new ArrayList<>();//判断Excel里税号是否重复
        List<String> orgCodeList = new ArrayList<>();//判断Excel里机构代码是否重复
        List<MoiraiOrg> orgLists = new ArrayList<>();//遍历获取的所有机构
        Map<String, Long> orgCodeMap = new HashMap<>();//存储orgCode和orgId
        orgCodeMap.put(parentOrg.getOrgCode(), parentOrgId);
        this.getAuthOrgCode(parentOrgId, userId, orgCodeMap);


        List<ArrayList<String>> sheetList = (List<ArrayList<String>>) excelMap.get("sheetList");
        List<Map<String, Object>> extList = (List<Map<String, Object>>) excelMap.get("extList");
        List<ArrayList<String>> checkOldList = new ArrayList<>();
        List<Long> orgIds = moiraiSysService.getNums(sheetList, Constants.MOIRAI_ORG);
        for (int i = 0; i < sheetList.size(); i++) {
            ArrayList<String> filedList = sheetList.get(i);
            MoiraiOrg moiraiOrg = new MoiraiOrg();
            Boolean[] taxCodeFlag = new Boolean[] {false};//false:非纳税主体
            //excel行数
            int rowNum = i + 3;
            //第一列:机构类型 ---1纳税主体、2非纳税主体
            String orgType = filedList.get(0);
            this.checkOrgType(str, parentOrg, moiraiOrg, taxCodeFlag, rowNum, orgType);
            boolean taxFlag = taxCodeFlag[0];

            /* 第二列：机构名称 */
            String orgName = filedList.get(1);
            this.checkOrgName(str, null, moiraiOrg, rowNum, orgName);

            /* 第三列：所属行业 */
            String belong_industry = filedList.get(2);
            this.checkBelongIndustry(str, moiraiOrg, rowNum, belong_industry, SSHY, taxFlag);

            /* 第四列：纳税人识别号 */
            String taxCode = filedList.get(3);
            this.checkTaxCode(str, taxCodeList, moiraiOrg, rowNum, taxCode, taxFlag);

            /* 第五列：纳税省份 */
            String taxProv = filedList.get(4);
            this.checkTaxProv(str, moiraiOrg, taxFlag, rowNum, taxProv, PROV);

            /* 第六️列：机构代码 */
            String orgCode = filedList.get(5);
            this.checkOrgCode(str, orgCodeList, null, moiraiOrg, rowNum, orgCode);

            /* 第7列：开户银行 */
            String bankDeposit = filedList.get(6);
            this.checkBankDeposit(str, moiraiOrg, rowNum, bankDeposit);

            /* 第8列：开户账号^[0-9][\\d-]*[0-9]$ */
            String accountNumber = filedList.get(7);
            this.checkAccountNumber(str, moiraiOrg, rowNum, bankDeposit, accountNumber);

            /* 第12列：使用本机构销方信息 */
            String useSelf = filedList.get(11);
            if (StringUtils.isNotEmpty(useSelf) && "是".equals(useSelf)) {
                // 非纳税主体使用自己的销方信息
                moiraiOrg.setUseSelfinfo("1");
            } else {
                moiraiOrg.setUseSelfinfo("0");
            }

            /* 第9列：联系电话 */
            String telphone = filedList.get(8);
            this.checkTelephone(str, null, moiraiOrg, taxFlag, rowNum, telphone);

            /* 第10列：营业地址 */
            String businessAddress = filedList.get(9);
            this.checkBusinessAddress(str, moiraiOrg, taxFlag, rowNum, businessAddress);

            /* 第11列：纳税资质 */
            String nszz = filedList.get(10);
            this.checkTaxQuali(str, moiraiOrg, rowNum, nszz, taxFlag);

            /* 第13列：开通服务 */ //产品以逗号（中文）分割，若不填写默认继承上级开通的产品
            String cps = filedList.get(12);
            boolean isJX = false;
            this.checkProducts(str, parentOrg, moiraiOrg, rowNum, cps, tenantInfo.getQdBm());

            /* 第14列：下横排号 */
            String crossNo = filedList.get(13);
            this.checkCrossNo(str, moiraiOrg, rowNum, crossNo);

            /* 第15列：出口资质 */
            String exportQualify = filedList.get(14);
            this.checkExportQualify(str, moiraiOrg, rowNum, isJX, exportQualify);

            /* 第16列：登记类型 */
            String djlx = filedList.get(15);
            this.checkDjType(str, moiraiOrg, rowNum, djlx, DJLX, taxFlag);

            /*第17列：父级机构代码*/
            String parentOrgCode = filedList.get(16);
            this.checkParentOrgCode(str, parentOrg, moiraiOrg, rowNum, orgCode, parentOrgCode);

            /*第18列：法定代表人姓名*/
            String legalName = filedList.get(17);
            this.checkLegalName(str, moiraiOrg, rowNum, legalName);
            /*第19列：注册地址*/
            String registerAddress = filedList.get(18);
            this.checkRegisterAddress(str, moiraiOrg, rowNum, registerAddress);

            /*第20列：所属地区/省*/
            /*第21列：所属地区/市*/
            /*第22列：所属地区/区*/
            String regProv = filedList.get(19);
            String regCity = filedList.get(20);
            String regArea = filedList.get(21);
            this.checkReg(str, moiraiOrg, rowNum, regProv, regCity, regArea, addMap);

            /*第23列：是否创建机构管理员*/
            String hasDefAdminUser = filedList.get(22);
            if (StrUtils.isEmpty(hasDefAdminUser)) {
                str.append("表格第" + rowNum + "行管理员创建与否不能为空;\r\n");
            } else {
                if ("是".equals(hasDefAdminUser)) {
                    moiraiOrg.setHasDefAdminUser(Constants.DEFAULT_ONE);
                } else {
                    moiraiOrg.setHasDefAdminUser(Constants.DEFAULT_ZERO);
                }
            }

            /*第24列：机构管理员邮箱*/
            String adminUserEmail = filedList.get(23);
            if (Constants.DEFAULT_ONE.equals(moiraiOrg.getHasDefAdminUser())) {
                if (passwordService.calculatePasswordMark() && StringUtils.isBlank(adminUserEmail)) {
                    str.append("表格第" + rowNum + "行管理员邮箱不能为空;\r\n");
                }
                this.checkEmail(adminUserEmail, str, rowNum);
                moiraiOrg.setAdminUserEmail(adminUserEmail);
            }

            Long time = DateTimeUtils.nowTimeLong();
            moiraiOrg.setCreater(creater);
            moiraiOrg.setTenantId(tenantId);
            moiraiOrg.setJrdCode(Constants.JRD_CODE);
            int id = i;
            moiraiOrg.setOrgId(orgIds.get(id));
            moiraiOrg.setCreateTime(time);
            moiraiOrg.setModifyUser(creater);
            moiraiOrg.setModifyTime(time);
            moiraiOrg.setSelfManage("0");
            moiraiOrg.setIsFetch(parentOrg.getIsFetch());
            moiraiOrg.setIsProof(parentOrg.getIsProof());
            moiraiOrg.setIsAuthe(parentOrg.getIsAuthe());
            moiraiOrg.setExt(extList.get(i));
            orgCodeMap.put(orgCode, orgIds.get(id));
            orgLists.add(moiraiOrg);
            checkOldList.add(filedList);
        }
        this.checkExcelParam(str, orgLists, Constants.DEFAULT_ZERO);
        moiraiSysService.excecutorOldDB(checkOldList, str, 3);
        Map<String, Object> map = new HashMap<>();
        map.put("taxCode", taxCodeList);
        map.put("org", orgLists);
        map.put("parentOrgCode", orgCodeMap);
        map.put("qdbm", tenantInfo.getQdBm());
        return map;
    }

    private void getAuthOrgCode(Long parentOrgId, String userId, Map<String, Long> orgCodeMap) {
        List<MoiraiOrg> children = moiraiOrgService.getOrgChildren(parentOrgId);
        List<MoiraiOrg> allOrg = new ArrayList<>();
        while (!children.isEmpty()) {
            allOrg.addAll(children);
            List<Long> parentIdList = new ArrayList<>();
            for (MoiraiOrg org : children) {
                parentIdList.add(org.getOrgId());
            }
            Map<String, List> moiraiOrgList = new HashMap<>();
            moiraiOrgList.put("parentOrg", parentIdList);
            children = moiraiOrgMapper.queryOrgListByCondition(moiraiOrgList);
        }

        if (StringUtils.isBlank(userId)) {
            for (MoiraiOrg pOrg : allOrg) {
                orgCodeMap.put(pOrg.getOrgCode(), pOrg.getOrgId());
            }
        } else {
            MoiraiUserCondition condition = new MoiraiUserCondition();
            condition.setUserId(Long.valueOf(userId));
            List<MoiraiUserAuthzOrg> userAuthzOrg = moiraiUserAuthzMapper.getUserAuthzOrg(condition);
            for (MoiraiUserAuthzOrg aOrg : userAuthzOrg) {
                for (MoiraiOrg pOrg : allOrg) {
                    if (aOrg.getAuthOrg().equals(pOrg.getOrgId())) {
                        orgCodeMap.put(aOrg.getOrgCode(), aOrg.getAuthOrg());
                    }
                }
            }
        }
    }

    /**
     * <B>方法名称：</B>租户excel导入<BR>
     * <B>概要说明：</B>校验全部Excel内容，并把错误信息上传至oss，待用户下载，否则导入成功<BR>
     *
     * @return
     * @since 2019年5月29日
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult readExcelRel(MultipartFile excelFile, String creater, List<Long> qdBmList,
        List<Long> prIdList) {
        //读取Excel
        List<ArrayList<String>> orgList = null;
        try {
            orgList = new ImportExcel().read(excelFile);
        } catch (InvalidFormatException e) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_READ_EXCEL_FAIL);
        } catch (IOException e) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_NOTEXCELFILE_ERROR);
        }
        if (orgList == null || orgList.size() <= 1 || orgList.get(0).size() < 18) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_READ_EXCEL_FAIL);
        }
        if (orgList.size() > Constants.ORGEXCEL_MAXCOUNT) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_SYS_FILE_UPLOAD_FAIL);
        }

        StringBuffer str = new StringBuffer();//拼接错误信息
        List<Long> tenantIds = moiraiSysService.getNums(orgList, Constants.MOIRAI_TENANT);
        Map<String, Object> map = this.checkTenantList(orgList, str, creater, qdBmList, tenantIds);
        if (StringUtils.isNotEmpty(str.toString())) {
            if (useFlag) {
                BWJsonResult<SysFileResponse> result = moiraiSysService.uploadErrorFile(str, Constants.ORGEXCEL_FILENAME);
                String errorExcel = (String) map.get("errorExcel");
                if (errorExcel != null) {
                    String url = result.getData().get(0).getUrl();
                    result.getData().get(0).setUrl(url + "&" + errorExcel);
                }
                return result;
            } else {
                return moiraiSysService.uploadString(str.toString(), Constants.MOIRAI_ORG_ERRORFILE, DateTimeUtils.nowTimeLong());
            }
        } else {
            List<MoiraiOrg> moiraiOrgList = (List<MoiraiOrg>) map.get("org");
            List<String> taxCodeList = (List<String>) map.get("taxCode");
            List<MoiraiTenant> moiraiTenantList = (List<MoiraiTenant>) map.get("tenant");
            this.excecutorImportOrgAndTenant(moiraiOrgList, moiraiTenantList, qdBmList.get(0), prIdList);

            //异步所有
            moiraiSysService.excecutorImport(tenantIds, taxCodeList, moiraiOrgList);
            List<EventModle> eventDBModles = new ArrayList<>();
            for (MoiraiOrg org : moiraiOrgList) {
                if (StringUtils.isNotBlank(org.getTaxCode())) {
                    EventModle eventDBModle = new EventModle();
                    eventDBModle.setEventId(org.getTaxCode());
                    eventDBModle.setEventType("USER_REGIST");//常量值
                    JSONObject data = new JSONObject();
                    data.put("taxNo", org.getTaxCode());
                    eventDBModle.setEventData(data.toJSONString());
                    eventDBModles.add(eventDBModle);
                }
            }
            if (!eventDBModles.isEmpty()) {
                sendEventUtils.sendEvent(eventDBModles);
            }
        }
        return new BWJsonResult("Excel导入成功");
    }

    private void excecutorImportOrgAndTenant(List<MoiraiOrg> moiraiOrgList, List<MoiraiTenant> moiraiTenantList,
        Long qdBm, List<Long> prIdList) {
        List<MoiraiUser> userList = new ArrayList<>();
        List<MoiraiUserAuthz> roleList = new ArrayList<>();
        List<MoiraiOrgProduct> products = new ArrayList<>();
        List<LazyDynaBean> lazyDynaBeans = this.completeInfomation(moiraiOrgList, null, userList, roleList, products, qdBm, prIdList);
        this.addOrgList(moiraiOrgList, userList, roleList, products);
        this.addTenantList(moiraiTenantList);
        moiraiSysService.excecutorSendEmail(lazyDynaBeans, Constants.DEFAULT_ZERO);
    }

    private void addTenantList(List<MoiraiTenant> moiraiTenantList) {
        int i = moiraiTenantMapper.addTenantList(moiraiTenantList);
        for (int j = 0; j < moiraiTenantList.size(); j++) {
            moiraiChannelTenantMapper.insertList(moiraiTenantList.get(j).getQdBmList());
        }
    }

    /**
     * <B>方法名称：</B>添加机构<BR>
     * <B>概要说明：</B>批量插入机构、用户、机构产品、授权信息<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    public void addOrgList(List<MoiraiOrg> moiraiOrgList, List<MoiraiUser> userList, List<MoiraiUserAuthz> roleList,
        List<MoiraiOrgProduct> products) {
        try {
            //机构
            int count = moiraiOrgDao.addOrgList(moiraiOrgList);
            if (!userList.isEmpty()) {
                //用户
                int addBatch = moiraiUserMapper.addBatch(userList);
                //授权
                int batchInsert = moiraiUserAuthzMapper.batchInsert(roleList);
            }
            //产品
            int pro = moiraiOrgProductMapper.addOrgProductList(products);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.Moirai_DB_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }

    }

    private Map<String, Object> checkTenantList(List<ArrayList<String>> orgList, StringBuffer str, String creater,
        List<Long> qdBmList, List<Long> tenantIds) {
        //纳税省份
        List<String> PROV = moiraiSysService.getDictCode("PROV");
        //所属行业
        List<String> SSHY = moiraiSysService.getDictCode("SSHY");
        //登记类型
        List<String> DJLX = moiraiSysService.getDictCode("DJLX");
        List<String> taxCodeList = new ArrayList<>();//判断Excel里税号是否重复
        List<String> orgCodeList = new ArrayList<>();//判断Excel里机构代码是否重复
        List<MoiraiOrg> orgLists = new ArrayList<>();//遍历获取的所有机构
        List<MoiraiTenant> tenantList = new ArrayList<>();//遍历获取的所有租户
        moiraiSysService.excecutorOldDB(orgList, str, 3);
        logger.info("共校验:{}条数据", orgList.size());
        List<Long> orgIds = moiraiSysService.getNums(orgList, Constants.MOIRAI_ORG);
        int orgListSize = orgList.size();
        int rowNum;
        int id;
        Long time;
        Long tenantId;
        MoiraiTenant moiraiTenant;
        MoiraiOrg moiraiOrg;
        ArrayList<String> org;
        for (int i = 1; i < orgListSize; i++) {
            moiraiTenant = new MoiraiTenant();
            moiraiOrg = new MoiraiOrg();
            org = orgList.get(i);
            //excel行数
            rowNum = i + 2;
            /* 第一列：纳税人识别号 taxCode */
            this.checkTaxCode(str, taxCodeList, moiraiOrg, rowNum, org.get(0), true);
            /* 第二列：机构名称 orgName */
            this.checkOrgName(str, moiraiTenant, moiraiOrg, rowNum, org.get(1));
            /* 第三列：开户银行 bankDeposit */
            this.checkBankDeposit(str, moiraiOrg, rowNum, org.get(2));
            /* 第四列：开户账号^[0-9][\\d-]*[0-9]$ accountNumber */
            this.checkAccountNumber(str, moiraiOrg, rowNum, org.get(2), org.get(3));
            /* 第四五列：联系邮箱 tenantEmail */
            this.checkTenantEmail(str, moiraiTenant, moiraiOrg, rowNum, org.get(4));
            /* 第六列：联系电话 telphone */
            this.checkTelephone(str, moiraiTenant, moiraiOrg, true, rowNum, org.get(5));
            /* 第七列：纳税省份 taxProv */
            this.checkTaxProv(str, moiraiOrg, true, rowNum, org.get(6), PROV);
            /* 第八列：营业地址 businessAddress */
            this.checkBusinessAddress(str, moiraiOrg, true, rowNum, org.get(7));
            /* 第九列：注册地址 registerAddress */
            this.checkRegisterAddress(str, moiraiOrg, rowNum, org.get(8));
            /* 第十列：设备编号 deviceType */
//            this.checkDeviceType(str, moiraiOrg, rowNum, org.get(9));
            /* 第十一列：托管方式 tgfs */
//            this.checkTgType(str, moiraiOrg, rowNum, org.get(10));
            /* 第十列：所属行业 belong_industry */
            this.checkBelongIndustry(str, moiraiOrg, rowNum, org.get(9), SSHY, true);
            /* 第十一列：法定代表人姓名 legalName */
            this.checkLegalName(str, moiraiOrg, rowNum, org.get(10));
            /* 第十二列：登记类型 djlx */
            this.checkDjType(str, moiraiOrg, rowNum, org.get(11), DJLX, true);
            /* 第十三列：纳税资质 nszz */
            this.checkTaxQuali(str, moiraiOrg, rowNum, org.get(12), true);
            /* 第十四列：是否开通电票 openTicket */
            this.checkOpenTicket(str, moiraiOrg, rowNum, org.get(13));
            /* 第十五️列：机构代码 orgCode */
            this.checkOrgCode(str, orgCodeList, moiraiTenant, moiraiOrg, rowNum, org.get(14));
            /* 第十六列：开通服务 cps */
            this.checkProducts(str, null, moiraiOrg, rowNum, org.get(15), qdBmList.get(0));
            /* 第十七列：下横排号 crossNo */
            this.checkCrossNo(str, moiraiOrg, rowNum, org.get(16));
            /* 第十八列：合同编号 contractNum */
            this.checkContractNum(moiraiOrg, org.get(17));

            time = DateTimeUtils.nowTimeLong();
            id = i - 1;
            tenantId = tenantIds.get(id);

            moiraiOrg.setOrgType(1);
            moiraiOrg.setTenantId(tenantId);
            moiraiOrg.setOrgId(orgIds.get(id));
            moiraiOrg.setCreater(creater);
            moiraiOrg.setJrdCode(Constants.JRD_CODE);
            moiraiOrg.setCreateTime(time);
            moiraiOrg.setModifyUser(creater);
            moiraiOrg.setModifyTime(time);
            moiraiOrg.setSelfManage("0");
            moiraiOrg.setParentOrg(0L);
            moiraiOrg.setIsAuthe("0");
            moiraiOrg.setIsFetch("0");
            moiraiOrg.setIsProof("0");
            orgLists.add(moiraiOrg);

            moiraiTenant.setTenantId(tenantId);
            moiraiTenant.setCreater(creater);
            moiraiTenant.setCreateTime(time);
            moiraiTenant.setModifyUser(creater);
            moiraiTenant.setModifyTime(time);
            moiraiTenant.setCheckTime(time);
            moiraiTenant.setRegisterTime(time);
            moiraiTenant.setTenantType("1");
            moiraiTenant.setOriginMark("3");
            moiraiTenant.setDjMark("1");
            moiraiTenant.setQdBm(qdBmList.get(0));
            moiraiTenant.setQdBmList(new ArrayList<>());
            List<Long> channelTenantIds = moiraiSysService.getNums(qdBmList, Constants.MOIRAI_CHANNEL_TENANT);
            for (int j = 0; j < qdBmList.size(); j++) {
                MoiraiChannelTenant channelTenant = new MoiraiChannelTenant();
                channelTenant.setChannelTenantId(channelTenantIds.get(j));
                channelTenant.setQdBm(qdBmList.get(j));
                channelTenant.setTenantId(moiraiTenant.getTenantId());
                moiraiTenant.getQdBmList().add(channelTenant);
            }
            tenantList.add(moiraiTenant);
        }
        List<MoiraiOrg> tenantOrgList = this.checkExcelParam(str, orgLists, Constants.DEFAULT_ONE);

        Map<String, Object> map = new HashMap<>();
        map.put("taxCode", taxCodeList);
        map.put("org", orgLists);
        map.put("tenant", tenantList);
        if (!tenantOrgList.isEmpty() && useFlag) {
            String errorExcelUrl = createErrorExcel(tenantOrgList);
            map.put("errorExcel", errorExcelUrl);
        }
        return map;
    }

    private String createErrorExcel(List<MoiraiOrg> orgList) {
        WriteExcel writer = new WriteExcel();
        Sheet sheet1 = writer.createSheet("已注册租户列表");
        writer.createRow(sheet1, new String[] {"租户名称", "租户税号", "管理员账号", "服务单位", "注册时间", "租户邮箱"}, true, true);
        List<Long> tenantIdList = new ArrayList<>();
        List<Long> orgIdList = new ArrayList<>();
        for (MoiraiOrg org : orgList) {
            tenantIdList.add(org.getTenantId());
            orgIdList.add(org.getOrgId());
        }
        Map<Long, MoiraiUser> userMap = moiraiUserMapper.selectAdminUser(orgIdList);
        Map<Long, MoiraiTenant> tenantMap = moiraiTenantMapper.selectEmail(tenantIdList);
        List<MoiraiChannelTenant> channelTenants = moiraiChannelTenantMapper.selectListByTenantIdList(tenantIdList);
        Set<Long> channelList = new HashSet<>();
        channelTenants.forEach(channel -> channelList.add(channel.getQdBm()));
        Map<Long, List<String>> channelMap = new HashMap<>();
        if (!channelList.isEmpty()) {
            BWJsonResult<Map<String, String>> result = managementServiceClient.batchWorkorder(new ArrayList<>(channelList));
            if (result.isSuccess()) {
                List<Map<String, String>> data = result.getData();
                if (data != null) {
                    for (MoiraiChannelTenant channel : channelTenants) {
                        for (Map<String, String> list : data) {
                            if (channel.getQdBm().toString().equals(list.get("qdBm"))) {
                                List<String> stringList = channelMap.get(channel.getTenantId());
                                if (stringList == null) {
                                    stringList = new ArrayList<>();
                                    channelMap.put(channel.getTenantId(), stringList);
                                }
                                stringList.add(list.get("qdName"));
                            }
                        }
                    }
                }
            }
        }
        for (MoiraiOrg org : orgList) {
            MoiraiUser moiraiUser = userMap.get(org.getOrgId());
            List<String> nameList = channelMap.get(org.getTenantId());
            MoiraiTenant moiraiTenant = tenantMap.get(org.getTenantId());
            writer.createRow(sheet1, new String[] {
                    org.getOrgName(), org.getTaxCode(), moiraiUser != null ? moiraiUser.getUserAccount() : "",
                    nameList != null ? nameList.toString() : "", org.getCreateTime().toString(), moiraiTenant != null ? moiraiTenant.getTenantEmail() : ""},
                false, true);
        }
        for (int i = 0; i <= 5; i++) {
            //设置列宽
            sheet1.setColumnWidth(i, 5304);
        }
        String path = Constants.ERRORFILE_LOCATION;
        // 临时文件
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        OutputStream out = null;
        InputStream in = null;
        try {
            out = new FileOutputStream(path + "errorExcelInfo.xlsx");
            writer.getWorkbook().write(out);
            in = new FileInputStream(path + "errorExcelInfo.xlsx");
            BWJsonResult<SysFileResponse> bwJsonResult = moiraiSysService.uploadErrorFile(in, "errorExcelInfo.xlsx");
            List<SysFileResponse> data = bwJsonResult.getData();
            if (data != null) {
                return data.get(0).getUrl();
            }
        } catch (IOException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            } catch (IOException e) {
                logger.error("" + MoiraiErrorEnum.MOIRAI_IO_EXCEPTION, e);
            }
        }
        return null;
    }

    private void checkOpenTicket(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String openTicket) {
        if (StringUtils.isNotEmpty(openTicket)) {
            if ("Y".equals(openTicket) || "N".equals(openTicket)) {
                moiraiOrg.setOpenTicket(openTicket);
            } else {
                str.append("表格第" + rowNum + "行是否开通电票内容不正确，请重新填写;\r\n");
            }
        } else {
            str.append("表格第" + rowNum + "行否开通电票为空，请重新填写;\r\n");
        }
    }

    public void checkRegisterAddress(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String registerAddress) {
        if (StringUtils.isNotEmpty(registerAddress)) {
            if (RegularExpUtils.validName(registerAddress) || registerAddress.indexOf(" ") != -1) {
                str.append("表格第" + rowNum + "行注册地址不能包含特殊字符及空格，请重新填写;\r\n");
            }
            if (registerAddress.length() > 80) {
                str.append("表格第" + rowNum + "行注册地址最大长度80，请重新填写;\r\n");
            }
            moiraiOrg.setRegisterAddress(registerAddress);
        }
    }

    private void checkReg(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String regProv, String regCity,
        String regArea, Map<String, Integer> cityMap) {
        if (StringUtils.isBlank(regProv) && StringUtils.isBlank(regCity) && StringUtils.isBlank(regArea)) {
            return;
        }
        if (StringUtils.isNotBlank(regProv) && StringUtils.isNotBlank(regCity) && StringUtils.isNotBlank(regArea)) {
            Integer provinceId = cityMap.get(regProv);
            if (regCity.startsWith("市辖区")) {
                regCity = "市辖区";
            } else if (regCity.startsWith("县")) {
                regCity = "县";
            }
            Integer cityId = cityMap.get(regCity + provinceId);
            Integer areaId = cityMap.get(regArea + cityId);
            if (provinceId == null) {
                str.append("表格第" + rowNum + "行所属地区/省不存在，请重新填写;\r\n");
            } else {
                moiraiOrg.setRegProv(Long.valueOf(provinceId));
            }

            if (provinceId != null) {
                if (cityId == null) {
                    str.append("表格第" + rowNum + "行所属地区/市不存在，请重新填写;\r\n");
                } else {
                    moiraiOrg.setRegCity(Long.valueOf(cityId));
                }
            }
            if (provinceId != null && cityId != null) {
                if (areaId == null) {
                    str.append("表格第" + rowNum + "行所属地区/区不存在，请重新填写;\r\n");
                } else {
                    moiraiOrg.setRegArea(Long.valueOf(areaId));
                }
            }
        } else {
            str.append("表格第" + rowNum + "行所属地区/省/市/区必须同时存在，请重新填写;\r\n");
        }
    }

    public void checkLegalName(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String legalName) {
        if (StringUtils.isNotEmpty(legalName)) {
            if (!RegularExpUtils.check(legalName) || legalName.length() > 64) {
                str.append("表格第" + rowNum + "行法定代表人姓名应为汉字或字母，长度小于64，请重新填写;\r\n");
            }
            moiraiOrg.setLegalName(legalName);
        }
    }

    private void checkTenantEmail(StringBuffer str, MoiraiTenant moiraiTenant, MoiraiOrg moiraiOrg, int rowNum,
        String tenantEmail) {
        if (StringUtils.isNotEmpty(tenantEmail)) {
            if (!RegularExpUtils.checkEmail(tenantEmail)) {
                str.append("表格第" + rowNum + "行联系邮箱填写有误;\r\n");
            }
            moiraiTenant.setTenantEmail(tenantEmail);
            moiraiOrg.setAdminUserEmail(tenantEmail);
        } else {
            str.append("表格第" + rowNum + "行联系邮箱为空，请重新填写;\r\n");
        }
    }

    public void checkEmail(String email, StringBuffer buffer, int row) {
        if (!StringUtils.isEmpty(email)) {
            //验证邮箱格式的问题
            boolean valid = RegularExpUtils.checkEmail(email);
            if (!valid) {
                buffer.append("表格第" + row + "行邮箱格式不正确;\r\n");
                return;
            }
        }
    }

    private byte[] getParamBtyes(String str) {
        byte[] bytes = new byte[0];
        try {
            bytes = str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARSET_CONVERT_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

        }
        return bytes;
    }

    public void checkCrossNo(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String crossNo) {
        if (StringUtils.isNotEmpty(crossNo)) {
            if (!RegularExpUtils.isNumeric(crossNo)) {
                str.append("表格第" + rowNum + "行下横排号只能为数字且最大为20位，请重新填写;\r\n");
            } else if (crossNo.length() > 20) {
                str.append("表格第" + rowNum + "行下横排号长度最大20位，请重新填写;\r\n");
            }
            moiraiOrg.setCrossNo(crossNo);
        }
    }

    private void checkContractNum(MoiraiOrg moiraiOrg, String contractNum) {
        if (StringUtils.isNotEmpty(contractNum)) {
            moiraiOrg.setContractNum(contractNum);
        }
    }

    public void checkOrgCode(StringBuffer str, List<String> orgCodeList, MoiraiTenant moiraiTenant,
        MoiraiOrg moiraiOrg, int rowNum, String orgCode) {
        if (StringUtils.isNotEmpty(orgCode)) {
            if (!RegularExpUtils.validOrgCode(orgCode) || orgCode.length() > 36) {
                str.append("表格第" + rowNum + "行机构代码只能是1-36位的字母或数字，请重新填写;\r\n");
            }
            if (null != moiraiTenant) {
                moiraiTenant.setTenantCode(orgCode);
            } else {
                if (orgCodeList.contains(orgCode)) {
                    str.append("表格第" + rowNum + "行机构代码重复，请重新填写;\r\n");
                }
            }
            orgCodeList.add(orgCode);
            moiraiOrg.setOrgCode(orgCode);
        } else {
            if (null != moiraiTenant) {
                moiraiTenant.setTenantCode(DateTimeUtils.nowTimeString() + rowNum);
            }
            moiraiOrg.setOrgCode(DateTimeUtils.nowTimeString() + rowNum);
        }
    }

    public void checkTaxQuali(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String nszz, boolean taxFlag) {
        if (StringUtils.isNotEmpty(nszz)) {
            if ("一般纳税人".equals(nszz)) {
                moiraiOrg.setTaxQuali("1");
            } else if ("小规模纳税人".equals(nszz)) {
                moiraiOrg.setTaxQuali("2");
            } else {
                str.append("表格第" + rowNum + "行纳税资质填写错误，请重新填写;\r\n");
            }
        } else if (taxFlag) {
            str.append("表格第" + rowNum + "行纳税资质不能为空，请重新填写;\r\n");
        } else {
            return;
        }
    }

    public void checkDjType(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String djlx, List<String> DJLX,
        boolean taxFlag) {
        if (StringUtils.isNotEmpty(djlx)) {
            if (DJLX != null && DJLX.size() > 0) {
                if (!DJLX.contains(djlx)) {
                    str.append("表格第" + rowNum + "行登记类型编号填写错误，请重新填写;\r\n");
                }
            } else {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_GET_DICTINFO_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg() + ":登记类型", ErrorType.CustomerError).toString());

            }
            moiraiOrg.setDjCompanyType(djlx);
        } else if (taxFlag) {
            str.append("表格第" + rowNum + "行登记类型不能为空，请重新填写;\r\n");
        } else {
            return;
        }
    }

    public void checkBelongIndustry(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String belong_industry,
        List<String> SSHY, boolean taxFlag) {
        if (StringUtils.isNotEmpty(belong_industry)) {
            if (SSHY != null && SSHY.size() > 0) {
                if (!SSHY.contains(belong_industry)) {
                    str.append("表格第" + rowNum + "行所属行业编号填写错误，请重新填写;\r\n");
                }
            } else {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_GET_DICTINFO_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg() + ":所属行业", ErrorType.CustomerError).toString());

            }
            moiraiOrg.setBelongIndustry(belong_industry);
        } else if (taxFlag) {
            str.append("表格第" + rowNum + "行所属行业不能为空，请重新填写;\r\n");
        } else {
            return;
        }
    }

//    public void checkTgType(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String tgfs) {
//        if (StringUtils.isNotEmpty(tgfs)) {
//            if ("平台托管".equals(tgfs)) {
//                moiraiOrg.setTgType("0");
//            } else if ("企业自建".equals(tgfs)) {
//                moiraiOrg.setTgType("1");
//            } else if ("小智自持".equals(tgfs)) {
//                moiraiOrg.setTgType("2");
//            } else {
//                str.append("表格第" + rowNum + "行托管方式填写错误，请重新填写;\r\n");
//            }
//        }
//    }

//    public void checkDeviceType(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String deviceType) {
//        if ("核心板".equals(deviceType)) {
//            moiraiOrg.setDeviceType("0");
//        } else if ("税控盘".equals(deviceType)) {
//            moiraiOrg.setDeviceType("1");
//        } else if ("虚拟UKey".equals(deviceType)) {
//            moiraiOrg.setDeviceType("4");
//        } else if ("税务UKey".equals(deviceType)) {
//            moiraiOrg.setDeviceType("5");
//        } else if ("简易税控盘".equals(deviceType)) {
//            moiraiOrg.setDeviceType("6");
//        } else if ("金税盘".equals(deviceType)) {
//            moiraiOrg.setDeviceType("7");
//        } else {
//            str.append("表格第" + rowNum + "行设备类型填写错误，请重新填写;\r\n");
//        }
//    }

    public void checkBusinessAddress(StringBuffer str, MoiraiOrg moiraiOrg, boolean taxFlag, int rowNum,
        String businessAddress) {
        if (StringUtils.isNotEmpty(businessAddress)) {
            if (RegularExpUtils.validName(businessAddress) || businessAddress.indexOf(" ") != -1) {
                str.append("表格第" + rowNum + "行营业地址不能包含特殊字符及空格，请重新填写;\r\n");
            }
            if (businessAddress.length() > 80) {
                str.append("表格第" + rowNum + "行营业地址最大长度80，请重新填写;\r\n");
            }
            moiraiOrg.setBusinessAddress(businessAddress);
        } else if (taxFlag || "1".equals(moiraiOrg.getUseSelfinfo())) {
            str.append("表格第" + rowNum + "行营业地址不能为空，请重新填写;\r\n");
        }
    }

    public void checkTaxProv(StringBuffer str, MoiraiOrg moiraiOrg, boolean taxFlag, int rowNum, String taxProv,
        List<String> PROV) {
        if (StringUtils.isNotEmpty(taxProv)) {
            if (PROV != null && PROV.size() > 0) {
                if (!PROV.contains(taxProv)) {
                    str.append("表格第" + rowNum + "行纳税省份填写错误，请重新填写;\r\n");
                }
            } else {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_GET_DICTINFO_ERROR;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg() + ":纳税省份", ErrorType.CustomerError).toString());

            }
            moiraiOrg.setTaxProv(taxProv);
        } else if (taxFlag) {
            str.append("表格第" + rowNum + "行纳税省份不能为空，请重新填写;\r\n");
        }
    }

    public void checkTelephone(StringBuffer str, MoiraiTenant moiraiTenant, MoiraiOrg moiraiOrg, boolean taxFlag,
        int rowNum, String telphone) {
        if (StringUtils.isNotEmpty(telphone)) {
            if (!(RegularExpUtils.checkMobile(telphone) || RegularExpUtils.isFixedPhone(telphone))) {
                str.append("表格第" + rowNum + "行联系电话格式不正确，请重新填写;\r\n");
            }
            moiraiOrg.setTelphone(telphone);
            if (null != moiraiTenant) {
                moiraiTenant.setTenantPhone(telphone);
            }
        } else if (taxFlag || "1".equals(moiraiOrg.getUseSelfinfo())) {// 纳税主体、非纳税主体主体使用本级开票信息
            str.append("表格第" + rowNum + "行联系电话不能为空，请重新填写;\r\n");
        }
    }

    public void checkAccountNumber(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String bankDeposit,
        String accountNumber) {
        if (StringUtils.isNotEmpty(accountNumber) && !"null".equals(accountNumber)) {
            //特殊字符
            boolean validatorReg = RegularExpUtils.validName(accountNumber);
            if (accountNumber.length() > 50 || validatorReg || accountNumber.indexOf(" ") != -1) {
                str.append("表格第" + rowNum + "行开户账号不能包含特殊字符及空格且长度最大位50，请重新填写;\r\n");
            }
            if (StringUtils.isBlank(bankDeposit)) {
                str.append("表格第" + rowNum + "行开户银行不存在，请重新填写;\r\n");
            } else {
                int maxLen = bankDeposit.length() + accountNumber.length();
                if (maxLen > 100) {
                    str.append("表格第" + rowNum + "行开户银行及账号总长度不超过100，请重新填写;\r\n");
                }
            }
            moiraiOrg.setAccountNumber(accountNumber);
        }
    }

    public void checkBankDeposit(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String bankDeposit) {
        if (StringUtils.isNotEmpty(bankDeposit)) {
            //校验行字、字母、数字、（）()
            boolean validatorReg = RegularExpUtils.validName(bankDeposit);
            if (validatorReg || bankDeposit.indexOf(" ") != -1) {
                str.append("表格第" + rowNum + "行开户银行不能包含特殊字符及空格，请重新填写;\r\n");
            }
            if (bankDeposit.length() > 80) {
                str.append("表格第" + rowNum + "行开户银行长度最大为80，请重新填写;\r\n");
            }
            moiraiOrg.setBankDeposit(bankDeposit);
        }
    }

    public void checkOrgName(StringBuffer str, MoiraiTenant moiraiTenant, MoiraiOrg moiraiOrg, int rowNum,
        String orgName) {
        if (StringUtils.isNotEmpty(orgName)) {
            if (RegularExpUtils.validName(orgName) || orgName.indexOf(" ") != -1) {
                str.append("表格第" + rowNum + "行机构名称填写有误，机构名称不能包含特殊字符及空格;\r\n");
            }
            if (orgName.length() > 150) {
                str.append("表格第" + rowNum + "行机构名称填写有误，机构名称长度不能超过150个字符;\r\n");
            }
            moiraiOrg.setOrgName(orgName);
            if (null != moiraiTenant) {
                moiraiTenant.setTenantName(orgName);
            }
        } else {
            str.append("表格第" + rowNum + "行机构名称为空，请重新填写;\r\n");
        }
    }

    public void checkTaxCode(StringBuffer str, List<String> taxCodeList, MoiraiOrg moiraiOrg, int rowNum,
        String taxCode, boolean taxFlag) {
        if (taxFlag && StringUtils.isNotEmpty(taxCode)) {
            taxCode = taxCode.trim();
            if (taxCode.length() < 15 || taxCode.length() > 20) {
                str.append("表格第" + rowNum + "行税号应是15-20位的数字和字母，请重新填写;\r\n");
            }
            if (taxCodeList.contains(taxCode)) {
                str.append("表格第" + rowNum + "行税号重复，请重新填写;\r\n");
            }
            taxCodeList.add(taxCode);
            moiraiOrg.setTaxCode(taxCode);
        } else if (taxFlag) {
            str.append("表格第" + rowNum + "行税号不能为空，请重新填写;\r\n");
        } else {
            moiraiOrg.setTaxCode(null);
        }
    }

    public void checkOrgType(StringBuffer str, MoiraiOrg parentOrg, MoiraiOrg moiraiOrg, Boolean[] taxCodeFlag,
        int rowNum, String orgType) {
        if (StringUtils.isEmpty(orgType)) {
            str.append("表格第" + rowNum + "行机构类型不能为空，请更改后在上传;\r\n");
        } else {
            if ("纳税主体".equals(orgType)) {// 纳税主体
                moiraiOrg.setOrgType(1);
                taxCodeFlag[0] = true;
            } else if ("非纳税主体".equals(orgType)) {// 非纳税主体
                moiraiOrg.setOrgType(2);
                /**继承父类属性*/
                moiraiOrg.setDeviceType(parentOrg.getDeviceType());
                moiraiOrg.setTgType(parentOrg.getTgType());
                moiraiOrg.setTaxQuali(parentOrg.getTaxQuali());
                moiraiOrg.setNeedSeal(parentOrg.getNeedSeal());
                moiraiOrg.setSealId(parentOrg.getSealId());
            } else {
                str.append("表格第" + rowNum + "行机构类型填写错误，请更改后在上传;\r\n");
            }
        }
    }

    public void checkExportQualify(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, boolean isJX,
        String exportQualify) {
        if (StringUtils.isNotEmpty(exportQualify)) {
            if ("非外贸企业".equals(exportQualify)) {
                moiraiOrg.setExportQualify("1");
            } else if ("外贸企业".equals(exportQualify)) {
                moiraiOrg.setExportQualify("2");
            } else if ("外贸综合服务企业".equals(exportQualify)) {
                moiraiOrg.setExportQualify("3");
            }
        } else if (isJX) {
            str.append("表格第" + rowNum + "行出口资质开通进项产品此列不能为空，请重新填写;\r\n");
        } else {
            moiraiOrg.setExportQualify("1");// 非进项产品给默认值：非外贸企业
        }
    }

    public void checkDeviceCode(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String deviceCode) {
        if (StringUtils.isNotEmpty(deviceCode)) {
            if (!RegularExpUtils.isNumeric(deviceCode)) {
                str.append("表格第" + rowNum + "行设备编号只能为数字，请重新填写;\r\n");
            } else if (deviceCode.length() > 20) {
                str.append("表格第" + rowNum + "行设备编号长度最大20位，请重新填写;\r\n");
            }
            moiraiOrg.setDeviceCode(deviceCode);
        }
    }

    public void checkParentOrgCode(StringBuffer str, MoiraiOrg parentOrg, MoiraiOrg moiraiOrg, int rowNum,
        String orgCode, String parentOrgCode) {
        if (StringUtils.isEmpty(parentOrgCode)) {
            moiraiOrg.setBackup(parentOrg.getOrgCode());
        } else {
            if (parentOrgCode.equals(orgCode)) {
                str.append("表格第" + rowNum + "父级机构代码不能是自己，请重新填写;\r\n");
            }
            //暂时用备用字段代替
            moiraiOrg.setBackup(parentOrgCode);
        }
    }

    // flag = '0' 机构上传； flag = '1' 租户上传
    private List<MoiraiOrg> checkExcelParam(StringBuffer str, List<MoiraiOrg> orgLists, String flag) {
        int listSize = orgLists.size();
        final int count = 500;
        int runSize = (listSize % count) == 0 ? listSize / count : listSize / count + 1;
        ExecutorService executorService = Executors.newFixedThreadPool(runSize);
        CountDownLatch end = new CountDownLatch(runSize);
        List<MoiraiOrg> newlist = null;//存放每个线程的执行数据
        List<MoiraiOrg> tenantOrgList = new ArrayList<>();
        for (int i = 0; i < runSize; i++) {
            //计算每个线程执行的数据
            if ((i + 1) == runSize) {
                int startIndex = (i * count);
                int endIndex = listSize;
                newlist = orgLists.subList(startIndex, endIndex);
            } else {
                int startIndex = (i * count);
                int endIndex = (i + 1) * count;
                newlist = orgLists.subList(startIndex, endIndex);
            }
            List<MoiraiOrg> finalNewlist = newlist;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (!finalNewlist.isEmpty()) {
                        Long tenantId = finalNewlist.get(0).getTenantId();
                        if (Constants.DEFAULT_ZERO.equals(flag)) {
                            List<MoiraiOrg> codeOrg = moiraiOrgMapper.selectOrgByOrgCode(finalNewlist, tenantId);
                            for (MoiraiOrg org : codeOrg) {
                                str.append(org.getOrgCode() + ",机构代码2.0库已存在;\r\n");
                            }
                        }
                        List<MoiraiOrg> taxOrg = moiraiOrgMapper.selectOrgBytaxCode(finalNewlist);
                        for (MoiraiOrg org : taxOrg) {
                            str.append(org.getTaxCode() + ",纳税人识别号2.0库已存在;\r\n");
                        }
                        tenantOrgList.addAll(taxOrg);
//                        List<String> emailList = new ArrayList<>();
//                        for (MoiraiOrg org : finalNewlist) {
//                            if (StringUtils.isNotBlank(org.getAdminUserEmail())) {
//                                emailList.add(org.getAdminUserEmail());
//                            }
//                        }
//                        if (!emailList.isEmpty()) {
//                            List<MoiraiUser> phonelist = moiraiUserMapper.selectUserByEmail(emailList, tenantId);
//                            for (MoiraiUser user : phonelist) {
//                                str.append("表格 " + user.getUserEmail() + " 该邮箱已经被注册使用;\r\n");
//                            }
//                        }
                    }
                    end.countDown();
                }
            });
        }
        try {
            end.await();
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_MORE_THREAD_CHECK_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
        }
        executorService.shutdown();
        return tenantOrgList;
    }

    public void checkProducts(StringBuffer str, MoiraiOrg parentOrg, MoiraiOrg moiraiOrg, int rowNum, String cps,
        Long qdBm) {
        if (parentOrg != null && StringUtils.isNotEmpty(cps)) {
            List<String> pList = this.openBi(str, rowNum, cps);
            //校验产品标示是否正确
            List<MoiraiOrgProduct> products = parentOrg.getProducts();
            List<MoiraiOrgProduct> productList = new ArrayList<MoiraiOrgProduct>();
            boolean charge = this.checkProduct(pList, products, productList);
            if (!charge) {
                str.append("表格第" + rowNum + "行开通服务有误，请根据上级开通产品填写;\r\n");
            }
            moiraiOrg.setProducts(productList);
        } else if (parentOrg != null && StringUtils.isEmpty(cps)) {// 没有填写开通的产品，默认继承上级的产品
            List<MoiraiOrgProduct> parentProduct = new ArrayList<>();
            parentOrg.getProducts().forEach(item -> {
                MoiraiOrgProduct product = new MoiraiOrgProduct();
                BeanUtils.copyProperties(item, product);
                parentProduct.add(product);
            });
            moiraiOrg.setProducts(parentProduct);
        } else if (parentOrg == null && StringUtils.isNotEmpty(cps)) {
            List<MoiraiOrgProduct> productList = new ArrayList<>();
            List<String> pList = this.openBi(str, rowNum, cps);
            pList.forEach(product -> {
                MoiraiProduct moiraiProduct = moiraiProductMapper.selectByPrimaryKey(Long.valueOf(product));
                if (moiraiProduct == null) {
                    str.append("表格第" + rowNum + "行开通服务：" + product + " 不存在;\r\n");
                }
                MoiraiOrgProduct orgProduct = new MoiraiOrgProduct();
                orgProduct.setProductId(moiraiProduct.getProductId());
                orgProduct.setProductType(Long.valueOf(moiraiProduct.getProductType()));
                orgProduct.setProductName(moiraiProduct.getProductName());
                orgProduct.setQdBm(qdBm == null ? Constants.DEFAULT_QDBM : qdBm);
                productList.add(orgProduct);
            });
            moiraiOrg.setProducts(productList);
        } else {
            str.append("表格第" + rowNum + "行开通服务不能为空;\r\n");
        }
    }

    private List<String> openBi(StringBuffer str, int rowNum, String cps) {
        String[] split = cps.split(",|，");
        List<String> pList = Arrays.asList(split);
        //不可以单独开通bi
        String[] openId = biOpenConditon.split(",");
        List<String> openIdList = Arrays.asList(openId);
        if (pList.contains(Constants.BI_PRODUCT.toString())) {
            boolean openFlag = pList.stream().filter(item -> openIdList.contains(item)).collect(Collectors.toList()).size() == 0;
            if (openFlag) {
                str.append("表格第" + rowNum + "行开通服务有误，BI看板不能单独开通;\r\n");
            }
        }
        return pList;
    }

    /**
     * <B>方法名称：</B>判断Excel开通产品要是上级机构开通产品的子集<BR>
     * <B>概要说明：</B>true表示是子集<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    private boolean checkProduct(List<String> list, List<MoiraiOrgProduct> products,
        List<MoiraiOrgProduct> productList) {
        if (list == null) {
            return true;
        }
        Set<String> productSet = new HashSet<>();
        list.forEach(item -> {
            MoiraiOrgProduct orgProduct = new MoiraiOrgProduct();
            orgProduct.setProductId(Long.valueOf(item));
            orgProduct.setProductType(0L);
            products.forEach(product -> {
                productSet.add(product.getProductId().toString());
                if (item.equals(product.getProductId().toString()) && product.getBelongProduct() != null) {
                    orgProduct.setProductType(1L);
                    orgProduct.setBelongProduct(product.getBelongProduct());
                }
            });
            productList.add(orgProduct);
        });
        List<String> parentList = new ArrayList<>(productSet);
        if (parentList != null && parentList.size() > 0) {
            if (parentList.containsAll(list)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * <B>方法名称：</B>机构开通的产品<BR>
     * <B>概要说明：</B>默认添加的是用户中心<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    public List<MoiraiOrgProduct> createProduct(MoiraiOrg org, boolean haDefault, Long qdBm) {
        List<MoiraiOrgProduct> orgProducts = org.getProducts();
        if (haDefault) {
            String[] defProducts = ORG_DEFAULT_PRODUCTS.split(",");
            if (defProducts != null && defProducts.length > 0) {
                for (int i = 0; i < defProducts.length; i++) {
                    String defProductId = defProducts[i];
                    if (orgProducts == null) {
                        orgProducts = new ArrayList<MoiraiOrgProduct>();
                        org.setProducts(orgProducts);
                    }
                    if (orgProducts.stream().filter(item -> defProductId.equals(item.getProductId().toString())).collect(Collectors.toList()).size() == 0) {
                        MoiraiOrgProduct product = new MoiraiOrgProduct();
                        product.setProductId(Long.valueOf(defProductId));
                        product.setQdBm(Constants.DEFAULT_QDBM);
                        orgProducts.add(product);
                    }
                }
            }
        }
        MoiraiOrgProduct moiraiOrgProduct = moiraiOrgService.addIncidentalProduct(org);
        if (moiraiOrgProduct != null) {
            orgProducts.add(moiraiOrgProduct);
        }
        for (MoiraiOrgProduct product : orgProducts) {
            Long productId = product.getProductId();
            Long qdbm = product.getQdBm();
            if (qdbm == null) {
                if (qdBm != null) {
                    qdbm = qdBm;
                } else {
                    qdbm = Constants.DEFAULT_QDBM;
                }
                product.setQdBm(qdbm);
            }
            if (productId == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_ADDORGERROR);
            }
            if (org.getCreater() != null) {
                product.setCreater(org.getCreater());
            } else {
                product.setCreater(org.getModifyUser());
            }
            product.setTenantId(org.getTenantId());
            product.setOrgId(org.getOrgId());
            if (product.getOpenType() == null) {
                product.setOpenType(1L);
            }
            if (product.getProductType() == null) {
                product.setProductType(0L);
            }
            product.setCreateTime(org.getCreateTime());
        }
        return orgProducts;
    }

    /**
     * <B>方法名称：</B>默认授权角色<BR>
     * <B>概要说明：</B>目前只默认授权角色1用户中心<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    public MoiraiUserAuthz createRole(MoiraiUser moiraiUser) {
        String[] roles = null;
        if ("default".equals(moiraiUser.getType()) && "1".equals(moiraiUser.getHasDefAdminRole())) { //租户账号添加  默认类型添加，用户账号系统指定
            roles = tenantRole.split(",");
        } else {
            return null;
        }
        MoiraiUserAuthz role = new MoiraiUserAuthz();
        role.setUserOrg(moiraiUser.getOrgId());
        role.setTenantId(moiraiUser.getTenantId());
        role.setUserId(moiraiUser.getUserId());
        role.setAuthOrg(moiraiUser.getOrgId());
        role.setCreater(moiraiUser.getCreater());
        role.setCreateTime(moiraiUser.getCreateTime());
        role.setRoleId(Long.parseLong(roles[0]));
        return role;
    }

    /**
     * <B>方法名称：</B>拼接机构管理员信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年3月6日
     */
    public MoiraiUser createUser(MoiraiOrg org) {
        MoiraiUser user = new MoiraiUser();
        user.setTenantId(org.getTenantId());
        user.setOrgId(org.getOrgId());
        user.setUserType("B");
        user.setType("default");
        user.setUserName("机构管理员");
        user.setHasDefAdminRole(org.getHasDefAdminRole());
        user.setUserAccount("admin_" + org.getOrgId());
        user.setUserCreatetype("0");

        user.setCreateTime(org.getCreateTime());
        user.setCreater(org.getCreater());
        user.setModifyTime(org.getCreateTime());
        user.setModifyUser(org.getCreater());
        user.setTelephone(org.getTelphone());
        user.setIsTenantAccount("Y");
        user.setDelFlag("N");//删除标志默认置N
        user.setUseFlag("Y");//启用标志默认置Y
        user.setPhoneValidate("N");
        user.setEmailValidate("N");
        user.setFirstLogin("Y");
        return user;
    }

    // parentOrgCode为null是租户导入
    private List<LazyDynaBean> completeInfomation(List<MoiraiOrg> moiraiOrgList, Map<String, Long> parentOrgCode,
        List<MoiraiUser> userList, List<MoiraiUserAuthz> roleList, List<MoiraiOrgProduct> products, Long qdBm,
        List<Long> prIdList) {
        List<Long> userIds = moiraiSysService.getNums(moiraiOrgList, Constants.MOIRAI_USER);
        List<Long> userInfoIds = moiraiSysService.getNums(moiraiOrgList, Constants.MOIRAI_USERINFO);
        List<Long> userAuthzIds = moiraiSysService.getNums(moiraiOrgList, Constants.MOIRAI_USER_AUTHZ);
        List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
        for (int i = 0; i < moiraiOrgList.size(); i++) {

            MoiraiOrg moiraiOrg = moiraiOrgList.get(i);
            //父级机构
            if (null != parentOrgCode) {
                Long parentOrgId = parentOrgCode.get(moiraiOrg.getBackup());
                if (null == parentOrgId) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_ORG_PARENTNOTEXT.getCode(), "表格第" + (i + 3) + "行父级机构代码不存在或未被授权,不能导入下级机构");
                } else {
                    moiraiOrg.setParentOrg(parentOrgId);
                }
            }
            //添加开通产品的操作
            boolean flag = true;
            if ("0".equals(moiraiOrg.getHasDefOrgProduct())) {
                flag = false;
            }
            List<MoiraiOrgProduct> product = this.createProduct(moiraiOrg, flag, qdBm);
            products.addAll(product);
            //判断是否生成机构管理员
            String hasDefAdminUser = moiraiOrg.getHasDefAdminUser();
            if (Constants.DEFAULT_ZERO.equals(hasDefAdminUser)) {
                continue;
            }
            MoiraiUser user = this.createUser(moiraiOrg);
            if (parentOrgCode != null) {
                user.setIsTenantAccount("N");
            }
            user.setUserId(userIds.get(i));
            user.setUserinfoId(userInfoIds.get(i));
            String uuid = AdminUtils.getUuid();
            String userPassword = passwordService.calculatePassword(Constants.MOIRAI_VERSION_V2);
            String password = AdminUtils.getUuidPasswd(userPassword, uuid);
            user.setUserPassword(password);
            user.setUuid(uuid);
            user.setUserEmail(moiraiOrg.getAdminUserEmail());
            userList.add(user);

            MoiraiUserAuthz roles = this.createRole(user);
            roles.setUorId(userAuthzIds.get(i));
            roleList.add(roles);

            String contextStr = "您的初始化帐号：" + "admin_" + moiraiOrg.getOrgId() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;您的初始化密码：" + userPassword;
            List<MoiraiOrgProduct> orgProducts = moiraiOrg.getProducts();
            String productStr = "";
            if (null == parentOrgCode && orgProducts != null) {
                List<Long> productIds = new ArrayList<>();
                contextStr += "<br>&nbsp;&nbsp;&nbsp;&nbsp;您注册开通产品：";
                for (MoiraiOrgProduct orgProduct : orgProducts) {
                    productIds.add(orgProduct.getProductId());
                    if (!orgProduct.getProductId().equals(Long.valueOf(Constants.DEFAULT_PRODUCT)) && !orgProduct.getProductId().equals(Constants.BI_PRODUCT)) {
                        productStr += orgProduct.getProductName() + "、";
                    }
                }
                // 赋权管理员产品的默认角色
                moiraiOrgService.gainAuthByProductId(productIds, user);
                productStr = productStr.substring(0, productStr.length() - 1);
                contextStr += productStr;
            }
            List<String> emailList = new ArrayList<>();
            emailList.add(moiraiOrg.getAdminUserEmail());
            LazyDynaBean lazyDynaBean = new LazyDynaBean();
            lazyDynaBean.set("emails", emailList);
            lazyDynaBean.set("context", contextStr);
            lazyDynaBean.set("productStr", productStr);
            lazyDynaBean.set("userName", moiraiOrg.getOrgName());
            lazyDynaBean.set("userAccount", "admin_" + moiraiOrg.getOrgId());
            lazyDynaBean.set("userPassword", userPassword);
            lazyDynaBean.set("moiraiOrg", moiraiOrg);
            lazyDynaBeans.add(lazyDynaBean);

            // 查询开通产品 与 默认角色的交集
            if (!prIdList.isEmpty()) {
                List<Long> productIdList = new ArrayList<>(products.size());
                products.forEach(item -> productIdList.add(item.getProductId()));

                // 查询产品对应的预制角色信息
                List<MoiraiRole> productRoleList = moiraiRoleMapper.selectProductRoles(productIdList);
                List<Long> endRoleList = new ArrayList<>();
                for (int r = 0; r < prIdList.size(); r++) {
                    for (int pr = 0; pr < productRoleList.size(); pr++) {
                        if (prIdList.get(r).equals(productRoleList.get(pr).getRoleId())) {
                            endRoleList.add(prIdList.get(r));
                        }
                    }
                }
                if (!endRoleList.isEmpty()) {
                    List<Long> erIds = moiraiSysService.getNums(endRoleList, Constants.MOIRAI_USER_AUTHZ);
                    for (int er = 0; er < endRoleList.size(); er++) {
                        MoiraiUserAuthz item = this.createRole(user);
                        item.setUorId(erIds.get(er));
                        item.setRoleId(endRoleList.get(er));
                        roleList.add(item);
                    }
                }
            }
        }
        List<Long> orgProductIds = moiraiSysService.getNums(products, Constants.MOIRAI_ORG_PRODUCT);
        for (int i = 0; i < products.size(); i++) {
            products.get(i).setOpId(orgProductIds.get(i));
        }
        return lazyDynaBeans;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>机构导入存储信息<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    private void excecutorImportOrg(List<MoiraiOrg> moiraiOrgList, Map<String, Long> parentOrgCode, Long orgId,
        Long qdBm) {
        List<MoiraiUser> userList = new ArrayList<>();
        List<MoiraiUserAuthz> roleList = new ArrayList<>();
        List<MoiraiOrgProduct> products = new ArrayList<>();
        List<LazyDynaBean> lazyDynaBeans = this.completeInfomation(moiraiOrgList, parentOrgCode, userList, roleList, products, qdBm, new ArrayList<>());
        this.addOrgList(moiraiOrgList, userList, roleList, products);
        moiraiSysService.excecutorSendEmail(lazyDynaBeans, Constants.DEFAULT_ONE);
    }
}
