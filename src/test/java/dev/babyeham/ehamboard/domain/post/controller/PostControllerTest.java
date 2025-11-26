package dev.babyeham.ehamboard.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.babyeham.ehamboard.domain.post.dto.CreatePostRequest;
import dev.babyeham.ehamboard.domain.post.dto.PostResponse;
import dev.babyeham.ehamboard.domain.post.dto.UpdatePostRequest;
import dev.babyeham.ehamboard.domain.post.service.PostService;
import dev.babyeham.ehamboard.domain.user.entity.User;
import dev.babyeham.ehamboard.global.exception.GlobalExceptionHandler;
import dev.babyeham.ehamboard.global.exception.PostNotFoundException;
import dev.babyeham.ehamboard.global.exception.UnauthorizedPostAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private ObjectMapper objectMapper;
    private User testUser;
    private PostResponse postResponse;
    private CreatePostRequest createPostRequest;
    private UpdatePostRequest updatePostRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUser = User.builder()
                .username("testuser")
                .password("password")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        postResponse = PostResponse.builder()
                .id(1L)
                .title("테스트 게시글")
                .content("테스트 내용")
                .userId(1L)
                .username("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createPostRequest = new CreatePostRequest();
        ReflectionTestUtils.setField(createPostRequest, "title", "새 게시글");
        ReflectionTestUtils.setField(createPostRequest, "content", "새 게시글 내용");

        updatePostRequest = new UpdatePostRequest();
        ReflectionTestUtils.setField(updatePostRequest, "title", "수정된 제목");
        ReflectionTestUtils.setField(updatePostRequest, "content", "수정된 내용");
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_Success() throws Exception {
        // given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(postService.createPost(any(CreatePostRequest.class), eq(1L)))
                .willReturn(postResponse);

        // when & then
        mockMvc.perform(post("/posts")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 게시글"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(postService, times(1)).createPost(any(CreatePostRequest.class), eq(1L));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void getPost_Success() throws Exception {
        // given
        given(postService.getPost(1L)).willReturn(postResponse);

        // when & then
        mockMvc.perform(get("/posts/{postId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 게시글"))
                .andExpect(jsonPath("$.content").value("테스트 내용"));

        verify(postService, times(1)).getPost(1L);
    }

    @Test
    @DisplayName("게시글 조회 실패 - 게시글을 찾을 수 없음")
    void getPost_NotFound() throws Exception {
        // given
        given(postService.getPost(999L))
                .willThrow(new PostNotFoundException("게시글을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/posts/{postId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(postService, times(1)).getPost(999L);
    }

    @Test
    @DisplayName("모든 게시글 조회 성공")
    void getAllPosts_Success() throws Exception {
        // given
        PostResponse post1 = PostResponse.builder()
                .id(1L)
                .title("게시글 1")
                .content("내용 1")
                .userId(1L)
                .username("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PostResponse post2 = PostResponse.builder()
                .id(2L)
                .title("게시글 2")
                .content("내용 2")
                .userId(1L)
                .username("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<PostResponse> posts = Arrays.asList(post1, post2);
        given(postService.getAllPosts()).willReturn(posts);

        // when & then
        mockMvc.perform(get("/posts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("게시글 1"))
                .andExpect(jsonPath("$[1].title").value("게시글 2"));

        verify(postService, times(1)).getAllPosts();
    }

    @Test
    @DisplayName("제목으로 게시글 검색 성공")
    void searchPosts_Success() throws Exception {
        // given
        List<PostResponse> posts = Arrays.asList(postResponse);
        given(postService.searchPostsByTitle("테스트")).willReturn(posts);

        // when & then
        mockMvc.perform(get("/posts/search")
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("테스트 게시글"));

        verify(postService, times(1)).searchPostsByTitle("테스트");
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() throws Exception {
        // given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        PostResponse updatedResponse = PostResponse.builder()
                .id(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .userId(1L)
                .username("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.updatePost(eq(1L), any(UpdatePostRequest.class), eq(1L)))
                .willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/posts/{postId}", 1L)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"));

        verify(postService, times(1)).updatePost(eq(1L), any(UpdatePostRequest.class), eq(1L));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void updatePost_Unauthorized() throws Exception {
        // given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(postService.updatePost(eq(1L), any(UpdatePostRequest.class), eq(1L)))
                .willThrow(new UnauthorizedPostAccessException("게시글을 수정할 권한이 없습니다."));

        // when & then
        mockMvc.perform(put("/posts/{postId}", 1L)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(postService, times(1)).updatePost(eq(1L), any(UpdatePostRequest.class), eq(1L));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() throws Exception {
        // given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        doNothing().when(postService).deletePost(1L, 1L);

        // when & then
        mockMvc.perform(delete("/posts/{postId}", 1L)
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 삭제 성공"));

        verify(postService, times(1)).deletePost(1L, 1L);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deletePost_Unauthorized() throws Exception {
        // given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        doThrow(new UnauthorizedPostAccessException("게시글을 삭제할 권한이 없습니다."))
                .when(postService).deletePost(1L, 1L);

        // when & then
        mockMvc.perform(delete("/posts/{postId}", 1L)
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(postService, times(1)).deletePost(1L, 1L);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("사용자별 게시글 조회 성공")
    void getPostsByUser_Success() throws Exception {
        // given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        List<PostResponse> posts = Arrays.asList(postResponse);
        given(postService.getPostsByUser(1L)).willReturn(posts);

        // when & then
        mockMvc.perform(get("/posts/getByUser")
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(postService, times(1)).getPostsByUser(1L);

        SecurityContextHolder.clearContext();
    }
}
