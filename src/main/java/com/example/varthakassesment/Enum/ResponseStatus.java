package com.example.varthakassesment.Enum;

public enum ResponseStatus {
    CREATED,
    ACCEPTED,
    CONFLICT, //When there's a duplication so to speak: Email already exists 409
    INTERNAL_SERVER_ERROR,
    BAD_REQUEST,
    NOT_FOUND,
    OK,
    UNAUTHORIZED  //401
    ,
    BAD_GATEWAY

}
