package cn.gmlee.tools.webapp.global;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.AgreedException;
import cn.gmlee.tools.base.ex.RemoteInvokeException;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.ex.VcException;
import cn.gmlee.tools.base.ex.agreed.UserExperienceException;
import cn.gmlee.tools.base.mod.JsonResult;
import cn.gmlee.tools.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * 全局异常处理
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
@Component
@RestControllerAdvice
public class GlobalExceptionHandlerAdvice {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice.class);

    /**
     * 未知异常
     *
     * @param request   the request
     * @param throwable the throwable
     * @return json result
     */
    @ExceptionHandler({Exception.class, Throwable.class})
    public JsonResult throwable(HttpServletRequest request, Throwable throwable) {
        logger.error("已捕捉: 未知异常\r\n{}", ExceptionUtil.getAllMsg(throwable));
        return JsonResult.FAIL.newly(throwable.getMessage());
    }

    /**
     * 运行时异常.
     *
     * @param request the request
     * @param ex      the ex
     * @return the json result
     */
    @ExceptionHandler({RuntimeException.class})
    public JsonResult runtimeException(HttpServletRequest request, RuntimeException ex) {
        logger.warn("已捕捉: 运行时异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly(ex.getMessage());
    }

    /**
     * 消息读取异常
     *
     * @param request the request
     * @param ex      the ex
     * @return json result
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public JsonResult invalidFormatException(HttpServletRequest request, HttpMessageNotReadableException ex) {
        logger.info("已捕捉: 消息读取异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly(XCode.UNREASONABLE4000, ExceptionUtil.getOriginMsg(ex));
    }

    /**
     * 技术异常.
     *
     * @param request the request
     * @param ex      the ex
     * @return the json result
     */
    @ExceptionHandler({SkillException.class})
    public JsonResult skillException(HttpServletRequest request, SkillException ex) {
        logger.error("已捕捉: 技术异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly(ex.getCode(), ex.getMessage());
    }

    /**
     * 需要验证码异常.
     *
     * @param request the request
     * @param ex      the ex
     * @return the json result
     */
    @ExceptionHandler({VcException.class})
    public JsonResult vcException(HttpServletRequest request, VcException ex) {
        logger.error("已捕捉: 需要输入验证码异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly(ex.getCode(), ex.getMessage());
    }


    /**
     * 远程调用异常.
     *
     * @param request the request
     * @param ex      the ex
     * @return the json result
     */
    @ExceptionHandler({RemoteInvokeException.class})
    public JsonResult remoteInvokeException(HttpServletRequest request, RemoteInvokeException ex) {
        logger.error("已捕捉: 远程调用异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly(ex.getCode(), ex.getMessage());
    }

    /**
     * 约定响应异常.
     *
     * @param request the request
     * @param ex      the ex
     * @return the json result
     */
    @ExceptionHandler({AgreedException.class})
    public JsonResult agreedException(HttpServletRequest request, AgreedException ex) {
        logger.info("已捕捉: 约定响应异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly(ex.getCode(), ex.getMessage());
    }

    /**
     * 用户体验异常.
     *
     * @param request the request
     * @param ex      the ex
     * @return the json result
     */
    @ExceptionHandler({UserExperienceException.class})
    public JsonResult userExperienceException(HttpServletRequest request, UserExperienceException ex) {
        logger.info("已捕捉: 用户体验异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly(ex.getCode(), ex.getMessage());
    }

    /**
     * 数据效验异常
     *
     * @param request the request
     * @param ex      the ex
     * @return json result
     */
    @ExceptionHandler({BindException.class})
    public JsonResult validateException(HttpServletRequest request, BindException ex) {
        logger.info("已捕捉: 数据效验异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        StringBuilder sb = new StringBuilder();
        for (ObjectError oe : ex.getAllErrors()) {
            sb.append(oe.getDefaultMessage());
            sb.append(";");
        }
        return JsonResult.FAIL.newly(sb.substring(0, sb.length() - 1));
    }

    /**
     * 数据效验异常
     *
     * @param request the request
     * @param ex      the ex
     * @return json result
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public JsonResult validateException(HttpServletRequest request, ConstraintViolationException ex) {
        logger.info("已捕捉: 数据效验异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation cv : ex.getConstraintViolations()) {
            sb.append(cv.getMessage());
            sb.append(";");
        }
        return JsonResult.FAIL.newly(sb.substring(0, sb.length() - 1));
    }

    /**
     * 数据效验异常
     *
     * @param request the request
     * @param ex      the ex
     * @return json result
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public JsonResult validateException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        logger.info("已捕捉: 数据效验异常\r\n", ExceptionUtil.getAllMsg(ex));
        StringBuilder sb = new StringBuilder();
        BindingResult result = ex.getBindingResult();
        for (ObjectError oe : result.getAllErrors()) {
            sb.append(oe.getDefaultMessage());
            sb.append(";");
        }
        return JsonResult.FAIL.newly(sb.substring(0, sb.length() - 1));
    }


    /**
     * 数据绑定异常
     *
     * @param request the request
     * @param ex      the ex
     * @return json result
     */
    @ExceptionHandler({
            ServletRequestBindingException.class, // SpringMVC注解绑定异常
            IllegalStateException.class // 非法参数绑定异常
    })
    public JsonResult slException(HttpServletRequest request, Exception ex) {
        logger.info("已捕捉: 数据绑定异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly("请求数据绑定异常");
    }

    /**
     * 参数类型异常
     *
     * @param request the request
     * @param ex      the ex
     * @return json result
     */
    @ExceptionHandler(TypeMismatchException.class)
    public JsonResult typeMismatchException(HttpServletRequest request, TypeMismatchException ex) {
        logger.info("已捕捉: 参数类型异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly("不支持的参数类型");
    }

    /**
     * 不支持的请求异常
     *
     * @param request the request
     * @param ex      the ex
     * @return json result
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, NoHandlerFoundException.class})
    public JsonResult servletException(HttpServletRequest request, ServletException ex) {
        logger.info("已捕捉: 不支持的请求异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly("不支持的请求方式");
    }

    /**
     * 请求超时异常.
     *
     * @param request the request
     * @param ex      the ex
     * @return the json result
     */
    @ExceptionHandler({SocketTimeoutException.class})
    public JsonResult socketTimeoutException(HttpServletRequest request, SocketTimeoutException ex) {
        logger.error("已捕捉: 请求超时异常\r\n{}", ExceptionUtil.getAllMsg(ex));
        return JsonResult.FAIL.newly("请求超时");
    }
}
