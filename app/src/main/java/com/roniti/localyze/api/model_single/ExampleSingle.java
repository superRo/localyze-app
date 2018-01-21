package com.roniti.localyze.api.model_single;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExampleSingle {

    @SerializedName("result")
    @Expose
    private ResultSingle resultSingle;
    @SerializedName("status")
    @Expose
    private String status;


    public ResultSingle getResult() {
        return resultSingle;
    }

    public void setResult(ResultSingle resultSingle) {
        this.resultSingle = resultSingle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
