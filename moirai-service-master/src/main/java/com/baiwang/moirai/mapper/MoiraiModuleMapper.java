package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiModuleService;
import com.baiwang.moirai.model.role.MoiraiModuleServiceCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface MoiraiModuleMapper {

    int deleteByPrimaryKey(Long id);

    int insertSelective(MoiraiModuleService moduleService);

    MoiraiModuleService selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MoiraiModuleService moduleService);

    List<MoiraiModuleService> selectListByCondition(MoiraiModuleServiceCondition serviceCondition);
}