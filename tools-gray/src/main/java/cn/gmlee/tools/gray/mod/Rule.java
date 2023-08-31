package cn.gmlee.tools.gray.mod;

/**
 * 规则定义.
 */
public abstract class Rule implements Enable {

    private Boolean enable;

    @Override
    public boolean getEnable() {
        return enable;
    }
}
