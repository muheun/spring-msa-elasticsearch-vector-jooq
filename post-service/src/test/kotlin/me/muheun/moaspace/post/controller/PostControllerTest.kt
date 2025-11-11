package me.muheun.moaspace.post.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.muheun.moaspace.post.domain.dto.PostCreateRequest
import me.muheun.moaspace.post.domain.dto.PostUpdateRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `POST posts 게시글 생성 201 Created`() {
        val request = PostCreateRequest(
            title = "컨트롤러 테스트 제목",
            content = "컨트롤러 테스트 내용",
            authorId = 1L
        )

        mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("컨트롤러 테스트 제목"))
            .andExpect(jsonPath("$.content").value("컨트롤러 테스트 내용"))
            .andExpect(jsonPath("$.authorId").value(1))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists())
    }

    @Test
    fun `POST posts 유효성 검증 실패 시 400 Bad Request`() {
        val request = PostCreateRequest(
            title = "",
            content = "",
            authorId = 1L
        )

        mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET posts {id} 게시글 조회 200 OK`() {
        val createRequest = PostCreateRequest(
            title = "조회용 게시글",
            content = "조회용 내용",
            authorId = 2L
        )

        val createResult = mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andReturn()

        val createdId = objectMapper.readTree(createResult.response.contentAsString)["id"].asLong()

        mockMvc.perform(get("/posts/$createdId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(createdId))
            .andExpect(jsonPath("$.title").value("조회용 게시글"))
    }

    @Test
    fun `GET posts {id} 존재하지 않는 게시글 404 Not Found`() {
        mockMvc.perform(get("/posts/999999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET posts 게시글 목록 조회 200 OK`() {
        repeat(3) { i ->
            val request = PostCreateRequest(
                title = "목록 게시글 $i",
                content = "내용 $i",
                authorId = 1L
            )
            mockMvc.perform(
                post("/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
        }

        mockMvc.perform(
            get("/posts")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)))
    }

    @Test
    fun `PUT posts {id} 게시글 수정 200 OK`() {
        val createRequest = PostCreateRequest(
            title = "수정 전 제목",
            content = "수정 전 내용",
            authorId = 1L
        )

        val createResult = mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andReturn()

        val createdId = objectMapper.readTree(createResult.response.contentAsString)["id"].asLong()

        val updateRequest = PostUpdateRequest(
            title = "수정 후 제목",
            content = "수정 후 내용"
        )

        mockMvc.perform(
            put("/posts/$createdId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(createdId))
            .andExpect(jsonPath("$.title").value("수정 후 제목"))
            .andExpect(jsonPath("$.content").value("수정 후 내용"))
    }

    @Test
    fun `PUT posts {id} 존재하지 않는 게시글 수정 404 Not Found`() {
        val updateRequest = PostUpdateRequest(
            title = "수정 제목",
            content = "수정 내용"
        )

        mockMvc.perform(
            put("/posts/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `DELETE posts {id} 게시글 삭제 204 No Content`() {
        val createRequest = PostCreateRequest(
            title = "삭제할 게시글",
            content = "삭제 테스트",
            authorId = 1L
        )

        val createResult = mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andReturn()

        val createdId = objectMapper.readTree(createResult.response.contentAsString)["id"].asLong()

        mockMvc.perform(delete("/posts/$createdId"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/posts/$createdId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `DELETE posts {id} 존재하지 않는 게시글 삭제 404 Not Found`() {
        mockMvc.perform(delete("/posts/999999"))
            .andExpect(status().isNotFound)
    }
}
