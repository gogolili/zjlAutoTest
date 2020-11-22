package com.baiwang.moirai.controller.sys;

import com.baiwang.cloud.common.exception.SystemException;
import com.baiwang.cloud.common.model.SyspageBasicQuery;
import com.baiwang.cloud.common.util.StringUtil;
import com.baiwang.moirai.api.MoiraiGetMethodSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.service.MoiraiExtService;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.MoiraiUserExtraService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <B>方法名称：</B>项目中所有的get请求的方法<BR>
 * <B>概要说明：</B>feign调用时request和response同时存在可能调用不通<BR>
 *
 * @author sxl
 * @since 2019年5月16日
 */
@RestController
public class MoiraiGetMethodController implements MoiraiGetMethodSvc {

    private static Logger logger = LoggerFactory.getLogger(MoiraiGetMethodController.class);

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private MoiraiUserExtraService moiraiUserExtraService;

    @Autowired
    private MoiraiExtService moiraiExtService;

    /**
     * <B>方法名称：</B>下载用户Excel模版:机构、用户<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年5月20日
     */
    @RequestMapping("/downLoadTemplate")
    public void downLoadTemplate(@RequestBody SyspageBasicQuery queryParam) {
        logger.info("生成导出模板数据参数:{}"+queryParam);
        String tenantId=queryParam.getTenantId();
        String userId=queryParam.getUserId();
        String orgId=queryParam.getOrgId();
        String pageUniqueName=queryParam.getPageUniqueName();
        String excelType=queryParam.getExcelType();
        String type=queryParam.getType();
        String excelName=queryParam.getExcelName();
        if(StringUtil.isEmpty(pageUniqueName)||StringUtil.isEmpty(userId)||StringUtil.isEmpty(excelType)||StringUtil.isEmpty(tenantId)||StringUtil.isEmpty(orgId)||StringUtil.isEmpty(type)){
            throw new SystemException("-2", "模版下载失败参数不正确");
        }
        moiraiExtService.createExcelTemplate(queryParam);
    }

    /**
     * <B>方法名称：</B>机构excel下载<BR>
     * <B>概要说明：</B>模版文件放在了代码里<BR>
     *
     * @return
     * @since 2019年5月20日
     */
    @Override
    public void downloadOrgTemplate(HttpServletRequest request, HttpServletResponse response) {
        String fileName = "机构导入模板.xls";
        String path = "templates/operateOrgTemplate.xls";
        moiraiSysService.downloadTemplate(request, response, fileName, path);
    }

    /**
     * <B>方法名称：</B>租户excel下载<BR>
     * <B>概要说明：</B>模版文件放在了代码里<BR>
     *
     * @return
     * @since 2019年5月29日
     */
    @Override
    public void downloadTenantTemplate(HttpServletRequest request, HttpServletResponse response) {
        String fileName = "租户导入模板.xls";
        String path = "templates/tenantTemplate.xls";
        moiraiSysService.downloadTemplate(request, response, fileName, path);
    }

    /**
     * <B>方法名称：</B>前端调用下载Excel错误信息<BR>
     * <B>概要说明：</B>大客户本地部署使用<BR>
     *
     * @return
     * @since 2019年5月20日
     */
    @Override
    public void downloadString(@RequestParam("path") String path, @RequestParam("orgId") Long orgId,
        HttpServletResponse response) {
        if (StringUtils.isEmpty(path) || orgId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiSysService.downloadString(path, orgId, response);
    }

    public void downloadUserAuthTemplate(@RequestParam("userId") Long userId, HttpServletResponse response) {
        if (userId == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiUserExtraService.downloadUserAuthTemplate(userId, response);
    }
}
