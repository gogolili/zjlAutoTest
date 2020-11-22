package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgConfig;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface MoiraiOrgConfigMapper {

    int deleteByPrimaryKey(Long ocId);

    int deleteByParam(MoiraiOrgConfig config);

    /** 新增组织机构配置信息 **/
    int insertSelective(MoiraiOrgConfig record);

    /** 更新组织机构配置信息 **/
    int updateByPrimaryKeySelective(MoiraiOrgConfig record);

    /** 根据组织机构ID和item项查询配置信息 **/
    MoiraiOrgConfig selectByOrgItem(MoiraiOrgConfig moiraiOrgConfig);

    /** 根据组织机构所有配置信息 **/
    List<MoiraiOrgConfig> selectByOrgId(Long orgId);

    List<MoiraiOrgConfig> selectByItemOrValue(MoiraiOrgCondition moiraiOrgConfig);

}