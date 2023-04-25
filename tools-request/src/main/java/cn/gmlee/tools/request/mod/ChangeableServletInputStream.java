package cn.gmlee.tools.request.mod;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 可变输入流.
 */
public class ChangeableServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream in;

	/**
	 * Instantiates a new Changeable servlet input stream.
	 *
	 * @param bytes the bytes
	 */
	public ChangeableServletInputStream(byte[] bytes) {
        in = new ByteArrayInputStream(bytes);
    }


    @Override
    public boolean isFinished() {
        return in.available() == 0;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener listener) {

    }

    @Override
    public int read() throws IOException {
        return in.read();
    }
}
