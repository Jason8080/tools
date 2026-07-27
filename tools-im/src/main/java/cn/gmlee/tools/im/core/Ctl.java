package cn.gmlee.tools.im.core;

import cn.gmlee.tools.base.mod.R;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

/**
 * 控制器
 *
 * @param <T> the type parameter
 */
public interface Ctl<T> {
    /**
     * 推送.
     *
     * @param queue     队列名称
     * @param urlParams 路径参数
     * @param t         推送内容
     * @return r 返回结果
     */
    default R<?> push(String queue, MultiValueMap<String, String> urlParams, T t) {
        return R.OK;
    }

    /**
     * 拉取.
     *
     * @param queue     队列名称
     * @param urlParams 路径参数
     * @return flux 拉取内容
     */
    default Flux<R<T>> pull(String queue, MultiValueMap<String, String> urlParams) {
        return Flux.empty();
    }
}
