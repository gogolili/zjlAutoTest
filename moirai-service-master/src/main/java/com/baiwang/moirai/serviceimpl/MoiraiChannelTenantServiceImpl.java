package com.baiwang.moirai.serviceimpl;

import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiChannelTenantMapper;
import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;
import com.baiwang.moirai.service.MoiraiChannelTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author LC
 * @date 2020/2/28 12:47
 */
@Service
public class MoiraiChannelTenantServiceImpl implements MoiraiChannelTenantService {

    @Autowired
    private MoiraiChannelTenantMapper moiraiChannelTenantMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    /**
     * 批量添加渠道租户信息
     */
    @Override
    public void addChannelTenantList(Long tenantId,List<Long> qdbmList) {
        MoiraiChannelTenant query = new MoiraiChannelTenant();
        query.setTenantId(tenantId);
        List<MoiraiChannelTenant> exQdbms = moiraiChannelTenantMapper.queryList(query);
        boolean exFlag = false;
        List<MoiraiChannelTenant> addList = new ArrayList<>();
        for (int i = 0; i < qdbmList.size(); i++) {
            for (int k = 0; k < exQdbms.size(); k++) {
                if (exQdbms.get(k).getQdBm().equals(qdbmList.get(i))) {
                    exFlag = true;
                    break;
                }
            }
            if (!exFlag) {
                MoiraiChannelTenant addItem = new MoiraiChannelTenant();
                addItem.setChannelTenantId(seqnumFeignClient.getNum(Constants.MOIRAI_CHANNEL_TENANT));
                addItem.setTenantId(tenantId);
                addItem.setQdBm(qdbmList.get(i));
                addList.add(addItem);
            }
        }
        if (!addList.isEmpty()) {
            moiraiChannelTenantMapper.insertList(addList);
        }
    }
}
