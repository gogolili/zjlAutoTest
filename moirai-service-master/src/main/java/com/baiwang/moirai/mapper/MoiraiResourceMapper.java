package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiProductCondition;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiResourceCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface MoiraiResourceMapper {

    List<MoiraiResource> selectAllPermissionResource();

    int deleteByPrimaryKey(Long resourceId);

    int addBatch(List<MoiraiResource> record);

    int insertSelective(MoiraiResource record);

    MoiraiResource selectByPrimaryKey(Long resourceId);

    List<MoiraiResource> selectBatchInfo(List<Long> idList);

    List<MoiraiResource> selectBatchInfoUsed(List<Long> idList);

    List<MoiraiResource> selectProductResourceList(Map productFliter);

    List<MoiraiResource> selectByBean(MoiraiResource resource);

    int updateByPrimaryKeySelective(MoiraiResource record);

    List<MoiraiResource> queryResourceList(MoiraiResourceCondition resourceCondition);

    List<MoiraiResource> selectResourceNoButton(MoiraiProduct product);

    List<HashMap> selectButtonResource();
    /** ===========  租户资源查询  ============ **/

    List<MoiraiResource> selectTenantResourceByBean(MoiraiResource moiraiResource);

    int insertResourceTenant(MoiraiResource resource);

    int addResourceTenantBatch(List<MoiraiResource> resourceList);

    int updateResourceTenant(MoiraiResource resource);

    int deleteResourceTenant(@Param("tenantId") Long tenantId, @Param("resourceId") Long resourceId);

    List<MoiraiResource> selectResourceTenantBatch(@Param("list") List<Long> resourceId,
        @Param("tenantId") Long tenantId);

    List<MoiraiResource> queryResourceTenantList(MoiraiResourceCondition resourceCondition);

    List<MoiraiResource> selectResourceTenantNoButton(MoiraiProductCondition productCondition);

    List<MoiraiResource> selectTenantProductResourceList(Map productFliter);

    List<MoiraiResource> queryResourceTenantIds(Long productId);

    int batchDeleteResourceTenant(@Param("tenantId") Long tenantId, @Param("resourceIds") List<Long> resourceIds);

    List<MoiraiResource> selectTenantProductResource(@Param("productId") Long productId,
        @Param("tenantId") Long tenantId);
}