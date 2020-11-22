/*
 * @项目名称: Moirai
 * @文件名称: MoiraiSecretKeyController.java
 * @Date: 17-12-5 下午7:47
 * @author Lance cui
 *
 */

package com.baiwang.moirai.controller.user;

import com.baiwang.moirai.api.MoiraiSecretKeySvc;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.user.MoiraiSecretKey;
import com.baiwang.moirai.service.MoiraiSecretKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MoiraiSecretKeyController implements MoiraiSecretKeySvc {

    @Autowired
    private MoiraiSecretKeyService moiraiSecretKeyService;

    @Override
    public BWJsonResult<MoiraiSecretKey> getAlllSecretkey(MoiraiOrg moiraiOrg) {

        List<MoiraiSecretKey> allSecretKey = moiraiSecretKeyService.selectAllSecretKey();

        BWJsonResult<MoiraiSecretKey> bwJsonResult=new BWJsonResult<MoiraiSecretKey>(allSecretKey);

        return bwJsonResult;
    }

}
