package cn.gmlee.tools.oss;

import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.builder.MapBuilder;
import cn.gmlee.tools.base.util.LocalDateTimeUtil;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * OSS 链接生成工具.
 */
public class OssUrl {
    /**
     * 文件上传链接.
     *
     * @param oss        客户端
     * @param bucketName 对象桶
     * @param objectName 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration   有效期
     * @return string 上传链接
     */
    public static URL upload(OSS oss, String bucketName, String objectName, int duration) {
        Date date = LocalDateTimeUtil.offsetCurrent(duration, ChronoUnit.SECONDS);
        return oss.generatePresignedUrl(bucketName, objectName, date, HttpMethod.PUT);
    }

    /**
     * 文件上传链接.
     *
     * @param oss        客户端
     * @param bucketName 对象桶
     * @param objectName 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration   有效期
     * @param headers    请求头
     * @return string 上传链接
     */
    public static URL upload(OSS oss, String bucketName, String objectName, int duration, String... headers) {
        Date date = LocalDateTimeUtil.offsetCurrent(duration, ChronoUnit.SECONDS);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.PUT);
        request.setExpiration(date);
        request.setHeaders(MapBuilder.of(headers));
        return oss.generatePresignedUrl(request);
    }

    /**
     * 文件下载链接.
     *
     * @param oss        客户端
     * @param bucketName 对象桶
     * @param objectName 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration   有效期
     * @return string 下载链接
     */
    public static URL download(OSS oss, String bucketName, String objectName, int duration) {
        Date date = LocalDateTimeUtil.offsetCurrent(duration, ChronoUnit.SECONDS);
        return oss.generatePresignedUrl(bucketName, objectName, date, HttpMethod.GET);
    }
}
