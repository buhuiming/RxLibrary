package com.bhm.sdk.rxlibrary.rxbus;

/**
 * RxBus data
 * Created by bhm on 2018/5/15.
 */
public class BusData {

    private String id;
    private String status;

    public BusData() {

    }

    public BusData(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}