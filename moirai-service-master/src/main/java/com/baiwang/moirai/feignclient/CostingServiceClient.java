package com.baiwang.moirai.feignclient;

import java.util.Map;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "COSTING-SERVICE")
@RequestMapping(path = "/costing-service")
public interface CostingServiceClient {
    @RequestMapping(value = "/addDeveloperCostingRule")
    int addDeveloperCostingRule(@RequestParam Map<String, Object> developerRule);

}
