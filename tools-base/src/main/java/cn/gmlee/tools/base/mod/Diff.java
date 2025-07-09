package cn.gmlee.tools.base.mod;

import cn.gmlee.tools.base.util.BoolUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


/**
 * 差异类型说明.
 * 新增: 旧数据为空, 新数据不为空
 * 删除: 旧数据不为空, 新数据为空
 * 修改: isSame() 为 false
 * 保持: isSame() 为 true
 *
 * @param <T> the type parameter
 */
@Data
public class Diff<T> implements Serializable {
    /**
     * 差异类型枚举.
     */
    public enum Enums {
        /**
         * 新增.
         */
        ADD,
        /**
         * 删除.
         */
        DEL,
        /**
         * 修改.
         */
        UPD,
        /**
         * 保持.
         */
        SAME,
    }

    /**
     * key
     */
    private String item;
    /**
     * The Source.
     */
    public final T source;
    /**
     * The Target.
     */
    public final T target;
    /**
     *
     */
    private List<Diff> subset;

    /**
     * Instantiates a new Diff.
     *
     * @param item   the item
     * @param source the source
     * @param target the target
     */
    public Diff(String item, T source, T target) {
        this.item = item;
        this.source = source;
        this.target = target;
    }


    /**
     * Is same boolean.
     *
     * @return the boolean
     */
    public boolean isSame() {
        return Objects.equals(source, target);
    }

    /**
     * Is basic boolean.
     *
     * @return boolean
     */
    public boolean isBasic() {
        return BoolUtil.isBaseClass(source, String.class, Number.class) && BoolUtil.isBaseClass(target, String.class, Number.class);
    }

    /**
     * Gets enums.
     *
     * @return the enums
     */
    public Enums getEnums() {
        if(isSame()){
            return Enums.SAME;
        }
        return source == null ? Enums.ADD : target == null ? Enums.DEL : Enums.UPD;
    }
}
