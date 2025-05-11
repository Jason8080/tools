package cn.gmlee.tools.oss;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.LocalDateTimeUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.oss.conf.StsProperties;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.Credentials;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.regions.Endpoint;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * 使用STS临时访问凭证访问OSS.
 */
@Slf4j
public class STSClient extends OSSClient implements STS {

    private OSSClient ossClient;

    private StsProperties stsProperties;

    /**
     * Instantiates a new Sts client.
     *
     * @param ossClient     the oss client
     * @param stsProperties the sts properties
     */
    public STSClient(OSSClient ossClient, StsProperties stsProperties) {
        super(ossClient.getEndpoint().toString(), ossClient.getCredentialsProvider(), ossClient.getClientConfiguration());
        this.ossClient = ossClient;
        this.stsProperties = stsProperties;
    }

    @Override
    public OSSClient refresh() throws ClientException {
        doRefresh();
        return ossClient;
    }

    @Override
    public Credentials cert() throws ClientException {
        return refresh().getCredentialsProvider().getCredentials();
    }

    private void doRefresh() throws ClientException {
        Long expire = stsProperties.getExpire();
        long current = System.currentTimeMillis();
        if (current > expire) {
            DefaultProfile.addEndpoint(ossClient.getEndpoint().toString(), "", "Sts", ossClient.getEndpoint().getHost());
            IClientProfile profile = DefaultProfile.getProfile("",
                    ossClient.getCredentialsProvider().getCredentials().getAccessKeyId(),
                    ossClient.getCredentialsProvider().getCredentials().getSecretAccessKey()
            );
            DefaultAcsClient acs = new DefaultAcsClient(profile);
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.POST);
            request.setRoleArn(stsProperties.getRoleArn());
            request.setRoleSessionName(stsProperties.getRoleSessionName());
            request.setDurationSeconds(stsProperties.getDuration());
            AssumeRoleResponse response = acs.getAcsResponse(request);
            checkResponseAndResetExpire(response);
            ossClient = new OSSClient(ossClient.getEndpoint().toString(), new DefaultCredentialProvider(
                    ossClient.getCredentialsProvider().getCredentials().getAccessKeyId(),
                    ossClient.getCredentialsProvider().getCredentials().getSecretAccessKey(),
                    response.getCredentials().getSecurityToken()
            ), ossClient.getClientConfiguration());
        }
    }

    private void checkResponseAndResetExpire(AssumeRoleResponse response) {
        AssertUtil.notNull(response, "STS刷新令牌失败");
        AssumeRoleResponse.Credentials credentials = response.getCredentials();
        AssertUtil.notNull(credentials, "STS获取令牌失败");
        log.info("STS refresh token: {} -> {}",
                NullUtil.get(() -> ossClient.getCredentialsProvider().getCredentials().getSecurityToken(), "原令牌是空"),
                response.getCredentials().getSecurityToken());
        String expiration = credentials.getExpiration();
        long current = System.currentTimeMillis();
        try {
            Date date = TimeUtil.parseTime(expiration);
            AssertUtil.notNull(date, "时间格式无法解析");
            Date offset = LocalDateTimeUtil.offset(date, 8, ChronoUnit.HOURS);
            AssertUtil.notNull(offset, "时间位移计算失败");
            Instant instant = offset.toInstant();
            AssertUtil.notNull(instant, "获取瞬时对象失败");
            long expire = instant.toEpochMilli();
            AssertUtil.notNull(expire, "获取到期时间戳失败");
            long e = expire - stsProperties.getRedundancy();
            stsProperties.setExpire(e > current ? e : expire);
        } catch (Exception ex) {
            Long expire = stsProperties.getDuration();
            long e = expire - stsProperties.getRedundancy();
            stsProperties.setExpire(e > current ? e : expire);
        }
    }
}
