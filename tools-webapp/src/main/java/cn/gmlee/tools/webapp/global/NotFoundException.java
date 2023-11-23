package cn.gmlee.tools.webapp.global;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.mod.R;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 捕捉404异常
 *
 * @author Jas°
 */
@RestController
@ConditionalOnClass(ErrorController.class)
public class NotFoundException implements ErrorController {

    @Override
    public String getErrorPath() {
        return "error";
    }

    /**
     * 处理404异常.
     *
     * @return the string
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "error", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object error(){
        return new R(XCode.RESOURCE_NOT_FOUND.code, "哇! 页面走丢了呢");
    }
}
