package com.baiwang.moirai.feignclient;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.po.ProductOpenPO;
import com.baiwang.moirai.model.vo.MoiraiProductVo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 产品服务
 */
//@FeignClient(value = "MOIRAI-PRODUCT", url="http://192.168.143.60:8788")
@FeignClient(value = "MOIRAI-PRODUCT-SERVICE")
public interface MoiraiProductClient {

    @RequestMapping(value = "/product/open/queryProductAndResMore", method = RequestMethod.POST)
    BWJsonResult<MoiraiProductVo> queryProductAndResMore(@RequestBody ProductOpenPO productOpenPO);

    @RequestMapping(value = "/product/open/queryRelationResource", method = RequestMethod.POST)
    BWJsonResult<MoiraiProductVo> queryRelationResource(@RequestBody ProductOpenPO productOpenPO);
}
