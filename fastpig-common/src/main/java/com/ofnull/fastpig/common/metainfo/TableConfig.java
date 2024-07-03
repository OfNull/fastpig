package com.ofnull.fastpig.common.metainfo;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/6/14 16:46
 */
public class TableConfig implements Serializable {

    @NotBlank
    private String catalog;
    @NotBlank
    private String schema = "";
    @NotBlank
    private String table;

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
