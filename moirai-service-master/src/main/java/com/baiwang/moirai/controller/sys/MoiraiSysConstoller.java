package com.baiwang.moirai.controller.sys;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.api.MoiraiSysSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysProvCityDist;
import com.baiwang.moirai.model.sys.SysProvCityDistTree;
import com.baiwang.moirai.model.sys.TenantSysDict;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.SysDictService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 角色相关
 *
 * @author 程路超
 */
@RestController
@SuppressWarnings("all")
public class MoiraiSysConstoller implements MoiraiSysSvc {
    private Logger logger = LoggerFactory.getLogger(MoiraiSysConstoller.class);

    @Autowired
    private MoiraiSysService moiraiSysService;

    @Autowired
    private SysDictService sysDictService;

    @Override
    public BWJsonResult<SysProvCityDist> getSysProvCityDist(@RequestBody SysProvCityDist sysProvCityDist) {
        if (sysProvCityDist == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<SysProvCityDist> dist = moiraiSysService.getSysProvCityDist(sysProvCityDist);
        return new BWJsonResult(dist);
    }

    @Override
    public BWJsonResult<SysProvCityDist> getSysProvCityDistTree() {
        BWJsonResult<SysProvCityDist> bwJsonResult;
        List<SysProvCityDistTree> trees;
        try {
            trees = moiraiSysService.getSysProvCityDistTree();
        } catch (Exception e) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR);
        }

        bwJsonResult = new BWJsonResult(trees, trees.size());

        return bwJsonResult;
    }

    @Override
    public BWJsonResult<SysDict> getSysDict(@RequestBody SysDict sysDict) {
        List<SysDict> sysDictList = moiraiSysService.getSysDict(sysDict);
        BWJsonResult<SysDict> bwJsonResult = new BWJsonResult(sysDictList, sysDictList.size());
        return bwJsonResult;
    }

    /**
     * 根据字典类型获取字典明细列表（缓存）
     *
     * @param sysDict
     */
    @Override
    public BWJsonResult<SysDict> getSysDictCacheByDictType(@RequestBody SysDict sysDict) {
        return BWJsonResult.success(sysDictService.getSysDictCacheByDictType(sysDict.getDictType()));
    }

    /**
     * 清除字典类型明细列表（缓存）
     *
     * @param sysDict
     */
    @Override
    public BWJsonResult clearSysDictCache(@RequestBody(required = false) SysDict sysDict) {
        sysDictService.clearSysDictCache(sysDict != null ? sysDict.getDictType() : null);
        return BWJsonResult.success();
    }

    /**
     * 查询租户字典信息
     */
    @Override
    public BWJsonResult<SysDict> getTenantSysDict(@RequestBody TenantSysDict sysDict){
        if (sysDict.getTenantId() == null || StringUtils.isBlank(sysDict.getDictType())){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return BWJsonResult.success(sysDictService.getTenantSysDict(sysDict));
    }

    @Override
    public BWJsonResult<SysDict> getSysDictBatch(@RequestBody List<String> dictTypes) {
        if (dictTypes == null || dictTypes.size() == 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<SysDict> sysDictList = moiraiSysService.getSysDictBatch(dictTypes);
        if (sysDictList == null || sysDictList.size() == 0) {
            return new BWJsonResult<>();
        }
        return new BWJsonResult<>(sysDictList);
    }

    @Override
    public BWJsonResult uploadUserCenterfile(@RequestParam(value = "pictureFile", required = false) MultipartFile pictureFile, HttpServletRequest request) {
        // 上传时生成的临时文件保存目录
        String tempPath = request.getSession().getServletContext().getRealPath("/temp");
        if (!ServletFileUpload.isMultipartContent(request)) {
            logger.info("文件不可为空");
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult bwJsonResult = moiraiSysService.uploadFile(pictureFile, tempPath);
        return bwJsonResult;
    }

    @RequestMapping(value = "/judgeUserAuth")
    public BWJsonResult judgeUserAuth(@RequestBody MoiraiUser moiraiUser) {
        Long userId = moiraiUser.getUserId();
        Long orgId = moiraiUser.getOrgId();
        Long tenantId = moiraiUser.getTenantId();
        if (userId == null || (orgId == null && tenantId == null)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiUserAuthz> authzs = moiraiSysService.getAuthzs(userId, orgId, tenantId);
        if (authzs == null || authzs.size() == 0) {
            BWJsonResult bwJsonResult = new BWJsonResult();
            bwJsonResult.setSuccess(false);
            return bwJsonResult;
        }
        return new BWJsonResult();
    }

}
