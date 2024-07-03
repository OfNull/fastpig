package com.ofnull.fastpig.spi.configure;


import org.apache.commons.cli.CommandLine;

import java.util.Map;

/**
 * @author ofnull
 * @date 2022/2/10 11:37
 */
public interface IConfigure {

    Map<String, Object> readCfg(CommandLine commandLine);

}
