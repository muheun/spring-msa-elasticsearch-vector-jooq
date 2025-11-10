CREATE TABLE IF NOT EXISTS posts.comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts.posts(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id ON posts.comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON posts.comments(created_at DESC);

COMMENT ON TABLE posts.comments IS '댓글 테이블';
COMMENT ON COLUMN posts.comments.post_id IS 'ON DELETE CASCADE: 게시글 삭제 시 댓글 자동 삭제';
