package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.tenant.MoiraiTenantListVO;
import com.baiwang.moirai.model.tenant.MoiraiTenantRelation;
import com.baiwang.moirai.model.tenant.MoiraiTenantRelationVo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 处理租户--租户之间关系
 */
@Mapper
@Service
public interface MoiraiTenantRelationMapper {

    /**
     * 查询上游租户下绑定的所有下游租户
     */
    public List<MoiraiTenantRelation> selectDownTenant(MoiraiTenantRelation miraiTenantRelation);

    /**
     * 查询下游租户所有税号及机构名称
     * @param list
     * @param upperTenantId 下游租户ID
     * @return
     */
    public List<MoiraiTenantRelationVo> selectDownTaxCode(@Param("list") List<MoiraiTenantRelation> list, @Param("upperTenantId")BigInteger upperTenantId);

    /**
     * 插入绑定关系
     * @param moiraiTenantRelation
     * @return
     */
    public int insertTenantRelation(MoiraiTenantRelation moiraiTenantRelation);

    /**
     * 更新绑定关系
     * @param moiraiTenantRelation
     * @return
     */
    public int updateTenantRelation(MoiraiTenantRelation moiraiTenantRelation);

    /**
     * 查找绑定关系
     * @param moiraiTenantRelation
     * @return
     */
    public Page selectTenantRelation(MoiraiTenantRelation moiraiTenantRelation);

    /**
     * 查询组织机构ID与租户ID对应关系
     * @param orgId 组织架构ID
     * @return
     */
    public MoiraiTenantListVO selectTenantId(BigDecimal orgId);
}
