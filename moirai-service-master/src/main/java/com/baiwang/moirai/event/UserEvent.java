package com.baiwang.moirai.event;

import com.baiwang.moirai.model.user.MoiraiUser;

import java.util.List;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-19 17:11
 * @Description:
 */
public class UserEvent {

    /**
     * 用户信息
     */
    private MoiraiUser user;

    private List<MoiraiUser> userList;

    /**
     * 操作delete
     */
    private boolean delete;

    /**
     * 操作delete
     */
    private boolean modify;

    private boolean batchSync;

    private Long tenantId;

    /**
     * 删除用户
     * @param user
     */
    public UserEvent(MoiraiUser user){
        this.delete=true;
        this.user=user;
    }

    /**
     * 添加/修改 用户
     * @param user
     */
    public UserEvent(MoiraiUser user,boolean modify){
        this.modify=modify;
        this.user=user;
    }

    /**
     * 批量添加/修改 用户
     * @param tenantId
     */
    public UserEvent(Long tenantId){
        this.batchSync=true;
        this.tenantId=tenantId;
    }

    public MoiraiUser getUser() {
        return user;
    }

    public void setUser(MoiraiUser user) {
        this.user = user;
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

    public List<MoiraiUser> getUserList() {
        return userList;
    }

    public void setUserList(List<MoiraiUser> userList) {
        this.userList = userList;
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
