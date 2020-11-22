package com.baiwang.cloud.common.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * (JxDkqk)实体类
 *
 * @author makejava
 * @since 2020-11-04 14:59:59
 */
public class JxDkqk extends BaseRowModel implements Serializable {
    private static final long serialVersionUID = 317493552474043839L;
    /**
    * 抵扣情况-ID
    */
    private Long jxDkqkId;
    /**
    * 发票代码
    */
//    @ContentRowHeight(10)//这个是作用在类上的主要是在写的时候写明格子的高度用的
    @ColumnWidth(20)//表示这个列的宽度有的列字太多了可能会被后面的盖着就看不见了，所以可以拉长一点，这样就不用自己动手去拉了。
    @ExcelProperty(value = "发票代码") //这个注解主要是申明了这个属性对应是哪个列index就是从第几列开始0对应A，1对应B以此类推。
    @NotBlank(message = "发票代码不能为空")
    @Size(max = 12,message = "发票长度最大12位")
    private String fpdm;
    /**
    * 发票号码
    */
    @ColumnWidth(20)//表示这个列的宽度有的列字太多了可能会被后面的盖着就看不见了，所以可以拉长一点，这样就不用自己动手去拉了。
    @NotBlank(message = "发票号码不能为空")
    @ExcelProperty(value = "发票号码") //这个注解主要是申明了这个属性对应是哪个列index就是从第几列开始0对应A，1对应B以此类推。
    @Size(max = 8,message = "发票长度最大8位")
    private String fphm;
    /**
    * 发票种类:01 增值税专用发票/02 货运专用发票/03 机动车专用发票
    */
    @ExcelProperty(value = "发票类型")
    @NotBlank(message = "发票类型不能为空")
    private String fpzl;
    /**
    * 开具时间
    */
    @ExcelProperty(value = "开具时间")
    private String fpkjsj;
    /**
    * 开票年份
    */
    @ExcelProperty(value = "开票年份")
    private String fpkjnf;
    /**
    * 开票月份
    */
    @ExcelProperty(value = "开票年份")
    private String fpkjyf;
    /**
    * 销方纳税识别号
    */
    @ExcelProperty(value = "销方纳税识别号")
    private String xfNssbh="ZJL88888888888";
    /**
    * 销方名称
    */
    @ExcelProperty(value = "销方名称")
    @NotBlank(message = "销方名称不能为空")
    private String xfKhmc="zjl股份有限公司";
    /**
    * 金额
    */
    @ExcelProperty(value = "合计金额")
    @NotNull(message = "合计金额不能为空")
    private Double hjje;
    /**
    * 税额
    */
    @ExcelProperty(value = "税额")
    @NotNull(message = "税额不能为空")
    private Double hjse;
    /**
    * 价税合计
    */
    @ExcelProperty(value = "价税合计")
    @NotNull(message = "价税合计不能为空")
    private Double jshj;
    /**
    * 是否勾选：1-是/0-否
    */
    @ExcelProperty(value = "是否勾选")
    @NotBlank(message = "是否勾选不能为空")
    private String sfgx="0";
    /**
    * 勾选时间
    */
    @ExcelProperty(value = "勾选时间")
    private String gxsj;
    /**
    * 勾选年份
    */
    @ExcelProperty(value = "勾选年份")
    private Integer gxnf;
    /**
    * 勾选月份
    */
    @ExcelProperty(value = "勾选月份")
    private Integer gxyf;
    /**
    * 发票状态:0 正常/2 作废/4 异常/1 失控/3 红冲
    */
    @ExcelProperty(value = "发票状态")
    @NotBlank(message = "发票状态不能为空")
    private String fpzt;
    /**
    * 状态：0-无操作；1-勾选中；2-勾选执行中；3-取消中；4-取消执行中；5-确认中；6-确认执行中
    */
    @ExcelProperty(value = "发票认证状态")
    @NotBlank(message = "状态不能为空")
    private String czzt="0";
    /**
    * 是否扫描认证：1-是/0-否
    */
    @ExcelProperty(value = "是否扫描认证")
    @NotBlank(message = "是否扫描认证不能为空")
    private String sfsmrz="0";
    /**
    * 是否已认证：1-是/0-否
    */
    @ExcelProperty(value = "是否已认证")
    @NotBlank(message = "是否已认证不能为空")
    private String sfyrz="0";
    /**
    * 认证日期
    */
    @ExcelProperty(value = "认证日期")
    private String rzrq;
    /**
    * 认证年份
    */
    @ExcelProperty(value = "认证年份")
    private String rznf;
    /**
    * 认证月份
    */
    @ExcelProperty(value = "认证月份")
    private String rzyf;
    /**
    * 购方纳税识别号
    */
    @ExcelProperty(value = "购方纳税识别号")
    @NotBlank(message = "购方纳税识别号不能为空")
    private String gfNssbh;
    /**
    * 发票详情_主信息ID(回填)
    */
    @ExcelProperty(value = "发票详情_主信息ID")
    private Long jxFpxqZxxId;
    /**
    * 是否逾期：1-是/-否
    */
    @ExcelProperty(value = "是否逾期")
    @NotBlank(message = "是否逾期不能为空")
    private String sfyq="0";
    /**
    * 逾期日期
    */
    @ExcelProperty(value = "逾期日期")
    @NotBlank(message = "逾期日期不能为空")
    private String yqrq="2022-09-09";
    /**
    * 地区编码:省/计划单列市编码
    */
    @ExcelProperty(value = "开票年份")
    private String dqbm;
    /**
    * 最新请求时间
    */
    @ExcelProperty(value = "开票年份")
    private String zxqqsj;
    /**
    * 所属租户ID
    */
    @ExcelProperty(value = "所属租户ID")
    @NotNull(message = "所属租户ID不能为空")
    private Long zhBm=-1L;
    /**
    * 所属组织ID
    */
    @ExcelProperty(value = "所属组织ID")
    @NotNull(message = "所属组织ID不能为空")
    private Long zzBm=-1L;
    /**
    * 扫描时间
    */
    @ExcelProperty(value = "扫描时间")
    private String smsj;
    /**
    * 0 未更新 1 更新 XXX 第三方获取
    */
    @ExcelProperty(value = "是否已取数")
    private String updated="1";

