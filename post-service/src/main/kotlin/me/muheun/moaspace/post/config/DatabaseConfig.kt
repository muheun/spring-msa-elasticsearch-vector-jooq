package me.muheun.moaspace.post.config

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    @Bean
    fun dslContext(dataSource: DataSource): DSLContext {
        val configuration = DefaultConfiguration()
        configuration.setDataSource(dataSource)
        configuration.setSQLDialect(org.jooq.SQLDialect.POSTGRES)

        val settings = configuration.settings()
        settings.isExecuteLogging = true

        return DSL.using(configuration)
    }
}
