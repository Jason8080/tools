package cn.gmlee.tools.logback.db;

import cn.gmlee.tools.base.entity.Id;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用日志持久化实体
 *
 * @author Jas°
 * @date 2021/3/8 (周一)
 */
@Data
public class Log extends Id implements Serializable {
    private String msg;
    private String throwableMsg;
}
