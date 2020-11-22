package com.baiwang.cloud.common.model;

public class JxdkqkPageParam {
    //第几页
    private int pageNo;
    //每页的数量
    private int pageSize;

    /**
     * 发票代码
     */
    private String fpdm;
    /**
     * 发票号码
     */
    private String fphm;
    /**
     * 发票种类:01 增值税专用发票/02 货运专用发票/03 机动车专用发票
     */
    private String fpzl;


    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getFpdm() {
        return fpdm;
    }

    public void setFpdm(String fpdm) {
        this.fpdm = fpdm;
    }

    public String getFphm() {
        return fphm;
    }

    public void setFphm(String fphm) {
        this.fphm = fphm;
    }

    public String getFpzl() {
        return fpzl;
    }

    public void setFpzl(String fpzl) {
        this.fpzl = fpzl;
    }
}
