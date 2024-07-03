package com.ofnull.fastpig.spi.metainfo;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/6/20
 */
public class SourceClassDefinition implements Serializable {

    private String name;
    private String definition;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }


}
