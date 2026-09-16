package com.example.varthakassesment.Mapper;


import com.example.varthakassesment.DTO.External.ExUserDTO;
import com.example.varthakassesment.Model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

//this annotation helps getting mapper class in IOC container of spring
@Mapper(componentModel = "spring")
public interface UserMapper {

    // specifying attributes whose names are different.
    @Mapping(source  ="id", target="externalUserId")

    @Mapping(source="createdAt", target="externalCreatedAt")

    @Mapping(source="updatedAt", target="externalUpdatedAt")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "company", ignore = true) //Added ignoring company mapping.


    //REVIEW
//    @Mapping(target = "company", ignore = true) // Stops MapStruct from overwriting the shared Company entity

    public void MapExUserToEntity(ExUserDTO ExternalUserDTO ,
                                 @MappingTarget User user);



}
