package com.baiwang.moirai.feignclient;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 工单服务
 */
@FeignClient(value = "SERVICE-WORKORDER")
public interface WorkorderServiceClient {

    @RequestMapping(value = "/portalApplySeal/batchWorkorder", method = RequestMethod.POST)
    BWJsonResult batchWorkorder(@RequestBody List<MoiraiOrg> orgList);
}
