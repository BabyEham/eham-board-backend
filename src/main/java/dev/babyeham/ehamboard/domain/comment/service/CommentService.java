package dev.babyeham.ehamboard.domain.comment.service;

import dev.babyeham.ehamboard.domain.comment.dto.CommentResponse;
import dev.babyeham.ehamboard.domain.comment.dto.CreateCommentRequest;
import dev.babyeham.ehamboard.domain.comment.dto.UpdateCommentRequest;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long postId, CreateCommentRequest request, Long userId);
    List<CommentResponse> getCommentsByPost(Long postId);
    CommentResponse getComment(Long commentId);
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId);
    void deleteComment(Long commentId, Long userId);
}
