package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import org.springframework.web.multipart.MultipartFile;

public interface MoiraiUserLoadService {

    public BWJsonResult readExcelRel(MultipartFile excelFile, Long tenantId, Long orgId,Long userId, Long resourceIdL, String creater);

    public BWJsonResult uploadAuthFile(MultipartFile excelFile, Long tenantId,Long orgId,Long userId);
}
