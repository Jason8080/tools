package cn.gmlee.tools.base;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Jas°
 * @date 2021/2/23 (周二)
 */
@Data
@XmlRootElement
public class Student implements Serializable {
    private String name = "李xx";
    private Integer sex = 1;
}
