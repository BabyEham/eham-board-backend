package dev.babyeham.ehamboard.domain.post.service;

import dev.babyeham.ehamboard.domain.post.dto.CreatePostRequest;
import dev.babyeham.ehamboard.domain.post.dto.PostResponse;
import dev.babyeham.ehamboard.domain.post.dto.UpdatePostRequest;
import dev.babyeham.ehamboard.domain.post.entity.Post;
import dev.babyeham.ehamboard.domain.post.repository.PostRepository;
import dev.babyeham.ehamboard.domain.user.entity.User;
import dev.babyeham.ehamboard.domain.user.repository.UserRepository;
import dev.babyeham.ehamboard.global.exception.PostNotFoundException;
import dev.babyeham.ehamboard.global.exception.UnauthorizedPostAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User testUser;
    private Post testPost;
    private CreatePostRequest createPostRequest;
    private UpdatePostRequest updatePostRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testPost = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .user(testUser)
                .build();
        ReflectionTestUtils.setField(testPost, "id", 1L);
        ReflectionTestUtils.setField(testPost, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testPost, "updatedAt", LocalDateTime.now());

        createPostRequest = new CreatePostRequest();
        ReflectionTestUtils.setField(createPostRequest, "title", "새 게시글");
        ReflectionTestUtils.setField(createPostRequest, "content", "새 게시글 내용");

        updatePostRequest = new UpdatePostRequest();
        ReflectionTestUtils.setField(updatePostRequest, "title", "수정된 제목");
        ReflectionTestUtils.setField(updatePostRequest, "content", "수정된 내용");
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(postRepository.save(any(Post.class))).willReturn(testPost);

        // when
        PostResponse response = postService.createPost(createPostRequest, 1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getContent()).isEqualTo("테스트 내용");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(userRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 사용자를 찾을 수 없음")
    void createPost_UserNotFound() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(createPostRequest, 999L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository, times(1)).findById(999L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void getPost_Success() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when
        PostResponse response = postService.getPost(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getContent()).isEqualTo("테스트 내용");

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시글 조회 실패 - 게시글을 찾을 수 없음")
    void getPost_NotFound() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(999L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("모든 게시글 조회 성공")
    void getAllPosts_Success() {
        // given
        Post post1 = Post.builder()
                .title("게시글 1")
                .content("내용 1")
                .user(testUser)
                .build();
        ReflectionTestUtils.setField(post1, "id", 1L);
        ReflectionTestUtils.setField(post1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(post1, "updatedAt", LocalDateTime.now());

        Post post2 = Post.builder()
                .title("게시글 2")
                .content("내용 2")
                .user(testUser)
                .build();
        ReflectionTestUtils.setField(post2, "id", 2L);
        ReflectionTestUtils.setField(post2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(post2, "updatedAt", LocalDateTime.now());

        given(postRepository.findAllByOrderByCreatedAtDesc()).willReturn(Arrays.asList(post1, post2));

        // when
        List<PostResponse> responses = postService.getAllPosts();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("게시글 1");
        assertThat(responses.get(1).getTitle()).isEqualTo("게시글 2");

        verify(postRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("제목으로 게시글 검색 성공")
    void searchPostsByTitle_Success() {
        // given
        given(postRepository.searchByTitle("테스트")).willReturn(Arrays.asList(testPost));

        // when
        List<PostResponse> responses = postService.searchPostsByTitle("테스트");

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("테스트 제목");

        verify(postRepository, times(1)).searchByTitle("테스트");
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when
        PostResponse response = postService.updatePost(1L, updatePostRequest, 1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getContent()).isEqualTo("수정된 내용");

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글을 찾을 수 없음")
    void updatePost_PostNotFound() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(999L, updatePostRequest, 1L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void updatePost_Unauthorized() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1L, updatePostRequest, 999L))
                .isInstanceOf(UnauthorizedPostAccessException.class)
                .hasMessage("게시글을 수정할 권한이 없습니다.");

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
        doNothing().when(postRepository).delete(testPost);

        // when
        postService.deletePost(1L, 1L);

        // then
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).delete(testPost);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글을 찾을 수 없음")
    void deletePost_PostNotFound() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.deletePost(999L, 1L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository, times(1)).findById(999L);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deletePost_Unauthorized() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1L, 999L))
                .isInstanceOf(UnauthorizedPostAccessException.class)
                .hasMessage("게시글을 삭제할 권한이 없습니다.");

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("사용자별 게시글 조회 성공")
    void getPostsByUser_Success() {
        // given
        given(postRepository.findPostsByUserId(1L)).willReturn(Arrays.asList(testPost));

        // when
        List<PostResponse> responses = postService.getPostsByUser(1L);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(1L);
        assertThat(responses.get(0).getUsername()).isEqualTo("testuser");

        verify(postRepository, times(1)).findPostsByUserId(1L);
    }
}
