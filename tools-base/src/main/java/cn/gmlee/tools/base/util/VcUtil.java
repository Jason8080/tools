package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.mod.Kv;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;

/**
 * 验证码工具.
 *
 * @author Jas
 */
public class VcUtil {
    private static final SecureRandom random = new SecureRandom();

    private static final char[] chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * 存储VC检测结果: true: 已通过检测, false: 没有通过检测
     */
    private static final ThreadLocal<Boolean> vc = new ThreadLocal();

    /**
     * 设置检测结果.
     *
     * @param result the result
     */
    public static void setCheckResult(Boolean result) {
        vc.set(result);
    }

    /**
     * 获取检测结果.
     * <p>
     * null: 没有检测
     * false: 检测不通过
     * true: 检测通过
     * </p>
     *
     * @param result the result
     * @return the check result
     */
    public static Boolean getCheckResult(Boolean result) {
        return vc.get();
    }

    /**
     * 是否通过检测.
     *
     * @return the boolean
     */
    public static boolean isOk() {
        return BoolUtil.isTrue(vc.get());
    }


    /**
     * 清理数据.
     */
    public static void remove() {
        vc.remove();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 生成随机验证码.
     *
     * @param length  长度
     * @param exclude 排除内容
     * @return string string
     */
    public static String generate(int length, Character... exclude) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            int index = random.nextInt(chars.length);
            Character code = chars[index];
            if (BoolUtil.notContainOne(exclude, code)) {
                sb.append(code);
            }
        }
        return sb.toString();
    }

    /**
     * 生成4位随机验证码.
     *
     * @param exclude the exclude
     * @return the string
     */
    public static String generate(Character... exclude) {
        return generate(4, exclude);
    }

    /**
     * 生成图片验证码.
     *
     * @param content 图片内容
     * @param width   宽度
     * @param height  高度
     * @return 图片 buffered image
     */
    public static BufferedImage generateImage(String content, int width, int height) {
        // 创建一张新图片
        BufferedImage image = ImgUtil.generate(width, height);
        // 设置图片背景色
        ImgUtil.setBgColor(image);
        // 设置图片的内容 (需要在背景色后面设置内容, 否则被覆盖)
        ImgUtil.setContent(image, content);
        // 设置图片干扰线
        ImgUtil.drawRandomLine(image);
        // 扭曲(折断)图片
        ImgUtil.shear(image);
        // 设置图片的边框
        ImgUtil.setBorder(image);
        // 设置图片噪点率
        ImgUtil.setNoise(image);
        return image;
    }


    /**
     * 生成4位长度的图片验证码.
     *
     * @param width  the width
     * @param height the height
     * @return the buffered image
     */
    public static Kv<String, BufferedImage> generateImage(int width, int height) {
        String vc = generate(4);
        return new Kv(vc, generateImage(vc, width, height));
    }


    /**
     * 生成BASE64的4位长度图片验证码.
     *
     * @param width  the width
     * @param height the height
     * @return the string
     */
    public static Kv<String, String> generateBase64(int width, int height) {
        String vc = generate(4);
        BufferedImage image = generateImage(vc, width, height);
        return new Kv(vc, ImgUtil.base64(image));
    }

    /**
     * 生成BASE64的指定长度图片验证码.
     *
     * @param len    the len
     * @param width  the width
     * @param height the height
     * @return the string
     */
    public static Kv<String, String> generateBase64(int len, int width, int height) {
        String vc = generate(len);
        BufferedImage image = generateImage(vc, width, height);
        return new Kv(vc, ImgUtil.base64(image));
    }
}
