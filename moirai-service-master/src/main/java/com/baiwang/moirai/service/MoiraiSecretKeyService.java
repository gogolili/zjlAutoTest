/*
 * @项目名称: Moirai
 * @文件名称: MoiraiSecretKeyService.java
 * @Date: 17-12-5 下午7:52
 * @author Lance cui
 *
 */

package com.baiwang.moirai.service;

import com.baiwang.moirai.model.user.MoiraiSecretKey;

import java.util.List;

public interface MoiraiSecretKeyService {


    List<MoiraiSecretKey> selectAllSecretKey();

}
