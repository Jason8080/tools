package cn.gmlee.tools.webapp.global;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
@RestControllerAdvice
public class GlobalExceptionHandlerAdvice {

    @ExceptionHandler({Exception.class, Throwable.class})
    public R throwable(HttpServletRequest request, Throwable throwable) {
        log.error("已捕捉: 未知异常\r\n{}", ExceptionUtil.getAllMsg(throwable));
        return R.FAIL.newly(throwable);
    }

    @ExceptionHandler({RuntimeException.class})
    public R runtimeException(HttpServletRequest request, RuntimeException ex) {
        log.warn("已捕捉: 运行时异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return R.FAIL.newly(ex);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public R invalidFormatException(HttpServletRequest request, HttpMessageNotReadableException ex) {
        log.info("已捕捉: 消息读取异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return R.FAIL.newly(XCode.REQUEST_FAIL, ExceptionUtil.getOriginMsg(ex));
    }

    @ExceptionHandler({BindException.class})
    public R validateException(HttpServletRequest request, BindException ex) {
        log.info("已捕捉: 数据效验异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        StringBuilder sb = new StringBuilder();
        for (ObjectError oe : ex.getAllErrors()) {
            sb.append(oe.getDefaultMessage());
            sb.append(";");
        }
        return R.FAIL.newly(XCode.API_PARAM, sb.substring(0, sb.length() - 1));
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public R validateException(HttpServletRequest request, ConstraintViolationException ex) {
        log.info("已捕捉: 数据效验异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation cv : ex.getConstraintViolations()) {
            sb.append(cv.getMessage());
            sb.append(";");
        }
        return R.FAIL.newly(XCode.API_PARAM, sb.substring(0, sb.length() - 1));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public R validateException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        log.info("已捕捉: 数据效验异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        StringBuilder sb = new StringBuilder();
        BindingResult result = ex.getBindingResult();
        for (ObjectError oe : result.getAllErrors()) {
            sb.append(oe.getDefaultMessage());
            sb.append(";");
        }
        return R.FAIL.newly(XCode.API_PARAM, sb.substring(0, sb.length() - 1));
    }


    @ExceptionHandler({
            ServletRequestBindingException.class, // SpringMVC注解绑定异常
            IllegalStateException.class // 非法参数绑定异常
    })
    public R slException(HttpServletRequest request, Exception ex) {
        log.info("已捕捉: 数据绑定异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return R.FAIL.newly(XCode.API_PARAM, ex);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public R typeMismatchException(HttpServletRequest request, TypeMismatchException ex) {
        log.info("已捕捉: 参数类型异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return R.FAIL.newly(XCode.API_PARAM, ex);
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, NoHandlerFoundException.class})
    public R servletException(HttpServletRequest request, ServletException ex) {
        log.info("已捕捉: 不支持的请求异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return R.FAIL.newly(XCode.API_METHOD, ex);
    }

    @ExceptionHandler({SocketTimeoutException.class})
    public R socketTimeoutException(HttpServletRequest request, SocketTimeoutException ex) {
        log.error("已捕捉: 请求超时异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return R.FAIL.newly(XCode.REQUEST_TIMEOUT, ex);
    }

    @ExceptionHandler({SkillException.class})
    public R skillException(HttpServletRequest request, SkillException ex) {
        log.error("已捕捉: 技术异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return R.FAIL.newly(ex);
    }
}
