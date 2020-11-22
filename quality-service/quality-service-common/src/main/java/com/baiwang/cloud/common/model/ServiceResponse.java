package com.baiwang.cloud.common.model;

import java.io.Serializable;
import java.util.List;

public class ServiceResponse<T> implements Serializable {
    private int code;
    private String message;
    private Boolean success;
    private T data;

    public static<T> ServiceResponse<T> success(T data,String message){
        ServiceResponse<T> serviceResponse = new ServiceResponse<T>();
        serviceResponse.setMessage(message);
        serviceResponse.setCode(0);
        serviceResponse.setData(data);
        return serviceResponse;
    }

    public static<T> ServiceResponse<T> error(String message,int code){
        ServiceResponse<T> serviceResponse = new ServiceResponse<T>();
        serviceResponse.setMessage(message);
        serviceResponse.setCode(code);
        return serviceResponse;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return code==0;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
