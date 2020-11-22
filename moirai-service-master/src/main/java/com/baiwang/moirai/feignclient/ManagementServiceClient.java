package com.baiwang.moirai.feignclient;

import com.baiwang.cloud.common.model.BWJsonResult;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 渠道服务
 */
@FeignClient(value = "SERVICE-MANAGEMENT")
public interface ManagementServiceClient {

    @RequestMapping(value = "/channel/getChannelByList", method = RequestMethod.POST)
    BWJsonResult<Map<String, String>> batchWorkorder(@RequestBody List<Long> channelList);
}
