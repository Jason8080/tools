package cn.gmlee.tools.api.coexist;

import cn.gmlee.tools.api.anno.ApiCoexist;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;

/**
 * 过滤器配置
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
public class ApiCoexistCondition implements RequestCondition<ApiCoexistCondition> {

    private final String version;

    public ApiCoexistCondition(String version) {
        this.version = version;
    }

    @Override
    public ApiCoexistCondition combine(ApiCoexistCondition apiCoexistCondition) {
        return new ApiCoexistCondition(apiCoexistCondition.version);
    }

    @Override
    public ApiCoexistCondition getMatchingCondition(HttpServletRequest request) {
        String version = request.getHeader(ApiCoexist.VERSION_NAME);
        if (this.version.equalsIgnoreCase(version)) {
            return this;
        }
        return null;
    }

    @Override
    public int compareTo(ApiCoexistCondition other, HttpServletRequest request) {
        return this.version.compareTo(other.version);
    }
}
