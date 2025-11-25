package dev.babyeham.ehamboard.domain.comment.controller;

import dev.babyeham.ehamboard.domain.comment.dto.CommentResponse;
import dev.babyeham.ehamboard.domain.comment.dto.CreateCommentRequest;
import dev.babyeham.ehamboard.domain.comment.dto.UpdateCommentRequest;
import dev.babyeham.ehamboard.domain.comment.service.CommentService;
import dev.babyeham.ehamboard.domain.user.entity.User;
import dev.babyeham.ehamboard.global.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId,
                                                          @Valid @RequestBody CreateCommentRequest request,
                                                          @AuthenticationPrincipal User user) {
        CommentResponse response = commentService.createComment(postId, request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "특정 게시글의 댓글 목록 조회")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> response = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{commentId}")
    @Operation(summary = "댓글 하나 조회")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long commentId) {
        CommentResponse response = commentService.getComment(commentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId,
                                                          @Valid @RequestBody UpdateCommentRequest request,
                                                          @AuthenticationPrincipal User user) {
        CommentResponse response = commentService.updateComment(commentId, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId,
                                                          @AuthenticationPrincipal User user) {
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.ok(new MessageResponse("댓글 삭제 성공"));
    }
}
