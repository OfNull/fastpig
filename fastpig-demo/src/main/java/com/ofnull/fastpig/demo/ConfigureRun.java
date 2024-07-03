package com.ofnull.fastpig.demo;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * local => -e qa -n kafka.yaml,pig.yaml -t local
 * apollo => -e qa -n ncdp_dwb_user_cards.yaml -t apollo -c default -a saas.mcloud-cdp-flink-stream
 *
 * @author ofnull
 * @date 2024/6/7 10:52
 */
public class ConfigureRun {
    public static final Logger LOG = LoggerFactory.getLogger(ConfigureRun.class);

    public static void main(String[] args) throws ParseException {
//        Options ops = new Options();
//        DefaultCli.addOption(ops);
//        CommandLine commandLine = new DefaultParser().parse(ops, args);
//
//        IConfigure configure = ServiceLoaderHelper.loadServices(IConfigure.class, commandLine.getOptionValue(DefaultCli.type.getOpt()));
//        Map<String, Object> cfg = configure.readCfg(commandLine);
//        System.out.println("--------");


        try {
            testNacos();
            new CountDownLatch(1).await();
            System.out.println("------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void testNacos() throws NacosException, InterruptedException {
        String serverAddr = "mse-b283f880-p.nacos-ans.mse.aliyuncs.com";
        String dataId = "msyql";
        String group = "TEST";
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.NAMESPACE, "");

        // 如果报错403，请将这三行反注释
        // properties.put(PropertyKeyConst.ACCESS_KEY, {accessKey});
        // properties.put(PropertyKeyConst.SECRET_KEY, "{securityKey}");

        ConfigService configService = NacosFactory.createConfigService(properties);
        String content = configService.getConfig(dataId, group, 5000);
        System.out.println(content);
        configService.addListener(dataId, group, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                System.out.println("recieve:" + configInfo);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });

        boolean isPublishOk = configService.publishConfig(dataId, group, "content");
        System.out.println(isPublishOk);

        Thread.sleep(3000);
        content = configService.getConfig(dataId, group, 5000);
        System.out.println(content);

        boolean isRemoveOk = configService.removeConfig(dataId, group);
        System.out.println(isRemoveOk);
        Thread.sleep(3000);

        content = configService.getConfig(dataId, group, 5000);
        System.out.println(content);
        Thread.sleep(300000);
    }
}
