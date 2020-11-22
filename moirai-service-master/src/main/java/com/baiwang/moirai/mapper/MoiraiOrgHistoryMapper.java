/*
 * @项目名称: Moirai
 * @文件名称: MoiraiOrgHistoryMapper.java
 * @Date: 17-11-17 下午8:45
 * @author Lance cui
 *
 */

package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgHistory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper
public interface MoiraiOrgHistoryMapper {

    int deleteByPrimaryKey(Long hisId);

    int insert(MoiraiOrgHistory record);

    int insertSelective(MoiraiOrgHistory record);

    MoiraiOrgHistory selectByPrimaryKey(Long hisId);

    int updateByPrimaryKeySelective(MoiraiOrgHistory record);

    int updateByPrimaryKey(MoiraiOrgHistory record);

    List<MoiraiOrgHistory> selectByHistoryTaxName(MoiraiOrg moiraiOrg);
}