-- 데이터베이스 생성 (존재하지 않는 경우)
CREATE DATABASE IF NOT EXISTS eham_board_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE eham_board_db;

-- 기존 테이블 삭제
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users;

-- users 테이블 생성
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- posts 테이블 생성
CREATE TABLE posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- comments 테이블 생성
CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테스트 사용자 데이터 삽입
-- 비밀번호는 모두 'password123' (BCrypt 암호화)
-- BCrypt 해시: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (username, password, created_at) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2024-01-01 10:00:00'),
('john_doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2024-01-02 11:00:00'),
('jane_smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2024-01-03 12:00:00'),
('alice_kim', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2024-01-04 13:00:00'),
('bob_lee', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2024-01-05 14:00:00');

-- 테스트 게시글 데이터 삽입
INSERT INTO posts (title, content, user_id, created_at, updated_at) VALUES
('환영합니다!', '이함 게시판에 오신 것을 환영합니다. 자유롭게 글을 작성해주세요.', 1, '2024-01-10 09:00:00', '2024-01-10 09:00:00'),
('Spring Boot 개발 팁', 'Spring Boot로 개발하면서 유용한 팁들을 공유합니다.\n\n1. @RestControllerAdvice를 활용한 전역 예외 처리\n2. JPA N+1 문제 해결하기\n3. 효율적인 페이징 구현', 2, '2024-01-11 10:30:00', '2024-01-11 10:30:00'),
('JPA 쿼리 최적화 방법', 'JPA를 사용할 때 성능을 향상시킬 수 있는 여러 방법들이 있습니다.\n\nFetch Join, EntityGraph, BatchSize 등을 적절히 활용하면 성능을 크게 개선할 수 있습니다.', 3, '2024-01-12 14:15:00', '2024-01-12 14:15:00'),
('JWT 인증 구현 후기', 'JWT를 이용한 인증/인가 시스템을 구현했습니다.\n\nAccessToken과 RefreshToken을 분리하여 보안성을 높였고, Redis를 활용한 토큰 관리도 구현했습니다.', 2, '2024-01-13 11:20:00', '2024-01-13 11:20:00'),
('REST API 설계 원칙', 'REST API를 설계할 때 지켜야 할 원칙들에 대해 정리해봤습니다.\n\n- 리소스 중심의 URL 설계\n- HTTP 메서드의 올바른 사용\n- 적절한 상태 코드 반환\n- API 버저닝 전략', 4, '2024-01-14 16:00:00', '2024-01-14 16:00:00'),
('데이터베이스 인덱싱 전략', '효율적인 데이터베이스 쿼리를 위한 인덱싱 전략을 공유합니다.', 3, '2024-01-15 13:45:00', '2024-01-15 13:45:00'),
('Docker로 개발 환경 구성하기', 'Docker Compose를 활용하여 개발 환경을 일관되게 구성하는 방법을 소개합니다.\n\nMySQL, Redis, 애플리케이션을 모두 컨테이너로 실행하면 팀원 간 환경 차이 문제를 해결할 수 있습니다.', 5, '2024-01-16 10:00:00', '2024-01-16 10:00:00'),
('테스트 코드 작성의 중요성', '단위 테스트와 통합 테스트를 작성하면서 느낀 점들을 공유합니다.', 1, '2024-01-17 15:30:00', '2024-01-17 15:30:00'),
('Git 브랜치 전략', 'Git Flow와 GitHub Flow 중 어떤 것을 선택할지에 대한 고민을 공유합니다.', 4, '2024-01-18 11:00:00', '2024-01-18 11:00:00'),
('코드 리뷰 문화 정착기', '팀에 코드 리뷰 문화를 정착시키기 위해 노력한 과정을 공유합니다.', 2, '2024-01-19 09:30:00', '2024-01-19 09:30:00');

-- 테스트 댓글 데이터 삽입
INSERT INTO comments (content, post_id, user_id, created_at, updated_at) VALUES
-- 첫 번째 게시글의 댓글
('감사합니다! 잘 사용하겠습니다.', 1, 2, '2024-01-10 10:00:00', '2024-01-10 10:00:00'),
('좋은 게시판이네요.', 1, 3, '2024-01-10 11:00:00', '2024-01-10 11:00:00'),
('활발한 커뮤니티가 되길 바랍니다.', 1, 4, '2024-01-10 12:00:00', '2024-01-10 12:00:00'),

-- 두 번째 게시글의 댓글
('유용한 정보 감사합니다!', 2, 1, '2024-01-11 11:00:00', '2024-01-11 11:00:00'),
('N+1 문제 관련해서 더 자세히 알고 싶습니다.', 2, 3, '2024-01-11 12:00:00', '2024-01-11 12:00:00'),
('@RestControllerAdvice 정말 유용하게 쓰고 있습니다.', 2, 5, '2024-01-11 13:00:00', '2024-01-11 13:00:00'),

-- 세 번째 게시글의 댓글
('Fetch Join 사용 시 주의사항도 알려주시면 좋겠습니다.', 3, 2, '2024-01-12 15:00:00', '2024-01-12 15:00:00'),
('BatchSize는 얼마로 설정하는 게 적절할까요?', 3, 4, '2024-01-12 16:00:00', '2024-01-12 16:00:00'),
('실무에서 바로 적용해보겠습니다!', 3, 5, '2024-01-12 17:00:00', '2024-01-12 17:00:00'),

-- 네 번째 게시글의 댓글
('RefreshToken 만료 시간은 어떻게 설정하셨나요?', 4, 1, '2024-01-13 12:00:00', '2024-01-13 12:00:00'),
('Redis 대신 다른 저장소를 사용할 수도 있나요?', 4, 3, '2024-01-13 13:00:00', '2024-01-13 13:00:00'),
('보안 관련해서 추가로 고려할 사항이 있을까요?', 4, 5, '2024-01-13 14:00:00', '2024-01-13 14:00:00'),

-- 다섯 번째 게시글의 댓글
('API 버저닝은 URL에 포함시키는 게 좋을까요?', 5, 1, '2024-01-14 17:00:00', '2024-01-14 17:00:00'),
('HATEOAS는 실무에서 많이 사용하나요?', 5, 2, '2024-01-14 18:00:00', '2024-01-14 18:00:00'),
('좋은 정리 감사합니다!', 5, 3, '2024-01-14 19:00:00', '2024-01-14 19:00:00'),

-- 여섯 번째 게시글의 댓글
('복합 인덱스 설계 시 컬럼 순서가 중요하다고 들었습니다.', 6, 1, '2024-01-15 14:30:00', '2024-01-15 14:30:00'),
('인덱스를 너무 많이 생성하면 쓰기 성능이 떨어지나요?', 6, 2, '2024-01-15 15:00:00', '2024-01-15 15:00:00'),

-- 일곱 번째 게시글의 댓글
('docker-compose.yml 예제도 공유해주시면 좋겠습니다.', 7, 1, '2024-01-16 11:00:00', '2024-01-16 11:00:00'),
('M1 맥에서도 잘 동작하나요?', 7, 2, '2024-01-16 12:00:00', '2024-01-16 12:00:00'),
('Volume 마운트 관련 팁도 부탁드립니다.', 7, 4, '2024-01-16 13:00:00', '2024-01-16 13:00:00'),

-- 여덟 번째 게시글의 댓글
('테스트 커버리지는 얼마나 유지하시나요?', 8, 2, '2024-01-17 16:00:00', '2024-01-17 16:00:00'),
('Mock 객체 사용 시 주의사항이 있을까요?', 8, 3, '2024-01-17 17:00:00', '2024-01-17 17:00:00'),

-- 아홉 번째 게시글의 댓글
('저희 팀은 GitHub Flow를 사용하고 있습니다.', 9, 1, '2024-01-18 12:00:00', '2024-01-18 12:00:00'),
('feature 브랜치는 어떤 네이밍 규칙을 사용하시나요?', 9, 3, '2024-01-18 13:00:00', '2024-01-18 13:00:00'),

-- 열 번째 게시글의 댓글
('코드 리뷰 시간이 오래 걸리는 게 고민입니다.', 10, 3, '2024-01-19 10:30:00', '2024-01-19 10:30:00'),
('리뷰 가이드라인을 문서화하는 게 도움이 될 것 같습니다.', 10, 4, '2024-01-19 11:00:00', '2024-01-19 11:00:00'),
('좋은 경험 공유 감사합니다!', 10, 5, '2024-01-19 12:00:00', '2024-01-19 12:00:00');

-- 데이터 확인 쿼리
SELECT 'Users:' as '';
SELECT id, username, created_at FROM users;

SELECT 'Posts:' as '';
SELECT p.id, p.title, u.username as author, p.created_at
FROM posts p
JOIN users u ON p.user_id = u.id
ORDER BY p.created_at;

SELECT 'Comments:' as '';
SELECT c.id, c.content, u.username as author, p.title as post_title, c.created_at
FROM comments c
JOIN users u ON c.user_id = u.id
JOIN posts p ON c.post_id = p.id
ORDER BY c.created_at
LIMIT 10;

SELECT 'Statistics:' as '';
SELECT
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM posts) as total_posts,
    (SELECT COUNT(*) FROM comments) as total_comments;
