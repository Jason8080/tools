package cn.gmlee.tools.request.filter;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.mod.JsonResult;
import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.request.config.EdProperties;
import cn.gmlee.tools.request.mod.ChangeableContentCachingRequestWrapper;
import cn.gmlee.tools.request.mod.ChangeableContentCachingResponseWrapper;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 加解密过滤器.
 *
 * @author Jas
 */
public class EdFilter extends OncePerRequestFilter {

    private EdProperties edProperties;

    public EdFilter(EdProperties edProperties) {
        this.edProperties = edProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 表单及其他请求体前端不方便加密; 因此场景并不广泛暂不实现 (若前端已实现该检测直接放开即可);
        if(!WebUtil.asJson(request)){
            filterChain.doFilter(request, response);
            return;
        }
        // 加密解密
        ChangeableContentCachingRequestWrapper req = new ChangeableContentCachingRequestWrapper(request);
        ChangeableContentCachingResponseWrapper res = new ChangeableContentCachingResponseWrapper(response);
        try {
            String decode = decode(req, res);
            req.reset(decode.getBytes());
        } catch (Exception e) {
            req.reset(((ContentCachingRequestWrapper)request).getContentAsByteArray());
            logger.warn("tools ed decode error!", e);
            if (edProperties.getMust()) {
                response.setCharacterEncoding(Charset.defaultCharset().name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                JsonResult result = new JsonResult(XCode.ASSERT_FAIL.code, e.getMessage());
                response.getWriter().println(JsonUtil.toJson(result));
                return;
            }
        }
        // 执行
        filterChain.doFilter(req, res);
        try {
            String encode = encode(req, res);
            res.reset(encode.getBytes());
        } catch (Exception e) {
            logger.warn("tools ed encode error!", e);
        } finally {
            res.copyBodyToResponse();
        }
    }

    private String decode(ContentCachingRequestWrapper req, ContentCachingResponseWrapper res) throws Exception {
        // 加密内容
        String text = StreamUtil.toString(req.getInputStream(), req.getCharacterEncoding());
        // 密文安全码
        String ciphertext = req.getHeader(edProperties.getCiphertext());
        // 明文安全码
        String plaintext = RsaUtil.privateDecoder(ciphertext, edProperties.getPrivateKey());
        // 解密内容
        return DesUtil.decode(text, plaintext);
    }

    private String encode(ContentCachingRequestWrapper req, ContentCachingResponseWrapper res) throws Exception {
        // 明文内容
        String content = new String(res.getContentAsByteArray());
        // 密文安全码
        String ciphertext = req.getHeader(edProperties.getCiphertext());
        // 明文安全码
        String plaintext = RsaUtil.privateDecoder(ciphertext, edProperties.getPrivateKey());
        // 加密内容
        return DesUtil.encode(content, plaintext);
    }
}
