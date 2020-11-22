package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedway;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedwayCondition;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface MoiraiWorkorderManagedwayMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(MoiraiWorkorderManagedway record);

    MoiraiWorkorderManagedway selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MoiraiWorkorderManagedway record);

    List<MoiraiWorkorderManagedway> selectList(MoiraiWorkorderManagedwayCondition record);

}