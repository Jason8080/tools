package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;

/**
 * 权限不足.
 *
 * @author Jas °
 */
public class PermissionDeniedException extends SkillException {

    /**
     * 默认权限不足异常.
     */
    public PermissionDeniedException() {
        super(XCode.USER_PERMISSION.code, XCode.USER_PERMISSION.msg);
    }
}
