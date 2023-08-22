package cn.gmlee.tools.gray.assist;

public class SqlAssist {
    public static final String SELECT_COLUMNS_ORACLE = "SELECT table_name, column_name, data_type, data_length FROM USER_TAB_COLUMNS WHERE TABLE_NAME LIKE '%'";
    public static final String SELECT_COLUMNS_MYSQL = "SELECT table_name, column_name, data_type, character_maximum_length FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME LIKE '%'";
    public static final String ADD_COLUMNS_ORACLE = "ALTER TABLE \"%s\".\"%s\" ADD (\"env\" NUMBER(2,0) DEFAULT 1)";
    public static final String ADD_COLUMNS_COMMENT_ORACLE = "COMMENT ON COLUMN \"%s\".\"%s\".\"env\" IS '环境: 0测试1生产; 默认1'";
    public static final String ADD_COLUMNS_MYSQL = "ALTER TABLE `%s`.`%s` ADD COLUMN `env` tinyint(2) NULL DEFAULT 1 COMMENT '环境: 0测试1生产; 默认1'";
}
