package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用权限信息对象
 *
 * @author Jas °
 * @date 2021 /1/27 (周三)
 */
@Data
public class PermissionInfo implements Serializable {
    /**
     * 角色列表
     * <p>
     * (修改后须重新登陆)
     * </p>
     */
    private List<Role> roles;
    /**
     * 功能列表
     * <p>
     * (可全部抽取之后存入Set达到去重效果)
     * </p>
     */
    private List<Feature> features;
    /**
     * 权限标识
     * <p>
     * 配置规范 (flag.code)
     * # 以[row-auth]开头默认为行权限
     * # 以[column-auth]开头默认为列权限
     *
     * </p>
     */
    private List<Flag> flags;
}
