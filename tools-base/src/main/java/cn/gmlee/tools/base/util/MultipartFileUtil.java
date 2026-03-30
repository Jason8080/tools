package cn.gmlee.tools.base.util;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

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
     * To multipart file.
     *
     * @param bytes the bytes
     * @return the multipart file
     */
    public static MultipartFile toFile(byte[] bytes) {
        return new ByteArrayMultipartFile("file", "file", MediaType.MULTIPART_FORM_DATA_VALUE, bytes);
    }

    /**
     * To file multipart file.
     *
     * @param filename the filename
     * @param bytes    the bytes
     * @return the multipart file
     */
    public static MultipartFile toFile(String filename, byte[] bytes) {
        return new ByteArrayMultipartFile("file", filename, MediaType.MULTIPART_FORM_DATA_VALUE, bytes);
    }
}
