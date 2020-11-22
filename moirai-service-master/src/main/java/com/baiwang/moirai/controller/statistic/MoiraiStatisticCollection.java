package com.baiwang.moirai.controller.statistic;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserLoginHistoryCondition;
import com.baiwang.moirai.service.MoiraiStatisticService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户中心监控
 *
 * @author LC
 * @date 2020/8/7 16:20
 */
@RestController
@RequestMapping("/statistic")
public class MoiraiStatisticCollection {

    @Autowired
    private MoiraiStatisticService statisticService;

    /**
     * 查询用户登陆统计信息
     */
    @PostMapping("/queryLoginCount")
    public BWJsonResult<Map> queryLoginCount(@RequestBody MoiraiUserLoginHistoryCondition query) {
        if (query.getLoginBeginTime() == null || query.getLoginEndTime() == null){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        List<Map> list = statisticService.queryLogin(query);
        return new BWJsonResult<>(list);
    }

    /**
     * 查询用户登陆列表
     */
    @PostMapping("/queryLoginList")
    public BWJsonResult<MoiraiUserLoginHistoryCondition> queryLoginList(@RequestBody MoiraiUserLoginHistoryCondition query){
        if (query.getLoginBeginTime() == null || query.getLoginEndTime() == null){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return statisticService.queryLoginList(query);
    }
}
