package cn.gmlee.tools.request.mod;

import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

/**
 * 可变请求.
 */
public class ChangeableContentCachingResponseWrapper extends ContentCachingResponseWrapper {
    private boolean change = false;
    private byte[] bytes;
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    private Map<String, String[]> parameters;

    /**
     * Instantiates a new Changeable content caching request wrapper.
     *
     * @param request the request
     */
    public ChangeableContentCachingResponseWrapper(HttpServletResponse request) {
        super(request);
    }

    /**
     * 改变内容.
     *
     * @param bytes the bytes
     */
    public void reset(byte[] bytes) {
        this.bytes = bytes;
        this.change = true;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if(!change){
            return super.getOutputStream();
        }
        if (outputStream == null) {
            outputStream = new ChangeableServletOutputStream(bytes);
            change = false;
            return outputStream;
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(!change){
            return super.getWriter();
        }
        if (writer == null) {
            return writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()));
        }
        return writer;
    }

    @Override
    public byte[] getContentAsByteArray() {
        if(!change){
            return super.getContentAsByteArray();
        }
        return bytes;
    }

    @Override
    public InputStream getContentInputStream() {
        if(!change){
            return super.getContentInputStream();
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void copyBodyToResponse() throws IOException {
        this.copyBodyToResponse(true);
    }

    @Override
    protected void copyBodyToResponse(boolean complete) throws IOException {
        if(!change){
            super.copyBodyToResponse(complete);
        } else if (this.bytes.length > 0) {
            HttpServletResponse rawResponse = (HttpServletResponse) getResponse();
            rawResponse.getOutputStream().write(bytes);
            if (complete) {
                super.flushBuffer();
            }
        }
    }
}
