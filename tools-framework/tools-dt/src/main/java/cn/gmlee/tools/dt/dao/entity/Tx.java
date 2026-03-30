package cn.gmlee.tools.dt.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 事务
 * </p>
 *
 * @author Jas °
 * @since 2022 -11-18
 */
public class Tx implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 本地事务编码(本地事务的唯一标识)
     */
    private String code;

    /**
     * 服务器地址
     */
    private String ip;

    /**
     * 服务器端口
     */
    private Integer port;

    /**
     * 应用编号
     */
    private Long appId;

    /**
     * 全局编号
     */
    private String globalCode;

    /**
     * 上级编号
     */
    private String superiorCode;

    /**
     * 远程事务数
     */
    private Integer count;

    /**
     * 隔离级别
     */
    private Integer isolation;

    /**
     * 传播行为
     */
    private Integer propagation;

    /**
     * 超时时间(毫秒)
     */
    private Integer timeout;

    /**
     * 位置(一般是Class.method)
     */
    private String site;

    /**
     * 入参
     */
    private String args;

    /**
     * 返回
     */
    private String ret;

    /**
     * 异常信息
     */
    private String exInfo;

    /**
     * 运行耗时(毫秒)
     */
    private Integer elapsedTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名
     */
    private String state;

    /**
     * 创建人编号
     */
    private Long createdBy;

    /**
     * 创建人名称
     */
    private String creator;

    /**
     * 修改人编号
     */
    private Long updatedBy;

    /**
     * 修改人名称
     */
    private String updater;

    /**
     * 删除标识
     */
    private Boolean del;

    /**
     * 验收标记: 0是测试数据, 1是真实数据
     */
    private Boolean mark;

    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 创建时间
     */
    private Date createdAt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getGlobalCode() {
        return globalCode;
    }

    public void setGlobalCode(String globalCode) {
        this.globalCode = globalCode;
    }

    public String getSuperiorCode() {
        return superiorCode;
    }

    public void setSuperiorCode(String superiorCode) {
        this.superiorCode = superiorCode;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getIsolation() {
        return isolation;
    }

    public void setIsolation(Integer isolation) {
        this.isolation = isolation;
    }

    public Integer getPropagation() {
        return propagation;
    }

    public void setPropagation(Integer propagation) {
        this.propagation = propagation;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }

    public String getExInfo() {
        return exInfo;
    }

    public void setExInfo(String exInfo) {
        this.exInfo = exInfo;
    }

    public Integer getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Boolean getDel() {
        return del;
    }

    public void setDel(Boolean del) {
        this.del = del;
    }

    public Boolean getMark() {
        return mark;
    }

    public void setMark(Boolean mark) {
        this.mark = mark;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
