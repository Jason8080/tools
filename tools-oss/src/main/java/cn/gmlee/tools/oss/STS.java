package cn.gmlee.tools.oss;

import com.aliyun.oss.OSS;
import com.aliyuncs.exceptions.ClientException;

/**
 * 使用STS临时访问凭证访问OSS.
 */
public interface STS extends OSS {
    /**
     * Oss oss.
     *
     * @return the oss
     */
    OSS refresh() throws ClientException;
}
