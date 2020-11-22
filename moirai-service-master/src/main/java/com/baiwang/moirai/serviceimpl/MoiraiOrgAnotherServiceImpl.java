package com.baiwang.moirai.serviceimpl;

import com.baiwang.moirai.mapper.MoiraiOrgConfigMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiOrgProductMapper;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgConfig;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.service.MoiraiOrgAnotherService;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoiraiOrgAnotherServiceImpl implements MoiraiOrgAnotherService {

    @Autowired
    private MoiraiOrgConfigMapper moiraiOrgConfigMapper;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiOrgProductMapper moiraiOrgProductMapper;

    public BWJsonResult<MoiraiOrgConfig> orgConfigFindService(MoiraiOrgCondition moiraiOrgCondition) {

        PageHelper.startPage(moiraiOrgCondition.getPageNo(), moiraiOrgCondition.getPageSize());
        List<MoiraiOrgConfig> orgConfigList = moiraiOrgConfigMapper.selectByItemOrValue(moiraiOrgCondition);
        PageInfo<MoiraiOrgConfig> pageInfo = new PageInfo<>(orgConfigList);
        return new BWJsonResult(orgConfigList, (int)pageInfo.getTotal());
    }

    public int getOrgConfigCount(MoiraiOrgCondition moiraiOrgCondition) {
        List<MoiraiOrgConfig> countByItems = moiraiOrgConfigMapper.selectByItemOrValue(moiraiOrgCondition);
        int size = 0;
        if (countByItems != null) {
            size = countByItems.size();
        }
        return size;
    }

    @Override
    public void deleteOrgConfig(MoiraiOrgConfig config) {
        moiraiOrgConfigMapper.deleteByParam(config);
    }

    public Map<String, Object> getOrgBySwitchState(MoiraiOrgCondition moiraiOrgCondition) {
        String isFetch = moiraiOrgCondition.getIsFetch();
        if (!StrUtils.isEmpty(isFetch)) {
            moiraiOrgCondition.setOrgType(1);
        }
        PageHelper.startPage(moiraiOrgCondition.getPageNo(), moiraiOrgCondition.getPageSize());
        List<MoiraiOrg> orgList = moiraiOrgMapper.queryOrgByCondition(moiraiOrgCondition);
        List<MoiraiOrg> org = new ArrayList<>();
        for (MoiraiOrg moiraiOrg : orgList) {
            MoiraiOrgProduct moiraiOrgProduct = new MoiraiOrgProduct();
            moiraiOrgProduct.setOrgId(moiraiOrg.getOrgId());
            moiraiOrgProduct.setProductId(1L);
            List<MoiraiOrgProduct> findOrgProductByCondition = moiraiOrgProductMapper.findOrgProductByCondition(moiraiOrgProduct);
            if (!StrUtils.isEmptyList(findOrgProductByCondition)) {
                org.add(moiraiOrg);
            }
        }
        PageInfo<MoiraiOrg> pageInfo = new PageInfo<>(orgList);
        Long total = pageInfo.getTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("orgList", orgList);
        return map;
    }

}
