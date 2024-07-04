# fastpig
> 一个通用性基于配置的可扩展的数据处理框架


## 项目介绍
### 什么是 Fastpig
基于Flink的一个通用性基于配置的可扩展的数据处理框架!
### 项目特性

1. 低代码: 配置化处理数据, 无需针对不同需求重复编写Flink代码;
2. 高扩展: 所有组件都SPI化, 用户可以自由替换组件;
3. 热加载: 基于动态编译,流程处理支持逻辑热更新无需重启; (结合广播流)
4. 简单易用: 一旦熟悉可以快速上手, 缩短开发时间;

## 快速开始
#### 配置表初始化
```sql
-- fastpig.meta_job_process definition
-- 任务清洗规则配置表
CREATE TABLE `meta_job_process` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `job_name` varchar(256) DEFAULT NULL COMMENT 'flink job名',
  `operator` varchar(100) NOT NULL COMMENT 'flink operator名',
  `conditions` text COMMENT '匹配条件',
  `yes_processor` varchar(100) DEFAULT NULL COMMENT '匹配时processor',
  `yes_config` text COMMENT '匹配时processor配置',
  `no_processor` varchar(100) DEFAULT NULL COMMENT '不匹配时processor',
  `no_config` text COMMENT '不匹配时processor配置',
  `orders` int NOT NULL DEFAULT '0' COMMENT '顺序',
  `sub_flow` varchar(100) DEFAULT NULL COMMENT '子流程',
  `fail_policy` varchar(100) NOT NULL DEFAULT 'DISCARD' COMMENT '失败策略,IGNORE,DISCARD,DISCARD_SILENT,BREAK_SUB_FLOW',
  `remark` varchar(500) DEFAULT NULL COMMENT '说明',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `process_FK` (`job_name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='flink任务清洗规则配置表';


