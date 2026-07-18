package com.placardo.mapper;

import com.placardo.dto.CommentDto;
import com.placardo.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId", source = "user.id")
    @Mapping(target = "authorName", source = "user.name")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtos(List<Comment> comments);
}
