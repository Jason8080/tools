package cn.gmlee.tools.datalog.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 使用ApiPrint标注的信息
 *
 * @author Jas°
 * @date 2021/4/8 (周四)
 */
@Data
public class LogApi implements Serializable {
    protected String api;
    protected String site;
    protected String params;
    protected String requestIp;
    protected String requestUrl;
    protected Date requestTime;
}
