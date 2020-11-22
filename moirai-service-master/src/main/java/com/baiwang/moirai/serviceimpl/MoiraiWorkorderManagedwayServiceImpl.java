package com.baiwang.moirai.serviceimpl;

import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiWorkorderManagedwayMapper;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedway;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderManagedwayCondition;
import com.baiwang.moirai.service.MoiraiWorkorderManagedwayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author LC
 * @date 2020/6/24 10:15
 */
@Service
public class MoiraiWorkorderManagedwayServiceImpl implements MoiraiWorkorderManagedwayService {

    @Autowired
    private MoiraiWorkorderManagedwayMapper managedwayMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    /**
     * 托管方式变更申请提交
     */
    @Override
    public void submit(MoiraiWorkorderManagedway managedway) {
        if (managedway.getBeforeTgType().equals(managedway.getAfterTgType())) {
            throw new MoiraiException("-1", "变更前后托管方式不能相同!");
        }
        if ((Constants.DEFAULT_TWO.equals(managedway.getAfterTgType())
                && Constants.DEFAULT_THREE.equals(managedway.getBeforeTgType()))
                || (Constants.DEFAULT_THREE.equals(managedway.getAfterTgType())
                && Constants.DEFAULT_TWO.equals(managedway.getBeforeTgType()))) {
            // 不能在小智自持和企业自持之间切换
            throw new MoiraiException("-1", "不支持在托管方式在“小智自持”和“企业自持（单盘）之间变更");
        }

        managedway.setId(seqnumFeignClient.getSeq().toString());
        managedway.setCreateTime(new Date());
        managedway.setAuditStatus(Constants.DEFAULT_ONE);
        managedwayMapper.insertSelective(managedway);
    }

    /**
     * 查询列表
     */
    @Override
    public List<MoiraiWorkorderManagedway> list(MoiraiWorkorderManagedwayCondition managedwayCondition) {
        return managedwayMapper.selectList(managedwayCondition);
    }

    /**
     * 审核工单
     */
    @Override
    public void update(MoiraiWorkorderManagedway managedway) {
        managedway.setAuditTime(new Date());
        managedwayMapper.updateByPrimaryKeySelective(managedway);
    }
}
