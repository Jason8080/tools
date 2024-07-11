package cn.gmlee.tools.base.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * Multipart file.
 */
public class MultipartFileUtil {

    /**
     * To multipart file.
     *
     * @param base64 the base 64
     * @return the multipart file
     */
    public static MultipartFile toFile(String base64){
        byte[] bytes = Base64.getDecoder().decode(base64);
        return toFile(bytes);
    }

    /**
     * To file multipart file.
     *
     * @param filename the filename
     * @param base64   the base 64
     * @return the multipart file
     */
    public static MultipartFile toFile(String filename, String base64){
        byte[] bytes = Base64.getDecoder().decode(base64);
        return toFile(filename, bytes);
    }

    /**
     * To commons multipart file.
     *
     * @param bytes the bytes
     * @return the commons multipart file
     */
    public static CommonsMultipartFile toFile(byte[] bytes) {
        FileItem fileItem = ExceptionUtil.suppress(() -> createFileItem(bytes));
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * To file commons multipart file.
     *
     * @param filename the filename
     * @param bytes    the bytes
     * @return the commons multipart file
     */
    public static CommonsMultipartFile toFile(String filename, byte[] bytes) {
        FileItem fileItem = ExceptionUtil.suppress(() -> createFileItem(filename, bytes));
        return new CommonsMultipartFile(fileItem);
    }

    private static FileItem createFileItem(byte... bytes) throws IOException {
        return createFileItem("file", bytes);
    }

    private static FileItem createFileItem(String name, byte... bytes) throws IOException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        FileItem item = factory.createItem("file", MediaType.MULTIPART_FORM_DATA_VALUE, true, name);
        item.getOutputStream().write(bytes);
        return item;
    }
}