    @ExcelProperty(value = "skssq")
    private String skssq;
    /**
    * ä»»åŠ¡ID
    */
    @ExcelProperty(value = "任务号")
    private String taskNo;

    @ExcelProperty(value = "sfdbts")
    @NotBlank(message = "sfdbts不能为空")
    private String sfdbts="0";

    @ExcelProperty(value = "rzlx")
    private String rzlx;
    /**
    * 校验码
    */
    @ExcelProperty(value = "校验码")
    private String jym;

    @ExcelProperty(value = "创建时间")
    private String createTime;

    @ExcelProperty(value = "更新时间")
    private Date updateTime;
    /**
    * 管理状态（0-正常，1-异常）
    */

    @ExcelProperty(value = "管理状态")
    private String glzt="0";
    /**
    * 信息来源（0-扫描认证，1-系统推送，2-出口转内销）
    */
    @ExcelProperty(value = "信息来源")
    private String xxly="1";
    /**
    * 有效税额
    */
    @ExcelProperty(value = "有效税额")
    private Double yxse;
    /**
    * 逾期可勾选标志（0-不可逾期勾选，1-可逾期勾选）
    */
    @ExcelProperty(value = "逾期可勾选标志")
    private String yqkgxbz;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getJxDkqkId() {
        return jxDkqkId;
    }

