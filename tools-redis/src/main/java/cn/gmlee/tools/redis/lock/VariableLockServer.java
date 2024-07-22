package cn.gmlee.tools.redis.lock;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.redis.anno.VariableLock;

/**
 * 数据加锁服务.
 */
public interface VariableLockServer {

    /**
     * 获取Key前缀.
     *
     * @return the key prefix
     */
    String getKeyPrefix();

    /**
     * 加锁.
     *
     * @param vl    the vl
     * @param value the value
     */
    void lock(VariableLock vl, String... value);

    /**
     * 解锁.
     *
     * @param vl    the vl
     * @param value the value
     */
    void unlock(VariableLock vl, String... value);


    /**
     * 获取key.
     *
     * @param vl     the vl
     * @param values the values
     * @return the key
     */
    default String getKey(VariableLock vl, String... values) {
        AssertUtil.eq(vl.value().length, values.length, "参数量不符合预期");
        StringBuilder sb = new StringBuilder(getKeyPrefix());
        // 追加业务前缀
        String biz = vl.biz();
        if(!biz.isEmpty()){
            sb.append(biz);
            sb.append(":");
        }
        for (int i = 0; i < vl.value().length; i++) {
            sb.append(vl.value()[i]);
            sb.append("_");
            sb.append(values[i]);
            if(i < vl.value().length - 1){
                sb.append("_");
            }
        }
        return sb.toString();
    }

    /**
     * 获取val.
     *
     * @param values the values
     * @return the val
     */
    default String getVal(String... values) {
        return String.join(",", values);
    }
}
