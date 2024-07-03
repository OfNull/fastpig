-- fastpig.meta_column definition

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

-- fastpig.meta_table definition

CREATE TABLE `meta_table` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
                              `catalog` varchar(100) DEFAULT NULL COMMENT '数据源',
                              `schema` varchar(100) NOT NULL DEFAULT '' COMMENT 'schema名',
                              `table` varchar(100) DEFAULT NULL COMMENT '表名',
                              `description` varchar(100) DEFAULT NULL COMMENT '描述',
                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `cdp_meta_table_UN` (`catalog`,`schema`,`table`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物理表定义';


-- fastpig.meta_datasource definition

CREATE TABLE `meta_datasource` (
                                   `catalog` varchar(20) NOT NULL COMMENT '数据源名称',
                                   `type` varchar(20) NOT NULL COMMENT '数据源类型和连接器SPI名称保持一致',
                                   `connect_info` text NOT NULL COMMENT '数据源连接信息,json格式',
                                   PRIMARY KEY (`catalog`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- fastpig.meta_field_update_impl definition

CREATE TABLE `meta_field_update_impl` (
                                          `name` varchar(100) NOT NULL,
                                          `definition` text NOT NULL,
                                          `description` varchar(100) DEFAULT NULL,
                                          PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字段更新策略实现表';

-- fastpig.meta_match_operator_impl definition

CREATE TABLE `meta_match_operator_impl` (
                                            `name` varchar(100) NOT NULL,
                                            `definition` text NOT NULL,
                                            `description` varchar(100) DEFAULT NULL,
                                            PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='比较符实现表';

-- fastpig.meta_record_processor_impl definition

CREATE TABLE `meta_record_processor_impl` (
                                              `name` varchar(100) NOT NULL,
                                              `definition` text NOT NULL,
                                              `description` varchar(100) DEFAULT NULL,
                                              PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='比较符实现表';

-- fastpig.meta_job_process definition

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
) ENGINE=InnoDB AUTO_INCREMENT=8180202 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='flink任务清洗规则配置表';

