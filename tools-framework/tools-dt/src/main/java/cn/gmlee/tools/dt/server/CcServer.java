package cn.gmlee.tools.dt.server;

import cn.gmlee.tools.dt.dao.entity.Tx;
import cn.gmlee.tools.dt.conf.DtProperties;
import cn.gmlee.tools.dt.repository.TxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * The type Tx server.
 */
public class CcServer {

    private static final Logger log = LoggerFactory.getLogger(CcServer.class);

    @Resource
    private TxRepository txRepository;

    @Resource
    private DtProperties dtProperties;

    /**
     * 通知上级事务 (远程事务code可以提交了).
     * <p>
     *  定时通知上级事务: 超时回滚
     *  得到上级事务确认: 先提交
     *  提交成功后通知上级: 已提交
     * </p>
     *
     * @param tx
     */
    public void commitNotify(Tx tx) {


    }
}
