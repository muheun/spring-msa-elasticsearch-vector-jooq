CREATE TABLE IF NOT EXISTS posts.posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts.posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts.posts(author_id);

COMMENT ON TABLE posts.posts IS '게시글 메인 테이블';
COMMENT ON COLUMN posts.posts.author_id IS 'users.users(id)';
