package com.ofnull.fastpig.connector.jdbc.bulkloader;

import com.alibaba.druid.pool.DruidDataSource;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author kun.zhou
 * @date 2024/7/16
 */
public class JdbcConfig implements Serializable {
    //  JDBC URL，根据不同的数据库，使用相应的JDBC连接字符串
    @NotEmpty(message = "url not Empty")
    private String url;
    // 用户名，
    @NotNull(message = "user not null")
    private String user;
    //密码，
    private String password;
    //JDBC驱动名，
    @NotNull(message = "driver not null")
    private String driver = "com.mysql.jdbc.Driver";

    // 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
    private int initialSize = 0;
    // 最大连接池数量
    private int maxActive = 8;
    // 最小连接池数量
    private int minIdle = 0;
    // 获取连接时最大等待时间，单位毫秒。配置了maxWait之后， 缺省启用公平锁，并发效率会有所下降， 如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
    private long maxWait = 1000L;
    // 是否缓存preparedStatement，也就是PSCache。 PSCache对支持游标的数据库性能提升巨大，比如说oracle。 在mysql5.5以下的版本中没有PSCache功能，建议关闭掉。作者在5.5版本中使用PSCache，通过监控界面发现PSCache有缓存命中率记录， 该应该是支持PSCache。
    private boolean poolPreparedStatements = false;
    // 要启用PSCache，必须配置大于0，当大于0时， poolPreparedStatements自动触发修改为true。 在Druid中，不会存在Oracle下PSCache占用内存过多的问题， 可以把这个数值配置大一些，比如说100
    private int maxOpenPreparedStatements = -1;
    // 用来检测连接是否有效的sql，要求是一个查询语句。 如果validationQuery为null，testOnBorrow、testOnReturn、 testWhileIdle都不会其作用。
    private String validationQuery = "SELECT 1";
    // 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
    private boolean testOnBorrow = true;
    // 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    private boolean testOnReturn = false;
    // 建议配置为true，不影响性能，并且保证安全性。 申请连接的时候检测，如果空闲时间大于 timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
    private boolean testWhileIdle = false;
    // 有两个含义： 1) Destroy线程会检测连接的间隔时间 2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
    private long timeBetweenEvictionRunsMillis = 60000L;
    // 物理连接初始化的时候执行的sql
    private String connectionInitSqls = "SELECT 1";
    //自动提交
    private boolean autoCommit = true;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public int getMaxOpenPreparedStatements() {
        return maxOpenPreparedStatements;
    }

    public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public String getConnectionInitSqls() {
        return connectionInitSqls;
    }

    public void setConnectionInitSqls(String connectionInitSqls) {
        this.connectionInitSqls = connectionInitSqls;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public DruidDataSource toDruidDataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(url);
        ds.setDriverClassName(driver);
        ds.setUsername(user);
        ds.setPassword(password);

        ds.setInitialSize(initialSize);
        ds.setMaxActive(maxActive);
        ds.setMinIdle(minIdle);
        ds.setMaxWait(maxWait);
        ds.setPoolPreparedStatements(poolPreparedStatements);
        ds.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
        ds.setValidationQuery(validationQuery);
        ds.setTestOnBorrow(testOnBorrow);
        ds.setTestOnReturn(testOnReturn);
        ds.setTestWhileIdle(testWhileIdle);
        ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        ds.setConnectionInitSqls(Arrays.asList(connectionInitSqls));

        ds.setDefaultAutoCommit(autoCommit);
        return ds;
    }
}
