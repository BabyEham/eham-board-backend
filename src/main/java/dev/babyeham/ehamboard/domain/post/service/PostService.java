package dev.babyeham.ehamboard.domain.post.service;

import dev.babyeham.ehamboard.domain.post.dto.CreatePostRequest;
import dev.babyeham.ehamboard.domain.post.dto.PostResponse;
import dev.babyeham.ehamboard.domain.post.dto.UpdatePostRequest;

import java.util.List;

public interface PostService {
    PostResponse createPost(CreatePostRequest request, Long userId);
    PostResponse getPost(Long postId);
    List<PostResponse> getAllPosts();
    List<PostResponse> searchPostsByTitle(String keyword);
    PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId);
    void deletePost(Long postId, Long userId);
    List<PostResponse> getPostsByUser(Long userId);
}
