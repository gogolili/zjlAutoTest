package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccount;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccountCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface MoiraiWorkorderAccountMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(MoiraiWorkorderAccount record);

    MoiraiWorkorderAccount selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MoiraiWorkorderAccount record);

    List<MoiraiWorkorderAccount> queryList(MoiraiWorkorderAccountCondition workorderPwd);
}