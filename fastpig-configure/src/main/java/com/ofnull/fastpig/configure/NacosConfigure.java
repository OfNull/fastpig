package com.ofnull.fastpig.configure;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.base.Preconditions;
import com.ofnull.fastpig.common.cli.DefaultCli;
import com.ofnull.fastpig.common.utils.PropertiesUtil;
import com.ofnull.fastpig.common.utils.YamlUtil;
import com.ofnull.fastpig.spi.configure.IConfigure;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * -n 对应 nacos data_id概念 配置ID
 * -c 对应 nacos  namespae概念  区分环境
 * -a 对应 nacos  Group 概念  默认 DEFAULT_GROUP
 *
 * @author ofnull
 * @date 2024/6/7 15:28
 */
public class NacosConfigure implements IConfigure {
    private static final String NACOS_ADDRESS = "nacosAddress";
    private static final String DEFAULT_GROUP = "defaultGroup";
    private static final String TIME_OUT = "timeout";
    public static final String NACOS_PROPERTIES = "nacos-base.properties";
    protected static final Map<String, String> nacosMap;


    static {
        try {
            InputStream inputStream = NacosConfigure.class.getClassLoader().getResource(NACOS_PROPERTIES).openStream();
            String nacosBase = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Properties properties = new Properties();
            properties.load(new StringReader(nacosBase));
            Preconditions.checkArgument(
                    properties.get(NACOS_ADDRESS) != null, "nacos-base.properties nacosAddress not null.");
            Preconditions.checkArgument(
                    properties.get(DEFAULT_GROUP) != null, "nacos-base.properties defaultGroup not null.");
            Preconditions.checkArgument(
                    properties.get(TIME_OUT) != null, "nacos-base.properties timeout not null.");
            nacosMap = PropertiesUtil.toMaps(properties);
        } catch (IOException e) {
            throw new RuntimeException("load nacos-base.properties IOException", e);
        }
    }

    @Override
    public Map<String, Object> readCfg(CommandLine commandLine) {
        String dataIds = DefaultCli.requiredOption(commandLine, DefaultCli.name);
        String namespae = DefaultCli.requiredOption(commandLine, DefaultCli.cluster);
        String group = DefaultCli.getOption(commandLine, DefaultCli.appId, nacosMap.get(DEFAULT_GROUP));

        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosMap.get(NACOS_ADDRESS));
        properties.put(PropertyKeyConst.NAMESPACE, namespae);
        StringBuffer config = new StringBuffer();
        for (String dataId : dataIds.split(",")) {
            try {
                ConfigService configService = NacosFactory.createConfigService(properties);
                String content = configService.getConfig(dataId, group, Long.valueOf(nacosMap.get(TIME_OUT)));
                config.append(content);
            } catch (NacosException e) {
                throw new RuntimeException("Nacos request fail, dataId:" + dataIds, e);
            }
        }
        return YamlUtil.parseYaml(config.toString());
    }
}
