package me.muheun.moaspace.search

import me.muheun.moaspace.search.config.CdcProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(CdcProperties::class)
class SearchApplication

fun main(args: Array<String>) {
    runApplication<SearchApplication>(*args)
}
