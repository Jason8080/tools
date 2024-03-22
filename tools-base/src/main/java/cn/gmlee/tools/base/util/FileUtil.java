package cn.gmlee.tools.base.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * 文件帮助类
 *
 * @author xiaohui
 */
@Slf4j
public class FileUtil {
    /**
     * 本地目录.
     */
    public final static String PATH = "/upload";

    /**
     * 文件存储 (SpringMVC)
     *
     * @param file 文件对象
     * @param path 文件路径
     * @param id   文件编号
     * @return boolean 是否成功
     */
    public static boolean upload(MultipartFile file, String path, String id) {
        // 判断文件是否为空，空则返回失败
        if (Objects.isNull(file) || StringUtils.isEmpty(path) || Objects.isNull(id)) {
            return false;
        }
        // 获取原文件名
        String fileName = id.concat(Objects.requireNonNull(file.getOriginalFilename()));
        // 创建文件实例
        File target = new File(path, fileName);
        // 如果文件目录不存在，创建目录
        if (!target.getParentFile().exists()) {
            boolean mkdir = target.getParentFile().mkdirs();
            AssertUtil.isTrue(mkdir, "上传目录创建失败");
            log.info(String.format("文件上传创建目录: %s", target));
        }
        // 写入文件
        try {
            file.transferTo(target);
        } catch (IOException e) {
            log.error(String.format("文件上传失败: %s", target), e);
            return false;
        }
        return true;
    }

    /**
     * 文件存储
     *
     * @param file 文件数据
     * @param path 文件的存储路径
     * @param name 生成的文件名
     */
    public static void uploadFile(byte[] file, String path, String name) {
        File targetFile = new File(path);
        if (!targetFile.exists()) {
            boolean mkdir = targetFile.mkdirs();
            AssertUtil.isTrue(mkdir, "上传目录创建失败");
        }
        ExceptionUtil.suppress(() -> {
            FileOutputStream out = new FileOutputStream(path.concat(name));
            out.write(file);
            out.close();
        });
    }

    /**
     * 文件上传
     *
     * @param request 请求对象
     * @param path    上传路径
     * @return map 上传结果 (K: 文件名 V: 文件编号[雪花])
     */
    public static Map<String, String> upload(MultipartHttpServletRequest request, String path) {
        MultiValueMap<String, MultipartFile> fileMap = request.getMultiFileMap();
        if (BoolUtil.isEmpty(fileMap)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        fileMap.forEach((k, v) -> {
            String id = String.valueOf(IdUtil.snowflakeId());
            boolean success = upload((MultipartFile) v, path, id);
            QuickUtil.isTrue(success, () -> map.put(((MultipartFile) v).getOriginalFilename(), id));
        });
        return map;
    }


    /**
     * 本地存储
     *
     * @param request 请求对象
     * @param url     文件链接
     * @param name    文件名称
     * @return string 绝对路径
     */
    public static String localStorage(HttpServletRequest request, String url, String name) {
        ByteArrayOutputStream out = UrlUtil.download(url);
        String path = request.getServletContext().getRealPath(PATH);
        uploadFile(out.toByteArray(), path, name);
        return path.endsWith(File.separator) ? path.concat(name) : path.concat(File.separator).concat(name);
    }

    /**
     * 本地存储
     *
     * @param request 请求对象
     * @param base64  文件编码
     * @return string 绝对路径
     */
    public static String localStorage(HttpServletRequest request, String base64) {
        String path = request.getServletContext().getRealPath(PATH);
        byte[] bytes = Base64.getDecoder().decode(base64);
        String name = Md5Util.encode(bytes);
        uploadFile(bytes, path, name);
        return path.endsWith(File.separator) ? path.concat(name) : path.concat(File.separator).concat(name);
    }

    /**
     * 生成缩略图
     *
     * @param base64 编码内容
     * @param pixels 新图宽度 (高度按比例变化)
     * @return 上传成功后的文件名 string
     */
    public static Map<String, ByteArrayOutputStream> generateThumbnail(String base64, int... pixels) {
        Map<String, ByteArrayOutputStream> outMap = new HashMap<>();
        for (int i = 0; pixels != null && i < pixels.length; i++) {
            outMap.put(String.valueOf(pixels[i]), resizeImage(base64, "png", pixels[i]));
        }
        return outMap;
    }

    /**
     * 生成缩略图
     *
     * @param base64 编码内容
     * @param pixel  新图宽度 (高度按比例变化)
     * @return 上传成功后的文件名 string
     */
    public static ByteArrayOutputStream generateThumbnail(String base64, int pixel) {
        return resizeImage(base64, "png", pixel);
    }

    /**
     * 生成缩略图
     *
     * @param bytes 字节数组
     * @param pixel 新图宽度 (高度按比例变化)
     * @return 上传成功后的文件名 string
     */
    public static ByteArrayOutputStream generateThumbnail(byte[] bytes, int pixel) {
        return resizeImage(bytes, "png", pixel);
    }

    /**
     * 图片重置
     *
     * @param base64 编码内容
     * @param format 新图格式
     * @param size   新图宽度 (高度按比例变化)
     * @return byte 字节流
     */
    public static ByteArrayOutputStream resizeImage(String base64, String format, int size) {
        return resizeImage(Base64.getDecoder().decode(base64), format, size);
    }

    /**
     * 图片重置
     *
     * @param bytes  字节数组
     * @param format 新图格式
     * @param size   新图宽度 (高度按比例变化)
     * @return byte 字节流
     */
    public static ByteArrayOutputStream resizeImage(byte[] bytes, String format, int size) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = ExceptionUtil.suppress(() -> ImageIO.read(in));
        double width = img.getWidth();
        double height = img.getHeight();
        double percent = size / width;
        int newWidth = (int) (width * percent);
        int newHeight = (int) (height * percent);
        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = image.createGraphics();
        graphics.drawImage(img, 0, 0, newWidth, newHeight, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExceptionUtil.suppress(() -> ImageIO.write(image, format, out));
        return out;
    }
}
