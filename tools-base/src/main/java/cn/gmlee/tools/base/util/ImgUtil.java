package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Int;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * 通用图片处理工具
 *
 * @author Jas °
 * @date 2020 /10/10 (周六)
 */
public class ImgUtil {
    /**
     * 默认的图片类型
     */
    public static final List<String> suffix =
            Arrays.asList(".png",".jpg",".jpeg",".gif",".bmp",".tif",".tiff");

    private static final Random random = new Random();

    /**
     * 图片字节数组转base64字符.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String bytes2base64(byte[] bytes) {
        byte[] encode = Base64.getEncoder().encode(bytes);
        return new String(encode);
    }

    /**
     * base64字符转图片字节数组.
     *
     * @param base64 the base 64
     * @return the byte [ ]
     */
    public static byte[] base642bytes(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * 图片字节数组转base64字符 (自动解析类型).
     * <p>
     * 自动解析类型后前端可直接展示.
     * </p>
     *
     * @param bytes       the bytes
     * @param contentType the content type
     * @return the string
     */
    @Deprecated
    public static String bytes2base64(byte[] bytes, String contentType) {
        byte[] encode = Base64.getEncoder().encode(bytes);
        String base64 = new String(encode);
        return "data:" + contentType + ";base64," + base64;
    }

    /**
     * base64字符转图片字节数组.
     *
     * @param base64     the base 64
     * @param trimBase64 the trim base 64
     * @return the byte [ ]
     */
    @Deprecated
    public static byte[] base642bytes(String base64, boolean trimBase64) {
        if (trimBase64) {
            base64 = base64.split(";base64,")[1];
        }
        return Base64.getDecoder().decode(base64);
    }


    /**
     * 创建图片.
     *
     * @param width  the width
     * @param height the height
     * @return the buffered image
     */
    public static BufferedImage generate(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * 写入内容.
     *
     * @param image   the image
     * @param content the content
     */
    public static void setContent(BufferedImage image, String content) {
        Graphics graphics = image.getGraphics();
        int width = image.getWidth();
        int height = image.getHeight();
        // 字体大小为图片高度的80%
        int size = (int) (height * 0.8);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));
        int x = 0, y = 0;
        for (int i = 0; i < content.length(); i++) {
            // 随机每个字符高低
            y = (int) ((Math.random() * 0.1 + 0.5) * height);
            graphics.setColor(getRandomColor());
            graphics.drawString(content.charAt(i) + "", x, y);
            //依据宽度浮动
            x += (width / content.length()) * (Math.random() * 0.3 + 0.8);
        }
    }

    /**
     * 设置背景噪点率.
     *
     * @param image 图片
     * @param rate  噪点率
     */
    public static void setNoise(BufferedImage image, float rate) {
        int noiseArea = (int) (rate * image.getWidth() * image.getHeight());
        for (int i = 0; i < noiseArea; i++) {
            int xxx = random.nextInt(image.getWidth());
            int yyy = random.nextInt(image.getHeight());
            int rgb = getRandomColor().getRGB();
            image.setRGB(xxx, yyy, rgb);
        }
    }

    /**
     * 设置背景0.05噪点率.
     *
     * @param image the image
     */
    public static void setNoise(BufferedImage image) {
        setNoise(image, 0.05f);
    }

    private static Color getRandomColor() {
        //使用之前首先清空内容
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Int.THREE; i++) {
            String hax = Integer.toHexString(random.nextInt(0xFF));
            if (hax.length() == 1) {
                hax = "0" + hax;
            }
            sb.append(hax);
        }
        return Color.decode("#" + sb);
    }

    /**
     * 设置图片的背景色
     *
     * @param image the image
     */
    public static void setBgColor(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        // 设置颜色
        graphics.setColor(Color.WHITE);
        // 填充区域
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * 设置图片的边框
     *
     * @param image the image
     */
    public static void setBorder(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        // 设置边框颜色
        // graphics.setColor(Color.BLUE)
        // 边框区域
        graphics.drawRect(1, 1, image.getWidth() - 2, image.getHeight() - 2);
    }

    /**
     * 在图片上画随机线条
     *
     * @param image 图片
     * @param line  线条数
     */
    public static void drawRandomLine(BufferedImage image, int line) {
        Graphics graphics = image.getGraphics();
        int x1 = random.nextInt(4), y1 = 0;
        int x2 = image.getWidth() - random.nextInt(4), y2 = 0;
        for (int i = 0; i < line; i++) {
            graphics.setColor(getRandomColor());
            y1 = random.nextInt(image.getHeight() - random.nextInt(4));
            y2 = random.nextInt(image.getHeight() - random.nextInt(4));
            graphics.drawLine(x1, y1, x2, y2);
        }
    }


    /**
     * 在图片上画随机线条.
     *
     * @param image the image
     */
    public static void drawRandomLine(BufferedImage image) {
        drawRandomLine(image, Int.FOUR);
    }

    /**
     * 扭曲/折断图片.
     *
     * @param image the image
     */
    public static void shear(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        shearX(graphics, image.getWidth(), image.getHeight(), graphics.getColor());
        shearY(graphics, image.getWidth(), image.getHeight(), graphics.getColor());
    }

    private static void shearX(Graphics graphics, int w1, int h1, Color color) {
        Random random = new Random();
        int period = 2;
        boolean borderGap = true;
        int frames = 1;
        int phase = random.nextInt(2);
        for (int i = 0; i < h1; i++) {
            double d = (double) (period >> 1) * Math.sin((double) i / (double) period
                    + (2.2831853071795862D * (double) phase) / (double) frames);
            graphics.copyArea(0, i, w1, 1, (int) d, 0);
            if (borderGap) {
                graphics.setColor(color);
                graphics.drawLine((int) d, i, 0, i);
                graphics.drawLine((int) d + w1, i, w1, i);
            }
        }
    }

    private static void shearY(Graphics g, int w1, int h1, Color color) {
        Random random = new Random();
        int period = random.nextInt(40) + 10;
        boolean borderGap = true;
        int frames = 20;
        int phase = random.nextInt(2);
        for (int i = 0; i < w1; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period
                    + (2.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(i, (int) d, i, 0);
                g.drawLine(i, (int) d + h1, i, h1);
            }
        }
    }

    /**
     * 写出图片.
     *
     * @param image the image
     * @param out   the out
     * @throws IOException the io exception
     */
    public static void write(RenderedImage image, OutputStream out) throws IOException {
        ImageIO.write(image, "png", out);
    }

    /**
     * 图片转BASE64字符.
     *
     * @param image the image
     * @return the string
     */
    public static String base64(RenderedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            ExceptionUtil.cast("图片写出异常", e);
        }
        return bytes2base64(out.toByteArray());
    }
}
