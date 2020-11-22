package com.baiwang.cloud.common.model;

import java.util.List;

public class PageResult<T> {

        //总数量
    private long total;

    private T source;


    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }



    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }


}
