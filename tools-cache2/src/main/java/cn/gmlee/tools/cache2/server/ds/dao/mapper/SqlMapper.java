package cn.gmlee.tools.cache2.server.ds.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 数据库脚本.
 */
public interface SqlMapper {
    /**
     * Mysql源.
     *
     * @return the list
     */
    @Select(
            "<script>" +
                    " SELECT * FROM ${table} " +
                    " <where> " +
                        " <if test=\"where!=null\"> " +
                            "${where}" +
                        " </if> " +
                    " </where> " +
            "</script>"
    )
    List<Map<String, Object>> list(@Param("table") String table, @Param("where") String where);
}
