package com.baiwang.moirai.controller.role;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.api.MoiraiResourcesSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.role.*;
import com.baiwang.moirai.service.MoiraiResourcesService;
import com.baiwang.moirai.service.MoiraiRoleService;
import com.baiwang.moirai.utils.AdminUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 产品相关
 *
 * @author 程路超
 */
@RestController
@SuppressWarnings("all")
public class MoiraiResourcesController implements MoiraiResourcesSvc {
    private Logger logger = LoggerFactory.getLogger(MoiraiResourcesController.class);

    @Autowired
    private MoiraiResourcesService moiraiResourcesService;

    @Autowired
    private MoiraiRoleService moiraiRoleService;

    /**
     * <B>方法名称：</B>查询资源树<BR>
     * <B>概要说明：</B>添加角色和角色资源详情使用<BR>
     *
     * @return
     * @since Long before
     */
    @Override
    public BWJsonResult<MoiraiResource> queryResourcesTree(@RequestBody MoiraiRole getTree) {
        BWJsonResult<MoiraiResource> productResource;
        if (getTree != null && getTree.getRoleId() != null) {
            MoiraiRole roleResource = moiraiRoleService.queryRolesInfo(getTree);
            List<MoiraiResource> resourceList = roleResource.getResourceList();
            productResource = new BWJsonResult(resourceList, resourceList.size());
        } else {
            productResource = moiraiResourcesService.queryResourcesTree(getTree);
        }
        return productResource;
    }

    /**
     * <B>方法名称：</B>添加资源<BR>
     * <B>概要说明：</B>页面单条添加<BR>
     *
     * @return
     * @since 2019年7月01日
     */
    @Override
    @UserCenterOperationLog(moduleName = "产品管理",action = "添加资源" ,description = "添加资源")
    public BWJsonResult addResources(@RequestBody MoiraiResourceCondition resourceCondition) {
        if (resourceCondition == null || StringUtils.isBlank(resourceCondition.getResourceName()) ||
            resourceCondition.getOrderIndex() == null || StringUtils.isBlank(resourceCondition.getCreater()) ||
            resourceCondition.getProductId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        int i = moiraiResourcesService.addResources(resourceCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("添加资源成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>资源详情<BR>
     * <B>概要说明：</B>编辑时回显<BR>
     *
     * @return
     * @since 2019年7月01日
     */
    @Override
    public BWJsonResult<MoiraiResource> queryResourcesInfo(
        @RequestBody MoiraiResourceCondition moiraiResourceCondition) {
        if (moiraiResourceCondition == null || moiraiResourceCondition.getResourceId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiResource resource = moiraiResourcesService.queryResourcesInfo(moiraiResourceCondition);
        BWJsonResult<MoiraiResource> bwJsonResult = new BWJsonResult<>(resource);
        bwJsonResult.setMessage("查询资源详情成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>编辑资源<BR>
     * <B>概要说明：</B>编辑<BR>
     *
     * @return
     * @since 2019年7月01日
     */
    @Override
    @UserCenterOperationLog(moduleName = "产品管理",action = "编辑资源" ,description = "编辑资源")
    public BWJsonResult editResources(@RequestBody MoiraiResourceCondition moiraiResource) {
        if (moiraiResource == null || moiraiResource.getResourceId() == null || StringUtils.isBlank(moiraiResource.getModifyUser())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        if (moiraiResource.getSource().equals("0") && moiraiResource.getTenantId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        int i = moiraiResourcesService.editResources(moiraiResource);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("编辑资源成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>删除资源<BR>
     * <B>概要说明：</B>删除<BR>
     *
     * @return
     * @since 2019年7月01日
     */
    @Override
    @UserCenterOperationLog(moduleName = "产品管理",action = "删除资源" ,description = "删除资源")
    public BWJsonResult delResources(@RequestBody MoiraiResourceCondition moiraiResource) {
        if (moiraiResource == null || moiraiResource.getResourceId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        int i = moiraiResourcesService.delResources(moiraiResource);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("删除资源成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>查询产品资源<BR>
     * <B>概要说明：</B>分配菜单时显示资源树<BR>
     *
     * @return
     * @since 2019年7月01日
     */
    @Override
    public BWJsonResult<MoiraiResource> queryResourceTreeByProduct(
        @RequestBody MoiraiProductCondition productCondition) {
        if (productCondition == null || productCondition.getProductId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiResource> resourceTree = moiraiResourcesService.queryResourceTreeByProduct(productCondition);
        BWJsonResult<MoiraiResource> bwJsonResult = new BWJsonResult<>(resourceTree);
        bwJsonResult.setMessage("查询产品资源树成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>查询资源列表<BR>
     * <B>概要说明：</B>点击资源树查询当前及当前下级资源列表<BR>
     *
     * @param resourceCondition
     * @return
     * @since 2019年7月18日
     */
    @Override
    public BWJsonResult<MoiraiResource> queryResourceList(@RequestBody MoiraiResourceCondition resourceCondition) {
        if (resourceCondition == null || resourceCondition.getProductId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        BWJsonResult<MoiraiResource> bwJsonResult = moiraiResourcesService.queryResourceList(resourceCondition);
        return bwJsonResult;
    }

    /**
     * 查询产品资源信息
     */
    @PostMapping("/selectProductRoleResourceTree")
    public BWJsonResult<MoiraiResource> selectProductRoleResourceTree(@RequestBody MoiraiRole role) {
        List<MoiraiResource> res = moiraiResourcesService.selectProductRoleResourceList(role);
        return BWJsonResult.success(res);
    }

    /**
     * <B>方法名称：</B>查询资源树，没有按钮<BR>
     * <B>概要说明：</B>添加资源时选择上级菜单<BR>
     *
     * @param productCondition
     * @return
     * @since 2019年7月25日
     */
    @Override
    public BWJsonResult<MoiraiResource> queryResourceTreeNoButton(
        @RequestBody MoiraiProductCondition productCondition) {
        if (productCondition == null || productCondition.getProductId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<MoiraiResource> resourceTree = moiraiResourcesService.queryResourceTreeNoButton(productCondition);
        BWJsonResult<MoiraiResource> bwJsonResult = new BWJsonResult<>(resourceTree);
        bwJsonResult.setMessage("查询产品资源树成功");
        return bwJsonResult;
    }

    /**
     * 查询按钮资源
     */
    @PostMapping("/queryButtonResource")
    public BWJsonResult<HashMap> queryButtonResource() {
        List<HashMap> list = moiraiResourcesService.queryButtonResource();
        return new BWJsonResult<>(list);
    }

    /**
     * <B>方法名称：</B>一键还原菜单<BR>
     * <B>概要说明：</B>租户分配资源页面<BR>
     *
     * @param productCondition
     * @return
     * @since 2019年7月26日
     */
    @Override
    public BWJsonResult recoveryResource(@RequestBody MoiraiProductCondition productCondition) {
        if (productCondition == null || productCondition.getProductId() == null || productCondition.getTenantId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiResourcesService.recoveryResource(productCondition);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("还原菜单成功");
        return bwJsonResult;
    }
}
