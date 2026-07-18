package com.placardo.repository;

import com.placardo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByAdIdOrderByCreatedAtAsc(Long adId);

    long countByAdId(Long adId);
}
