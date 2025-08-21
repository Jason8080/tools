package cn.gmlee.tools.base.mod;

import lombok.Data;

@Data
public class Mode {
    private Boolean enableSearch = Boolean.FALSE; // 是否开启搜索
    private Boolean enableThinking = Boolean.FALSE; // 是否开启思考
    private Boolean hasThoughts = Boolean.FALSE; // 是否展示意图
    private String audioFormat = "pcm"; // 默认音频格式
    private String spec = "720*1280"; // 默认分辨率
    private Integer duration = 5; // 默认视频时长
    private Integer num = 1; // 默认图片数量
    private Integer seed = Integer.MAX_VALUE; // 默认随机数
}