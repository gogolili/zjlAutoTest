package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.role.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author 程路超
 */
public interface MoiraiResourcesService {

    int addResources(MoiraiResourceCondition resourceCondition);

    BWJsonResult<MoiraiResource> queryResourcesTree(MoiraiRole moiraiRole);

    MoiraiResource queryResourcesInfo(MoiraiResourceCondition moiraiResourceCondition);

    int editResources(MoiraiResourceCondition moiraiResource);

    int delResources(MoiraiResourceCondition moiraiResource);

    String getOrgResourceFilterType(MoiraiOrg org);

    List<MoiraiResource> queryResourceTreeByProduct(MoiraiProductCondition productCondition);

    BWJsonResult<MoiraiResource> queryResourceList(MoiraiResourceCondition resourceCondition);

    List<MoiraiResource> queryResourceTreeNoButton(MoiraiProductCondition productCondition);

    List<MoiraiResource> selectTenantProductResourceList(Long productId, String resFilterType, Long tenantId);

    void recoveryResource(MoiraiProductCondition productCondition);

    /**
     * 根据产品id查询资源信息
     */
    List<MoiraiResource> selectProductRoleResourceList(MoiraiRole product);

    // 查询所有按钮资源信息
    List<HashMap> queryButtonResource();
}
