package com.baiwang.moirai.feignclient;

import java.util.List;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 序列号生成
 *
 * @author sxl
 */
@FeignClient(value = "VESTA-SERVICES")
public interface SeqnumFeignClient {
    @RequestMapping(value = "/seqnum/getNum")
    Long getNum(@RequestParam("type") String type);

    @RequestMapping(value = "/seqnum/getNums")
    List<Long> getNums(@RequestParam("type") String type, @RequestParam("num") int num);

    @RequestMapping(value = "/seq", method = RequestMethod.GET)
    Long getSeq();
}