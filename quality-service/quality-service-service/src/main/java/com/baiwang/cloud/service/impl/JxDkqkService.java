package com.baiwang.cloud.service.impl;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.baiwang.cloud.common.model.JxdkqkPageParam;
import com.baiwang.cloud.common.model.PageResult;
import com.baiwang.cloud.common.model.JxDkqk;
import com.baiwang.cloud.common.utils.DateUtil;
import com.baiwang.cloud.common.utils.EasyExcelUtil;
import com.baiwang.cloud.dao.impl.JxDkqkMapper;
import com.baiwang.cloud.service.IJxDkqkService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JxDkqkService implements IJxDkqkService,ApplicationContextAware{
    private static final Logger LOGGER = LoggerFactory.getLogger(JxDkqkService.class);
    private ApplicationContext context;
    @Autowired
    private JxDkqkMapper jxDkqkMapper;



    /**
     * 分页查询
     * @param jxdkqkPageParam  发票信息作为条件
     * @return
     */
    @Transactional
    @Override
    public PageResult<List<JxDkqk>> queryJxDkqkPage(JxdkqkPageParam jxdkqkPageParam) {
        Page<JxDkqk> page = PageHelper.startPage(jxdkqkPageParam.getPageNo(), jxdkqkPageParam.getPageSize());

        PageResult<List<JxDkqk>> pageResult = new  PageResult<List<JxDkqk>>();

        List<JxDkqk> jxDkqks = jxDkqkMapper.queryJxDkqkByConditions(jxdkqkPageParam);

        pageResult.setSource(jxDkqks);
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }


    /**
     * 根据条件查询
     * @param jxDkqk  发票信息作为条件
     * @return
     */
    @Transactional
    @Override
    public List<JxDkqk> queryJxDkqkByConditions(JxDkqk jxDkqk) {
        List<JxDkqk> jxDkqks = jxDkqkMapper.queryJxDkqkByConditions(jxDkqk);
        return jxDkqks;
    }


    /**
     * 单条添加
     * @param jxDkqk  插入发票信息
     * @return
     */
    @Transactional
    @Override
    public int inserJxDkqk(@Validated JxDkqk jxDkqk) {
        List<JxDkqk> jxDkqks = this.queryJxDkqkByConditions(jxDkqk);

        if (!jxDkqks.isEmpty()){
            throw new RuntimeException("发票代码号码已存在");

        }else {
            if (StringUtils.isEmpty(jxDkqk.getFpkjsj())){
                jxDkqk.setFpkjsj(DateUtil.currentDate());
            }
            if (StringUtils.isEmpty(jxDkqk.getFpkjnf())){
                jxDkqk.setFpkjnf(DateUtil.currentYear());
            }
            if (StringUtils.isEmpty(jxDkqk.getFpkjyf())){
                jxDkqk.setFpkjyf(DateUtil.currentMonth());
            }
            if (StringUtils.isEmpty(jxDkqk.getCreateTime())){
                jxDkqk.setCreateTime(DateUtil.currentTime());
            }
        }
        return jxDkqkMapper.insert(jxDkqk);
    }

    /**
     *假批量
     * @param jxDkqk  插入发票信息
     * @return
     */
    @Transactional
    @Override
    public int batchInserJxDkqk(@Validated JxDkqk jxDkqk,List<String> messages) {
        List<JxDkqk> jxDkqks = this.queryJxDkqkByConditions(jxDkqk);

        if (!jxDkqks.isEmpty()){
//            throw new RuntimeException("发票代码号码已存在");
            messages.add(jxDkqks.get(0).getFpdm() +"、"+jxDkqks.get(0).getFphm()+"发票代码号码已存在");
            return 0;
        }else {
            if (StringUtils.isEmpty(jxDkqk.getFpkjsj())){
                jxDkqk.setFpkjsj(DateUtil.currentDate());
            }
            if (StringUtils.isEmpty(jxDkqk.getFpkjnf())){
                jxDkqk.setFpkjnf(DateUtil.currentYear());
            }
            if (StringUtils.isEmpty(jxDkqk.getFpkjyf())){
                jxDkqk.setFpkjyf(DateUtil.currentMonth());
            }
            if (StringUtils.isEmpty(jxDkqk.getCreateTime())){
                jxDkqk.setCreateTime(DateUtil.currentTime());
            }
        }
        return jxDkqkMapper.insert(jxDkqk);
    }

    /**
     *批量
     * @param jxDkqkList  插入发票信息
     * @return
     */
    @Transactional
    @Override
    public int batchInserJxDkqk2(List<JxDkqk> jxDkqkList) {
        List<String> messages =new ArrayList<>();

        if (jxDkqkList.isEmpty()){
            throw new RuntimeException("数据为空");
        }
        for(JxDkqk jxDkqk:jxDkqkList){
            List<JxDkqk> jxDkqks = this.queryJxDkqkByConditions(jxDkqk);
            if (!jxDkqks.isEmpty()){
                messages.add(jxDkqks.get(0).getFpdm() +"-"+jxDkqks.get(0).getFphm()+"发票代码号码已存在");
                return 0;
            }
            if (StringUtils.isEmpty(jxDkqk.getFpkjsj())){
                jxDkqk.setFpkjsj(DateUtil.currentDate());
            }
            if (StringUtils.isEmpty(jxDkqk.getFpkjnf())){
                jxDkqk.setFpkjnf(DateUtil.currentYear());
            }
            if (StringUtils.isEmpty(jxDkqk.getFpkjyf())){
                jxDkqk.setFpkjyf(DateUtil.currentMonth());
            }
            if (StringUtils.isEmpty(jxDkqk.getCreateTime())){
                jxDkqk.setCreateTime(DateUtil.currentTime());
            }
            jxDkqkMapper.insert(jxDkqk);
        }
        if(messages.isEmpty()){
            return 0;
        }else {
            throw new RuntimeException(JSON.toJSONString(messages));
        }
    }

    /**
     * 修改
     * @param jxDkqk  更新发票信息
     * @return
     */
    @Transactional
    @Override
    public int updateJxDkqk(JxDkqk jxDkqk) {
        List<JxDkqk> jxDkqks = this.queryJxDkqkByConditions(jxDkqk);
        int result;
        if (!jxDkqks.isEmpty()){
            jxDkqk.setJxDkqkId(jxDkqks.get(0).getJxDkqkId());
            result = this.jxDkqkMapper.update(jxDkqk);
        }else {
            throw new RuntimeException("发票代码号码不存在");
        }

        return result;

    }

    @Override
    public int deleteJxDkqk(JxDkqk jxDkqk) {
        List<JxDkqk> jxDkqks = this.queryJxDkqkByConditions(jxDkqk);
        int result;
        if (!jxDkqks.isEmpty()){
            jxDkqk.setJxDkqkId(jxDkqks.get(0).getJxDkqkId());
            result = this.jxDkqkMapper.deleteJxDkqk(jxDkqk);
        }else {
            throw new RuntimeException("发票代码号码不存在");
        }
        return result;

    }


    /**
     * 1、SqlSession控制手动提交
     * 2、mapper，foreach
     * @param multipartFile
     * @return
     */

    @Transactional
    @Override
    public boolean importExcel(MultipartFile multipartFile) {
        List<JxDkqk> jxDkqkList = null;
        List<String> messages = new ArrayList<>();
        try {
            jxDkqkList = EasyExcelUtil.readExcelWithModel(multipartFile.getInputStream(),JxDkqk.class, ExcelTypeEnum.XLS);
            IJxDkqkService dkqkService = this.context.getBean(IJxDkqkService.class);
            for (JxDkqk jxDkqk:jxDkqkList){
                dkqkService.batchInserJxDkqk(jxDkqk,messages);
            }
        } catch (IOException e) {
            throw new RuntimeException("文件不正确");
        }
        if(messages.isEmpty()){
            return true;
        }else {
            throw new RuntimeException(JSON.toJSONString(messages));
        }
    }

    @Transactional
    @Override
    public boolean importExcel2(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        List<JxDkqk> jxDkqkList = null;
        try {
            jxDkqkList = EasyExcelUtil.readExcelWithModel(multipartFile.getInputStream(), JxDkqk.class, ExcelTypeEnum.XLS);
            IJxDkqkService dkqkService = this.context.getBean(IJxDkqkService.class);
            dkqkService.batchInserJxDkqk2(jxDkqkList);
        } catch (IOException e) {
            throw new RuntimeException("批量插入异常");
        }
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;

    }
}
