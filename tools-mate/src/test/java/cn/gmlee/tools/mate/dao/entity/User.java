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
 * 用户表
 * </p>
 *
 * @author Jas°
 * @since 2021-11-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long sysId;

    private String type;

    private String idCard;

    private String mobile;

    private String email;

    private String username;

    private String password;

    private String pwd;

    private String otp;

    private String salt;

    private Integer gender;

    private String realName;

    private Date birthday;

    private String portrait;

    private String city;

    private Date registerTime;

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
