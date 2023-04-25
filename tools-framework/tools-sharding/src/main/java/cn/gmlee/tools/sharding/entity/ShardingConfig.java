package cn.gmlee.tools.sharding.entity;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Jas°
 * @date 2021/7/5 (周一)
 */
@Data
public class ShardingConfig implements Serializable {
    Long id;
    @NotEmpty(message = "逻辑库是空")
    String logicDb;
    @NotEmpty(message = "逻辑表是空")
    String logicTab;
    String dbShardingColumn = "created_at";
    String dbShardingAlgorithmName = "AUTO-EXPANSION";
    String dbShardingAlgorithmType = "AUTO-EXPANSION";
    String dbShardingAlgorithmKvs;
    String tabShardingColumn = "created_at";
    String tabShardingAlgorithmName = "AUTO-EXPANSION";
    String tabShardingAlgorithmType = "AUTO-EXPANSION";
    String tabShardingAlgorithmKvs;
    String primaryKey = "id";
    String primaryKeyType = "snowflake";
    String auditorNames = "sharding_key_none_auditor";
    Boolean auditorAllowHintDisable = true;
    String actualDataNodes;
    Date updatedAt;
}
