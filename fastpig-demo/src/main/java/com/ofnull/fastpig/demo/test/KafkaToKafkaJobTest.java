package com.ofnull.fastpig.demo.test;

import com.ofnull.fastpig.run.jobs.KafkaToStoreJob;

/**
 * -e qa -n datasource.yaml,kafka.yaml,pig.yaml -t local
 *
 * @author ofnull
 * @date 2024/6/26
 */
public class KafkaToKafkaJobTest {
    public static void main(String[] args) throws Exception {
        KafkaToStoreJob.main(args);
    }
}
