package io.tonglink.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class TongLinkApplication

fun main(args: Array<String>) {
    runApplication<TongLinkApplication>(*args)
}
