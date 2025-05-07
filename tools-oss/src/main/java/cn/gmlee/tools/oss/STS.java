package cn.gmlee.tools.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.auth.Credentials;
import com.aliyuncs.exceptions.ClientException;

/**
 * 使用STS临时访问凭证访问OSS.
 */
public interface STS extends OSS {
    /**
     * 获取最新客户端.
     *
     * @return the oss
     * @throws ClientException the client exception
     */
    OSS refresh() throws ClientException;

    /**
     * 获取最新的证书.
     * <p>
     * 把这个给前端就可以完成上传
     * </p>
     *
     * @return the credentials
     * @throws ClientException the client exception
     */
    Credentials cert() throws ClientException;
}
