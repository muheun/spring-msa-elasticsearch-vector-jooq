package com.moaspace.post

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Rollback
class PostServiceApplicationTests {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun contextLoads() {
    }

    @Test
    fun flywayMigrationSuccess() {
        val schemaExists = jdbcTemplate.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = 'posts')",
            Boolean::class.java
        )
        assertTrue(schemaExists == true, "posts schema should exist")

        val postsTableExists = jdbcTemplate.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = 'posts' AND table_name = 'posts')",
            Boolean::class.java
        )
        assertTrue(postsTableExists == true, "posts.posts table should exist")

        val commentsTableExists = jdbcTemplate.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = 'posts' AND table_name = 'comments')",
            Boolean::class.java
        )
        assertTrue(commentsTableExists == true, "posts.comments table should exist")

        val migrationCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM posts.flyway_schema_history WHERE success = true AND version IS NOT NULL",
            Int::class.java
        )
        assertEquals(3, migrationCount, "Should have 3 successful versioned migrations")
    }
}
