package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface MoiraiTenantAnotherService {

    List<MoiraiTenant> batchQueryTenant(List<String> tenantIds);

    BWJsonResult readExcelRel(MultipartFile file, String creater, List<Long> qdBmList, List<Long> roleList);

    /**
     * 读取Excel并校验拼接完整参数
     *
     * @param excelFile
     * @param tenantId
     * @param orgId
     * @param creater
     * @return
     */
    BWJsonResult readExcelRel(MultipartFile excelFile, Long tenantId, Long orgId, String userId, String creater);

    void checkOrgName(StringBuffer str, MoiraiTenant moiraiTenant, MoiraiOrg moiraiOrg, int rowNum, String orgName);

    void checkBelongIndustry(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String belong_industry,
        List<String> SSHY, boolean taxFlag);

    void checkTaxCode(StringBuffer str, List<String> taxCodeList, MoiraiOrg moiraiOrg, int rowNum, String taxCode,
        boolean taxFlag);

    void checkTaxProv(StringBuffer str, MoiraiOrg moiraiOrg, boolean taxFlag, int rowNum, String taxProv,
        List<String> PROV);

    void checkOrgCode(StringBuffer str, List<String> orgCodeList, MoiraiTenant moiraiTenant, MoiraiOrg moiraiOrg,
        int rowNum, String orgCode);

    void checkBankDeposit(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String bankDeposit);

    void checkAccountNumber(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String bankDeposit,
        String accountNumber);

//    void checkTgType(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String tgfs);

//    void checkDeviceType(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String deviceType);

    void checkTaxQuali(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String nszz, boolean taxFlag);

    void checkDjType(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String djlx, List<String> DJLX,
        boolean taxFlag);

    void checkOrgType(StringBuffer str, MoiraiOrg parentOrg, MoiraiOrg moiraiOrg, Boolean[] taxCodeFlag,
        int rowNum, String orgType);

    void checkTelephone(StringBuffer str, MoiraiTenant moiraiTenant, MoiraiOrg moiraiOrg, boolean taxFlag, int rowNum,
        String telphone);

    void checkBusinessAddress(StringBuffer str, MoiraiOrg moiraiOrg, boolean taxFlag, int rowNum,
        String businessAddress);

    void checkCrossNo(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String crossNo);

    void checkExportQualify(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, boolean isJX,
        String exportQualify);

    void checkDeviceCode(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String deviceCode);

    void checkParentOrgCode(StringBuffer str, MoiraiOrg parentOrg, MoiraiOrg moiraiOrg, int rowNum,
        String orgCode, String parentOrgCode);

    void checkProducts(StringBuffer str, MoiraiOrg parentOrg, MoiraiOrg moiraiOrg, int rowNum, String cps, Long qdBm);

    void addOrgList(List<MoiraiOrg> moiraiOrgList, List<MoiraiUser> userList, List<MoiraiUserAuthz> roleList,
        List<MoiraiOrgProduct> products);

    void checkLegalName(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String legalName);

    void checkRegisterAddress(StringBuffer str, MoiraiOrg moiraiOrg, int rowNum, String registerAddress);
}
