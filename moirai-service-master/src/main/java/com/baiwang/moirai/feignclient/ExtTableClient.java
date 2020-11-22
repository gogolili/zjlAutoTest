package com.baiwang.moirai.feignclient;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ExtFieldMetaVo;
import com.baiwang.cloud.common.model.SyspageBasicQuery;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Component
@FeignClient(value = "service-ext-table")
public interface ExtTableClient {

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     *查询模板数据信息
     * @return
     * @since 2019/12/18
     */
    @RequestMapping(value = "/ext/listPageBasicExcel", method = RequestMethod.POST)
    public BWJsonResult listPageBasicExcel(@RequestBody SyspageBasicQuery queryParam);

    @RequestMapping(value = "/extField/queryExtField", method = RequestMethod.POST)
    public BWJsonResult queryExtFieldMate(@RequestBody ExtFieldMetaVo extFieldMetaVo);
}
