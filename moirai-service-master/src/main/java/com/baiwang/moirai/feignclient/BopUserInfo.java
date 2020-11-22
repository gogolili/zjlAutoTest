package com.baiwang.moirai.feignclient;

import java.util.Date;

public class BopUserInfo {
    private Long id;//

    private String loginId;//登录用户名

    private String userName;//个人姓名（如果是企业类型，该字段则表示联系人）

    private String tenantId;//租户ID

    private String idCardNo;//身份证号

    private String idCardUrl;//身份证照片地址

    private String email;//邮箱

    private String phone;//手机

    private String address;//联系地址

    private String companyName;//公司名称

    private String companySh;//企业税号

    private String qualificationUrl;//资质证书地址

    private String status;//状态  默认0：未激活  1：激活 2:审核通过  3：审核拒绝 4 冻结

    private String reason;//审核理由

    private String type;//类型 0：个人  1：企业

    private String sourceOrigin;//数据来源 0 百望云平台

    private Date createTime;//创建时间

    private Date lastModifyTime;//修改时间

    private String clientId;//

    private String clientSecret;//

    private String channel;//渠道标志

    private String deposit;//余额

    private Integer appLevel;//app 等级

    private Integer asynThreshold;//异步限流

    private Integer syncThreshold;//同步限流

    private String ruleType;//

    private String slbUrl;

    public BopUserInfo() {
        super();
    }

    public BopUserInfo(String loginId, String email, String phone, String companyName,
        String companySh, String qualificationUrl, String type, String address) {
        this.loginId = loginId;
        this.email = email;
        this.phone = phone;
        this.companyName = companyName;
        this.companySh = companySh;
        this.qualificationUrl = qualificationUrl;
        this.type = type;
        this.address = address;
    }

    public Integer getAppLevel() {
        return appLevel;
    }

    public void setAppLevel(Integer appLevel) {
        this.appLevel = appLevel;
    }

    public Integer getAsynThreshold() {
        return asynThreshold;
    }

    public void setAsynThreshold(Integer asynThreshold) {
        this.asynThreshold = asynThreshold;
    }

    public Integer getSyncThreshold() {
        return syncThreshold;
    }

    public void setSyncThreshold(Integer syncThreshold) {
        this.syncThreshold = syncThreshold;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getSlbUrl() {
        return slbUrl;
    }

    public void setSlbUrl(String slbUrl) {
        this.slbUrl = slbUrl;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getIdCardUrl() {
        return idCardUrl;
    }

    public void setIdCardUrl(String idCardUrl) {
        this.idCardUrl = idCardUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanySh() {
        return companySh;
    }

    public void setCompanySh(String companySh) {
        this.companySh = companySh;
    }

    public String getQualificationUrl() {
        return qualificationUrl;
    }

    public void setQualificationUrl(String qualificationUrl) {
        this.qualificationUrl = qualificationUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceOrigin() {
        return sourceOrigin;
    }

    public void setSourceOrigin(String sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
