package cn.gmlee.tools.base.util;

import java.io.*;

/**
 * 字节流工具.
 *
 * @author Jas °
 */
public class StreamUtil {

    /**
     * 转换为可复制流.
     *
     * @param in the in
     * @return the byte array output stream
     * @throws IOException the io exception
     */
    public static ByteArrayOutputStream toStream(InputStream in) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(0);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > -1) {
            stream.write(buffer, 0, len);
        }
        stream.flush();
        return stream;
    }

    /**
     * Gets stream.
     *
     * @param in the in
     * @return the stream
     * @throws IOException the io exception
     */
    public static ByteArrayInputStream getStream(InputStream in) throws IOException {
        byte[] bytes = toStream(in).toByteArray();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 将流转换为字符.
     *
     * @param in      the in
     * @param charset the charset
     * @return the string
     * @throws IOException the io exception
     */
    public static String toString(InputStream in, String... charset) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[1024];
        for (int n; (n = in.read(b)) != -1; ) {
            if (BoolUtil.isEmpty(charset) || BoolUtil.isEmpty(charset[0])) {
                out.append(new String(b, 0, n));
            } else {
                out.append(new String(b, 0, n, charset[0]));
            }
        }
        return out.toString();
    }

    /**
     * To bytes byte [ ].
     *
     * @param in the in
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] toBytes(InputStream in) throws IOException {
        return toStream(in).toByteArray();
    }
}
