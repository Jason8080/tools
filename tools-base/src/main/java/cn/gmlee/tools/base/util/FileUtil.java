package cn.gmlee.tools.base.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * 文件帮助类
 *
 * @author xiaohui
 */
public class FileUtil {

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    ///上传图片路径
    public final static String PATH = "/upload";

    private final static int xhdpi = 660;
    private final static int mhdpi = 1280;

    /**
     * SpringMVC 文件上传专用工具
     *
     * @param file     不为空
     * @param filePath 不为空
     * @param fileId   不为空
     * @return 上传是否成功
     */
    public static boolean upload(MultipartFile file, String filePath, String fileId) {
        // 判断文件是否为空，空则返回失败
        if (Objects.isNull(file) || StringUtils.isEmpty(filePath) || Objects.isNull(fileId)) {
            return false;
        }
        // 获取原文件名
        String fileName = fileId.concat(file.getOriginalFilename());
        // 创建文件实例
        File target = new File(filePath, fileName);
        // 如果文件目录不存在，创建目录
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
            logger.info(String.format("文件上传创建目录: %s", target));
        }
        // 写入文件
        try {
            file.transferTo(target);
        } catch (IOException e) {
            logger.error(String.format("文件上传失败: %s", target), e);
            return false;
        }
        return true;
    }

    /**
     * 上传图片，以文件的形式上传，对图片进行压缩，会生成多个分辨率的图片
     *
     * @param file     文件数据
     * @param filePath 文件的存储路径
     * @param fileName 生成的文件名
     * @throws Exception
     */
    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {

        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
        out.close();
//		System.out.println(filePath+PATH + "/"+fileName);
//		String upNameXhdpi = "xhdpi-" + fileName;
//		String upNameMhdpi = "mhdpi-" + fileName;
//
//		FileOutputStream outXhdpi = new FileOutputStream(filePath+ "/" +upNameXhdpi);
//		FileOutputStream outMhdpi = new FileOutputStream(filePath+ "/" +upNameMhdpi);
//
//		resizeImageFile(file, outXhdpi, xhdpi, extension);
//		resizeImageFile(file, outMhdpi, mhdpi, extension);
    }

    /**
     * 上传文件，以文件的形式，不会对图片进行压缩
     *
     * @param file         要上传的文件
     * @param fileFileName 文件名
     * @return 上传成功后的文件名
     */
    public static String upload(HttpServletRequest request, File file, String fileFileName) throws Exception {
        String dir = request.getSession().getServletContext().getRealPath("/");
        File fileLocation = new File(dir);
        if (!fileLocation.exists()) {
            boolean isCreated = fileLocation.mkdir();
            if (!isCreated) {
                //目标上传目录创建失败,可做其他处理,例如抛出自定义异常等,一般应该不会出现这种情况。
                return null;
            }
        }
        //      fileFileName = UUID.randomUUID().toString()+System.currentTimeMillis()+fileFileName;
        fileFileName = fileFileName;

        InputStream is = new FileInputStream(file);

        OutputStream os = new FileOutputStream(dir + "/" + fileFileName);


        // 因为file是存放在临时文件夹的文件，我们可以将其文件名和文件路径打印出来，看和之前的fileFileName是否相同

        byte[] buffer = new byte[500];
        int length = 0;

        while (-1 != (length = is.read(buffer, 0, buffer.length))) {
            os.write(buffer);
        }

        os.close();
        is.close();

        return fileFileName;
    }


    /**
     * 将网络资源的图片下载到本地，比如说将微信用户头像下载到本地
     *
     * @param request
     * @param fileUrl 网络资源的路径
     * @return
     */
    public static String uploadGrabUrl(HttpServletRequest request, String fileUrl) {
        //获取文件名，文件名实际上在URL中可以找到
        String fileName = UUID.randomUUID().toString() + System.currentTimeMillis() + ".jpg";
        //这里服务器上要将此图保存的路径
        String savePath = request.getServletContext().getRealPath(PATH);
        System.out.println();
        try {
            URL url = new URL(fileUrl);/*将网络资源地址传给,即赋值给url*/
            /*此为联系获得网络资源的固定格式用法，以便后面的in变量获得url截取网络资源的输入流*/
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            /*此处也可用BufferedInputStream与BufferedOutputStream*/
            DataOutputStream out = new DataOutputStream(new FileOutputStream(savePath + "\\" + fileName));
            /*将参数savePath，即将截取的图片的存储在本地地址赋值给out输出流所指定的地址*/
            byte[] buffer = new byte[4096];
            int count = 0;
            /*将输入流以字节的形式读取并写入buffer中*/
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();/*后面三行为关闭输入输出流以及网络资源的固定格式*/
            in.close();
            connection.disconnect();
            //返回内容是文件名
            return fileName;/*网络资源截取并存储本地成功返回true*/

        } catch (Exception e) {
            System.out.println(e + fileUrl + savePath);
            return null;
        }
    }

    /**
     * 上传图片，以base64的形式
     *
     * @param base64 图片编码后的字符串
     * @return 上传成功后的文件名
     */
    public static String upload(HttpServletRequest request, String base64) {
        String dir = request.getServletContext().getRealPath(PATH);
        File fileLocation = new File(dir);
        if (!fileLocation.exists()) {
            boolean isCreated = fileLocation.mkdir();
            if (!isCreated) {
                //目标上传目录创建失败,可做其他处理,例如抛出自定义异常等,一般应该不会出现这种情况。
                return null;
            }
        }
        if (base64.indexOf("jpeg") != -1) {
            //存在jpeg文件的情况
            base64 = base64.replaceFirst("jpeg", "jpg");
        }
        String upName = UUID.randomUUID().toString() + System.currentTimeMillis() + "." + base64.substring(11, 14);
        FileOutputStream out;
        String iconBase64 = base64.substring(22);
        try {
            byte[] buffer = Base64.getDecoder().decode(iconBase64);
            out = new FileOutputStream(dir + "/" + upName);
            out.write(buffer);
            out.close();
            return upName;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 上传图片，会生成多个分辨率的图片
     *
     * @param base64 图片编码后的字符串
     * @return 上传成功后的文件名
     */
    public static String uploadResize(HttpServletRequest request, String base64) {
        String dir = request.getServletContext().getRealPath(PATH);
        if (base64.indexOf("jpeg") != -1) {
            //存在jpeg文件的情况
            base64 = base64.replaceFirst("jpeg", "jpg");
        }
        File fileLocation = new File(dir);
        String fileType = base64.substring(11, 14);
        if (!fileLocation.exists()) {
            boolean isCreated = fileLocation.mkdir();
            if (!isCreated) {
                //目标上传目录创建失败,可做其他处理,例如抛出自定义异常等,一般应该不会出现这种情况。
                return null;
            }
        }

        String tempStr = UUID.randomUUID().toString() + System.currentTimeMillis();

        String upName = tempStr + "." + fileType;
        FileOutputStream out;
        String iconBase64 = base64.substring(22);
        try {
            byte[] buffer = Base64.getDecoder().decode(iconBase64);
            out = new FileOutputStream(dir + "/" + upName);
            out.write(buffer);
            out.close();

            String upNameXhdpi = "xhdpi-" + upName;
            String upNameMhdpi = "mhdpi-" + upName;

            FileOutputStream outXhdpi = new FileOutputStream(dir + "/" + upNameXhdpi);
            FileOutputStream outMhdpi = new FileOutputStream(dir + "/" + upNameMhdpi);

            resizeImage(iconBase64, outXhdpi, xhdpi, fileType);
            resizeImage(iconBase64, outMhdpi, mhdpi, fileType);

            return upName;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 改变图片的大小到宽为size，然后高随着宽等比例变化
     *
     * @param is     上传的图片的输入流
     * @param os     改变了图片的大小后，把图片的流输出到目标OutputStream
     * @param size   新图片的宽
     * @param format 新图片的格式
     * @throws IOException
     */
    public static void resizeImage(String is, OutputStream os, int size, String format) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(is));
        ByteArrayInputStream in2 = new ByteArrayInputStream(Base64.getDecoder().decode(is));
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in2.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        System.out.println(out.length());
        BufferedImage prevImage = ImageIO.read(in);
        double width = prevImage.getWidth();
        double height = prevImage.getHeight();
        double percent = size / width;
        int newWidth = (int) (width * percent);
        int newHeight = (int) (height * percent);
        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = image.createGraphics();
        graphics.drawImage(prevImage, 0, 0, newWidth, newHeight, null);
        ImageIO.write(image, format, os);
        os.flush();
        in.close();
        os.close();
    }

    /**
     * 改变图片的大小到宽为size，然后高随着宽等比例变化
     *
     * @param os     改变了图片的大小后，把图片的流输出到目标OutputStream
     * @param size   新图片的宽
     * @param format 新图片的格式
     * @throws IOException
     */
    public static void resizeImageFile(byte[] file, OutputStream os, int size, String format) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(file);
        ByteArrayInputStream in2 = new ByteArrayInputStream(file);
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in2.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        System.out.println(out.length());
        BufferedImage prevImage = ImageIO.read(in);
        double width = prevImage.getWidth();
        double height = prevImage.getHeight();
        double percent = size / width;
        int newWidth = (int) (width * percent);
        int newHeight = (int) (height * percent);
        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = image.createGraphics();
        graphics.drawImage(prevImage, 0, 0, newWidth, newHeight, null);
        ImageIO.write(image, format, os);
        os.flush();
        in.close();
        os.close();
    }
}
