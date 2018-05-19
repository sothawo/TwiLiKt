package com.sothawo.twilikt

import com.sothawo.twilikt.twitter.TwitterConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(TwitterConfiguration::class)
class TwiliktApplication

fun main(args: Array<String>) {
    runApplication<TwiliktApplication>(*args)
}
