package me.muheun.moaspace.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SearchApplicationTests {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `Context loads`() {
        assertThat(applicationContext).isNotNull
    }

    @Test
    fun `Application starts`() {
        val beanNames = applicationContext.beanDefinitionNames
        assertThat(beanNames).isNotEmpty
        assertThat(beanNames).contains(
            "elasticsearchConfig",
            "elasticsearchPostSearchService",
            "cdcEventListener",
            "postDocumentCdcEventHandler"
        )
    }
}
