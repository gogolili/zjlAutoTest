package com.baiwang.moirai.serviceimpl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.dao.MoiraiUserDao;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.event.UserEvent;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.ExtTableClient;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserDataScopeMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.MoiraiUserinfoMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzExample;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.user.MoiraiUserinfo;
import com.baiwang.moirai.service.MoiraiExtService;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.service.MoiraiRoleService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiUserLoadService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.ImportExcel;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MoiraiUserLoadServiceImpl implements MoiraiUserLoadService {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiUserLoadServiceImpl.class);

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    private MoiraiUserDataScopeMapper moiraiUserDataScopeMapper;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    ExtTableClient extTableClient;

    @Autowired
    MoiraiExtService moiraiExtService;

    @Autowired
    private MoiraiUserDao moiraiUserDao;

    @Value("${uploadUrl}")
    private String uploadUrl;

    @Value("${downloadUrl}")
    private String downloadUrl;

    @Value("${use.method}")
    private boolean useFlag;

    @Resource
    private ApplicationEventPublisher context;

    @Autowired
    private MoiraiUserinfoMapper moiraiUserinfoMapper;

    @Autowired
    private MoiraiRoleService moiraiRoleService;

    @Autowired(required = false)
    private PasswordService passwordService;

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     * 认证资料上传
     *
     * @return
     * @since 2019/11/25
     */
    public BWJsonResult uploadAuthFile(MultipartFile file, Long tenantId, Long orgId, Long userId) {
        logger.info("tenantId = 【{}】 orgId = 【{}】 userId = 【{}】", tenantId, orgId, userId);
        //工具读取Excel
        List<ArrayList<String>> list = null;
        StringBuffer buffer = new StringBuffer();
        try {
            list = new ImportExcel().read(file);
        } catch (InvalidFormatException e) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_READ_EXCEL_FAIL);
        } catch (IOException e) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_NOTEXCELFILE_ERROR);
        }
        if (list == null || list.size() <= 1) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_READ_EXCEL_FAIL);
        }

        MoiraiUserAuthzExample example = new MoiraiUserAuthzExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<MoiraiUserAuthz> authzs = moiraiUserAuthzMapper.selectByExample(example);
        List<Long> authOrgId = new ArrayList<>();
        authzs.forEach(org -> authOrgId.add(org.getAuthOrg()));
        List<MoiraiUser> users = null;
        MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
        String dualFactor = moiraiTenant.getDualFactor();
        if (!authOrgId.isEmpty()) {
            MoiraiUserCondition userCondition = new MoiraiUserCondition();
            userCondition.setAuthOrgIds(authOrgId);
            users = moiraiUserMapper.findUserListByCondition(userCondition);
        }
        Map<String, MoiraiUser> accountMap = new HashMap<>();
        switch (dualFactor) {
            case Constants.MOIRAI_DUCL_FACTOR_DEFAULT:
            case Constants.MOIRAI_DUCL_FACTOR_EMAIL:
                for (MoiraiUser moiraiUser : users) {
                    String email = moiraiUser.getUserEmail();
                    if (!RegularExpUtils.checkEmail(email)) {
                        accountMap.put(moiraiUser.getUserAccount(), moiraiUser);
                    }
                }
                break;
            case Constants.MOIRAI_DUCL_FACTOR_PHONE:
                for (MoiraiUser moiraiUser : users) {
                    String telephone = moiraiUser.getTelephone();
                    if (!RegularExpUtils.checkMobile(telephone)) {
                        accountMap.put(moiraiUser.getUserAccount(), moiraiUser);
                    }
                }
                break;
            default:
                break;
        }
        List<String> phoneOrEmailList = null;
        if (Constants.MOIRAI_DUCL_FACTOR_PHONE.equals(dualFactor)) {
            // 本租户所有用户
            MoiraiUserCondition moiraiUserCondition = new MoiraiUserCondition();
            moiraiUserCondition.setTenantId(tenantId);
            List<MoiraiUser> allUserList = moiraiUserMapper.findUserByCondition(moiraiUserCondition);
            phoneOrEmailList = new ArrayList<>();
            for (MoiraiUser moiraiUser : allUserList) {
                String telephone = moiraiUser.getTelephone();
                phoneOrEmailList.add(telephone);
            }
        }

        List<MoiraiUser> dataList = new ArrayList<>();
        List<String> tempList = new ArrayList<>();
        List<String> accounts = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            ArrayList<String> row = list.get(i);
            if (row == null) {
                continue;
            }
            int rowNum = i + 2;
            //循环当前行
            String userAccount = row.get(0);
            String phoneOrEmail = row.get(2);

            if (StrUtils.isEmpty(userAccount)) {
                buffer.append("表格第 " + rowNum + " 行用户账号不能为空;\r\n");
                continue;
            }
            MoiraiUser moiraiUser = accountMap.get(userAccount);
            if (moiraiUser == null) {
                buffer.append("表格第 " + rowNum + " 行用户账号禁止修改(或不存在);\r\n");
                continue;
            }
            if (accounts.contains(userAccount)) {
                buffer.append("表格第 " + rowNum + " 行用户账号重复;\r\n");
                continue;
            }
            if (Constants.MOIRAI_DUCL_FACTOR_PHONE.equals(dualFactor)) {
                if (StrUtils.isEmpty(phoneOrEmail)) {
                    buffer.append("表格第 " + rowNum + " 行手机号不能为空;\r\n");
                    continue;
                }
                if (tempList.contains(phoneOrEmail)) {
                    buffer.append("表格第 " + rowNum + " 行手机号excel里重复;\r\n");
                }
                boolean smartPhone = RegularExpUtils.checkMobile(phoneOrEmail);
                if (!smartPhone) {
                    buffer.append("表格第 " + rowNum + " 行电话号码格式不正确;\r\n");
                }
                if (phoneOrEmailList.contains(phoneOrEmail)) {
                    buffer.append("表格第 " + rowNum + " 行电话号码已经存在，禁止重复导入;\r\n");
                }
                moiraiUser.setTelephone(phoneOrEmail);
            }

            if (Constants.MOIRAI_DUCL_FACTOR_EMAIL.equals(dualFactor) || Constants.MOIRAI_DUCL_FACTOR_DEFAULT.equals(dualFactor)) {
                if (StrUtils.isEmpty(phoneOrEmail)) {
                    buffer.append("表格第 " + rowNum + " 行邮箱不能为空;\r\n");
                    continue;
                }
                boolean valid = RegularExpUtils.checkEmail(phoneOrEmail);
                if (!valid) {
                    buffer.append("表格第 " + rowNum + " 行邮箱格式不正确;\r\n");
                }
                moiraiUser.setUserEmail(phoneOrEmail);
            }
            tempList.add(phoneOrEmail);
            accounts.add(userAccount);
            dataList.add(moiraiUser);
        }
        BWJsonResult bwJsonResult = null;
        try {
            if (buffer.length() <= 0) {
                if (dataList != null && dataList.size() > 0) {
                    for (MoiraiUser user : dataList) {
                        user.setModifyTime(DateTimeUtils.nowTimeLong());
                        moiraiUserMapper.updateByPrimaryKeySelective(user);
                    }
                }
                bwJsonResult = new BWJsonResult("Excel导入成功");
            } else {//将错误信息提示文件上传至文件系统
                if (useFlag) {
                    bwJsonResult = moiraiSysService.uploadErrorFile(buffer, Constants.USEREXCEL_FILENAME);
                } else {
                    bwJsonResult = moiraiSysService.uploadString(buffer.toString(), Constants.MOIRAI_USER_ERRORFILE, orgId);
                }
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_USER_AUTHZ_FILE_FAIL;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>重构原始字段、扩展字段<BR>
     *
     * @return
     * @since 2019/12/11
     */
    public BWJsonResult readExcelRel(MultipartFile excelFile, Long tenantId, Long orgId, Long userId, Long resourceIdL,
        String creater) {

        InputStream inputStream = null;
        List<Map<String, Object>> list = null;
        BWJsonResult bwJsonResult = null;
        try {
            ImportParams params = new ImportParams();
            //设置excel文件中数据开始的位置
            params.setHeadRows(2);
            inputStream = excelFile.getInputStream();
            list = ExcelImportUtil.importExcel(inputStream, Map.class, params);
        } catch (Exception e) {
            logger.error("上传用户失败", e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e1) {
                logger.error("关闭流异常", e1);
            }
        }
        Map<String, Map<String, String>> dicMap = moiraiExtService.calExtMap(String.valueOf(tenantId), String.valueOf(orgId), String.valueOf(userId), Constants.MOIRAI_USER_PAGE_UNIQUE_NAME, Constants.MOIRAI_USER_IMPORT_TAG);
        Map<String, String> extMap = dicMap.get("extMap");
        List<ArrayList<String>> sheetList = new ArrayList<>();
        List<Map<String, Object>> extList = new ArrayList<>();
        Map<String, Object> excelMap = new HashMap<>();
        excelMap.put("sheetList", sheetList);
        excelMap.put("extList", extList);
        for (Map<String, Object> map : list) {
            Map<String, Object> ext = new HashMap<>();
            ArrayList<String> rowList = new ArrayList<>();
            for (String cnName : map.keySet()) {//excel列名
                //实体类属性
                String extFiled = extMap.get(cnName);
                if (extFiled != null) {
                    Object extValue = map.get(cnName);
                    if (extValue != null) {
                        String extSelectLists = extMap.get(cnName + "SelectList");
                        if (StringUtils.isBlank(extSelectLists)) {
                            ext.put(extFiled, extValue);
                            continue;
                        }
                        JSONArray extSelectList = JSON.parseArray(extSelectLists);
                        for (int i = 0; i < extSelectList.size(); i++) {
                            JSONObject item = extSelectList.getJSONObject(i);
                            if (extValue.equals(item.getString("dictCode"))
                                || extValue.equals(item.getString("dictName"))) {
                                ext.put(extFiled, item.getString("dictCode"));
                            }
                        }
                    }
                    continue;
                }
                Object nomalValue = map.get(cnName);
                if (nomalValue != null) {
                    rowList.add(nomalValue + "");
                } else {
                    rowList.add(null);
                }
            }
            sheetList.add(rowList);
            extList.add(ext);
        }
        bwJsonResult = checkAndInsert(excelMap, tenantId, orgId, userId, creater, resourceIdL);

        return bwJsonResult;
    }

    /**
     * 用户信息校验以及实例化
     *
     * @param tenantId
     * @param orgId 当前机构id
     * @param creater
     * @return
     * @throws Exception
     */
//    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult checkAndInsert(Map<String, Object> excelMap, Long tenantId, Long orgId, Long userId,
        String creater, Long resourceId) {
        long start = System.currentTimeMillis();
        //最终数据
        List<ArrayList<String>> sheetList = (List<ArrayList<String>>) excelMap.get("sheetList");
        List<Map<String, Object>> extList = (List<Map<String, Object>>) excelMap.get("extList");
        //工具读取Excel
        if (sheetList.size() > Constants.USEREXCEL_MAXCOUNT) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_SYS_FILE_UPLOAD_FAIL);
        }
        MoiraiTenant moiraiTenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
        String dualFactor = moiraiTenant.getDualFactor();

        //授权机构
        List<MoiraiUserAuthz> authOrgs = moiraiOrgService.getUserAuthBycondition(userId, resourceId);
        if (authOrgs != null && authOrgs.size() <= 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_NOT_AUTH);
        }

        BWJsonResult bwJsonResult = null;
        StringBuffer stringBuffer = new StringBuffer();

        //校验上传数据，循环每行
        MoiraiOrgCondition queryTenantRole = new MoiraiOrgCondition();
        queryTenantRole.setPageNo(0);
        queryTenantRole.setTenantId(tenantId);
        List<MoiraiRole> roleList = moiraiRoleService.getTenantAllRole(queryTenantRole).getData();
        Map<String, MoiraiRole> roleMap = new HashMap<>();
        for (MoiraiRole moiraiRole : roleList) {
            roleMap.put(moiraiRole.getRoleName(), moiraiRole);
        }
        List<Long> authOrgIds = authOrgs.stream().map(items -> items.getAuthOrg()).collect(Collectors.toList());
        MoiraiOrgCondition moiraiOrgCondition = new MoiraiOrgCondition();
        moiraiOrgCondition.setItems(authOrgIds);
        List<MoiraiOrg> moiraiOrgs = moiraiOrgMapper.selectOrgBatcher(moiraiOrgCondition);
        Map<String, Long> codeMap = new HashMap<>();
        for (MoiraiOrg moiraiOrg : moiraiOrgs) {
            codeMap.put(moiraiOrg.getOrgCode(), moiraiOrg.getOrgId());
        }
        int size = sheetList.size();
        List<ArrayList<String>> oldChecklist = new ArrayList<>();
        MoiraiUser moiraiUser;
        MoiraiUserinfo moiraiUserinfo;
        List<MoiraiUser> userList = new ArrayList<>();
        List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
        List<MoiraiUserinfo> userInfolist = new ArrayList<>();
        List<MoiraiUserAuthz> authList = new ArrayList<>();
        List<MoiraiUserDataScope> scopeList = new ArrayList<>();
        List<String> commonList = new ArrayList<>();//Excel数据
        List<Long> userIds = moiraiSysService.getNums(sheetList, Constants.MOIRAI_USER);
        List<Long> userInfoIds = moiraiSysService.getNums(sheetList, Constants.MOIRAI_USERINFO);
        Map<String, Boolean> userAccountMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            moiraiUser = new MoiraiUser();
            moiraiUserinfo = new MoiraiUserinfo();
            ArrayList<String> row = sheetList.get(i);
            int rowNum = i + 3;
            //账号：userAccount
            String account = row.get(0);
            this.checkUserAccount(account, stringBuffer, rowNum, commonList);
            if (userAccountMap.containsKey(account)){
                stringBuffer.append("表格第 " + rowNum + " 行用户账号,表格中已存在同样账号;\r\n");
            }
            userAccountMap.put(account, true);
            //用户名：userName
            String userName = row.get(1);
            this.checkUserName(userName, stringBuffer, rowNum);
            //性别
            String sex = row.get(2);
            this.checkSex(sex, stringBuffer, rowNum);

            //机构代码
            String orgCode = row.get(3);
            this.checkOrgCode2(orgCode, stringBuffer, rowNum, codeMap);
            Long userOrg = codeMap.get(orgCode);
            queryTenantRole.setOrgId(userOrg);
            //telephone
            String telephone = row.get(4);
            if (Constants.MOIRAI_DUCL_FACTOR_PHONE.equals(dualFactor) && StrUtils.isEmpty(telephone)) {
                stringBuffer.append("表格第 " + rowNum + " 行电话号码不能为空;\r\n");
            }
            this.checkTelephone(telephone, stringBuffer, rowNum, commonList);
            //email
            String email = row.get(5);

            if ((Constants.MOIRAI_DUCL_FACTOR_EMAIL.equals(dualFactor) || passwordService.calculatePasswordMark())
                    && StrUtils.isEmpty(email)) {
                stringBuffer.append("表格第 " + rowNum + " 行邮箱不能为空;\r\n");
            }
            this.checkEmail(email, stringBuffer, rowNum, commonList);
            //userDetaile
            String userDetaile = row.get(6);
            this.checkDetail(userDetaile, stringBuffer, rowNum);
            //roleName角色名称必须存在
            String roleName = row.get(7);
            this.checkRolel(roleName, stringBuffer, rowNum, tenantId, userOrg, roleList);
            //scope
            String scope = row.get(8);
            this.checkScope(scope, stringBuffer, rowNum);

            /** 用户 **/
            moiraiUser.setUserAccount(account);
            moiraiUser.setUserName(userName);
            moiraiUser.setTelephone(telephone);
            moiraiUser.setUserEmail(email);
            moiraiUser.setUserDetaile(userDetaile);
            moiraiUser.setOrgId(userOrg);
            moiraiUser.setTenantId(tenantId);
            moiraiUser.setUserId(userIds.get(i));
            String uuid = AdminUtils.getUuid();
            String userPassword = passwordService.calculatePassword(Constants.MOIRAI_VERSION_V2);
            String password = AdminUtils.getUuidPasswd(userPassword, uuid);//指定默认密码为123456
            moiraiUser.setUserPassword(password);
            moiraiUser.setUuid(uuid);
            Long nowTime = DateTimeUtils.nowTimeLong();
            moiraiUser.setCreateTime(nowTime);
            moiraiUser.setCreater(creater);
            moiraiUser.setModifyTime(nowTime);
            moiraiUser.setModifyUser(creater);
            moiraiUser.setIsTenantAccount("N");
            moiraiUser.setDelFlag("N");//删除标志默认置N
            moiraiUser.setUseFlag("Y");//启用标志默认置Y
            moiraiUser.setUserType("B");
            moiraiUser.setPhoneValidate("N");
            moiraiUser.setEmailValidate("N");
            moiraiUser.setUserCreatetype("4");
            moiraiUser.setUserinfoId(userInfoIds.get(i));
            moiraiUser.setFirstLogin("Y");
            moiraiUser.setExt(extList.get(i));
            userList.add(moiraiUser);

            /** 用户详情 **/
            moiraiUserinfo.setUserinfoId(userInfoIds.get(i));
            moiraiUserinfo.setSex(sex);
            moiraiUserinfo.setModifyTime(nowTime);
            moiraiUserinfo.setUserId(userIds.get(i));
            moiraiUserinfo.setDelFlag("N");
            moiraiUserinfo.setName(userName);
            moiraiUserinfo.setUseFlag("Y");
            moiraiUserinfo.setModifyUser(creater);
            userInfolist.add(moiraiUserinfo);

            /** 授权 **/
            String[] roles = new String[0];
            if (!StrUtils.isEmpty(roleName)) {
                roles = roleName.split(",");
            }
            for (String role : roles) {
                MoiraiUserAuthz authz = new MoiraiUserAuthz();
                authz.setTenantId(tenantId);
                authz.setUserId(userIds.get(i));
                authz.setUserOrg(userOrg);
                authz.setCreater(creater);
                authz.setCreateTime(nowTime);
                authz.setAuthOrg(userOrg);//授权所属机构
                MoiraiRole moiraiRole3 = roleMap.get(role);
                if (moiraiRole3 != null) {
                    authz.setRoleId(moiraiRole3.getRoleId());
                    authz.setRoleOrg(moiraiRole3.getOrgId());
                    authList.add(authz);
                }
            }

            /** 数据范围 **/
            MoiraiUserDataScope dataScope = new MoiraiUserDataScope();
            dataScope.setCreater(creater);
            dataScope.setCreateTime(nowTime);
            dataScope.setScope(scope);
            dataScope.setUserId(userIds.get(i));
            scopeList.add(dataScope);
            oldChecklist.add(row);

            List<String> emailList = new ArrayList<>();
            emailList.add(moiraiUser.getUserEmail());
            LazyDynaBean lazyDynaBean = new LazyDynaBean();
            lazyDynaBean.set("emails", emailList);
            lazyDynaBean.set("context", "您的初始化帐号：" + moiraiUser.getUserAccount() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;您的初始化密码：" + userPassword);
            lazyDynaBean.set("userName", moiraiUser.getUserAccount());
            lazyDynaBeans.add(lazyDynaBean);
        }
        moiraiSysService.excecutorOldDB(oldChecklist, stringBuffer, 4);
        this.checkAccountParam(stringBuffer, userList);
        if (stringBuffer.length() <= 0) {
            logger.info("*******************实例化用户导入信息*****************************");
            //保存用户信息
            int addBatch = moiraiUserDao.addBatch(userList);
            if (addBatch <= 0) {
                throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
            }
            //保存用户详情信息
            int i1 = moiraiUserinfoMapper.addBatch(userInfolist);
            if (i1 <= 0) {
                throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
            }
            //保存用户分配的角色信息
            List<Long> uorIds = moiraiSysService.getNums(authList, Constants.MOIRAI_USER_AUTHZ);
            for (int uorid = 0; uorid < authList.size(); uorid++) {
                authList.get(uorid).setUorId(uorIds.get(uorid));
            }
            int batchInsert = moiraiUserAuthzMapper.batchInsert(authList);
            if (batchInsert <= 0) {
                throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
            }
            //分配数据范围
            if (scopeList != null && scopeList.size() > 0) {
                List<Long> udsIds = moiraiSysService.getNums(scopeList, Constants.MOIRAI_USER_DATA_SCOPE);
                for (int udsId = 0; udsId < scopeList.size(); udsId++) {
                    scopeList.get(udsId).setUdsId(udsIds.get(udsId));
                }
                int batchInsert2 = moiraiUserDataScopeMapper.batchInsert(scopeList);
                if (batchInsert2 <= 0) {
                    throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
                }
            }
            moiraiSysService.excecutorSendEmail(lazyDynaBeans, Constants.DEFAULT_TWO);
            bwJsonResult = new BWJsonResult("Excel导入成功");
            //同步cp机构和用户
            context.publishEvent(new UserEvent(tenantId));
        } else {//将错误信息提示文件上传至文件系统
            if (useFlag) {
                bwJsonResult = moiraiSysService.uploadErrorFile(stringBuffer, Constants.USEREXCEL_FILENAME);
            } else {
                bwJsonResult = moiraiSysService.uploadString(stringBuffer.toString(), Constants.MOIRAI_USER_ERRORFILE, orgId);
            }
        }
        logger.info("批量导入用户耗时 {} ms", System.currentTimeMillis() - start);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>校验邮箱、手机号、用户账号的重复性现象<BR>
     *
     * @return
     * @since 2019/10/28
     */
    private void checkAccountParam(StringBuffer str, List<MoiraiUser> userLists) {
        int listSize = userLists.size();
        final int count = 500;
        int runSize = (listSize % count) == 0 ? listSize / count : listSize / count + 1;
        ExecutorService executorService = Executors.newFixedThreadPool(runSize);
        CountDownLatch end = new CountDownLatch(runSize);
        List<MoiraiUser> newlist = null;//存放每个线程的执行数据
        for (int i = 0; i < runSize; i++) {
            //计算每个线程执行的数据
            if ((i + 1) == runSize) {
                int startIndex = (i * count);
                int endIndex = listSize;
                newlist = userLists.subList(startIndex, endIndex);
            } else {
                int startIndex = (i * count);
                int endIndex = (i + 1) * count;
                newlist = userLists.subList(startIndex, endIndex);
            }
            List<MoiraiUser> finalNewlist = newlist;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Long tenantId = finalNewlist.get(0).getTenantId();
                    List<MoiraiUser> accountlist = moiraiUserMapper.selectUserByAccount(finalNewlist);
                    for (MoiraiUser user : accountlist) {
                        str.append("表格 " + user.getUserAccount() + " 用户账号已经存在;\r\n");
                    }
                    List<String> telephoneList = new ArrayList<>();
//                    List<String> emailList = new ArrayList<>();
                    for (MoiraiUser moiraiUser : finalNewlist) {
                        if (StringUtils.isNotBlank(moiraiUser.getTelephone())) {
                            telephoneList.add(moiraiUser.getTelephone());
                        }
//                        if (StringUtils.isNotBlank(moiraiUser.getUserEmail())) {
//                            emailList.add(moiraiUser.getUserEmail());
//                        }
                    }
//                    if (!emailList.isEmpty()) {
//                        List<MoiraiUser> emailist = moiraiUserMapper.selectUserByEmail(emailList, tenantId);
//                        for (MoiraiUser user : emailist) {
//                            str.append("表格 " + user.getUserEmail() + " 该邮箱已经被注册使用;\r\n");
//                        }
//                    }
                    if (!telephoneList.isEmpty()) {
                        List<MoiraiUser> phonelist = moiraiUserMapper.selectUserByPhone(telephoneList, tenantId);
                        for (MoiraiUser user : phonelist) {
                            str.append("表格 " + user.getTelephone() + " 该手机号已经被注册使用;\r\n");
                        }
                    }
                    end.countDown();
                }
            });
        }
        try {
            end.await();
        } catch (InterruptedException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_MORE_THREAD_CHECK_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
        }
        executorService.shutdown();
    }

    public void checkOrgCode2(String orgCode, StringBuffer buffer, int row, Map<String, Long> codeMap) {
        if (!codeMap.containsKey(orgCode)) {
            buffer.append("表格第 " + row + " 行机构代码对应的机构未在用户授权列表中;\r\n");
        }
    }

    /**
     * <B>方法名称：</B>获取机构对应的角色<BR>
     * <B>概要说明：</B>同一个租户下<BR>
     *
     * @return
     * @since 2019年3月30日
     */
    private Map<Long, List<MoiraiRole>> getOrgRoles(List<MoiraiRole> roles) {
        Map<Long, List<MoiraiRole>> map = new HashMap<>();
        for (MoiraiRole moiraiRole : roles) {
            Long orgId = moiraiRole.getOrgId();
            if (map.containsKey(orgId)) {
                List<MoiraiRole> roleList = map.get(orgId);
                roleList.add(moiraiRole);
            } else {
                List<MoiraiRole> list = new ArrayList<>();
                list.add(moiraiRole);
                map.put(orgId, list);
            }
        }
        return map;
    }

    /*
     * <B>方法名称：</B>校验账号规则、以及Excel文件中账号的重复性<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/12
     */
    public void checkUserAccount(String userAccount, StringBuffer buffer, int row, List<String> commonList) {
        boolean mobile = RegularExpUtils.checkMobile(userAccount);
        boolean flag = true;
        if (mobile) {
            buffer.append("表格第 " + row + " 行用户账号,系统不支持使用手机号创建账号;\r\n");
            return;
        }
        if (StrUtils.isEmpty(userAccount)) {
            buffer.append("表格第 " + row + " 行用户账号不可以为空;\r\n");
            return;
        }

        if (commonList.contains(userAccount)) {
            buffer.append("表格第 " + row + " 行用户账号已经在Excel中存在，禁止重复导入;\r\n");
            return;
        }
        boolean valid = RegularExpUtils.valid(userAccount);
        if (!valid) {
            flag = false;
            buffer.append("表格第 " + row + " 行用户账号格式是数字、字母、汉字的集合,且不能包含特殊字符@;");
        }
        byte[] bytes = this.getParamBtyes(userAccount);
        if (bytes.length > 32 || bytes.length < 1) {
            if (!flag) {
                buffer.append("用户账号字符长度不应超过32个字符;");
            } else {
                flag = false;
                buffer.append("表格第 " + row + " 行用户账号字符长度至少为1个字符,且不应超过32个字符;");
            }
        }
        if (!flag) {
            buffer.append("\r\n");
        }
    }

    public void checkSex(String sex, StringBuffer buffer, int row) {
        if (StringUtils.isNotBlank(sex)) {
            sex = sex.trim();
            if (!("男".equals(sex) || "女".equals(sex))) {
                buffer.append("表格第 " + row + " 行性别输入不符合规定;\r\n");
            }
        }
    }

    public void checkUserName(String userName, StringBuffer buffer, int row) {
        boolean valid = RegularExpUtils.valid(userName);
        boolean flag = true;
        if (StrUtils.isEmpty(userName)) {
            buffer.append("表格第 " + row + " 行用户名不可以为空;");
            return;
        }

        if (!valid) {
            flag = false;
            buffer.append("表格第 " + row + " 行用户姓名格式錯誤，不是字母、数字、汉字的組合;");
        }
        byte[] bytes = this.getParamBtyes(userName);
        if (bytes.length > 20) {
            if (!flag) {
                buffer.append("以及用户名字符长度不应超过20个字符;");
            } else {
                flag = false;
                buffer.append("表格第 " + row + " 行用户名字符长度不应超过150个字符;");
            }
        }
        if (!flag) {
            buffer.append("\r\n");
        }

    }

    public void checkTelephone(String phone, StringBuffer buffer, int row, List<String> commonList) {
        if (!StrUtils.isEmpty(phone)) {
            //电话号码格式验证
            boolean smartPhone = RegularExpUtils.checkMobile(phone);
            if (commonList.contains(phone)) {
                buffer.append("表格第 " + row + " 行电话号码在Excel文件中已经存在，禁止重复导入;\r\n");
                return;
            }
            if (!smartPhone) {
                buffer.append("表格第 " + row + " 行电话号码格式不正确;\r\n");
            }
        }
    }

    public void checkEmail(String email, StringBuffer buffer, int row, List<String> commonList) {
        if (!StringUtils.isEmpty(email)) {
            //验证邮箱格式的问题
            boolean valid = RegularExpUtils.checkEmail(email);
            if (!valid) {
                buffer.append("表格第 " + row + " 行邮箱格式不正确;\r\n");
                return;
            }
        }
    }

    public void checkRolel(String roleName, StringBuffer buffer, int row, Long tenantId, Long orgId,
        List<MoiraiRole> roleList) {
        if (StrUtils.isEmpty(roleName)) {
            buffer.append("表格第 " + row + " 行角色名称不可以为空;\r\n");
            return;
        }
        String[] roles = roleName.split(",");
        for (String role : roles) {
            List<String> list = new ArrayList<>();
            roleList.forEach(item -> {
                if (role.equals(item.getRoleName())) {
                    list.add(role);
                    Long currentOrgId = item.getOrgId();
                    if (Constants.flag_Y.equals(item.getLowerSee()) && Constants.flag_Y.equals(item.getDefaultFlag())) {
                        // 系统角色所有组织机构都能使用
                    } else if (!currentOrgId.equals(orgId)) {
                        MoiraiOrgCondition condition = new MoiraiOrgCondition();
                        condition.setTenantId(tenantId);
                        condition.setParentOrg(0L);
                        List<MoiraiOrg> orgFatherInfo = moiraiOrgMapper.queryOrgByCondition(condition);
                        List<MoiraiOrg> collect = orgFatherInfo.stream().filter(org -> org.getOrgId().equals(currentOrgId)).collect(Collectors.toList());
                        if ("N".equals(item.getLowerSee()) || collect == null || collect.size() == 0) {
                            buffer.append("表格第 " + row + " 行" + role.trim() + "角色不存在于机构的角色列表中!\r\n");
                        }
                    }
                }
            });
            if (list.size() == 0) {
                buffer.append("表格第 " + row + " 行" + role + " 角色不存在于机构的角色列表中!\r\n");
            }
        }
    }

    public void checkScope(String scope, StringBuffer buffer, int row) {
        boolean checkNumber = RegularExpUtils.checkNumber(scope);
        if (StrUtils.isEmpty(scope) || !checkNumber) {
            buffer.append("表格第 " + row + " 行数据范围值格式错误:请填写数字:0-本机构数据,1-本人数据;\r\n");
        }
    }

    public void checkDetail(String detail, StringBuffer buffer, int row) {
        if (!StrUtils.isEmpty(detail)) {
            byte[] bytes = this.getParamBtyes(detail);
            if (bytes.length > 150) {
                buffer.append("表格第 " + row + " 行用户信息描述的字符数不超过150个 ;\r\n");
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

}
