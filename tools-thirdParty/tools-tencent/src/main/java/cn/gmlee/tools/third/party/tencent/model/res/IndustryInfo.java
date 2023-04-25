package cn.gmlee.tools.third.party.tencent.model.res;

import lombok.Data;

import java.io.Serializable;

/**
 * 模板消息所属行业响应实体
 *
 * @author Jas °
 * @date 2021 /2/22 (周一)
 */
@Data
public class IndustryInfo implements Serializable {
    /**
     * 主营
     */
    private Industry primary_industry;
    /**
     * 副营
     */
    private Industry secondary_industry;

    @Data
    public static class Industry implements Serializable {
        /**
         * 主营
         */
        private String first_class;
        /**
         * 副营
         */
        private String second_class;
    }
}