    public void setJxDkqkId(Long jxDkqkId) {
        this.jxDkqkId = jxDkqkId;
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

    public String getFpkjsj() {
        return fpkjsj;
    }

    public void setFpkjsj(String fpkjsj) {
        this.fpkjsj = fpkjsj;
    }

    public String getFpkjnf() {
        return fpkjnf;
    }

    public void setFpkjnf(String fpkjnf) {
        this.fpkjnf = fpkjnf;
    }

    public String getFpkjyf() {
        return fpkjyf;
    }

    public void setFpkjyf(String fpkjyf) {
        this.fpkjyf = fpkjyf;
    }

    public String getXfNssbh() {
        return xfNssbh;
    }

    public void setXfNssbh(String xfNssbh) {
        this.xfNssbh = xfNssbh;
    }

    public String getXfKhmc() {
        return xfKhmc;
    }

    public void setXfKhmc(String xfKhmc) {
        this.xfKhmc = xfKhmc;
    }

    public Double getHjje() {
        return hjje;
    }

    public void setHjje(Double hjje) {
        this.hjje = hjje;
    }

    public Double getHjse() {
        return hjse;
    }

    public void setHjse(Double hjse) {
        this.hjse = hjse;
    }

    public Double getJshj() {
        return jshj;
    }

    public void setJshj(Double jshj) {
        this.jshj = jshj;
    }

    public String getSfgx() {
        return sfgx;
    }

    public void setSfgx(String sfgx) {
        this.sfgx = sfgx;
    }

    public String getGxsj() {
        return gxsj;
    }

    public void setGxsj(String gxsj) {
        this.gxsj = gxsj;
    }

    public Integer getGxnf() {
        return gxnf;
    }

    public void setGxnf(Integer gxnf) {
        this.gxnf = gxnf;
    }

    public Integer getGxyf() {
        return gxyf;
    }

    public void setGxyf(Integer gxyf) {
        this.gxyf = gxyf;
    }

    public String getFpzt() {
        return fpzt;
    }

    public void setFpzt(String fpzt) {
        this.fpzt = fpzt;
    }

    public String getCzzt() {
        return czzt;
    }

    public void setCzzt(String czzt) {
        this.czzt = czzt;
    }

    public String getSfsmrz() {
        return sfsmrz;
    }

    public void setSfsmrz(String sfsmrz) {
        this.sfsmrz = sfsmrz;
    }

    public String getSfyrz() {
        return sfyrz;
    }

    public void setSfyrz(String sfyrz) {
        this.sfyrz = sfyrz;
    }

    public String getRzrq() {
        return rzrq;
    }

    public void setRzrq(String rzrq) {
        this.rzrq = rzrq;
    }

    public String getRznf() {
        return rznf;
    }

    public void setRznf(String rznf) {
        this.rznf = rznf;
    }

    public String getRzyf() {
        return rzyf;
    }

    public void setRzyf(String rzyf) {
        this.rzyf = rzyf;
    }

    public String getGfNssbh() {
        return gfNssbh;
    }

    public void setGfNssbh(String gfNssbh) {
        this.gfNssbh = gfNssbh;
    }

    public Long getJxFpxqZxxId() {
        return jxFpxqZxxId;
    }

    public void setJxFpxqZxxId(Long jxFpxqZxxId) {
        this.jxFpxqZxxId = jxFpxqZxxId;
    }

    public String getSfyq() {
        return sfyq;
    }

    public void setSfyq(String sfyq) {
        this.sfyq = sfyq;
    }

    public String getYqrq() {
        return yqrq;
    }

    public void setYqrq(String yqrq) {
        this.yqrq = yqrq;
    }

    public String getDqbm() {
        return dqbm;
    }

    public void setDqbm(String dqbm) {
        this.dqbm = dqbm;
    }

    public String getZxqqsj() {
        return zxqqsj;
    }

    public void setZxqqsj(String zxqqsj) {
        this.zxqqsj = zxqqsj;
    }

    public Long getZhBm() {
        return zhBm;
    }

    public void setZhBm(Long zhBm) {
        this.zhBm = zhBm;
    }

    public Long getZzBm() {
        return zzBm;
    }

    public void setZzBm(Long zzBm) {
        this.zzBm = zzBm;
    }

    public String getSmsj() {
        return smsj;
    }

    public void setSmsj(String smsj) {
        this.smsj = smsj;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getSkssq() {
        return skssq;
    }

    public void setSkssq(String skssq) {
        this.skssq = skssq;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getSfdbts() {
        return sfdbts;
    }

    public void setSfdbts(String sfdbts) {
        this.sfdbts = sfdbts;
    }

    public String getRzlx() {
        return rzlx;
    }

    public void setRzlx(String rzlx) {
        this.rzlx = rzlx;
    }

    public String getJym() {
        return jym;
    }

    public void setJym(String jym) {
        this.jym = jym;
    }

    public String  getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getGlzt() {
        return glzt;
    }

    public void setGlzt(String glzt) {
        this.glzt = glzt;
    }

    public String getXxly() {
        return xxly;
    }

    public void setXxly(String xxly) {
        this.xxly = xxly;
    }

    public Double getYxse() {
        return yxse;
    }

    public void setYxse(Double yxse) {
        this.yxse = yxse;
    }

    public String getYqkgxbz() {
        return yqkgxbz;
    }

    public void setYqkgxbz(String yqkgxbz) {
        this.yqkgxbz = yqkgxbz;
    }


}