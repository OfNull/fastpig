package com.ofnull.fastpig.connector.kafka.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ofnull.fastpig.spi.kafkadeser.KafkaFormatOptions;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * @author ofnull
 * @date 2022/8/18 17:48
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaSinkConfig {

    @NotEmpty
    private String topic;
    @NotEmpty
    private String format;

    private Map<String, Object> properties;
    //分区器
    private String partitioner = "default";

    private KafkaFormatOptions formatOptions;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getPartitioner() {
        return partitioner;
    }

    public void setPartitioner(String partitioner) {
        this.partitioner = partitioner;
    }

    public KafkaFormatOptions getFormatOptions() {
        return formatOptions;
    }

    public void setFormatOptions(KafkaFormatOptions formatOptions) {
        this.formatOptions = formatOptions;
    }
}
