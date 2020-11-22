/*
 * @项目名称: Moirai
 * @文件名称: MoiraiSecretKeyServiceImpl.java
 * @Date: 17-12-5 下午7:53
 * @author Lance cui
 *
 */

/*
 * @项目名称: Moirai
 * @文件名称: MoiraiOrgServiceImpl.java
 * @Date: 17-11-7 下午6:08
 * @author Lance cui
 *
 */

package com.baiwang.moirai.serviceimpl;

import com.baiwang.moirai.mapper.MoiraiSecretKeyMapper;
import com.baiwang.moirai.model.user.MoiraiSecretKey;
import com.baiwang.moirai.service.MoiraiSecretKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MoiraiSecretKeyServiceImpl implements MoiraiSecretKeyService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiSecretKeyMapper moiraiSecretKeyMapper;


    @Override
    public List<MoiraiSecretKey> selectAllSecretKey() {

        List<MoiraiSecretKey> allSecretKey = moiraiSecretKeyMapper.selectAllSecretKey();
        logger.info(allSecretKey.toString());

        return allSecretKey;
    }
}
