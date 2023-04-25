/*
Navicat MySQL Data Transfer

Source Server         : dev-crm
Source Server Version : 50725
Source Host           : 172.19.193.244:3306
Source Database       : db_lala_pay

Target Server Type    : MYSQL
Target Server Version : 50725
File Encoding         : 65001

Date: 2021-06-08 15:19:15
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for datalog
-- ----------------------------
DROP TABLE IF EXISTS `datalog`;
CREATE TABLE `datalog` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `start_ms` bigint(20) DEFAULT NULL COMMENT '开始记录时间',
    `total_ms` bigint(20) DEFAULT NULL COMMENT '记录完成耗时',
    `original_sql` text COMMENT '更新原始sql',
    `where_sql` text COMMENT '条件语句',
    `old_data_json` text COMMENT '旧数据',
    `new_data_json` text COMMENT '新数据',
    `datalog_select_sql` text COMMENT '查询旧数据所用语句',
    `data_table` varchar(64) DEFAULT NULL COMMENT '更新数据表名',
    `data_log` text COMMENT '更新日志',
    `api` varchar(32) DEFAULT NULL COMMENT '请求地址',
    `site` varchar(255) DEFAULT NULL COMMENT '请求位置',
    `params` text COMMENT '请求参数',
    `request_ip` varchar(16) DEFAULT NULL COMMENT '请求IP',
    `request_url` varchar(255) DEFAULT NULL COMMENT '请求url',
    `request_time` datetime DEFAULT NULL COMMENT '请求时间',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户编号',
    `username` varchar(64) DEFAULT '游客' COMMENT '用户名称',
    `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
