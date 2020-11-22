package com.baiwang.moirai.service;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysFileResponse;
import com.baiwang.moirai.model.sys.SysProvCityDist;
import com.baiwang.moirai.model.sys.SysProvCityDistTree;
import com.baiwang.moirai.model.user.MoiraiUser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.LazyDynaBean;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 程路超
 */

public interface MoiraiSysService {
    List<SysProvCityDist> getSysProvCityDist(SysProvCityDist sys);

    List<SysProvCityDistTree> getSysProvCityDistTree();

    List<SysDict> getSysDict(SysDict sysDict);

//    Map<String, Object> getProvCityDist(String taxNo) throws Exception;
//
//    MoiraiOrg getProvCityDistToOrg(MoiraiOrg org) throws Exception;

    JSONObject commonMethod(Map<String, String> param, int flag);

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>批量获取各个表的ID<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    List<Long> getNums(List list, String moirai);

    BWJsonResult<SysFileResponse> uploadErrorFile(StringBuffer str, String fileName);

    BWJsonResult<SysFileResponse> uploadErrorFile(InputStream in, String fileName);

    BWJsonResult uploadFile(MultipartFile item, String tempPath);

    void excecutorOldDB(List<ArrayList<String>> excelList, StringBuffer str, int tag);

    BWJsonResult uploadString(String str, String path, Long orgId);

    void downloadString(String path, Long orgId, HttpServletResponse response);

    void downloadTemplate(HttpServletRequest request, HttpServletResponse response, String fileName, String path);

    List<String> getDictCode(String dictType);

    void excecutorImport(List<Long> tenantId, List<String> taxCodeList, List<MoiraiOrg> moiraiOrgList);

    List<SysDict> getSysDictBatch(List<String> dictTypeList);

    MoiraiUser gainCacheUser();

    Long gainCacheUserId();

    boolean getUserOfLanders(MoiraiUser moiraiUser);

    List<MoiraiUserAuthz> getAuthzs(Long userId, Long orgId, Long tenantId);

    void excecutorSendEmail(List<LazyDynaBean> lazyDynaBeans, String titleFrom);

    void removeTaxCode(String taxCode);

    //void sendEmail(String toUser, String subject, String content) throws MessagingException;
}
