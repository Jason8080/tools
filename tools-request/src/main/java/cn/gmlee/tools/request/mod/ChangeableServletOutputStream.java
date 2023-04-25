package cn.gmlee.tools.request.mod;

import cn.gmlee.tools.base.util.ExceptionUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 可变输入流.
 */
public class ChangeableServletOutputStream extends ServletOutputStream {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	/**
	 * Instantiates a new Changeable servlet input stream.
	 *
	 * @param bytes the bytes
	 */
	public ChangeableServletOutputStream(byte[] bytes){
        try {
            out.write(bytes);
        } catch (IOException e) {
            ExceptionUtil.cast(e);
        }
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener listener) {

    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }
}
