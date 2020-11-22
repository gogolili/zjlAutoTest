package com.baiwang.moirai.service;

import com.baiwang.cloud.logaop.model.Log;
import com.baiwang.moirai.exception.MoiraiException;

public interface PrintLogWriterService {

    public void write(Log log) throws MoiraiException;
}
