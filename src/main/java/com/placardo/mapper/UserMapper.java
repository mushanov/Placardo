package com.placardo.mapper;

import com.placardo.dto.UserDto;
import com.placardo.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtos(List<User> users);
}
