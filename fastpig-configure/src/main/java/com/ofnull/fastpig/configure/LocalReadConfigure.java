package com.ofnull.fastpig.configure;


import com.ofnull.fastpig.common.cli.DefaultCli;
import com.ofnull.fastpig.common.utils.YamlUtil;
import com.ofnull.fastpig.spi.configure.IConfigure;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * example -e qa -n pig.yml -t local
 *
 * @author ofnull
 * @date 2022/2/10 15:43
 */
public class LocalReadConfigure implements IConfigure {
    public static final Logger LOG = LoggerFactory.getLogger(LocalReadConfigure.class);
    private static final String PREFIX = "./";

    @Override
    public Map<String, Object> readCfg(CommandLine commandLine) {
        String yamlCfg = localReadConfig(commandLine);
        return YamlUtil.parseYaml(yamlCfg);
    }

    private String localReadConfig(CommandLine commandLine) {
        String fullName = DefaultCli.requiredOption(commandLine, DefaultCli.name);
        StringBuffer yamlCfg = new StringBuffer();
        for (String name : fullName.split(",")) {
            InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PREFIX + name);
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(inStream, writer, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("parseYaml load file error fileName: " + name, e);
            } finally {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOG.warn("parseYaml close InputStream Exception", e);
                }
            }
            yamlCfg.append(writer);
        }

        return yamlCfg.toString();
    }
}
