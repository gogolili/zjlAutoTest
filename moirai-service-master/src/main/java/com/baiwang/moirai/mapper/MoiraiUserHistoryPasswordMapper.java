package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.user.MoiraiUserHistoryPassword;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author LC
 * @date 2019/12/6 16:23
 */
@Service
@Mapper
public interface MoiraiUserHistoryPasswordMapper {


    /**
     * 插入用户历史密码id
     *
     * @param moiraiUserHistoryPassword
     */
    int insert(MoiraiUserHistoryPassword moiraiUserHistoryPassword);

    /**
     * 根据主键删除数据
     *
     * @param id
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 根据用户id 查询历史密码
     *
     * @param userId
     */
    List<MoiraiUserHistoryPassword> selectListByUserId(Long userId);


}
