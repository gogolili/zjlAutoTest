/*
 * @项目名称: Moirai
 * @文件名称: MoiraiOrgZomMapper.java
 * @Date: 17-11-4 下午3:58
 * @author Lance cui
 *
 */

package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper
public interface MoiraiOrgZomMapper {

    /**根据税号查询组织机构信息**/
    MoiraiOrg selectByTaxCode(String taxCode);

    /**根据名称查询组织机构信息**/
    List<MoiraiOrg> selectByOrgName(String orgName);

    /**逻辑删除**/
    void deleteLogic(MoiraiOrg zombieOrg);

    int deleteByPrimaryKey(Long orgId);

    int insertSelective(MoiraiOrg record);


    MoiraiOrg selectByPrimaryKey(Long orgId);

    int updateByPrimaryKeySelective(MoiraiOrg record);
}