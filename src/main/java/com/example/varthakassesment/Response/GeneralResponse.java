package com.example.varthakassesment.Response;

import com.example.varthakassesment.DTO.External.ExPaginationDTO;
import com.example.varthakassesment.Enum.ResponseStatus;

//For internal API Response, return generic type
public class GeneralResponse<T>{
    private T data;
    private ResponseStatus response;
    private String message;

    public GeneralResponse(T data, ResponseStatus response) {
        this.data = data;
        this.response = response;
    }

    public GeneralResponse() {
    }

    public GeneralResponse(ResponseStatus response, String message, T data ) {
        this.data = data;
        this.message = message;
        this.response = response;
    }



    public T getData() {
        return data;
    }

    public void setResponse(ResponseStatus response) {
        this.response = response;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ResponseStatus getResponse() {
        return response;
    }

    public String getMessage() {
        return message;
    }
}
