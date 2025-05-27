package cn.gmlee.tools.oss;

import cn.gmlee.tools.base.builder.MapBuilder;
import cn.gmlee.tools.base.util.*;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OSS 链接生成工具.
 */
public class OssUrl {

    /**
     * 获取文件链接.
     *
     * @param oss     the oss
     * @param request the request
     * @return the url
     */
    public static URL get(OSS oss, GeneratePresignedUrlRequest request) {
        return oss.generatePresignedUrl(request);
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
     * 文件上传链接.
     *
     * @param oss        客户端
     * @param bucketName 对象桶
     * @param objectName 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration   有效期
     * @param headers    请求头
     * @return string 上传链接
     */
    public static URL upload(OSS oss, String bucketName, String objectName, int duration, Map<String, String> headers) {
        Date date = LocalDateTimeUtil.offsetCurrent(duration, ChronoUnit.SECONDS);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.PUT);
        request.setExpiration(date);
        request.setHeaders(headers);
        request.setContentType(CollectionUtil.getIgnoreCase(headers, HttpUtil.CONTENT_TYPE));
        return oss.generatePresignedUrl(request);
    }

    /**
     * 文件下载链接.
     *
     * @param oss        客户端
     * @param bucketName 对象桶
     * @param objectName 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration   有效期
     * @param headers    请求头
     * @return string 下载链接
     */
    public static URL download(OSS oss, String bucketName, String objectName, int duration, Map<String, String> headers) {
        if (!exist(oss, bucketName, objectName)) {
            return null;
        }
        Date date = LocalDateTimeUtil.offsetCurrent(duration, ChronoUnit.SECONDS);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);
        request.setExpiration(date);
        request.setHeaders(headers);
        request.setContentType(CollectionUtil.getIgnoreCase(headers, HttpUtil.CONTENT_TYPE));
        return oss.generatePresignedUrl(request);
    }


    /**
     * 文件下载链接.
     *
     * @param oss        客户端
     * @param bucketName 对象桶
     * @param objectName 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration   有效期
     * @param headers    请求头
     * @return string 下载链接
     */
    public static URL download(OSS oss, String bucketName, String objectName, int duration, String... headers) {
        if (!exist(oss, bucketName, objectName)) {
            return null;
        }
        Date date = LocalDateTimeUtil.offsetCurrent(duration, ChronoUnit.SECONDS);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);
        request.setExpiration(date);
        request.setHeaders(MapBuilder.of(headers));
        return oss.generatePresignedUrl(request);
    }


    /**
     * 文件下载链接.
     *
     * @param oss         客户端
     * @param bucketName  对象桶
     * @param objectNames 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration    有效期
     * @param headers     请求头
     * @return string 下载链接
     */
    public static List<URL> download(OSS oss, String bucketName, List<String> objectNames, int duration, String... headers) {
        if (BoolUtil.isEmpty(objectNames)) {
            return Collections.emptyList();
        }
        return objectNames.parallelStream().filter(Objects::nonNull)
                .map(objectName ->
                        ExceptionUtil.sandbox(() -> download(oss, bucketName, objectName, duration, headers), true))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * 文件下载链接.
     *
     * @param oss         客户端
     * @param bucketName  对象桶
     * @param objectNames 路径名 (填写Object完整路径,不能包含Bucket名称)
     * @param duration    有效期
     * @param headers     请求头
     * @return string 下载链接
     */
    public static List<URL> download(OSS oss, String bucketName, List<String> objectNames, int duration, Map<String, String> headers) {
        if (BoolUtil.isEmpty(objectNames)) {
            return Collections.emptyList();
        }
        return objectNames.parallelStream().filter(Objects::nonNull)
                .map(objectName ->
                        ExceptionUtil.sandbox(() -> download(oss, bucketName, objectName, duration, headers), true))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Exist boolean.
     *
     * @param oss        the oss
     * @param bucketName the bucket name
     * @param objectName the object name
     * @return the boolean
     */
    public static boolean exist(OSS oss, String bucketName, String objectName) {
        return oss.doesObjectExist(bucketName, objectName);
    }
}
