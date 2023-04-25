package cn.gmlee.tools.mate.dao.mapper;

import cn.gmlee.tools.mate.dao.entity.Log;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;

/**
 * <p>
 * 日志提交记录 Mapper 接口
 * </p>
 *
 * @author Jas °
 * @since 2022 -07-11
 */
public interface ExLogMapper extends BaseMapper<Log> {
    /**
     * Save integer.
     *
     * @param log the log
     * @return the integer
     */
    @Insert("insert into log (sys_name, request_params) values(#{sysName}, #{requestParams})")
    Integer save(Log log);
}
