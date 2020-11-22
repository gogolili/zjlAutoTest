package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.user.MoiraiSysUsers;
import com.baiwang.moirai.model.user.MoiraiUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
 * @Author: liuzhenyun
 * @Date: 2019-08-31 08:07
 * @Description:
 */
@Service
@Mapper
public interface MoiraiSysUsersMapper {
    int deleteByPrimaryKey(Long id);

    int deleteByUserAccount(String userAccount);

    int insert(MoiraiSysUsers record);

    int insertSelective(MoiraiSysUsers record);

    MoiraiSysUsers selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MoiraiSysUsers record);

    int updateByPrimaryKey(MoiraiSysUsers record);

    MoiraiUser getBwUser(String sysUserId);

    MoiraiSysUsers selectByUserAccount(String userAccount);
}