-- fastpig.meta_match_operator_impl definition
-- 比较符实现表 
CREATE TABLE `meta_match_operator_impl` (
  `name` varchar(100) NOT NULL,
  `definition` text NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='比较符实现表';

-- fastpig.meta_record_processor_impl definition
-- 数据清洗处理器实现表
CREATE TABLE `meta_record_processor_impl` (
  `name` varchar(100) NOT NULL,
  `definition` text NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据清洗处理器实现表';

-- fastpig.meta_field_update_impl definition
-- 字段更新策略实现表  
CREATE TABLE `meta_field_update_impl` (
  `name` varchar(100) NOT NULL,
  `definition` text NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字段更新策略实现表';


-- fastpig.meta_datasource definition
-- 数据源管理表
CREATE TABLE `meta_datasource` (
  `catalog` varchar(20) NOT NULL COMMENT '数据源名称',
  `type` varchar(20) NOT NULL COMMENT '数据源类型和连接器SPI名称保持一致',
  `connect_info` text NOT NULL COMMENT '数据源连接信息,json格式',
  PRIMARY KEY (`catalog`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- fastpig.meta_table definition
-- 物理表定义
CREATE TABLE `meta_table` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `catalog` varchar(100) DEFAULT NULL COMMENT '数据源',
  `schema` varchar(100) NOT NULL DEFAULT '' COMMENT 'schema名',
  `table` varchar(100) DEFAULT NULL COMMENT '表名',
  `description` varchar(100) DEFAULT NULL COMMENT '描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cdp_meta_table_UN` (`catalog`,`schema`,`table`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物理表定义';

-- fastpig.meta_column definition
-- 物理表字段定义
CREATE TABLE `meta_column` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `table_id` bigint NOT NULL COMMENT '数据表id',
  `column_name` varchar(100) NOT NULL COMMENT '字段名',
  `field_name` varchar(100) DEFAULT NULL COMMENT '打点数据流中对应的字段名, 默认为字段名的驼峰形式',
  `column_type` varchar(100) NOT NULL COMMENT '字段类型',
  `column_size` int NOT NULL DEFAULT '-1' COMMENT '字段长度,-1:无限制',
  `nullable` int NOT NULL DEFAULT '1' COMMENT '是否可为空,1:是,0:否',
  `primary_key` int NOT NULL DEFAULT '0' COMMENT '是否主键,1:是,0:否',
  `orders` int NOT NULL DEFAULT '0',
  `update_policy` varchar(100) NOT NULL DEFAULT 'Default' COMMENT '更新策略',
  `update_policy_args` text COMMENT '更新策略参数',
  `storage_transform` varchar(100) DEFAULT NULL COMMENT '存储层面的转换操作',
  `description` text COMMENT '字段描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cdp_meta_column_UN` (`table_id`,`column_name`),
  KEY `cdp_meta_column_FK_1` (`update_policy`),
  KEY `cdp_meta_column_FK_2` (`column_type`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物理表字段定义';
```

#### 初始化配置
配置支持 Apollo、Nacos、Local，这里以Local配置为例。
fastpig-run 工程 resources 文件下创建 example.yaml 配置文件
```yaml
job:
  name: exampleJob
  parallelism: 1
  checkpoint:
    enable: true
    time: 1000
    interval: 1000
    timeout: 10000
    cleanup: RETAIN_ON_CANCELLATION
    concurrent: 1
  restart:
    strategy: fixed-delay
    attempts: 3
    delay: 10



datasources:
  metadata: &metadata
    name: 'metadata'
    driverName: com.mysql.jdbc.Driver
    url: 'jdbc:mysql://mysql.sqlpub.com:3306/fastpig?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai&useLocalSessionState=true'
    username: 'userName'
    password: 'password'

kafkaConfig:
  consumer:
    properties: &kafkaConsumerProp
      bootstrap.servers: kafkaAdrress:9092
      enable.auto.commit: true
      auto.commit.interval.ms: 5000
      auto.offset.reset: latest
      request.timeout: 1000
      session.timeout.ms isolation.level: read_committed
      flink.partition-discovery.interval-millis: 60000

  producer:
    properties: &kafkaProducerProp
      bootstrap.servers: kafkaAdrress:9092
      linger.ms: 5
      batch.size: 1310720
      acks: all
      retries: 10
      retry.backoff.ms: 100
      request.timeout.ms: 60000
      enable.idempotence: true
      max.in.flight.requests.per.connection: 1
      transaction.timeout.ms: 600000

dataGrouping:
  keys:
    - 
  jsonPathSupported: false


source1: &source1
  topic: from_topic
  type: kafkaSource
  startupMode: EARLIEST
  format: json
  uid: source1
  name: source1
  properties:
    <<: *kafkaConsumerProp
    group.id: zk-test-group

dataProduct:
  type: kafkaSink
  topic: to_topic
  format: json
  partitioner: default
  formatOptions:
    jsonPathSupport: true
    keyField: 
  properties:
    <<: *kafkaProducerProp


metaInfo:
  <<: *metadata


sources:
  -
    <<: *source1
```

#### 项目运行
将以上配置补充 Kafka Broker 地址信息和元数据配置库 mysql 账号密码信息，选择启动模型 fastpig-run 工程下的  KafkaToKafkaJob 启动类，添加启动参数 `-e qa -n example.yaml -t local`  
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720063348743-526cd7b8-c90c-4baa-bb92-799a0922391e.png#averageHue=%233f4345&clientId=ue02559f6-dae8-4&from=paste&height=230&id=u093eb580&originHeight=287&originWidth=545&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=19675&status=done&style=none&taskId=u2cea5b02-5a9f-4f43-855d-7ee1fdf6bf0&title=&width=436)
最后运行即可，该模型可以实现 接收kafka消息-处理数据-发送kafka消息；
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720064370923-54d4e063-401a-4cac-8233-155252394f6b.png#averageHue=%23191817&clientId=ue02559f6-dae8-4&from=paste&height=209&id=u20c70d49&originHeight=261&originWidth=910&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=23962&status=done&style=none&taskId=ub4591118-fafe-4b37-bb84-498c133502c&title=&width=728)

#### 进阶使用
如果需要实现处理逻辑需要 `meta_job_process` 表中增加清洗逻辑，这里简单实现一个逻辑如果字段等于某个逻辑，我们将当前数据字段驼峰转化为下划线；

首先在将 `fastpig-dyncomp` 工程下面 `com.ofnull.fastpig.dyncomp.matchoperate.EqualMatchOperator` 源码写入 `meta_match_operator_impl` 中。
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720064624113-25f18c22-cb5b-41e5-a93e-7da7b2da8568.png#averageHue=%23c2ccb2&clientId=ue02559f6-dae8-4&from=paste&height=219&id=uf9f0a9ed&originHeight=274&originWidth=1172&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=141324&status=done&style=none&taskId=ubf14ae34-dcb0-471f-80dd-b944d6a2bee&title=&width=937.6)
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720064797404-02c87227-b347-49e8-a4b6-db6b4c921aed.png#averageHue=%23f9f8f7&clientId=ue02559f6-dae8-4&from=paste&height=262&id=u22dc26e1&originHeight=328&originWidth=1401&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=160366&status=done&style=none&taskId=u81b6efa3-f874-494a-88a3-73cf340da53&title=&width=1120.8)

其次将 `fastpig-dyncomp` 工程下面 `com.ofnull.fastpig.dyncomp.recordprocessor.CamelToUnderScoreProcessor` 将驼峰转下划线源码 写入 `meta_record_processor_impl`表中 
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720065171760-648b6578-4fa4-43c1-be28-2aa0775bbc0f.png#averageHue=%23bdccad&clientId=ue02559f6-dae8-4&from=paste&height=214&id=u94215b3c&originHeight=268&originWidth=1289&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=87686&status=done&style=none&taskId=u5823b49f-d979-4ed7-bf18-9703cf3609b&title=&width=1031.2)

最后任务清洗配置规则配置表 (`meta_job_process`) 新增一条规则
```plsql
INSERT INTO meta_job_process (job_name, operator, conditions, yes_processor, yes_config, no_processor, no_config, orders, sub_flow, fail_policy, remark, create_time, update_time) VALUES('exampleJob', 'default', '{
                                                                                                                                                                                          "r1": [
                                                                                                                                                                                          {
                                                                                                                                                                                          "key":"$.jobName",
                                                                                                                                                                                          "operator": "Equal",
                                                                                                                                                                                          "values": "fastpig"
                                                                                                                                                                                          }
                                                                                                                                                                                          ]
                                                                                                                                                                                          }', 'CamelToUnderScore', '{
                                                                                                                                                                                          "field": "$"
                                                                                                                                                                                          }', NULL, NULL, 0, NULL, 'DISCARD', NULL, '2024-07-04 13:33:52', '2024-07-04 13:33:52');

```
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720071404293-3367669f-1aae-499a-979d-d7cddf2667d3.png#averageHue=%23beb095&clientId=ue02559f6-dae8-4&from=paste&height=126&id=u4495e670&originHeight=157&originWidth=1181&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=63781&status=done&style=none&taskId=u207cc326-6996-46c5-9f1b-4a694c4223f&title=&width=944.8)
解释下: 该规则意思 如果收到数据字段jobName值为fastpig就将 jobName解析称下划线格式 job_name,否者不变.
重新启动任务发送数据：
发送数据 `{"jobName":"fastpig"}` 预期结果  `{"job_name":"fastpig"}`
发送数据 ` {"jobName":"jack"}`  预期结果 `{"jobName":"jack"}`
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720071818911-0c2a322c-9214-47f9-a3fe-d113f49180d2.png#averageHue=%231d1c1c&clientId=ue02559f6-dae8-4&from=paste&height=148&id=u91916b7f&originHeight=185&originWidth=749&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=20020&status=done&style=none&taskId=u7e876c22-494b-4c60-9c8a-5eabea74892&title=&width=599.2)
可以看到符合预期结果，我们只需在任务清洗规则表配置业务逻辑即可实现数据处理，并且每个处理器都可以用户根据业务方实现通用处理器逻辑,以便自己后续复用。

## 项目介绍
### 工程结构
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720071967869-6278ff0d-98f8-4adc-b4ca-aec0c36cbab3.png#averageHue=%233f464e&clientId=ue02559f6-dae8-4&from=paste&height=166&id=u845ef8d5&originHeight=207&originWidth=540&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=8637&status=done&style=none&taskId=ubca2de69-1a18-49c9-9e36-2c5cbe93974&title=&width=432)

- fastpig-blocks  Job通用模块代码
- fastpig-common 工程基础公共依赖
- fastpig-configura 配置中心实现模块
- fastpig-connectores 连接组件实现模块
- fastpig-dyncomp 动态编译源码模块 (比较符实现实现 、数据清洗处理器实现、字段更新策略实现)
- fastpig-spi 扩展点模块
- fastpig-run 通用Job模块
- fast-demo 示例模块
## 配置中心
项目和SpringBoot思想类似，都采取约定大于配置，配置文件类型以YAML为主；支持多种配置中心获取配置。
目前支持：

- Local
- Apollo
- Nacos

项目采用启动参数匹配对应的配置中心，项目支持如下启动命令：

| **short opt** | **long opt** | **说明** | **示例** |
| --- | --- | --- | --- |
| e  | env | 运行环境 约定值 qa ，online  | -e qa  |
| t | type | 配置类型 local， apollo, nacos | -t local |
| n | fileName | 配置文件名  | -n test.yaml,kafka.yaml |
| c | cluster | apollo、nacos使用 集群名称 | -c default |
| a | appId | apollo、nacos使用 appid | -a flink_job_config |


#### Local
采用项目类路径获取配置文件.
示例使用 `-e qa -n example.yaml -t local`

#### Apollo 
支持采用Apollo配置中心获取配置文件.
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720073731297-9b3d8b33-adf7-4e0a-a229-4028b2207a57.png#averageHue=%233f4650&clientId=ue02559f6-dae8-4&from=paste&height=134&id=QpfQz&originHeight=168&originWidth=544&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=7975&status=done&style=none&taskId=u9a56bfb5-cc6e-4b2e-8f51-d0091498466&title=&width=435.2)
注意需要修改项目 `apollo-env.properties`  文件， 根据环境替换对应apollo地址：
```properties
qa=http://81.68.181.139/
online=http://81.68.181.139/
```
| **short opt** | **long opt** | **说明** |
| --- | --- | --- |
| e  | env | 运行环境 约定值 qa ，online  根据环境选择apollo地址拉取配置 |
| t | type | apollo  |
| n | fileName | 配置文件名   多个,分割 |
| c | cluster | apollo集群名称 不填写默认 default |
| a | appId | apollo appid |


示例说明: `-e qa -n test.yaml -t apollo -c default -a flink-config`

#### Nacos
支持nacos作为配置中心
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720074196464-6af8cfdd-e1cc-4063-9fc0-ab02042ffb48.png#averageHue=%23393f43&clientId=ue02559f6-dae8-4&from=paste&height=205&id=u0d74719a&originHeight=256&originWidth=533&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=11416&status=done&style=none&taskId=u5131118f-f6c6-4d2d-8b17-6c6f6c0302a&title=&width=426.4)
首先需要修改项目 `nacos-base.properties`  文件， 根据环境替换对应nacos地址：
```properties
nacosAddress = mse-b283f880-p.nacos-ans.mse.aliyuncs.com
defaultGroup = DEFAULT_GROUP
timeout = 5000
```
| **short opt** | **long opt** | **说明** |
| --- | --- | --- |
| e  | env | 运行环境 约定值 qa ，online |
| t | type | apollo  |
| n | fileName | 对应  dataId 多个,分割 |
| c | cluster | 对应 nacos namespae  |
| a | appId | 对应nacos group |


#### 自定义配置中心
继承如下扩展点类，可以自定义实现配置中心
```properties
public interface IConfigure {
    /**
     * 读取配置
     *
     * @param commandLine 参数命令
     * @return
     */
    Map<String, Object> readCfg(CommandLine commandLine);

}

```

## 基础能力
项目抽象了 规则比较符、处理器、字段更新(状态) SPI接口能, 基于以上SPI实现的能力实现任务清洗规则处理;
### 比较符实现
项目自己实现了一套规则处理器，涉及到数据比较判断进行逻辑匹配，这里提供比较符实现表用来实现比较符逻辑；

比较符提供SPI扩展点：
```java
public interface IMatchOperator extends Closeable {
    /**
     * 
     * @param values 目标参数
     * @param expectValues 预期参数
     * @return
     */
    boolean eval(Object values, Object expectValues);

    /**
     * 资源清理
     */
    @Override
    default void close(){}

}

```

该项目已经默认提供等于、不等于、空、不为空判断等几种实现，用户可以之际实现以上SPI接口实现业务需要的比较符逻辑，甚至可以支持脚本比较器 `AviatorMatchOperator` 可以参考该实现。

该逻辑实现于` fastpig-dyncomp` 模块，该模块未打入fastpig jar，需要将源码写入 `meta_match_operator_impl` 元数据中，最后动态java动态编译执行。
### 数据清洗处理器
项目自己实现了一套规则处理器，根据条件执行对应的分支，每个分支需要匹配一个数据清洗处理器，该处理器通过`meta_record_processor_impl`元数据表定义源码。

数据清洗处理器SPI代码：
```java
public interface IRecordProcessor<T> extends Closeable {

       /**
     * Job配置信息 
     * @param jobConfiguration
     */
    default void setJobConfiguration(IJobConfiguration jobConfiguration) {

    }

    /**
     * 资源初始化类
     * @param config
     * @throws Exception
     */
    default void init(T config) throws Exception {

    }

    /**
     * 参数classType 
     *
     * @return 参数类型
     */
    Class<T> argsType();

    /**
     * 处理规逻辑入口
     *
     * @param event   待处理数据
     * @param config  处理器定义参数配置类 如果没有定义为空
     * @param context 处理上下文
     * @throws Exception
     */
    default void doTransform(IJsonPathSupportedMap event, T config, ProcessContext context) throws Exception {

    }

    /**
     * 资源关闭
     *
     * @throws IOException
     */
    @Override
    default void close() throws IOException {

    }
}
```

可参考实现类：`CamelToUnderScoreProcessor` 实现对应逻辑。

该逻辑实现于` fastpig-dyncomp` 模块，该模块未打入fastpig jar，需要将源码写入 `meta_record_processor_impl`元数据表中，最后动态java动态编译执行。

### 字段更新策略
项目自己实现了自己的表元数据管理，涉及到表字段状态处理，需要实现字段更新策略， 该实现类通过`meta_field_update_impl`元数据表定义源码。

字段更新SPI：
```java
    /**
     * 参数配置类型
     *
     * @return
     */
    default Class<?> argsType() {
        return null;
    }

    /**
     * 字段初始化
     *
     * @param event       待处理数据
     * @param columnMeta  字段元数据信息
     * @param tableMeta   表元数据信息
     * @param eventTime   业务时间
     * @param recordState 状态包装器 封装了flink状态操作
     * @throws Exception
     */
    void init(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception;

    /**
     * 字段新增
     *
     * @param event       待处理数据
     * @param columnMeta  字段元数据信息
     * @param tableMeta   表元数据信息
     * @param eventTime   业务时间
     * @param recordState 状态包装器 封装了flink状态操作
     * @throws Exception
     */
    void add(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception;

    /**
     * 字段合并
     * @param event       待处理数据
     * @param columnMeta  字段元数据信息
     * @param tableMeta   表元数据信息
     * @param eventTime   业务时间
     * @param recordState 状态包装器 封装了flink状态操作
     * @throws Exception
     */
    void merge(Map<String, Object> event, ColumnMetaInfo columnMeta, TableMetaInfo tableMeta, Long eventTime, IRecordState recordState) throws Exception;

```

项目实现了 `DefaultFieldUpdater` 默认字段更新策略（该策略默认啥也不干），如果涉及取最大值，最小值，平均值都可以在这里实现。

该逻辑实现于` fastpig-dyncomp` 模块，该模块未打入fastpig jar，需要将源码写入 `meta_field_update_impl`元数据表中，最后动态java动态编译执行。

### 任务清洗规则实现：
扩展点是：
```yaml
public abstract class BaseRecordProcessEngine<K, I, O> extends KeyedProcessFunction<K, I, O> {

}
```
默认实现是：`com.ofnull.fastpig.run.base.DefaultJobRecordProcessor`
该实现主要依赖 `meta_job_process`配置的任务清洗规则

| **字段** | **说明** |
| --- | --- |
| job_name | 任务名称 |
| operator | 操作名称， 如果同一个任务模型配置多个规则，可以通过操作符区分 |
| conditions | 执行条件 |
| yes_processor | 条件结果true执行该处理器 查看SPI实现com.ofnull.fastpig.spi.recordprocessor.IRecordProcessor |
| yes_config | 条件结果true处理器配置 |
| no_processor | 条件结果false执行该处理器 查看SPI实现com.ofnull.fastpig.spi.recordprocessor.IRecordProcessor |
| no_config | 条件结果false处理器配置 |
| orders | 规则执行顺序 |
| sub_flow | 子流程名称  一旦执行失败，策略为：BREAK_SUB_FLOW 后续相同的子流程都不再执行， |
| fail_policy | 失败策略   com.ofnull.fastpig.spi.metainfo.FailPolicy |


FailPolicy：

| **名称** | **说明** |
| --- | --- |
| IGNORE | 记录日志, 忽略处理异常，继续往下处理 |
| IGNORE_SILENT | ,忽略处理异常，继续往下处理 |
| DISCARD | 处理失败，丢弃当前数据 |
| DISCARD_SILENT | 处理失败，丢弃当前数据 |
| BREAK_SUB_FLOW | 跳出子流程处理，继续往下处理 |


## 连接器

### Kafka
项目封装了Flink Kafka Source Sink连接器，可以只需在Yaml配置即实现Kafka连接和写入；

该连接器也是提供公共的SPI：
```java
public interface IInstanceGenerate<T> {

    /**
     * 配置初始化
     *
     * @param cfg
     */
    void open(Map<String, Object> cfg);

    /**
     * 生成实例对象
     *
     * @return
     */
    T generateInstance();

}

```

Kafka序列化SPI：
```java
public interface BaseKafkaDeserialization<T> extends KafkaDeserializationSchema<T> {

    /**
     * 扩展配置 
     * @param attachMeta 返序列是否添加kafka元数据信息
     */
    void configure(boolean attachMeta);

}


```
默认实现了Json反序列化器 `JsonDeserializationSchemaWrap`
```java
public interface BaseKafkaSerialization<T> extends KafkaSerializationSchema<T> {

    /**
     * 扩展配置 
     * @param topic 主题 
     * @param partitioner 分区器 
     * @param options 附加信息 可以设置路由keyField topicField
     */
    void configure(String topic, FlinkKafkaPartitioner<Map<String, Object>> partitioner, KafkaFormatOptions options);
}
```
  默认实现了Json序列话器  `JsonDeserializationSchemaWrap`

Flink Kafka 分区器SPI
```java
public abstract class BaseKafkaPartitioner<T> extends FlinkKafkaPartitioner<T> {
}
```
默认实现： 固定分区器`KafkaFixedPartitioner` 和 默认分区器 `KafkaDefaultPartitioner`

KafkaSource配置说明：
```java
kafkaConfig:
  consumer:
    properties: &kafkaConsumerProp
      bootstrap.servers: kafka-broker:9092
      enable.auto.commit: true
      auto.commit.interval.ms: 5000
      auto.offset.reset: latest
      request.timeout: 1000
      session.timeout.ms isolation.level: read_committed
      flink.partition-discovery.interval-millis: 60000

source1: &source1
  topic: from_topic
  type: kafkaSource
  startupMode: EARLIEST
  format: json
  uid: source1
  name: source1
  properties:
    <<: *kafkaConsumerProp
    group.id: zk-test-group

sources:
  -
    <<: *source1

```
| 字段 | 说明 |
| --- | --- |
| type | SPI实现的服务名 |
| topic | 消费topic |
| startupMode | Kafka启动模型 参考org.apache.flink.streaming.connectors.kafka.config.StartupMode |
| format | SPI反序列化实现服务名 |
| properties | Kafka额外配置信息 |


KafkaSink配置说明
```java
  producer:
    properties: &kafkaProducerProp
      bootstrap.servers: kafka-broker:9092
      linger.ms: 5
      batch.size: 1310720
      acks: all
      retries: 10
      retry.backoff.ms: 100
      request.timeout.ms: 60000
      enable.idempotence: true
      max.in.flight.requests.per.connection: 1
      transaction.timeout.ms: 600000


dataProduct:
  type: kafkaSink
  topic: to_topic
  format: json
  partitioner: default
  formatOptions:
    jsonPathSupport: true
    keyField:
  properties:
    <<: *kafkaProducerProp

```
| 字段 | 说明 |
| --- | --- |
| type | SPI实现的服务名 |
| topic | 发送topic |
| format | SPI序列化实现服务名 |
| partitioner | SPI分区器服务名 这里使用默认 |
| formatOptions.jsonPathSupport | 是否使用json取值配置 |
| formatOptions.keyField | 根据keyField取值进行hash取值kafka分区 |
| formatOptions.topicField | 如果不为空根据topicField值作为topic发送数据 |
| properties | Kafka额外配置信息 |


### ElasticSearch
项目既支持写入Kafka，也支持写入持久化组件，这里只实现了ES入库操作，不同于官方使用的ES SINK，这里采取自己实现的数据载入器，支持最少一次语义。

数据入库SPI
```java
public abstract class BaseBulkLoader<R extends IPreparedWrite> implements Closeable, IWriteOperate<R>, Serializable {
    protected static final String SEPARATOR = ":";

    /**
     * 资源初始化
     *
     * @param parallelism   数据分区信息
     * @param tableMetaInfo 表元数据信息
     * @throws Exception
     */
    public abstract void init(int parallelism, TableMetaInfo tableMetaInfo) throws Exception;

    /**
     * 数据预写入信息
     *
     * @param partition       数据分区信息
     * @param operateType     数据入库操作类型
     * @param event           待入库数据
     * @param columnMetaInfos 表字段元数据信息
     * @return
     * @throws Exception
     */
    public R prepareWrite(int partition, WriteOperateEnum operateType, Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) throws Exception {
        R prepareWrite = innerPrepareWrite(partition, operateType, event, columnMetaInfos);
        return prepareWrite;
    }

    /**
     * 真正执行写入DB入口 
     * @param partition 数据分区信息
     * @param execBatch 数据批量容器
     * @throws Exception
     */
    public abstract void doBatchWrite(int partition, ExecBatch execBatch) throws Exception;

    /**
     * 主键字段 独立转换器 默认实现了字段反转
     * @param name
     * @return
     */
    public abstract IKeyTransform finderStorageTransform(String name);
```

实现类：`ElasticsearchBulkLoader`
使用前需要先在数据源元数据表维护ES连接信息
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720081134075-bff2ecd1-003d-49eb-923d-2c18029911ab.png#averageHue=%23f9f9f9&clientId=u492cb6ad-3b24-4&from=paste&height=202&id=ucdb3ee16&originHeight=252&originWidth=1240&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=56545&status=done&style=none&taskId=u0ad35fe8-2928-425e-ac95-c7a31cdb815&title=&width=992)

| **字段** | **说明** |
| --- | --- |
| catalog | 连接器名称 |
| type | SPI入库实现类服务名 |
| connect_info | 数据源连接信息json |


创建物理表(`meta_table`)元数据信息 
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720081530811-a2742027-949d-4487-9720-ed017d7beba0.png#averageHue=%23f4f4f3&clientId=u492cb6ad-3b24-4&from=paste&height=154&id=ub5c617e9&originHeight=193&originWidth=537&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=25248&status=done&style=none&taskId=u710e64ac-fcde-4a14-8a48-79551af8075&title=&width=429.6)

| **字段** | **说明** |
| --- | --- |
| id | 主键自增 |
| catalog | 同数据源表连接器名称 |
| schema | ES可以不填  |
| table | ES索引名 |


创建表物理表(`meta_column`)字段元数据信息：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720081742549-31855d9e-ef21-4124-947b-b681d4f1818e.png#averageHue=%23c8bca4&clientId=u492cb6ad-3b24-4&from=paste&height=103&id=u77e2feaf&originHeight=129&originWidth=1239&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=47162&status=done&style=none&taskId=udcc3274a-f497-4296-b3c6-ab387c9945c&title=&width=991.2)

| **字段** | **说明** |
| --- | --- |
| table_id | 物理表主键字段 |
| column_name | DB列名称 |
| field_name  | 数据取值名称 步填写 默认会通过column_name 转驼峰 |
| column_type | 列类型 可以参考SPI实现
com.weimob.saas.flink.spi.columntype.IColumnType |
| column_size | 列长度配置 写入前会被验证 |
| nullable | 是否可以空 写入前会被验证 |
| primary_key | 是否主键  |
| orders | 字段顺序 如果使用了字段状态这个不要随便修改顺序否者影响状态结果 |
| update_policy | 更新策略 SPI 参考 com.ofnull.fastpig.spi.fieldupdater.IFieldUpdater |
| update_policy_args | 更新策略 配置信息 |
| storage_transform | 独立转换器 参考SPI实现 
com.ofnull.fastpig.spi.columntransform.IKeyTransform |


YAML配置：
```yaml
tableInfo:
  catalog: qa-es
  schema: _doc
  table: simple_es

dbWrite:
  parallelism: 2
  maxBatchSize: 1000
  maxBlockTime: 10000
  batchTimeout: 60000
  flushOnSnapshot: true
```

数据入库配置间隔控制说明：

| **字段** | **缺省** | **说明** |
| --- | --- | --- |
| parallelism | 2 | 写入线程池大小  |
| maxBatchSize | 1000 | 批量入库最大间隔大小 |
| maxBlockTime | 10000 | 批量入库最大间隔时间 单位毫秒 |
| batchTimeout | 60000 | 批量写入超时时间 单位毫秒 |
| flushOnSnapshot | true | 快照是否刷新入库 不开启可能会丢数据 |


## 任务模型
默认实现了 两种数据处理模型

| **任务** | **说明** |
| --- | --- |
| `KafkaToKafkaJob` | Kafka -> 清洗 -> Kafka |
| `KafkaToStoreJob` | Kafka->清洗->状态处理->入库 |


#### `KafkaToKafkaJob`
收到Kafka数据之后业务需要对数据进行清洗，清洗之前可能需要对数据进行分区keyed，这里提供清洗keyed配置
```yaml
dataGrouping:
  keys:
    - id
  jsonPathSupported: false
```

jsonPathSupported : 是否支持jsonPath取值
keys：只是多个字段 如果不填写默认随机分区 
其他配置参考Kafka连接器这一小节
#### `KafkaToStoreJob`
收到Kafka数据之后业务需要对数据进行清洗，清洗之前可能需要对数据进行分区keyed，这里提供清洗keyed配置
```yaml
dataGrouping:
  keys:
    - id
  jsonPathSupported: false
```

jsonPathSupported : 是否支持jsonPath取值
keys：只是多个字段 如果不填写默认随机分区 

字段更新处理这块支持配置状态Keyed,  可以不配置，未配置默认取值table主键字段作为分区键
```yaml
stateGrouping:
  keys:
    - id
```

数据入库配置参考ElasticSerach这一小结；

#### 任务清洗规则说明：
扩展点是：
```yaml
public abstract class BaseRecordProcessEngine<K, I, O> extends KeyedProcessFunction<K, I, O> {

}
```
默认实现是：`com.ofnull.fastpig.run.base.DefaultJobRecordProcessor`
该实现主要依赖 `meta_job_process`配置的任务清洗规则

| **字段** | **说明** |
| --- | --- |
| job_name | 任务名称 |
| operator | 操作名称， 如果同一个任务模型配置多个规则，可以通过操作符区分 |
| conditions | 执行条件 |
| yes_processor | 条件结果true执行该处理器 查看SPI实现com.ofnull.fastpig.spi.recordprocessor.IRecordProcessor |
| yes_config | 条件结果true处理器配置 |
| no_processor | 条件结果false执行该处理器 查看SPI实现com.ofnull.fastpig.spi.recordprocessor.IRecordProcessor |
| no_config | 条件结果false处理器配置 |
| orders | 规则执行顺序 |
| sub_flow | 子流程名称  一旦执行失败，策略为：BREAK_SUB_FLOW 后续相同的子流程都不再执行， |
| fail_policy | 失败策略   com.ofnull.fastpig.spi.metainfo.FailPolicy |


FailPolicy：

| **名称** | **说明** |
| --- | --- |
| IGNORE | 记录日志, 忽略处理异常，继续往下处理 |
| IGNORE_SILENT | ,忽略处理异常，继续往下处理 |
| DISCARD | 处理失败，丢弃当前数据 |
| DISCARD_SILENT | 处理失败，丢弃当前数据 |
| BREAK_SUB_FLOW | 跳出子流程处理，继续往下处理 |


#### 任务配置参数解析：
```yaml
job:
  name: exampleJob
  parallelism: 1
  enableLocalWeb: true
  checkpoint:
    enable: true
    betweenInterval: 6000
    interval: 6000
    timeout: 60000
    cleanup: RETAIN_ON_CANCELLATION
    concurrent: 1
    checkpointingMode: EXACTLY_ONCE
  restart:
    strategy: fixed-delay
    attempts: 3
    delay: 10

```
| **字段** | **解释** |
| --- | --- |
| name | 任务名称 如果存在数据清洗 和 `meta_job_process` job_name保持一致 |
| parallelism | 任务并行度 |
| enableLocalWeb | true 开启本地UI |
| checkpoint.enable | 是否开启检查点 |
| checkpoint.betweenInterval | 两次检查点最小时间间隔 |
| checkpoint.interval | 检查点间隔 |
| checkpoint.timeout | 检查点超时时间 |
| checkpoint.cleanup | 检查点清理策略  |
| checkpoint.concurrent | 检查点最大并发 |
| checkpoint.checkpointingMode | 检查点原子语义org.apache.flink.streaming.api.CheckpointingMode |
| restart | 重新策略配置 参考：com.ofnull.fastpig.common.job.RestartStrategyInfo |
| 
 | 
 |

## 扩展点
整个项目在各个节点都提供相扩展点，除了项目基础提供的实现，可以自己根据业务实现特殊的扩展点实现。
![image.png](https://cdn.nlark.com/yuque/0/2024/png/136684/1720102494310-a1f3f827-8a42-476b-8421-28337a63d210.png#averageHue=%233e434a&clientId=u67388993-08d0-4&from=paste&height=408&id=uae78f825&originHeight=816&originWidth=738&originalType=binary&ratio=2&rotation=0&showTitle=false&size=74846&status=done&style=none&taskId=u9322d456-ab8c-41c3-941b-cbc71a8b027&title=&width=369)



### 









