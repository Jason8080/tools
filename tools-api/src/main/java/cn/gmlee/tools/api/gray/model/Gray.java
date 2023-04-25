package cn.gmlee.tools.api.gray.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 灰度发布配置
 * <p>
 * 注意: 任何没有匹配成功的配置均不启用灰度, 并打印灰度启用失败的日志
 * </p>
 *
 * @author Jas°
 */

@Data
public final class Gray {

    public static final int MAX_WEIGHT = 100;

    /**
     * 灰度Api的路径规则
     * 默认: http://**
     */
    private String urlPatterns = "**";

    /**
     * 灰度Api的版本号
     * 默认: 1.0.0-GRAY
     */
    private String version = "1.0.0-GRAY";
    /**
     * 灰度Api的规则: ips/tokens/weight
     * 默认: weight
     */
    private String rule = "weight";
    /**
     * ips 灰度数据
     */
    private List<String> ips = new ArrayList();
    /**
     * tokens 灰度数据
     */
    private List<String> tokens = new ArrayList();
    /**
     * weight 灰度数据 (单位: %)
     * 默认: 1
     */
    private Integer weight = 1;
}
