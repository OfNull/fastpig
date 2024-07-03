package com.ofnull.fastpig.common.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author ofnull
 * @date 2022/2/10 18:34
 */
public class DefaultCli {
    public static final Option env = new Option("e", "env", true, "environment");
    public static final Option type = new Option("t", "type", true, "config type");

    public static final Option name = new Option("n", "fileName", true, "apollo -> namespace | local -> fileName | ");
    public static final Option cluster = new Option("c", "cluster", true, "config cluster name");
    public static final Option appId = new Option("a", "appId", true, "app id");

    public static void addOption(Options ops) {
        ops.addOption(env);
        ops.addOption(name);
        ops.addOption(type);

        ops.addOption(cluster);
        ops.addOption(appId);
    }

    public static String getOption(CommandLine commandLine, Option op, String defaultValue) {
        if (commandLine.hasOption(op.getOpt())) {
            return commandLine.getOptionValue(op.getOpt());
        }
        return defaultValue;
    }

    public static String getOption(CommandLine commandLine, Option op) {
        return getOption(commandLine, op, null);
    }

    public static String requiredOption(CommandLine commandLine, Option op) {
        String value = getOption(commandLine, op, null);
        if (value == null) {
            throw new IllegalArgumentException("Missing required param [-" + op.getOpt() + "]");
        }
        return value;

    }


}
