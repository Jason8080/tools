package cn.gmlee.tools.cache2.config;

import cn.gmlee.tools.cache2.enums.DataType;

import java.io.Serializable;

/**
 * Cache2参数
 */
public interface Cache2Conf extends Serializable {
    // ------------------------------------
    boolean isLog();
    int getDepth();
    // ------------------------------------
    DataType getDataType();
    Boolean getEnable();
    Long getExpire();
    // ------------------------------------
    String getTable();
    String getKey();
    String getGet();
    String getWhere();
    // ------------------------------------
}
