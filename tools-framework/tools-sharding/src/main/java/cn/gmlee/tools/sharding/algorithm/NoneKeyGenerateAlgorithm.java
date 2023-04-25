package cn.gmlee.tools.sharding.algorithm;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;

import java.util.List;
import java.util.Properties;

public class NoneKeyGenerateAlgorithm implements KeyGenerateAlgorithm {

    @Getter
    private Properties props;

    @Override
    public void init(Properties props) {
        this.props = props;
    }

    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public Comparable<?> generateKey() {
        return null;
    }

    @Override
    public String getType() {
        return "NONE_KEY";
    }
}
