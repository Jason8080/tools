package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;

/**
 * 权限不足.
 *
 * @author Jas °
 * @date 2021 /10/20 (周三)
 */
public class PermissionDeniedException extends SkillException {

    /**
     * 默认权限不足异常.
     */
    public PermissionDeniedException() {
        super(XCode.ACCOUNT_PERMISSION7002.code, XCode.ACCOUNT_PERMISSION7002.msg);
    }
}
