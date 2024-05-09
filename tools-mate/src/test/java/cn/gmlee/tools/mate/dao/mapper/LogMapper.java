package cn.gmlee.tools.mate.dao.mapper;

import cn.gmlee.tools.base.anno.DataScope;
import cn.gmlee.tools.base.anno.DataFilter;
import cn.gmlee.tools.mate.dao.entity.Log;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 日志提交记录 Mapper 接口
 * </p>
 *
 * @author Jas°
 * @since 2022-07-11
 */
public interface LogMapper extends BaseMapper<Log> {
    @DataScope(row = "row_sys_auth", col = "column-auth-user")
    @Select("select log.* from log log left join user u on log.created_by = u.id where log.request_ip = #{requestIp}")
    Page<Log> list(Page page, @Param("requestIp") String host);
}
