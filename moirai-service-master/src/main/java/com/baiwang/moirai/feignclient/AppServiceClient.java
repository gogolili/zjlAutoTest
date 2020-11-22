package com.baiwang.moirai.feignclient;

import com.baiwang.cloud.common.model.BWJsonResult;
import java.util.Map;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 查询开放平台相关接口
 */
@FeignClient(value = "app-service")
public interface AppServiceClient {

    /**
     * 注册时立即成为开发者
     */
    @RequestMapping(value = "/app-service/app/addDeveloperForRegister", method = RequestMethod.POST)
    BWJsonResult addDeveloperForRegister(@RequestBody BopUserInfo bopUserInfo);

    /**
     * appKey绑定api
     */
    @RequestMapping(value = "/app-service/app/appKeyBindApi", method = RequestMethod.POST)
    BWJsonResult appKeyBindApi(@RequestBody Map<String, Object> appkeyApi);

    /**
     * 根据租户ID查询是否绑定appkey
     */
    @RequestMapping(value = {"/app-service/app/queryClientInfoByTenantId"},method = {RequestMethod.POST})
    BWJsonResult queryClientInfoByTenantId(@RequestParam(value = "tenantId", required = true) Long tenantId);

}
