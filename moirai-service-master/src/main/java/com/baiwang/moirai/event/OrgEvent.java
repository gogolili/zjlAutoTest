package com.baiwang.moirai.event;

import com.baiwang.moirai.model.org.MoiraiOrg;

import java.util.List;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-20 11:13
 * @Description:
 */
public class OrgEvent {

    private MoiraiOrg org;

    private List<MoiraiOrg> orgList;

    private boolean delete;

    private boolean modify;

    private boolean batchSync;

    private Long tenantId;

    public OrgEvent(MoiraiOrg org, boolean modify) {
        this.modify = modify;
        this.org = org;
    }

    public OrgEvent(MoiraiOrg org) {
        this.org = org;
        this.delete = true;
    }

    /*public OrgEvent(List<MoiraiOrg> orgList){
        this.orgList=orgList;
        this.batchSync=true;
    }*/

    public OrgEvent(Long tenantId) {
        this.tenantId = tenantId;
        this.batchSync = true;
    }

    public MoiraiOrg getOrg() {
        return org;
    }

    public void setOrg(MoiraiOrg org) {
        this.org = org;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isModify() {
        return modify;
    }

    public void setModify(boolean modify) {
        this.modify = modify;
    }

    public List<MoiraiOrg> getOrgList() {
        return orgList;
    }

    public void setOrgList(List<MoiraiOrg> orgList) {
        this.orgList = orgList;
    }

    public boolean isBatchSync() {
        return batchSync;
    }

    public void setBatchSync(boolean batchSync) {
        this.batchSync = batchSync;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
