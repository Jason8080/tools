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
 * 系统表
 * </p>
 *
 * @author Jas°
 * @since 2021-11-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys")
public class Sys implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String sysName;

    private String sysType;

    private String appCode;

    private String appKey;

    private Long appAudit;

    private String appUrl;

    private String userType;

    private String admin;

    private String otherInfo;

    private Integer status;

    private Long authId;

    private String authType;

    private String authName;

    private Long createdBy;

    private String creator;

    private Long updatedBy;

    private String updater;

    private Boolean del;

    private Date updatedAt;

    private Date createdAt;


}
