package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;

import java.util.*;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Mapper
public interface MoiraiOrgMapper {

    MoiraiOrg selectOneOrg(MoiraiOrg moiraiOrg);

    List<MoiraiOrg> selectMoreOrg(MoiraiOrg moiraiOrg);

	List<MoiraiOrg> selectOrgBatcher(MoiraiOrgCondition moiraiOrgCondition);

    List<MoiraiOrg> selectOrgByAuth(MoiraiOrgCondition moiraiOrgCondition);

    /**更新组织机构**/
    int updateOrg(MoiraiOrg moiraiOrg);

    /**添加非纳税主体组织机构**/
    int addNoTaxOrg(MoiraiOrg moiraiOrg);

    /**添加纳税主体组织机构**/
    int addTaxOrg(MoiraiOrg moiraiOrg);

    /**查询机构的组织机构树**/
    List<MoiraiOrg> queryOrgTreeByTenant(Long tenantId);

    /**查询机构的组织机构树-精简展示字段**/
    List<MoiraiOrg> queryOrgSimpleTreeByTenant(Long tenantId);

    /**删除组织机构**/
    int deleteOrg(Long orgId);

    /**查询租户拥有的税号数**/
    int queryTenantTaxCodeTotal(Long tenantId);

    /**查询组织机构列表，也是纳税主体列表**/
    List<MoiraiOrg> queryOrgListPage(MoiraiOrgCondition moiraiOrgCondition);

    /**查询组织机构列表，也是纳税主体列表**/
    List<MoiraiOrg> queryOrgListPage2(MoiraiOrgCondition moiraiOrgCondition);

    List<MoiraiOrg> queryOrgByCondition(MoiraiOrgCondition moiraiOrgCondition);

    /**批量查询组织机构*/
    List<MoiraiOrg> queryOrgListByCondition(Map<String, List> moiraiOrgList);

    /**修改组织机构税号或者名称*/
    int updateOrgTaxName(MoiraiOrg moiraiOrg);

    /**批量添加组织机构，纳税和非纳税主体**/
    int addOrgList(List<MoiraiOrg> moiraiOrgList);
    
    int updateOrgSwitchState(MoiraiOrg moiraiOrg);

    List<Map<String, Object>> findTaxCodeListInfo(Map<String, Object> map);

    List<MoiraiOrg> selectOrgBytaxCode(List<MoiraiOrg> moiraiOrgs);

    List<MoiraiOrg> selectOrgByOrgCode(@Param("orgList") List<MoiraiOrg> orgList, @Param("tenantId") Long tenantId);

    /**
     * 查询顶级组织机构
     * 运营后台租户查询
     */
    List<MoiraiOrg> selectTopOrgListByTenantIdList(List<Long> tenandIdList);

    /**
     * 批量查询租户组织机构数
     * 运营后台租户查询
     */
    List<Map<String, Object>> queryTenantTaxCodeTotalList(List<Long> tenandIdList);

    /**
     * 查询组织机构信息 根据渠道编码
     * @param moiraiOrg
     * @return
     */
    List<MoiraiOrg> queryChannelOrg(MoiraiOrgCondition moiraiOrg);
}