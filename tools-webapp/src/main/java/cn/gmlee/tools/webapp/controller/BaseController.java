package cn.gmlee.tools.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 通用基础控制器
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
public class BaseController implements Serializable {
    /**
     * 日志打印
     */
    protected Logger logger = LoggerFactory.getLogger(super.getClass().getName());
}
