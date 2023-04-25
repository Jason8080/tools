package cn.gmlee.tools.request.mod;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.WebUtil;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 可变请求.
 */
public class ChangeableContentCachingRequestWrapper extends ContentCachingRequestWrapper {
    private boolean change = false;
    private byte[] bytes;
    private ServletInputStream inputStream;
    private BufferedReader reader;

    private Map<String, String[]> parameters;

    /**
     * Instantiates a new Changeable content caching request wrapper.
     *
     * @param request the request
     */
    public ChangeableContentCachingRequestWrapper(HttpServletRequest request) {
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
    public ServletInputStream getInputStream() throws IOException {
        if(!change){
            return super.getInputStream();
        }
        if (inputStream == null) {
            inputStream = new ChangeableServletInputStream(bytes);
            change = false;
            return inputStream;
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if(!change){
            return super.getReader();
        }
        if (reader == null) {
            return reader = new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
        }
        return reader;
    }

    @Override
    public String getParameter(String name) {
        if(!change){
            return super.getParameter(name);
        }
        if (parameters == null) {
            writeToParameters();
        }
        String[] array = parameters.get(name);
        return BoolUtil.isEmpty(array) ? null : array[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if(!change){
            return super.getParameterMap();
        }
        if (parameters == null) {
            writeToParameters();
        }
        return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if(!change){
            return super.getParameterNames();
        }
        if (parameters == null) {
            writeToParameters();
        }
        return new Enumeration<String>() {

            private Iterator<String> it = parameters.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public String nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public String[] getParameterValues(String name) {
        if(!change){
            return super.getParameterValues(name);
        }
        if (parameters == null) {
            writeToParameters();
        }
        return parameters.get(name);
    }

    @Override
    public byte[] getContentAsByteArray() {
        if(!change){
            return super.getContentAsByteArray();
        }
        return bytes;
    }


    private boolean as(String header) {
        String contentType = getContentType();
        if (contentType.startsWith(header)) {
            return true;
        }
        return false;
    }

    private void writeToParameters() {
        String content = new String(bytes);
        if (as(HttpUtil.JSON_HEADER)) {
            Map<String, Object> map = JsonUtil.toBean(content, Map.class);
            parameters = WebUtil.toParameterMap(map);
        } else if (as(HttpUtil.FORM_HEADER)) {
            Map<String, Object> map = WebUtil.getParams(content);
            parameters = WebUtil.toParameterMap(map);
        } else if (as(HttpUtil.DATA_HEADER)) {
            Map<String, Object> map = WebUtil.getParams(content);
            parameters = WebUtil.toParameterMap(map);
        }
    }
}
