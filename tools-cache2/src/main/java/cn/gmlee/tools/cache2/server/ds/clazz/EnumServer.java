package cn.gmlee.tools.cache2.server.ds.clazz;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.enums.DataType;
import cn.gmlee.tools.cache2.server.ds.AbstractDsServer;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class EnumServer extends AbstractDsServer implements ClazzServer {

    @Override
    public boolean support(Cache cache) {
        return DataType.ENUMS.equals(cache.dataType());
    }

    @Override
    protected List<Map<String, Object>> list(Cache cache, Object result, Field field) {
        Class<?> clazz = ExceptionUtil.sandbox(() -> Class.forName(cache.target()));
        AssertUtil.notNull(clazz, "枚举类不存在");
        AssertUtil.isTrue(clazz.isEnum(), "不是枚举类字节码");
        Class<? extends Enum> eClazz = (Class<? extends Enum>) clazz;
        Enum[] enums = eClazz.getEnumConstants();
        return Arrays.stream(NullUtil.get(enums, () -> new Enum[0])).map(ClassUtil::generateCurrentMap).collect(Collectors.toList());
    }
}
