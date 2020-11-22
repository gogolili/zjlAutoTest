package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.user.MoiraiUserLoginHistory;
import com.baiwang.moirai.model.user.MoiraiUserLoginHistoryCondition;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户登录历史相关操作
 *
 * @author sxl
 */
@Mapper
@Service
public interface MoiraiUserLoginHistoryMapper {

    /**
     * 查询最后一次登录机构
     *
     * @param moiraiUserLoginHistory
     * @return
     */
    List<MoiraiUserLoginHistory> selectLastLogin(MoiraiUserLoginHistory moiraiUserLoginHistory);

    /**
     * 插入最后一次登录
     *
     * @param moiraiUserLoginHistory
     * @return
     */
    int insetLastLogin(MoiraiUserLoginHistory moiraiUserLoginHistory);

    /**
     * 更新登录时间
     *
     * @param moiraiUserLoginHistory
     * @return
     */
    int updateLastLogin(MoiraiUserLoginHistory moiraiUserLoginHistory);

    /**
     * 根据client分组统计
     */
    List<Map> selectClientCount(MoiraiUserLoginHistoryCondition query);

    List<MoiraiUserLoginHistoryCondition> selectLoginList(MoiraiUserLoginHistoryCondition query);
}
