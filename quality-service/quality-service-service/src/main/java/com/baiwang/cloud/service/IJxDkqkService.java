package com.baiwang.cloud.service;

import com.baiwang.cloud.common.model.JxdkqkPageParam;
import com.baiwang.cloud.common.model.PageResult;
import com.baiwang.cloud.common.model.JxDkqk;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IJxDkqkService {


    /**
     * 分页查询
     * @param jxdkqkPageParam  发票信息作为条件
     * @return
     */
    public PageResult<List<JxDkqk>> queryJxDkqkPage(JxdkqkPageParam jxdkqkPageParam);

    /**
     * 查询发票信息根据条件
     * @param jxDkqk  发票信息作为条件
     * @return
     */
    public List<JxDkqk> queryJxDkqkByConditions(JxDkqk jxDkqk);


    /**
     * 插入发票信息
     * @param jxDkqk  插入发票信息
     */
    public int inserJxDkqk(JxDkqk jxDkqk);

    @Transactional
    int batchInserJxDkqk(@Validated JxDkqk jxDkqk, List<String> messages);

    @Transactional
    int batchInserJxDkqk2(List<JxDkqk> jxDkqkList);

    /**
     * 更新发票信息
     * @param jxDkqk  更新发票信息
     */
    public int updateJxDkqk(JxDkqk jxDkqk);

    /**
     * 删除发票信息
     * @param jxDkqk 用户信息
     */
    public int deleteJxDkqk(JxDkqk jxDkqk);



    boolean importExcel(MultipartFile multipartFile);

    @Transactional
    boolean importExcel2(MultipartFile multipartFile);
}
