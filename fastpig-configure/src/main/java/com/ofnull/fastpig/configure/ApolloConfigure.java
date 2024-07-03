package com.ofnull.fastpig.configure;

import cn.hutool.http.HttpUtil;
import com.jayway.jsonpath.JsonPath;
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
 * https://www.apolloconfig.com/#/zh/client/java-sdk-user-guide
 *
 * @author ofnull
 * @date 2024/6/7 11:24
 */
public class ApolloConfigure implements IConfigure {
    public static final String APOLLO_ENV_PROPERTIES = "apollo-env.properties";
    public static final String DEFAULT_CLUSTER = "default";
    protected static final Map<String, String> apolloAddressMap;

    static {
        try {
            InputStream inputStream = ApolloConfigure.class.getClassLoader().getResource(APOLLO_ENV_PROPERTIES).openStream();
            String apolloEnvStr = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Properties properties = new Properties();
            properties.load(new StringReader(apolloEnvStr));
            apolloAddressMap = PropertiesUtil.toMaps(properties);
        } catch (IOException e) {
            throw new RuntimeException("load apollo-env.properties IOException", e);
        }
    }

    @Override
    public Map<String, Object> readCfg(CommandLine commandLine) {
        String yamlCfg = remoteReadConfig(commandLine);
        return YamlUtil.parseYaml(yamlCfg);
    }

    private String remoteReadConfig(CommandLine commandLine) {
        String apolloAddress = getApolloAddress(commandLine);
        String cluster = DefaultCli.getOption(commandLine, DefaultCli.cluster, DEFAULT_CLUSTER);
        String appId = DefaultCli.requiredOption(commandLine, DefaultCli.appId);
        String namespace = DefaultCli.requiredOption(commandLine, DefaultCli.name);

        StringBuffer config = new StringBuffer();
        for (String name : namespace.split(",")) {
            String url = String.format("%s/configs/%s/%s/%s", apolloAddress, appId, cluster, name);
            String content = HttpUtil.get(url, 2000);
            String read = JsonPath.read(content, "$.configurations.content").toString();
            config.append(read);
        }
        return config.toString();
    }

    public String getApolloAddress(CommandLine commandLine) {
        String env = DefaultCli.requiredOption(commandLine, DefaultCli.env);
        String apolloAddress = apolloAddressMap.get(env);
        if (apolloAddress == null) {
            throw new RuntimeException("No such apollo env: " + env);
        }
        return apolloAddress;
    }
}
