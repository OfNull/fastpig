package com.ofnull.fastpig.connector.elasticsearch.bulkloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author ofnull
 * @date 2024/6/13 15:46
 */
public class ElasticsearchConfig implements Serializable {
    static final Logger LOG = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @NotEmpty(message = "es config urls empty")
    private List<String> urls;

    private String user;

    private String password;

    @NotEmpty(message = "es config maxRetryTimeout empty")
    private Integer maxRetryTimeout = 30000;

    @NotEmpty(message = "es config connectTimeout empty")
    private Integer connectTimeout = 1000;

    @NotEmpty(message = "es config socketTimeout empty")
    private Integer socketTimeout = 10000;

    @NotEmpty(message = "es config maxConnTotal empty")
    private Integer maxConnTotal = 150;

    @NotEmpty(message = "es config maxConnPerRoute empty")
    private Integer maxConnPerRoute = 50;

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaxRetryTimeout() {
        return maxRetryTimeout;
    }

    public void setMaxRetryTimeout(Integer maxRetryTimeout) {
        this.maxRetryTimeout = maxRetryTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Integer getMaxConnTotal() {
        return maxConnTotal;
    }

    public void setMaxConnTotal(Integer maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    public Integer getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(Integer maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }
}
