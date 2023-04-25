package cn.gmlee.tools.mate.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 时间轴
 * </p>
 *
 * @author Jas°
 * @since 2022-07-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("log")
public class Log implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统编号
     */
    private Long sysId;

    /**
     * 系统名称
     */
    private String sysName;

    /**
     * 接口日志
     */
    private String url;

    /**
     * 接口描述
     */
    private String print;

    /**
     * 影响类型: -1常规, 0用户组织, 1用户密码, 2用户角色, 3用户菜单, 4用户可见性, 5数据权限
     */
    private Integer type;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求头
     */
    private String requestHeaders;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求时间
     */
    private Date requestTime;

    /**
     * 响应参数
     */
    private String responseParams;

    /**
     * 响应时间
     */
    private Date responseTime;

    /**
     * 耗时
     */
    private Long elapsedTime;

    /**
     * 位置
     */
    private String site;

    /**
     * 异常信息
     */
    private String ex;

    /**
     * 状态: 0离线, 1在线
     */
    private Integer status;

    /**
     * 权限编号
     */
    private Long authId;

    /**
     * 权限类型: user(个人), user_group(用户组), role(角色), post(岗位), department(部门), sub_company(子公司), company(公司), group(集团)
     */
    private String authType;

    /**
     * 权限名称
     */
    private String authName;

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
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 创建时间
     */
    private Date createdAt;


}
