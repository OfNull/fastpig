package com.ofnull.fastpig.spi.metainfo;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/6/12 13:44
 */
public class DatasourceMetaInfo implements Serializable {
    private String catalog;
    //注意和 连接器的SPI名称一直
    private String type;
    private String connectInfo;

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConnectInfo() {
        return connectInfo;
    }

    public void setConnectInfo(String connectInfo) {
        this.connectInfo = connectInfo;
    }
}
