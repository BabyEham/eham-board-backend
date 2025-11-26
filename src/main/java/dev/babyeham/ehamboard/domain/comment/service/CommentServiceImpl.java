package dev.babyeham.ehamboard.domain.comment.service;

import dev.babyeham.ehamboard.domain.comment.dto.CommentResponse;
import dev.babyeham.ehamboard.domain.comment.dto.CreateCommentRequest;
import dev.babyeham.ehamboard.domain.comment.dto.UpdateCommentRequest;
import dev.babyeham.ehamboard.domain.comment.entity.Comment;
import dev.babyeham.ehamboard.domain.comment.repository.CommentRepository;
import dev.babyeham.ehamboard.domain.post.entity.Post;
import dev.babyeham.ehamboard.domain.post.repository.PostRepository;
import dev.babyeham.ehamboard.domain.user.entity.User;
import dev.babyeham.ehamboard.domain.user.repository.UserRepository;
import dev.babyeham.ehamboard.global.exception.CommentNotFoundException;
import dev.babyeham.ehamboard.global.exception.PostNotFoundException;
import dev.babyeham.ehamboard.global.exception.UnauthorizedCommentAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .build();
        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("게시글을 찾을 수 없습니다.");
        }
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));
        return CommentResponse.from(comment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));
        if (!comment.isAuthor(userId)) {
            throw new UnauthorizedCommentAccessException("댓글을 수정할 권한이 없습니다.");
        }
        comment.update(request.getContent());
        return CommentResponse.from(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));
        if (!comment.isAuthor(userId)) {
            throw new UnauthorizedCommentAccessException("댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
}

