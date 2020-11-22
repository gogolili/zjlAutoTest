package com.baiwang.moirai.feignclient;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "defense-service")
public interface DefenseServiceClient {

    @RequestMapping(method = RequestMethod.GET, value = "/checkCert")
    String checkToken(@RequestParam("cert") String cert);
}
