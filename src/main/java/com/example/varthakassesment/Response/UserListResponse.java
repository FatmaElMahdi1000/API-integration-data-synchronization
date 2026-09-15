package com.example.varthakassesment.Response;

import com.example.varthakassesment.DTO.External.ExPaginationDTO;
import com.example.varthakassesment.DTO.External.ExUserDTO;

import java.util.List;

//External API Response
public class UserListResponse {

    List<ExUserDTO> data;
    private ExPaginationDTO pagination;

    public UserListResponse() {
    }

    public List<ExUserDTO> getData() {
        return data;
    }

    public void setData(List<ExUserDTO> data) {
        this.data = data;
    }

    public ExPaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(ExPaginationDTO pagination) {
        this.pagination = pagination;
    }
}
