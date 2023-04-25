package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 菜单权限: 功能.
 *
 * @author Jas
 */
@Data
public class Feature implements Serializable {
    private Long id;
    private String code;
    private String name;
    /**
     * 功能类型: menu,button,api,function,clazz.
     */
    private String type;
    private String url;
    private String method;
    /**
     * 是否显示.
     */
    private Boolean show;
    /**
     * 是否公开.
     */
    private Boolean open;
    private Integer sort;

    /**
     * 上级主键.
     */
    private Long parentId;
    private String parentName;

    /**
     * 应用编号.
     */
    private Long appId;
    // ----------------------------------------------------------------
    // 可全部抽取之后存入Set达到去重效果: code相同视为一致
    // ----------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Feature feature = (Feature) o;
        return code.equals(feature.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
