/*
 * @项目名称: Moirai
 * @文件名称: MoiraiSecretKeyController.java
 * @Date: 17-12-5 下午7:47
 * @author Lance cui
 *
 */

package com.baiwang.moirai.controller.user;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.api.MoiraiUserExtraSvc;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.exception.MoiraiUserException;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.role.MoiraiUserAuthzCondition;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.MoiraiUserExtraService;
import com.baiwang.moirai.service.MoiraiUserLoadService;
import com.baiwang.moirai.service.MoiraiUserService;
import com.baiwang.moirai.utils.StrUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@SuppressWarnings("all")
public class MoiraiUserExtraController implements MoiraiUserExtraSvc {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiUserExtraController.class);

    @Autowired
    private MoiraiUserLoadService moiraiUserLoadService;

    @Autowired
    private MoiraiUserExtraService moiraiUserExtraService;

    @Autowired
    private MoiraiUserService moiraiUserService;

    /**
     * <B>方法名称：</B>上传用户Excel模版<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年5月16日
     */
    @Override
    @UserCenterOperationLog(moduleName = "用户管理", action = "批量上传用户", description = "批量上传用户")
    public BWJsonResult uploadUserData(@RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
                                       HttpServletRequest request) {
        String tenantId = request.getParameter("tenantId");
        String orgId = request.getParameter("orgId");
        String creater = request.getParameter("creater");
        String userId = request.getParameter("userId");
        String resourceId = request.getParameter("resourceId");
        logger.info("************tenantId:{}, orgId:{}, creater:{}, resourceId:{}*************", tenantId, orgId, creater, resourceId);
        if (tenantId == null || orgId == null || StrUtils.isEmpty(creater) || userId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        // 文件不存在的情况
        if (excelFile == null || StringUtils.isEmpty(excelFile.getOriginalFilename())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_EXCELFILE_ERROR);
        }
        Long tenantId1 = Long.valueOf(tenantId);
        Long orgIdq = Long.valueOf(orgId);
        Long userIdL = Long.valueOf(userId);
        Long resourceIdL = null;
        if(resourceId != null && !"null".equals(resourceId)){
            resourceIdL = Long.valueOf(resourceId);
        }
        BWJsonResult readExcelRel = moiraiUserLoadService.readExcelRel(excelFile, tenantId1, orgIdq, userIdL, resourceIdL, creater);
        return readExcelRel;
    }

    /**
     * <B>方法名称：</B>获取用户数据范围<BR>
     * <B>概要说明：</B>销项调用<BR>
     *
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUserDataScope> getUserDateScope(@RequestBody MoiraiUserCondition condition) {
        if (StrUtils.isEmpty(condition.getUserId() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiUserDataScope> userDataScope = moiraiUserExtraService.getUserDataScope(condition);
        return new BWJsonResult<MoiraiUserDataScope>(userDataScope);
    }

    /**
     * <B>方法名称：</B>删除授权<BR>
     * <B>概要说明：</B>页面点击角色叉号调用<BR>
     *
     * @return
     * @since 2019年10月24日
     */
    @Override
    public BWJsonResult delUserAuth(@RequestBody MoiraiUserAuthz moiraiUserAuthz) {
        if (moiraiUserAuthz == null || moiraiUserAuthz.getTenantId() == null || moiraiUserAuthz.getUserId() == null || moiraiUserAuthz.getRoleId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiUserExtraService.delUserAuth(moiraiUserAuthz);
    }

    /**
     * <B>方法名称：</B>新授权页面查询角色列表<BR>
     * <B>概要说明：</B>区分授权和非授权<BR>
     *
     * @return
     * @since 2019年10月25日
     */
    @Override
    public BWJsonResult<MoiraiRole> getRoleListByUserAuth(@RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (moiraiUserCondition == null || moiraiUserCondition.getTenantId() == null ||
            moiraiUserCondition.getOrgId() == null || moiraiUserCondition.getUserId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiRole> listByAuth = moiraiUserExtraService.getRoleListByUserAuth(moiraiUserCondition);
        if (listByAuth != null) {
            return new BWJsonResult<>(listByAuth);
        } else {
            return new BWJsonResult<>();
        }
    }

    /**
     * <B>方法名称：</B>新授权页面查询授权机构<BR>
     * <B>概要说明：</B>点击角色时展示已授权机构-用户详情-授权信息页面展示使用<BR>
     *
     * @return
     * @since 2019年10月25日
     */
    @Override
    public BWJsonResult<MoiraiOrg> getAuthOrgByRole(@RequestBody MoiraiUserAuthz moiraiUserAuthz) {
        if (moiraiUserAuthz == null || moiraiUserAuthz.getTenantId() == null ||
            moiraiUserAuthz.getRoleId() == null || moiraiUserAuthz.getUserId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiOrg> orgList = moiraiUserExtraService.getAuthOrgByRole(moiraiUserAuthz);
        if (orgList.isEmpty()) {
            return new BWJsonResult<>();
        } else {
            return new BWJsonResult<>(orgList);
        }
    }

    /**
     * <B>方法名称：</B>新授权页面查询授权机构<BR>
     * <B>概要说明：</B>点击角色时标记已授权机构-用户授权-已授权页面使用<BR>
     *
     * @return
     * @since 2019年5月19日
     */
    @Override
    public BWJsonResult<Long> getAuthOrgIdsByRole(@RequestBody MoiraiUserAuthz moiraiUserAuthz) {
        if (moiraiUserAuthz == null || moiraiUserAuthz.getTenantId() == null ||
            moiraiUserAuthz.getRoleId() == null || moiraiUserAuthz.getUserId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<Long> idsByRole = moiraiUserExtraService.getAuthOrgIdsByRole(moiraiUserAuthz);
        if (idsByRole != null) {
            return new BWJsonResult<>(idsByRole);
        }
        return new BWJsonResult<>();
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     *认证文件上传
     * @return
     * @since 2019/11/22
     */
    public BWJsonResult uploadAuthFile(@RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
        HttpServletRequest request) throws IOException {
        String tenantId = request.getParameter("tenantId");
        String orgId = request.getParameter("orgId");
        String userId = request.getParameter("userId");
        logger.info("************tenantId:{}, orgId:{}, creater:{}*************", tenantId, orgId, userId);
        if (tenantId == null || orgId == null || userId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        // 文件不存在的情况
        if (excelFile == null || StringUtils.isEmpty(excelFile.getOriginalFilename())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_EXCELFILE_ERROR);
        }
        return moiraiUserLoadService.uploadAuthFile(excelFile,Long.valueOf(tenantId),Long.valueOf(orgId),Long.valueOf(userId));
    }

    /**
     * <B>方法名称：</B>查询用户列表<BR>
     * <B>概要说明：</B>用户管理首页使用<BR>
     *
     * @return
     * @since 2019年5月16日
     */
    @Override
    @Deprecated
    public BWJsonResult<MoiraiUserAuthzCondition> getUserList(@RequestBody MoiraiUserCondition condition) {
        if (condition == null || condition.getTenantId() == null ||
                StrUtils.isEmpty(condition.getUserType() + "") || StrUtils.isEmptyList(condition.getAuthOrgIds())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }

        Map<String, Object> userListMap = moiraiUserExtraService.getUserList(condition);
        if (ObjectUtils.isEmpty(userListMap)) {
            return new BWJsonResult<>();
        }
        Long total = (Long) userListMap.get("total");
        List<MoiraiUserAuthzCondition> userList = (List<MoiraiUserAuthzCondition>) userListMap.get("userList");
        BWJsonResult<MoiraiUserAuthzCondition> bwJsonResult = new BWJsonResult<>(userList);
        bwJsonResult.setTotal(total.intValue());
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>判断用户信息是否重复<BR>
     * <B>概要说明：</B>添加用户使用<BR>
     *
     * @return
     * @since 2019年5月16日
     */
    @Override
    @Deprecated
    public BWJsonResult isUserExist(@RequestBody MoiraiUserCondition condition) {
        if (StrUtils.isEmpty(condition.getUserAccount())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiUserExtraService.checkUserInfo(condition);
        return new BWJsonResult();
    }

    /**
     * 查询用户 分页 添加用户后展示的分页列表
     *
     * @param moiraiUserCondition
     * @return
     */
    @UserCenterOperationLog(moduleName = "用户管理", action = "导出用户信息", description = "根据条件导出用户信息")
    @PostMapping("/exportUserInfo")
    public BWJsonResult exportUserInfo(HttpServletRequest request, HttpServletResponse response, @RequestBody MoiraiUserCondition moiraiUserCondition) {
        if (null == moiraiUserCondition) {
            return new BWJsonResult(new MoiraiUserException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        moiraiUserCondition.setPageNo(1);
        moiraiUserCondition.setPageSize(50000);
        Workbook workbook = moiraiUserService.exportUserList(moiraiUserCondition);
        // 判断使用的浏览器
        String fileName = "用户信息.xls";
        ServletOutputStream outputStream = null;
        try {
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
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
            response.flushBuffer();
        } catch (UnsupportedEncodingException e) {
            logger.error("编码转换失败！", e);
            throw new MoiraiException("", "用户导出失败！");
        } catch (IOException e) {
            logger.error("IO解析异常！", e);
            throw new MoiraiException("", "用户导出失败！");
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
        return BWJsonResult.withSuccessMessage("数据导出成功！");
    }

    /**
     * <B>方法名称：</B>运营后台2.0企业用户管理<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2020年8月07日
     */
    @RequestMapping("/findBUserListByCondition")
    public BWJsonResult<MoiraiUserCondition> findBUserListByCondition(@RequestBody MoiraiUserCondition condition) {
        return moiraiUserExtraService.findBUserListByCondition(condition);
    }
}
