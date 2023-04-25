/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.gmlee.tools.sharding.algorithm;

import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 自动扩缩容分片算法.
 * <p>
 * 背景:
 *  不是所有的项目都需要分库分表, 但所有项目都有可能需要分库分表.
 *  即使项目未来会产生巨量数据, 但首期项目产生的数据也少的可怜, 如何使项目具备自动应对数据量陡增的情况?
 *  自动扩缩容组件(tools-sharding)运应而生。
 * 需求:
 *  # 避免项目预建库表
 *  # 当数据量达到阈值自动 建库&建表
 *  # 启用新建的库表存储新的数据
 *  # 分库分表规则可以动态配置
 * </p>
 *
 *
 */
public class AutoExpansionStandardShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

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
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Comparable<?>> shardingValue) {
        return doSharding(availableTargetNames, Range.singleton(shardingValue.getValue())).stream().findFirst().orElse(null);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Comparable<?>> shardingValue) {
        return doSharding(availableTargetNames, shardingValue.getValueRange());
    }

    private Collection<String> doSharding(final Collection<String> availableTargetNames, final Range<Comparable<?>> range) {
        return availableTargetNames.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    @Override
    public String getType() {
        return "AUTO-EXPANSION";
    }
}
