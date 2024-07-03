package com.ofnull.fastpig.spi.bulkloader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ofnull
 * @date 2024/6/12 19:34
 */
public class ExecBatch {

    private List<?> dataList = new ArrayList<>();

    public List<?> getDataList() {
        return dataList;
    }

    public void setDataList(List<?> dataList) {
        this.dataList = dataList;
    }
}
