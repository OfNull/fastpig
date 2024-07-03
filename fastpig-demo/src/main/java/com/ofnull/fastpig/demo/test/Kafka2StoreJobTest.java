package com.ofnull.fastpig.demo.test;

import com.ofnull.fastpig.run.jobs.KafkaToStoreJob;

/**
 * -e qa -n datasource.yaml,kafka.yaml,KafkatoStore.yaml -t local
 *
 * @author ofnull
 * @date 2024/7/1
 */
public class Kafka2StoreJobTest {
    public static void main(String[] args) throws Exception {
        KafkaToStoreJob.main(args);
    }
}
