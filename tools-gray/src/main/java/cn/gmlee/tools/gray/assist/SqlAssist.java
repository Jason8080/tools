package cn.gmlee.tools.gray.assist;

public class SqlAssist {
    public static final String SELECT_COLUMNS_ORACLE = "SELECT column_name, data_type, data_length FROM user_tab_columns WHERE table_name='%s'";
    public static final String SELECT_COLUMNS_MYSQL = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '%s'";
    public static final String ADD_COLUMNS_ORACLE = "ALTER TABLE \"%s\".\"%s\" ADD (\"env\" NUMBER(2,0) DEFAULT 1); COMMENT ON COLUMN \"%s\".\"%s\".\"env\" IS '环境: 0测试1生产; 默认1'";
    public static final String ADD_COLUMNS_MYSQL = "ALTER TABLE `%s`.`%s` ADD COLUMN `env` tinyint(2) NULL DEFAULT 1 COMMENT '环境: 0测试1生产; 默认1'";
}
