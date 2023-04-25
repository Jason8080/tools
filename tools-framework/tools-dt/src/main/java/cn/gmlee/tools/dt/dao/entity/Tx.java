package cn.gmlee.tools.dt.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Getter
@Setter
@ToString
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


}
