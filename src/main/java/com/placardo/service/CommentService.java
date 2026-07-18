package com.placardo.service;

import com.placardo.dto.CommentDto;
import com.placardo.entity.*;
import com.placardo.exception.NotFoundException;
import com.placardo.mapper.CommentMapper;
import com.placardo.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final AdService adService;

    @Transactional(readOnly = true)
    public List<CommentDto> listByAd(Long adId) {
        return commentMapper.toDtos(commentRepository.findByAdIdOrderByCreatedAtAsc(adId));
    }

    @Transactional
    public CommentDto add(Long adId, User author, String body) {
        Ad ad = adService.getOrThrow(adId);
        Comment comment = commentRepository.save(Comment.builder()
                .ad(ad)
                .user(author)
                .body(body.trim())
                .build());
        return commentMapper.toDto(comment);
    }

    /** Удалить может автор комментария или администратор */
    @Transactional
    public void delete(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден: " + commentId));
        boolean allowed = currentUser.getRole() == Role.ADMIN
                || comment.getUser().getId().equals(currentUser.getId());
        if (!allowed) {
            throw new AccessDeniedException("Нельзя удалить чужой комментарий");
        }
        commentRepository.delete(comment);
    }
}
