package cn.gmlee.tools.cloud.fallback;


import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.mod.R;

/**
 * Hystrix 降级/熔断说明:
 *  客户端降级实现: 服务端手动实现远程接口的降级方案 与 接口一起发布
 *  服务端降级实现: 继承或者手动实现降级方案使用@HystrixCommand指定其方法名称
 *      也可以统一配置: @DefaultProperties(defaultFallback = "defaultFallback")
 *  客户端/服务端降级: 超时、异常、线程数、熔断
 *  客户端/服务端熔断: 降级比例达到阀值触发熔断, 并停用熔断实例指定时间, 在指定时间后释放部分请求(半熔断), 达标则重新启用实例(关闭熔断)
 * @author Jas°
 */
public class ApiFallback {
    /**
     * 服务端继承后在Controller类上加即可在失败后使用该方法快速响应(熔断).
     *
     * @return 响应实体
     */
    public R defaultFallback() {
        return R.FAIL.newly(XCode.THIRD_PARTY_FAIL);
    }
}
