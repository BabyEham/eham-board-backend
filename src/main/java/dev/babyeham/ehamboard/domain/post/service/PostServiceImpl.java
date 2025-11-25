package dev.babyeham.ehamboard.domain.post.service;

import dev.babyeham.ehamboard.domain.post.entity.Post;
import dev.babyeham.ehamboard.domain.post.repository.PostRepository;
import dev.babyeham.ehamboard.domain.post.dto.CreatePostRequest;
import dev.babyeham.ehamboard.domain.post.dto.PostResponse;
import dev.babyeham.ehamboard.domain.post.dto.UpdatePostRequest;
import dev.babyeham.ehamboard.domain.user.entity.User;
import dev.babyeham.ehamboard.domain.user.repository.UserRepository;
import dev.babyeham.ehamboard.global.exception.PostNotFoundException;
import dev.babyeham.ehamboard.global.exception.UnauthorizedPostAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        return PostResponse.from(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> searchPostsByTitle(String keyword) {
        return postRepository.searchByTitle(keyword)
                .stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.isAuthor(userId)) {
            throw new UnauthorizedPostAccessException("게시글을 수정할 권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());
        return PostResponse.from(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.isAuthor(userId)) {
            throw new UnauthorizedPostAccessException("게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}
