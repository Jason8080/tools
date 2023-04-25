package cn.gmlee.tools.datalog.model;

import lombok.Data;

/**
 * 使用ApiPrint标注的信息
 *
 * @author Jas°
 * @date 2021/4/8 (周四)
 */
@Data
public class LogUser extends LogApi {
    protected Long userId;
    protected String username;
}
