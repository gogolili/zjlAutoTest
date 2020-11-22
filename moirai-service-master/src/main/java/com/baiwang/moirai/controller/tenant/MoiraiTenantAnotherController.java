package com.baiwang.moirai.controller.tenant;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.moirai.api.MoiraiTenantAnotherSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.service.MoiraiTenantAnotherService;
import com.baiwang.moirai.utils.StrUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@SuppressWarnings("all")
public class MoiraiTenantAnotherController implements MoiraiTenantAnotherSvc {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiTenantAnotherController.class);

    @Autowired
    private MoiraiTenantAnotherService moiraiTenantAnotherService;

    /**
     * 参数请求方式:{"tenantId":["1000000000003","1000000000002"]}
     */
    @Override
    public BWJsonResult<MoiraiTenant> batchQueryTenant(@RequestBody Map<String, List<String>> tenantIds) {

        List<String> ids = (List<String>) tenantIds.get("tenantId");
        logger.info("ids={}" + ids);
        if (StrUtils.isEmptyList(ids) || ids.size() == 0) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        List<MoiraiTenant> batchQueryTenant = moiraiTenantAnotherService.batchQueryTenant(ids);
        return new BWJsonResult<MoiraiTenant>(batchQueryTenant);
    }

    /**
     * <B>方法名称：</B>租户excel导入<BR>
     * <B>概要说明：</B>校验全部Excel内容，并把错误信息上传至oss，待用户下载，否则导入成功<BR>
     *
     * @return
     * @since 2019年5月29日
     */
    @Override
    public BWJsonResult uploadTenantExcel(
            @RequestParam(value = "tenantExcelFile", required = false) MultipartFile excelFile,
            HttpServletRequest request) {
        String creater = request.getParameter("creater");
        String qdBm = request.getParameter("qdBm");
        String qdBms = request.getParameter("qdBms");
        String roles = request.getParameter("roles");
        List<Long> qdBmList = new ArrayList<>();
        if (StringUtils.isNotEmpty(qdBms)) {
            String[] temp = qdBms.split(",");
            for (int i = 0; i < temp.length; i++) {
                qdBmList.add(Long.valueOf(temp[i]));
            }
        }
        if (StringUtils.isNotEmpty(qdBm)) {
            qdBmList.add(Long.valueOf(qdBm));
        }
        List<Long> prIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(roles)) {
            String[] roleTemp = roles.split(",");
            for (int i = 0; i < roleTemp.length; i++) {
                prIdList.add(Long.valueOf(roleTemp[i]));
            }
        }
        logger.info("************creater:{}；qdBmList:{};productRoleIdList:{}*************",
                creater, JSONObject.toJSONString(qdBmList), JSONObject.toJSONString(prIdList));
        if (StringUtils.isEmpty(creater) || qdBmList.isEmpty()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        // 文件不存在的情况
        if (excelFile == null || StringUtils.isEmpty(excelFile.getOriginalFilename())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_EXCELFILE_ERROR);
        }
        BWJsonResult readExcelRel = moiraiTenantAnotherService.readExcelRel(excelFile, creater, qdBmList, prIdList);
        return readExcelRel;
    }
}
