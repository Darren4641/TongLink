package io.tonglink.app

import io.tonglink.app.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableCaching
@SpringBootApplication
@EnableJpaRepositories("io.tonglink")
@EnableConfigurationProperties(AppProperties::class)
class TongLinkApplication

fun main(args: Array<String>) {
    runApplication<TongLinkApplication>(*args)
}
