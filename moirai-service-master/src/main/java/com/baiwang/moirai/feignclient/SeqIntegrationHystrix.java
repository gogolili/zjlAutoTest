//package com.baiwang.moirai.feignclient;
//
//import com.alibaba.fastjson.JSONObject;
//import com.baiwang.cloud.service.IdService;
//import feign.hystrix.FallbackFactory;
//import java.util.ArrayList;
//import java.util.List;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.PathVariable;
//
///**
// *
// */
//@Component
//public class SeqIntegrationHystrix implements FallbackFactory<SeqIntegrationClient> {
//    private static final Logger logger = LoggerFactory.getLogger(SeqIntegrationHystrix.class);
//
//    @Autowired
//    private IdService idService;
//
//    @Override
//    public SeqIntegrationClient create(Throwable throwable) {
//        logger.info("fallback; reason was: {}", throwable);
//
//        return new SeqIntegrationClient() {
//            @Override
//            public Long getSeq() {
//                return idService.genId();
//            }
//
//            @Override
//            public JSONObject getBatchSeq(@PathVariable("num") Long num) {
//                JSONObject seqMap = new JSONObject();
//                seqMap.put("msg", "成功");
//                seqMap.put("code", "0");
//                List<Long> data = new ArrayList<>();
//                for (int i = 0; i < num; i++) {
//                    data.add(idService.genId());
//                }
//                seqMap.put("data", data);
//                return seqMap;
//            }
//        };
//    }
//}
